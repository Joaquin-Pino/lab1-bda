-- Pruebas del Trigger 2: auditoría de precios de tipo_entrada
BEGIN;

DO $$
DECLARE
    v_productor  BIGINT;
    v_asistente  BIGINT;
    v_recinto    BIGINT;
    v_festival   BIGINT;
    v_tipo_a     BIGINT;   -- precio inicial 10000
    v_tipo_b     BIGINT;   -- precio inicial 20000
    v_orden      BIGINT;
    v_n          INT;
    v_n2         INT;
    v_anterior   NUMERIC;
    v_nuevo      NUMERIC;
    v_usuario    BIGINT;
    v_ok         INT := 0;
    v_falla      INT := 0;
BEGIN
    -- Datos
    INSERT INTO rol (tipo) VALUES ('PRODUCTOR') ON CONFLICT (tipo) DO NOTHING;
    INSERT INTO rol (tipo) VALUES ('ASISTENTE') ON CONFLICT (tipo) DO NOTHING;

    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    VALUES ((SELECT id FROM rol WHERE tipo = 'PRODUCTOR'),
            'TEST Productor auditoria', 'test.prod.audit@prueba.cl', 'x')
    RETURNING id_usuario INTO v_productor;

    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    VALUES ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'),
            'TEST Asistente auditoria', 'test.asi.audit@prueba.cl', 'x')
    RETURNING id_usuario INTO v_asistente;

    INSERT INTO recinto (nombre, direccion, aforo_maximo)
    VALUES ('TEST Recinto auditoria', 'Calle Falsa 123', 10000)
    RETURNING id_recinto INTO v_recinto;

    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival auditoria', '2026-12-05', '2026-12-06', 'PUBLICADO')
    RETURNING id_festival INTO v_festival;

    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
    VALUES (v_festival, 'TEST General', 10000, 10, 10) RETURNING id_tipo_entrada INTO v_tipo_a;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
    VALUES (v_festival, 'TEST VIP', 20000, 10, 10) RETURNING id_tipo_entrada INTO v_tipo_b;

    -- Caso 1: Pasar
    BEGIN
        UPDATE tipo_entrada SET precio_actual = 12000 WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        SELECT precio_anterior, precio_nuevo, id_usuario_modificador INTO v_anterior, v_nuevo, v_usuario
          FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_n = 1 AND v_anterior = 10000 AND v_nuevo = 12000 AND v_usuario IS NULL THEN
            RAISE NOTICE 'OK    caso 1 (cambio de precio sin usuario definido: 1 registro con usuario NULL)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 1: registros=%, anterior=%, nuevo=%, usuario=%', v_n, v_anterior, v_nuevo, v_usuario;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 1: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 2: Pasar (no genera registro)
    BEGIN
        UPDATE tipo_entrada SET precio_actual = precio_actual WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_n = 1 THEN
            RAISE NOTICE 'OK    caso 2 (actualizar con el mismo precio no genera registro)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 2: se registró un cambio que no era cambio (registros=%)', v_n;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 2: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 3: Pasar (no genera registro)
    BEGIN
        UPDATE tipo_entrada
           SET nombre_tipo = 'TEST General renombrada', aforo_disponible = aforo_disponible - 1
         WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_n = 1 THEN
            RAISE NOTICE 'OK    caso 3 (cambiar nombre o aforo no genera registro)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 3: se registró un cambio de otra columna (registros=%)', v_n;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 3: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 4: Pasar (con usuario definido, como lo hace AuditContext)
    BEGIN
        PERFORM set_config('app.user_id', v_productor::text, true);
        UPDATE tipo_entrada SET precio_actual = 15000 WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        SELECT precio_anterior, precio_nuevo, id_usuario_modificador INTO v_anterior, v_nuevo, v_usuario
          FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a
         ORDER BY id_auditoria DESC LIMIT 1;
        IF v_n = 2 AND v_anterior = 12000 AND v_nuevo = 15000 AND v_usuario = v_productor THEN
            RAISE NOTICE 'OK    caso 4 (cambio de precio con app.user_id: queda el usuario que lo hizo)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 4: registros=%, anterior=%, nuevo=%, usuario=% (esperado %)',
                v_n, v_anterior, v_nuevo, v_usuario, v_productor;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 4: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 5: Pasar (variable vacía = sin usuario, no debe fallar al convertir a número)
    BEGIN
        PERFORM set_config('app.user_id', '', true);
        UPDATE tipo_entrada SET precio_actual = 16000 WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        SELECT id_usuario_modificador INTO v_usuario
          FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a
         ORDER BY id_auditoria DESC LIMIT 1;
        IF v_n = 3 AND v_usuario IS NULL THEN
            RAISE NOTICE 'OK    caso 5 (app.user_id vacía: registro con usuario NULL)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 5: registros=%, usuario=%', v_n, v_usuario;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 5: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 6: Pasar (una compra solo descuenta aforo, no toca el precio)
    BEGIN
        CALL sp_comprar_entradas(v_asistente, v_tipo_b, 1, false, v_orden);
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_b;
        IF v_n = 0 THEN
            RAISE NOTICE 'OK    caso 6 (comprar entradas no genera registro de precio)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 6: la compra generó % registro(s) de precio', v_n;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 6: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 7: Rechazo (precio negativo: lo frena el CHECK y no debe quedar registro)
    BEGIN
        UPDATE tipo_entrada SET precio_actual = -1 WHERE id_tipo_entrada = v_tipo_a;
        RAISE NOTICE 'FALLA caso 7: se aceptó un precio negativo';
        v_falla := v_falla + 1;
    EXCEPTION WHEN check_violation THEN
        SELECT count(*) INTO v_n FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_n = 3 THEN
            RAISE NOTICE 'OK    caso 7 (precio negativo rechazado, sin registro) -> %', SQLERRM;
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 7: quedó un registro de un cambio rechazado (registros=%)', v_n;
            v_falla := v_falla + 1;
        END IF;
    END;

    -- Caso 8: Pasar (un UPDATE que toca varias filas registra una por cada tipo de entrada)
    BEGIN
        UPDATE tipo_entrada SET precio_actual = precio_actual + 1000 WHERE id_festival = v_festival;
        SELECT count(*) INTO v_n  FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n2 FROM auditoria_precio_entrada WHERE id_tipo_entrada = v_tipo_b;
        IF v_n = 4 AND v_n2 = 1 THEN
            RAISE NOTICE 'OK    caso 8 (UPDATE masivo: un registro por cada tipo de entrada)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 8: tipo A=% registros (esperado 4), tipo B=% (esperado 1)', v_n, v_n2;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 8: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
