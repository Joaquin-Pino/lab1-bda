-- Pruebas de sp_cancelar_festival y sp_cancelar_tipo_entrada (devolución de crédito)
BEGIN;

DO $$
DECLARE
    v_productor BIGINT;
    v_a BIGINT;   -- pase VIGENTE, ya compensado 500 por una presentación
    v_b BIGINT;   -- 2 entradas día 1
    v_c BIGINT;   -- pase UTILIZADO
    v_d BIGINT;   -- entrada día 1 con orden PENDIENTE
    v_recinto BIGINT;
    v_esc BIGINT;
    v_art BIGINT;
    v_fest BIGINT;
    v_pase BIGINT;
    v_dia1 BIGINT;
    v_p1 BIGINT;
    v_p2 BIGINT;
    v_orden_a BIGINT;
    v_orden_b BIGINT;
    v_orden_c BIGINT;
    v_orden_d BIGINT;
    v_notif INT;
    v_total NUMERIC;
    v_cred NUMERIC;
    v_n INT;
    v_txt TEXT;
    v_ok INT := 0;
    v_falla INT := 0;
BEGIN
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Productor', 'test.prod.cfest@prueba.cl', 'x' FROM rol WHERE tipo = 'PRODUCTOR' RETURNING id_usuario INTO v_productor;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente A', 'test.a.cfest@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_a;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente B', 'test.b.cfest@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_b;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente C', 'test.c.cfest@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_c;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente D', 'test.d.cfest@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_d;

    INSERT INTO recinto (nombre, direccion, aforo_maximo) VALUES ('TEST Recinto cfest', 'x', 1000) RETURNING id_recinto INTO v_recinto;
    INSERT INTO escenario (id_recinto, nombre, capacidad_maxima) VALUES (v_recinto, 'E1', 500) RETURNING id_escenario INTO v_esc;
    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista cfest', 'x') RETURNING id_artista INTO v_art;

    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival cfest', DATE '2030-06-10', DATE '2030-06-11', 'PUBLICADO')
    RETURNING id_festival INTO v_fest;

    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Pase', 1000, 100, 98, NULL) RETURNING id_tipo_entrada INTO v_pase;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Día 1', 400, 100, 97, DATE '2030-06-10') RETURNING id_tipo_entrada INTO v_dia1;

    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_fest, v_esc, v_art, TIMESTAMP '2030-06-10 20:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-10 21:00' AT TIME ZONE 'America/Santiago')
    RETURNING id_presentacion INTO v_p1;
    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_fest, v_esc, v_art, TIMESTAMP '2030-06-11 20:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-11 21:00' AT TIME ZONE 'America/Santiago')
    RETURNING id_presentacion INTO v_p2;

    INSERT INTO orden_compra (id_usuario_asistente, monto_total) VALUES (v_a, 1000) RETURNING id_orden INTO v_orden_a;
    INSERT INTO ticket (codigo_unico, id_orden, id_tipo_entrada, precio) VALUES ('TESTCFEST-A1', v_orden_a, v_pase, 1000);
    INSERT INTO orden_compra (id_usuario_asistente, monto_total) VALUES (v_b, 800) RETURNING id_orden INTO v_orden_b;
    INSERT INTO ticket (codigo_unico, id_orden, id_tipo_entrada, precio) VALUES ('TESTCFEST-B1', v_orden_b, v_dia1, 400), ('TESTCFEST-B2', v_orden_b, v_dia1, 400);
    INSERT INTO orden_compra (id_usuario_asistente, monto_total) VALUES (v_c, 1000) RETURNING id_orden INTO v_orden_c;
    INSERT INTO ticket (codigo_unico, id_orden, id_tipo_entrada, precio, estado, fecha_uso) VALUES ('TESTCFEST-C1', v_orden_c, v_pase, 1000, 'UTILIZADO', now());
    INSERT INTO orden_compra (id_usuario_asistente, monto_total, estado) VALUES (v_d, 400, 'PENDIENTE') RETURNING id_orden INTO v_orden_d;
    INSERT INTO ticket (codigo_unico, id_orden, id_tipo_entrada, precio) VALUES ('TESTCFEST-D1', v_orden_d, v_dia1, 400);

    -- Compensación previa: se canceló la presentación del día 2 (el pase paga 1000/2 = 500)
    CALL sp_cancelar_presentacion(v_p2, v_notif, v_total);

    -- Caso 1: cancelar el tipo "Día 1" devuelve 800 a B y no toca a A ni a D
    CALL sp_cancelar_tipo_entrada(v_dia1, v_notif, v_total);
    SELECT creditos_compensacion INTO v_cred FROM usuario WHERE id_usuario = v_b;
    IF v_notif = 1 AND v_total = 800 AND v_cred = 800 THEN
        RAISE NOTICE 'OK    caso 1 (tipo cancelado: B recibe 800)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 1: notif=%, total=%, credito B=%', v_notif, v_total, v_cred; v_falla := v_falla + 1;
    END IF;

    -- Caso 2: los tickets de B quedan CANCELADO, su orden CANCELADA y el tipo sin cupo; D (orden PENDIENTE) intacto
    SELECT count(*) INTO v_n FROM ticket WHERE id_orden = v_orden_b AND estado = 'CANCELADO';
    IF v_n = 2
       AND (SELECT estado FROM orden_compra WHERE id_orden = v_orden_b) = 'CANCELADA'
       AND (SELECT aforo_disponible FROM tipo_entrada WHERE id_tipo_entrada = v_dia1) = 0
       AND (SELECT estado FROM ticket WHERE codigo_unico = 'TESTCFEST-D1') = 'VIGENTE' THEN
        RAISE NOTICE 'OK    caso 2 (tickets y orden cancelados, cupo cerrado)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 2: tickets cancelados=%', v_n; v_falla := v_falla + 1;
    END IF;

    -- Caso 3: B tiene una notificación del tipo cancelado
    SELECT mensaje INTO v_txt FROM notificacion WHERE id_usuario = v_b;
    IF v_txt LIKE '%Día 1%' AND v_txt LIKE '%800%' THEN
        RAISE NOTICE 'OK    caso 3 (notificación a B)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 3: mensaje=%', v_txt; v_falla := v_falla + 1;
    END IF;

    -- Caso 4: cancelar el festival: A recibe 1000 - 500 ya compensados = 500; C (UTILIZADO) y D (PENDIENTE) nada
    CALL sp_cancelar_festival(v_fest, v_notif, v_total);
    SELECT creditos_compensacion INTO v_cred FROM usuario WHERE id_usuario = v_a;
    IF v_notif = 1 AND v_total = 500 AND v_cred = 1000 THEN
        RAISE NOTICE 'OK    caso 4 (festival cancelado: A recibe solo el saldo, 500)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 4: notif=%, total=%, credito A=%', v_notif, v_total, v_cred; v_falla := v_falla + 1;
    END IF;

    -- Caso 5: nunca se devuelve más que el precio del ticket
    SELECT COALESCE(SUM(monto), 0) INTO v_total FROM compensacion c JOIN ticket t ON t.id_ticket = c.id_ticket
    WHERE t.codigo_unico = 'TESTCFEST-A1';
    IF v_total = 1000 THEN
        RAISE NOTICE 'OK    caso 5 (total compensado = precio)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 5: compensado=%', v_total; v_falla := v_falla + 1;
    END IF;

    -- Caso 6: estado del festival, presentaciones y tickets
    IF (SELECT estado FROM festival WHERE id_festival = v_fest) = 'CANCELADO'
       AND (SELECT count(*) FROM presentacion WHERE id_festival = v_fest AND estado <> 'CANCELADA') = 0
       AND (SELECT estado FROM ticket WHERE codigo_unico = 'TESTCFEST-A1') = 'CANCELADO'
       AND (SELECT estado FROM orden_compra WHERE id_orden = v_orden_a) = 'CANCELADA'
       AND (SELECT estado FROM ticket WHERE codigo_unico = 'TESTCFEST-C1') = 'UTILIZADO'
       AND (SELECT estado FROM orden_compra WHERE id_orden = v_orden_c) = 'PAGADA' THEN
        RAISE NOTICE 'OK    caso 6 (festival, presentaciones, tickets y órdenes)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 6: estados incorrectos'; v_falla := v_falla + 1;
    END IF;

    -- Caso 7: el detalle por usuario queda disponible para el backend
    SELECT count(*) INTO v_n FROM tmp_devolucion;
    IF v_n = 1 THEN
        RAISE NOTICE 'OK    caso 7 (tmp_devolucion con el detalle)'; v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 7: filas=%', v_n; v_falla := v_falla + 1;
    END IF;

    -- Caso 8: cancelar de nuevo el festival se rechaza
    BEGIN
        CALL sp_cancelar_festival(v_fest, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 8: canceló dos veces'; v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 8 (festival ya cancelado rechazado)'; v_ok := v_ok + 1;
    END;

    -- Caso 9: cancelar un tipo de entrada de un festival cancelado se rechaza
    BEGIN
        CALL sp_cancelar_tipo_entrada(v_pase, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 9: canceló entrada de festival cancelado'; v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 9 (festival cancelado rechaza cancelar entradas)'; v_ok := v_ok + 1;
    END;

    -- Caso 10: inexistentes
    BEGIN
        CALL sp_cancelar_festival(-1, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 10: aceptó festival inexistente'; v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        v_ok := v_ok + 1;
    END;
    BEGIN
        CALL sp_cancelar_tipo_entrada(-1, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 10: aceptó tipo inexistente'; v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 10 (inexistentes rechazados)'; v_ok := v_ok + 1;
    END;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
