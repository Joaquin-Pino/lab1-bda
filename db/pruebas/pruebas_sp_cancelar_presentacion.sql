-- Pruebas del Procedimiento Almacenado 2: sp_cancelar_presentacion
BEGIN;

DO $$
DECLARE
    v_productor BIGINT;
    v_a BIGINT;   -- pase + entrada día 1
    v_b BIGINT;   -- entrada día 2
    v_c BIGINT;   -- entrada día 1 con ticket CANCELADO
    v_d BIGINT;   -- entrada día 1 con orden PENDIENTE
    v_recinto BIGINT;
    v_esc1 BIGINT;
    v_esc2 BIGINT;
    v_art1 BIGINT;
    v_art2 BIGINT;
    v_fest BIGINT;
    v_pase BIGINT;
    v_dia1 BIGINT;
    v_dia2 BIGINT;
    v_p1 BIGINT;  -- día 1
    v_p2 BIGINT;  -- día 1, otro escenario y artista
    v_p3 BIGINT;  -- día 2
    v_p4 BIGINT;  -- ya REALIZADA
    v_orden BIGINT;
    v_notif INT;
    v_total NUMERIC;
    v_cred NUMERIC;
    v_n INT;
    v_ok INT := 0;
    v_falla INT := 0;
BEGIN
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Productor', 'test.prod.cancel@prueba.cl', 'x' FROM rol WHERE tipo = 'PRODUCTOR' RETURNING id_usuario INTO v_productor;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente A', 'test.a.cancel@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_a;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente B', 'test.b.cancel@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_b;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente C', 'test.c.cancel@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_c;
    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    SELECT id, 'TEST Asistente D', 'test.d.cancel@prueba.cl', 'x' FROM rol WHERE tipo = 'ASISTENTE' RETURNING id_usuario INTO v_d;

    INSERT INTO recinto (nombre, direccion, aforo_maximo) VALUES ('TEST Recinto cancel', 'x', 1000) RETURNING id_recinto INTO v_recinto;
    INSERT INTO escenario (id_recinto, nombre, capacidad_maxima) VALUES (v_recinto, 'E1', 500) RETURNING id_escenario INTO v_esc1;
    INSERT INTO escenario (id_recinto, nombre, capacidad_maxima) VALUES (v_recinto, 'E2', 500) RETURNING id_escenario INTO v_esc2;
    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista 1 cancel', 'x') RETURNING id_artista INTO v_art1;
    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista 2 cancel', 'x') RETURNING id_artista INTO v_art2;

    -- Festival de 2 días (10 y 11 de junio de 2030)
    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
    VALUES (v_productor, v_recinto, 'TEST Festival cancel', DATE '2030-06-10', DATE '2030-06-11', 'PUBLICADO')
    RETURNING id_festival INTO v_fest;

    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Pase', 1000, 100, 100, NULL) RETURNING id_tipo_entrada INTO v_pase;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Día 1', 400, 100, 100, DATE '2030-06-10') RETURNING id_tipo_entrada INTO v_dia1;
    INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
    VALUES (v_fest, 'Día 2', 400, 100, 100, DATE '2030-06-11') RETURNING id_tipo_entrada INTO v_dia2;

    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_fest, v_esc1, v_art1, TIMESTAMP '2030-06-10 20:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-10 21:00' AT TIME ZONE 'America/Santiago')
    RETURNING id_presentacion INTO v_p1;
    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_fest, v_esc2, v_art2, TIMESTAMP '2030-06-10 20:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-10 21:00' AT TIME ZONE 'America/Santiago')
    RETURNING id_presentacion INTO v_p2;
    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_fest, v_esc1, v_art1, TIMESTAMP '2030-06-11 20:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-11 21:00' AT TIME ZONE 'America/Santiago')
    RETURNING id_presentacion INTO v_p3;
    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin, estado)
    VALUES (v_fest, v_esc1, v_art2, TIMESTAMP '2030-06-10 22:00' AT TIME ZONE 'America/Santiago', TIMESTAMP '2030-06-10 23:00' AT TIME ZONE 'America/Santiago', 'REALIZADA')
    RETURNING id_presentacion INTO v_p4;

    -- Compras
    CALL sp_comprar_entradas(v_a, v_pase, 1, false, v_orden);
    CALL sp_comprar_entradas(v_a, v_dia1, 1, false, v_orden);
    CALL sp_comprar_entradas(v_b, v_dia2, 1, false, v_orden);
    CALL sp_comprar_entradas(v_c, v_dia1, 1, false, v_orden);
    UPDATE ticket SET estado = 'CANCELADO' WHERE id_orden = v_orden;
    CALL sp_comprar_entradas(v_d, v_dia1, 1, false, v_orden);
    UPDATE orden_compra SET estado = 'PENDIENTE' WHERE id_orden = v_orden;

    -- Caso 1: cancelar presentación del día 1 (A: pase 500 + día 1 400; B, C y D no son afectados)
    BEGIN
        CALL sp_cancelar_presentacion(v_p1, v_notif, v_total);
        SELECT creditos_compensacion INTO v_cred FROM usuario WHERE id_usuario = v_a;
        IF v_notif = 1 AND v_total = 900 AND v_cred = 900
           AND (SELECT estado FROM presentacion WHERE id_presentacion = v_p1) = 'CANCELADA' THEN
            RAISE NOTICE 'OK    caso 1 (notifica solo a A, crédito 900, presentación CANCELADA)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 1: notificados=%, total=%, crédito A=%', v_notif, v_total, v_cred;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 1: %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 2: A recibe una notificación vinculada a la presentación, con el crédito en el mensaje
    SELECT count(*) INTO v_n FROM notificacion
    WHERE id_usuario = v_a AND id_presentacion = v_p1 AND NOT leida AND mensaje LIKE '%abonaron $900%';
    IF v_n = 1 THEN
        RAISE NOTICE 'OK    caso 2 (notificación no leída de A con el monto)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 2: notificaciones de A=%', v_n;
        v_falla := v_falla + 1;
    END IF;

    -- Caso 3: B (otro día), C (ticket cancelado) y D (orden pendiente) no reciben nada
    SELECT count(*) INTO v_n FROM notificacion WHERE id_usuario IN (v_b, v_c, v_d);
    SELECT count(*) INTO v_total FROM usuario WHERE id_usuario IN (v_b, v_c, v_d) AND creditos_compensacion <> 0;
    IF v_n = 0 AND v_total = 0 THEN
        RAISE NOTICE 'OK    caso 3 (no se notifica ni compensa a otro día, ticket cancelado ni orden pendiente)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 3: notificaciones=%, usuarios con crédito=%', v_n, v_total;
        v_falla := v_falla + 1;
    END IF;

    -- Caso 4: cancelar de nuevo la misma presentación se rechaza
    BEGIN
        CALL sp_cancelar_presentacion(v_p1, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 4: se canceló dos veces';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 4 (presentación ya cancelada rechazada: %)', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 5: segunda cancelación del mismo día: notifica a A pero sin crédito adicional
    BEGIN
        CALL sp_cancelar_presentacion(v_p2, v_notif, v_total);
        SELECT creditos_compensacion INTO v_cred FROM usuario WHERE id_usuario = v_a;
        SELECT count(*) INTO v_n FROM notificacion
        WHERE id_usuario = v_a AND id_presentacion = v_p2 AND mensaje NOT LIKE '%abonaron%';
        IF v_notif = 1 AND v_total = 0 AND v_cred = 900 AND v_n = 1 THEN
            RAISE NOTICE 'OK    caso 5 (mismo día: se notifica sin pagar dos veces)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 5: notificados=%, total=%, crédito A=%, notif sin monto=%', v_notif, v_total, v_cred, v_n;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 5: %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 6: día 2: A (otra mitad del pase, 500) y B (400); el pase suma 1000 en total
    BEGIN
        CALL sp_cancelar_presentacion(v_p3, v_notif, v_total);
        SELECT creditos_compensacion INTO v_cred FROM usuario WHERE id_usuario = v_a;
        IF v_notif = 2 AND v_total = 900 AND v_cred = 1400
           AND (SELECT creditos_compensacion FROM usuario WHERE id_usuario = v_b) = 400 THEN
            RAISE NOTICE 'OK    caso 6 (pase compensado por día: A=1400 en total, B=400)';
            v_ok := v_ok + 1;
        ELSE
            RAISE NOTICE 'FALLA caso 6: notificados=%, total=%, crédito A=%', v_notif, v_total, v_cred;
            v_falla := v_falla + 1;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'FALLA caso 6: %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 7: los tickets siguen VIGENTES y hay una compensación por ticket y día
    SELECT count(*) INTO v_n FROM ticket t JOIN tipo_entrada te USING (id_tipo_entrada)
    WHERE te.id_festival = v_fest AND t.estado = 'VIGENTE';
    SELECT count(*) INTO v_total FROM compensacion c JOIN ticket t USING (id_ticket) JOIN tipo_entrada te USING (id_tipo_entrada)
    WHERE te.id_festival = v_fest;
    IF v_n = 4 AND v_total = 4 THEN
        RAISE NOTICE 'OK    caso 7 (tickets siguen vigentes; 4 compensaciones: pase x2 días, día 1 y día 2)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 7: vigentes=%, compensaciones=%', v_n, v_total;
        v_falla := v_falla + 1;
    END IF;

    -- Caso 8: presentación REALIZADA no se puede cancelar
    BEGIN
        CALL sp_cancelar_presentacion(v_p4, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 8: se canceló una presentación realizada';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 8 (presentación realizada rechazada)';
        v_ok := v_ok + 1;
    END;

    -- Caso 9: presentación inexistente
    BEGIN
        CALL sp_cancelar_presentacion(-1, v_notif, v_total);
        RAISE NOTICE 'FALLA caso 9: aceptó una presentación inexistente';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 9 (presentación inexistente rechazada)';
        v_ok := v_ok + 1;
    END;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
