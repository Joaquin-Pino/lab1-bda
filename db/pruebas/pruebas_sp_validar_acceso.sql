-- Pruebas del Procedimiento Almacenado 3: sp_validar_acceso
BEGIN;

DO $$
DECLARE
    v_productor BIGINT;
    v_a BIGINT;
    v_recinto BIGINT;
    v_fest BIGINT;
    v_fest_otro BIGINT;
    v_fest_pasado BIGINT;
    v_hoy DATE := fn_dia_festival(now());
    v_t_hoy BIGINT;       -- entrada de hoy
    v_t_pase BIGINT;      -- pase (fecha NULL)
    v_t_manana BIGINT;    -- entrada de mañana
    v_t_pasado BIGINT;    -- festival ya terminado
    v_orden BIGINT;
    v_cod_hoy TEXT;
    v_cod_pase TEXT;
    v_cod_manana TEXT;
    v_cod_anulado TEXT;
    v_cod_pend TEXT;
    v_cod_pasado TEXT;
    v_res VARCHAR;
    v_id BIGINT;
    v_n INT;
    v_ok INT := 0;
    v_falla INT := 0;
BEGIN
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Productor', 'test.prod.acceso@prueba.cl', 'x' FROM rol WHERE tipo = 'PRODUCTOR' RETURNING id_usuario INTO v_productor;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente', 'test.a.acceso@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_a;
    INSERT INTO recinto (nombre, direccion, aforo_maximo) VALUES ('TEST Recinto acceso', 'x', 1000) RETURNING id_recinto INTO v_recinto;

    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival acceso', v_hoy - 1, v_hoy + 1, 'PUBLICADO') RETURNING id_festival INTO v_fest;
    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival otro acceso', v_hoy, v_hoy, 'PUBLICADO') RETURNING id_festival INTO v_fest_otro;

    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Hoy', 100, 50, 50, v_hoy) RETURNING id_tipo_entrada INTO v_t_hoy;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Pase', 100, 50, 50, NULL) RETURNING id_tipo_entrada INTO v_t_pase;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Mañana', 100, 50, 50, v_hoy + 1) RETURNING id_tipo_entrada INTO v_t_manana;

    CALL sp_comprar_entradas(v_a, v_t_hoy, 1, false, v_orden);
    SELECT codigo_unico INTO v_cod_hoy FROM ticket WHERE id_orden = v_orden;
    CALL sp_comprar_entradas(v_a, v_t_pase, 1, false, v_orden);
    SELECT codigo_unico INTO v_cod_pase FROM ticket WHERE id_orden = v_orden;
    CALL sp_comprar_entradas(v_a, v_t_manana, 1, false, v_orden);
    SELECT codigo_unico INTO v_cod_manana FROM ticket WHERE id_orden = v_orden;
    CALL sp_comprar_entradas(v_a, v_t_hoy, 1, false, v_orden);
    SELECT codigo_unico INTO v_cod_anulado FROM ticket WHERE id_orden = v_orden;
    UPDATE ticket SET estado = 'CANCELADO' WHERE id_orden = v_orden;
    CALL sp_comprar_entradas(v_a, v_t_hoy, 1, false, v_orden);
    SELECT codigo_unico INTO v_cod_pend FROM ticket WHERE id_orden = v_orden;
    UPDATE orden_compra SET estado = 'PENDIENTE' WHERE id_orden = v_orden;

    -- Festival terminado: se crea ya vendido (insertando directo el ticket)
    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival pasado acceso', v_hoy - 5, v_hoy - 4, 'PUBLICADO') RETURNING id_festival INTO v_fest_pasado;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest_pasado, 'Pase', 100, 50, 50, NULL) RETURNING id_tipo_entrada INTO v_t_pasado;
    INSERT INTO orden_compra (id_usuario_asistente, monto_total) VALUES (v_a, 100) RETURNING id_orden INTO v_orden;
    INSERT INTO ticket (codigo_unico, id_orden, id_tipo_entrada, precio) VALUES ('TESTPASADO0001', v_orden, v_t_pasado, 100);
    v_cod_pasado := 'TESTPASADO0001';

    -- Caso 1: ticket válido de hoy
    CALL sp_validar_acceso(v_cod_hoy, v_fest, v_productor, v_res, v_id);
    IF v_res = 'VALIDO' AND (SELECT estado FROM ticket WHERE id_ticket = v_id) = 'UTILIZADO'
       AND (SELECT fecha_uso FROM ticket WHERE id_ticket = v_id) IS NOT NULL THEN
        RAISE NOTICE 'OK    caso 1 (ticket válido: queda UTILIZADO con fecha_uso)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 1: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 2: el mismo ticket otra vez
    CALL sp_validar_acceso(v_cod_hoy, v_fest, v_productor, v_res, v_id);
    IF v_res = 'YA_USADO' THEN
        RAISE NOTICE 'OK    caso 2 (ticket ya usado)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 2: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 3: ticket anulado (cancelado)
    CALL sp_validar_acceso(v_cod_anulado, v_fest, v_productor, v_res, v_id);
    IF v_res = 'ANULADO' THEN
        RAISE NOTICE 'OK    caso 3 (ticket anulado)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 3: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 4: orden no pagada se trata como anulado y el ticket no se consume
    CALL sp_validar_acceso(v_cod_pend, v_fest, v_productor, v_res, v_id);
    IF v_res = 'ANULADO' AND (SELECT estado FROM ticket WHERE id_ticket = v_id) = 'VIGENTE' THEN
        RAISE NOTICE 'OK    caso 4 (orden no pagada: anulado, ticket sigue vigente)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 4: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 5: código inexistente
    CALL sp_validar_acceso('NO-EXISTE', v_fest, v_productor, v_res, v_id);
    IF v_res = 'INEXISTENTE' AND v_id IS NULL THEN
        RAISE NOTICE 'OK    caso 5 (código inexistente)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 5: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 6: lector de otro festival; el ticket no se consume
    CALL sp_validar_acceso(v_cod_pase, v_fest_otro, v_productor, v_res, v_id);
    IF v_res = 'OTRO_FESTIVAL' AND (SELECT estado FROM ticket WHERE id_ticket = v_id) = 'VIGENTE' THEN
        RAISE NOTICE 'OK    caso 6 (ticket de otro festival)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 6: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 7: pase (fecha NULL) dentro de las fechas del festival
    CALL sp_validar_acceso(v_cod_pase, v_fest, v_productor, v_res, v_id);
    IF v_res = 'VALIDO' THEN
        RAISE NOTICE 'OK    caso 7 (pase válido)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 7: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 8: entrada de mañana usada hoy
    CALL sp_validar_acceso(v_cod_manana, v_fest, v_productor, v_res, v_id);
    IF v_res = 'FECHA_INCORRECTA' AND (SELECT estado FROM ticket WHERE id_ticket = v_id) = 'VIGENTE' THEN
        RAISE NOTICE 'OK    caso 8 (entrada de otro día)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 8: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 9: pase de un festival que ya terminó
    CALL sp_validar_acceso(v_cod_pasado, v_fest_pasado, v_productor, v_res, v_id);
    IF v_res = 'FECHA_INCORRECTA' THEN
        RAISE NOTICE 'OK    caso 9 (festival fuera de fechas)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 9: resultado=%', v_res; v_falla := v_falla + 1;
    END IF;

    -- Caso 10: log_entrada registra los intentos sobre tickets existentes (no el inexistente)
    SELECT count(*) INTO v_n FROM log_entrada WHERE id_usuario = v_productor;
    IF v_n = 8 THEN
        RAISE NOTICE 'OK    caso 10 (8 intentos registrados en log_entrada)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 10: registros=%', v_n; v_falla := v_falla + 1;
    END IF;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
