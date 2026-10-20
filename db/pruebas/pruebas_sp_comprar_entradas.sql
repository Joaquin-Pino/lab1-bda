-- Pruebas del Procedimiento Almacenado 1: sp_comprar_entradas
BEGIN;

DO $$
DECLARE
    v_productor  BIGINT;
    v_asi_a      BIGINT;   -- crédito 5000
    v_asi_b      BIGINT;   -- solo para casos de rechazo, nunca debe tener órdenes
    v_asi_c      BIGINT;   -- crédito 20000
    v_recinto    BIGINT;
    v_fest_pub   BIGINT;
    v_fest_bor   BIGINT;
    v_tipo_a     BIGINT;   -- publicado, aforo 3
    v_tipo_b     BIGINT;   -- publicado, aforo 10
    v_tipo_bor   BIGINT;   -- festival en borrador
    v_orden      BIGINT;
    v_total      NUMERIC;
    v_aplicado   NUMERIC;
    v_credito    NUMERIC;
    v_aforo      INT;
    v_n          INT;
    v_n2         INT;
    v_ok         INT := 0;
    v_falla      INT := 0;
BEGIN
    -- Datos
    INSERT INTO rol (tipo) VALUES ('PRODUCTOR') ON CONFLICT (tipo) DO NOTHING;
    INSERT INTO rol (tipo) VALUES ('ASISTENTE') ON CONFLICT (tipo) DO NOTHING;

    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    VALUES ((SELECT id FROM rol WHERE tipo = 'PRODUCTOR'), 'TEST Productor', 'test.prod.compra@prueba.cl', 'x')
    RETURNING id_usuario INTO v_productor;

    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion)
    VALUES ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), 'TEST Asistente A', 'test.asi.a@prueba.cl', 'x', 5000)
    RETURNING id_usuario INTO v_asi_a;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion)
    VALUES ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), 'TEST Asistente B', 'test.asi.b@prueba.cl', 'x', 0)
    RETURNING id_usuario INTO v_asi_b;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion)
    VALUES ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), 'TEST Asistente C', 'test.asi.c@prueba.cl', 'x', 20000)
    RETURNING id_usuario INTO v_asi_c;

    INSERT INTO recinto (nombre, direccion, aforo_maximo)
    VALUES ('TEST Recinto compra', 'Calle Falsa 123', 10000)
    RETURNING id_recinto INTO v_recinto;

    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival publicado', '2026-12-05', '2026-12-06', 'PUBLICADO')
    RETURNING id_festival INTO v_fest_pub;
    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival borrador', '2026-12-10', '2026-12-11', 'BORRADOR')
    RETURNING id_festival INTO v_fest_bor;

    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
    VALUES (v_fest_pub, 'TEST General', 10000, 3, 3) RETURNING id_tipo_entrada INTO v_tipo_a;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
    VALUES (v_fest_pub, 'TEST VIP', 10000, 10, 10) RETURNING id_tipo_entrada INTO v_tipo_b;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
    VALUES (v_fest_bor, 'TEST Borrador', 10000, 5, 5) RETURNING id_tipo_entrada INTO v_tipo_bor;

    -- Caso 1: Pasar (aforo de tipo A: 3 -> 2)
    BEGIN
        CALL sp_comprar_entradas(v_asi_c, v_tipo_a, 1, false, v_orden);
        SELECT monto_total, monto_credito_aplicado INTO v_total, v_aplicado
          FROM orden_compra WHERE id_orden = v_orden;
        SELECT count(*) INTO v_n FROM ticket
         WHERE id_orden = v_orden AND length(codigo_unico) = 32
           AND precio = 10000 AND estado = 'VIGENTE';
        SELECT aforo_disponible INTO v_aforo FROM tipo_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_total = 10000 AND v_aplicado = 0 AND v_n = 1 AND v_aforo = 2 THEN
            RAISE NOTICE 'OK    caso 1 (compra normal: orden, 1 ticket y aforo descontado)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 1: total=%, credito=%, tickets=%, aforo=%', v_total, v_aplicado, v_n, v_aforo;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 1: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 2: Rechazo (pide 3 y quedan 2; no debe cambiar nada)
    BEGIN
        CALL sp_comprar_entradas(v_asi_b, v_tipo_a, 3, false, v_orden);
        RAISE NOTICE 'FALLA caso 2: se vendieron más entradas que el aforo disponible';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        SELECT aforo_disponible INTO v_aforo FROM tipo_entrada WHERE id_tipo_entrada = v_tipo_a;
        SELECT count(*) INTO v_n FROM orden_compra WHERE id_usuario_asistente = v_asi_b;
        IF v_aforo = 2 AND v_n = 0 THEN
            RAISE NOTICE 'OK    caso 2 (pide más que el aforo, nada cambia) -> %', SQLERRM;
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 2: quedó a medias (aforo=%, ordenes=%)', v_aforo, v_n;
            v_falla := v_falla + 1;
        END IF;
    END;

    -- Caso 3: Pasar (compra el último cupo: 2 -> 0, dos códigos distintos)
    BEGIN
        CALL sp_comprar_entradas(v_asi_c, v_tipo_a, 2, false, v_orden);
        SELECT count(*), count(DISTINCT codigo_unico) INTO v_n, v_n2
          FROM ticket WHERE id_orden = v_orden;
        SELECT aforo_disponible INTO v_aforo FROM tipo_entrada WHERE id_tipo_entrada = v_tipo_a;
        IF v_n = 2 AND v_n2 = 2 AND v_aforo = 0 THEN
            RAISE NOTICE 'OK    caso 3 (compra de 2 entradas: 2 tickets con código distinto, aforo en 0)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 3: tickets=%, codigos distintos=%, aforo=%', v_n, v_n2, v_aforo;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 3: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 4: Rechazo (cupo agotado)
    BEGIN
        CALL sp_comprar_entradas(v_asi_b, v_tipo_a, 1, false, v_orden);
        RAISE NOTICE 'FALLA caso 4: se vendió con el aforo en 0';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 4 (cupo agotado) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 5: Rechazo (festival en borrador)
    BEGIN
        CALL sp_comprar_entradas(v_asi_b, v_tipo_bor, 1, false, v_orden);
        RAISE NOTICE 'FALLA caso 5: se vendió una entrada de un festival no publicado';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 5 (festival no publicado) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 6: Rechazo (cantidad 0)
    BEGIN
        CALL sp_comprar_entradas(v_asi_b, v_tipo_b, 0, false, v_orden);
        RAISE NOTICE 'FALLA caso 6: se aceptó cantidad 0';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 6 (cantidad 0) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 7: Rechazo (cantidad negativa, no debe sumar aforo)
    BEGIN
        CALL sp_comprar_entradas(v_asi_b, v_tipo_b, -2, false, v_orden);
        RAISE NOTICE 'FALLA caso 7: se aceptó cantidad negativa';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        SELECT aforo_disponible INTO v_aforo FROM tipo_entrada WHERE id_tipo_entrada = v_tipo_b;
        IF v_aforo = 10 THEN
            RAISE NOTICE 'OK    caso 7 (cantidad negativa, aforo intacto) -> %', SQLERRM;
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 7: el aforo cambió a %', v_aforo;
            v_falla := v_falla + 1;
        END IF;
    END;

    -- Caso 8: Pasar (crédito parcial: 5000 de 10000)
    BEGIN
        CALL sp_comprar_entradas(v_asi_a, v_tipo_b, 1, true, v_orden);
        SELECT monto_total, monto_credito_aplicado INTO v_total, v_aplicado
          FROM orden_compra WHERE id_orden = v_orden;
        SELECT creditos_compensacion INTO v_credito FROM usuario WHERE id_usuario = v_asi_a;
        IF v_total = 10000 AND v_aplicado = 5000 AND v_credito = 0 THEN
            RAISE NOTICE 'OK    caso 8 (crédito menor al total: se usa todo el crédito)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 8: total=%, aplicado=%, credito restante=%', v_total, v_aplicado, v_credito;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 8: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 9: Pasar (crédito mayor al total: solo se usa lo que cuesta)
    BEGIN
        CALL sp_comprar_entradas(v_asi_c, v_tipo_b, 1, true, v_orden);
        SELECT monto_total, monto_credito_aplicado INTO v_total, v_aplicado
          FROM orden_compra WHERE id_orden = v_orden;
        SELECT creditos_compensacion INTO v_credito FROM usuario WHERE id_usuario = v_asi_c;
        IF v_total = 10000 AND v_aplicado = 10000 AND v_credito = 10000 THEN
            RAISE NOTICE 'OK    caso 9 (crédito mayor al total: queda saldo a favor)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 9: total=%, aplicado=%, credito restante=%', v_total, v_aplicado, v_credito;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 9: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 10: Pasar (no pide usar crédito: el saldo no se toca)
    BEGIN
        CALL sp_comprar_entradas(v_asi_c, v_tipo_b, 1, false, v_orden);
        SELECT monto_credito_aplicado INTO v_aplicado FROM orden_compra WHERE id_orden = v_orden;
        SELECT creditos_compensacion INTO v_credito FROM usuario WHERE id_usuario = v_asi_c;
        IF v_aplicado = 0 AND v_credito = 10000 THEN
            RAISE NOTICE 'OK    caso 10 (sin usar crédito: el saldo queda igual)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 10: aplicado=%, credito=%', v_aplicado, v_credito;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 10: error inesperado -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 11: Pasar (todos los códigos de esta prueba son únicos y de 32 caracteres)
    BEGIN
        SELECT count(*), count(DISTINCT t.codigo_unico) INTO v_n, v_n2
          FROM ticket t JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
         WHERE te.id_festival = v_fest_pub AND length(t.codigo_unico) = 32;
        IF v_n = 6 AND v_n2 = 6 THEN
            RAISE NOTICE 'OK    caso 11 (7 tickets, 7 códigos distintos)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 11: tickets=%, codigos distintos=%', v_n, v_n2;
            v_falla := v_falla + 1;
        END IF;
    END;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
