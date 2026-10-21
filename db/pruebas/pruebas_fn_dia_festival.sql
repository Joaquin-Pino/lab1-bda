-- Pruebas de la función fn_dia_festival
BEGIN;

DO $$
DECLARE
    v_dia_utc    DATE;
    v_dia_scl    DATE;
    v_ok         INT := 0;
    v_falla      INT := 0;
BEGIN
    -- Caso 1: 22:00 en Chile con la sesión en UTC (un ::date directo daría 2026-12-06)
    SET LOCAL TIME ZONE 'UTC';
    IF fn_dia_festival('2026-12-05 22:00-03') = DATE '2026-12-05' THEN
        RAISE NOTICE 'OK    caso 1 (22:00 Chile con sesión UTC -> 2026-12-05)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 1: devolvió %', fn_dia_festival('2026-12-05 22:00-03');
        v_falla := v_falla + 1;
    END IF;

    -- Caso 2: 01:00 AM cuenta como el día calendario siguiente
    IF fn_dia_festival('2026-12-06 01:00-03') = DATE '2026-12-06' THEN
        RAISE NOTICE 'OK    caso 2 (01:00 Chile -> 2026-12-06)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 2: devolvió %', fn_dia_festival('2026-12-06 01:00-03');
        v_falla := v_falla + 1;
    END IF;

    -- Caso 3: mismo resultado con la sesión en America/Santiago y en UTC
    SET LOCAL TIME ZONE 'UTC';
    v_dia_utc := fn_dia_festival('2026-12-05 22:00-03');
    SET LOCAL TIME ZONE 'America/Santiago';
    v_dia_scl := fn_dia_festival('2026-12-05 22:00-03');
    IF v_dia_utc = v_dia_scl AND v_dia_scl = DATE '2026-12-05' THEN
        RAISE NOTICE 'OK    caso 3 (mismo día con sesión UTC y America/Santiago)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 3: UTC=%, Santiago=%', v_dia_utc, v_dia_scl;
        v_falla := v_falla + 1;
    END IF;

    -- Caso 4: borde de medianoche (23:59:59 y 00:00:00)
    SET LOCAL TIME ZONE 'UTC';
    IF fn_dia_festival('2026-12-05 23:59:59-03') = DATE '2026-12-05'
       AND fn_dia_festival('2026-12-06 00:00:00-03') = DATE '2026-12-06' THEN
        RAISE NOTICE 'OK    caso 4 (23:59:59 -> 2026-12-05, 00:00:00 -> 2026-12-06)';
        v_ok := v_ok + 1;
    ELSE
        RAISE NOTICE 'FALLA caso 4: 23:59:59=%, 00:00:00=%',
            fn_dia_festival('2026-12-05 23:59:59-03'), fn_dia_festival('2026-12-06 00:00:00-03');
        v_falla := v_falla + 1;
    END IF;

    RAISE NOTICE 'RESUMEN: % OK, % FALLA', v_ok, v_falla;
END;
$$;

ROLLBACK;
