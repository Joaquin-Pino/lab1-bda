-- Pruebas del Trigger 1
BEGIN;

DO $$
DECLARE
    v_productor  BIGINT;
    v_recinto    BIGINT;
    v_festival   BIGINT;
    v_esc_a      BIGINT;
    v_esc_b      BIGINT;
    v_art_1      BIGINT;
    v_art_2      BIGINT;
    v_art_3      BIGINT;
    v_p1         BIGINT;
    v_p2         BIGINT;
    v_ok         INT := 0;
    v_falla      INT := 0;
BEGIN
	-- Datos
    INSERT INTO rol (tipo) VALUES ('PRODUCTOR') ON CONFLICT (tipo) DO NOTHING;

    INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
    VALUES ((SELECT id FROM rol WHERE tipo = 'PRODUCTOR'),
            'TEST Productor', 'test.productor@prueba.cl', 'x')
    RETURNING id_usuario INTO v_productor;

    INSERT INTO recinto (nombre, direccion, aforo_maximo)
    VALUES ('TEST Recinto', 'Calle Falsa 123', 10000)
    RETURNING id_recinto INTO v_recinto;

    INSERT INTO escenario (id_recinto, nombre, capacidad_maxima)
    VALUES (v_recinto, 'TEST Escenario A', 5000) RETURNING id_escenario INTO v_esc_a;
    INSERT INTO escenario (id_recinto, nombre, capacidad_maxima)
    VALUES (v_recinto, 'TEST Escenario B', 3000) RETURNING id_escenario INTO v_esc_b;

    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista 1', 'Rock')
    RETURNING id_artista INTO v_art_1;
    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista 2', 'Pop')
    RETURNING id_artista INTO v_art_2;
    INSERT INTO artista (nombre, genero_musical) VALUES ('TEST Artista 3', 'Jazz')
    RETURNING id_artista INTO v_art_3;

    INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin)
    VALUES (v_productor, v_recinto, 'TEST Festival', '2026-12-05', '2026-12-06')
    RETURNING id_festival INTO v_festival;

    -- Presentación base: Escenario A, Artista 1, 18:00 a 19:00
    INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
    VALUES (v_festival, v_esc_a, v_art_1, '2026-12-05 18:00-03', '2026-12-05 19:00-03')
    RETURNING id_presentacion INTO v_p1;

    -- Caso 1: Rechazo
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_a, v_art_2, '2026-12-05 18:30-03', '2026-12-05 19:30-03');
        RAISE NOTICE 'FALLA caso 1: se permitió un cruce en el mismo escenario';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 1 (mismo escenario, horario cruzado) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 2: Rechazo
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_a, v_art_3, '2026-12-05 18:15-03', '2026-12-05 18:45-03');
        RAISE NOTICE 'FALLA caso 2: se permitió una presentación contenida en otra';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 2 (contenida dentro de otra) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

     -- Caso 3: Rechazo
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_b, v_art_1, '2026-12-05 18:30-03', '2026-12-05 19:30-03');
        RAISE NOTICE 'FALLA caso 3: se permitió un artista en dos escenarios a la vez';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 3 (mismo artista, otro escenario) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

     -- Caso 4: Pasar
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_a, v_art_2, '2026-12-05 19:00-03', '2026-12-05 20:00-03')
        RETURNING id_presentacion INTO v_p2;
        RAISE NOTICE 'OK    caso 4 (empieza justo cuando termina la otra)';
        v_ok := v_ok + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'FALLA caso 4: se rechazó un borde que solo se toca -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 5: Pasar
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_b, v_art_3, '2026-12-05 18:30-03', '2026-12-05 19:30-03');
        RAISE NOTICE 'OK    caso 5 (mismo horario, otro escenario y otro artista)';
        v_ok := v_ok + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'FALLA caso 5 -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

    -- Caso 6: Pasar
    BEGIN
        UPDATE presentacion
           SET fecha_hora_fin = fecha_hora_fin
         WHERE id_presentacion = v_p1;
        RAISE NOTICE 'OK    caso 6 (editar sin cambiar horario, no choca consigo misma)';
        v_ok := v_ok + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'FALLA caso 6: la presentación chocó consigo misma -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

     -- Caso 7: Rechazo
    BEGIN
        UPDATE presentacion
           SET fecha_hora_inicio = '2026-12-05 18:30-03'
         WHERE id_presentacion = v_p2;
        RAISE NOTICE 'FALLA caso 7: se permitió mover una presentación encima de otra';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 7 (UPDATE que mueve una encima de otra) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 8: Pasar
    BEGIN
        UPDATE presentacion SET estado = 'CANCELADA' WHERE id_presentacion = v_p1;
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_a, v_art_1, '2026-12-05 18:00-03', '2026-12-05 19:00-03');
        RAISE NOTICE 'OK    caso 8 (una cancelada libera el horario)';
        v_ok := v_ok + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'FALLA caso 8: la cancelada siguió bloqueando -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;

     -- Caso 9: Rechazo
    BEGIN
        UPDATE presentacion SET estado = 'PROGRAMADA' WHERE id_presentacion = v_p1;
        RAISE NOTICE 'FALLA caso 9: se reactivó una cancelada sobre un horario ocupado';
        v_falla := v_falla + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'OK    caso 9 (reactivar cancelada con horario ya ocupado) -> %', SQLERRM;
        v_ok := v_ok + 1;
    END;

    -- Caso 10: Pasar
    BEGIN
        INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
        VALUES (v_festival, v_esc_a, v_art_1, '2026-12-06 18:00-03', '2026-12-06 19:00-03');
        RAISE NOTICE 'OK    caso 10 (misma hora, pero otro día)';
        v_ok := v_ok + 1;
    EXCEPTION WHEN raise_exception THEN
        RAISE NOTICE 'FALLA caso 10 -> %', SQLERRM;
        v_falla := v_falla + 1;
    END;
    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;