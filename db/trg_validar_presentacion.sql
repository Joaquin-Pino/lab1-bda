-- Trigger 1: Validación de traslapes en presentaciones

CREATE OR REPLACE FUNCTION fn_validar_presentacion()
RETURNS TRIGGER AS $$
DECLARE
v_recinto_festival  BIGINT;
	v_recinto_escenario BIGINT;
	v_fecha_inicio      DATE;
	v_fecha_fin         DATE;
BEGIN
	-- Una presentación cancelada no ocupa horario: no se valida
	IF NEW.estado = 'CANCELADA' THEN
		RETURN NEW;
END IF;

	-- El escenario debe pertenecer al recinto del festival
SELECT id_recinto, fecha_inicio, fecha_fin
INTO v_recinto_festival, v_fecha_inicio, v_fecha_fin
FROM festival
WHERE id_festival = NEW.id_festival;

SELECT id_recinto
INTO v_recinto_escenario
FROM escenario
WHERE id_escenario = NEW.id_escenario;

-- Si el festival o el escenario no existen, las variables quedan NULL,
-- no se lanza nada aquí y luego falla la clave foránea con su propio error
IF v_recinto_escenario <> v_recinto_festival THEN
		RAISE EXCEPTION 'El escenario no pertenece al recinto del festival';
END IF;

	-- La presentación debe ser en un día del festival
	IF fn_dia_festival(NEW.fecha_hora_inicio) NOT BETWEEN v_fecha_inicio AND v_fecha_fin THEN
		RAISE EXCEPTION 'La presentación debe ser entre el % y el % (fechas del festival)',
			v_fecha_inicio, v_fecha_fin;
END IF;

	-- Mismo escenario con horario cruzado
	IF EXISTS (
		SELECT 1
			FROM presentacion p
			WHERE p.id_escenario = NEW.id_escenario
				AND p.id_presentacion <> COALESCE(NEW.id_presentacion, -1)
				AND p.estado <> 'CANCELADA'
				AND p.fecha_hora_inicio < NEW.fecha_hora_fin
				AND NEW.fecha_hora_inicio < p.fecha_hora_fin
	) THEN
		RAISE EXCEPTION 'El escenario ya tiene una presentación en ese horario';
	END IF;

	-- Mismo artista con horario cruzado
	IF EXISTS (
		SELECT 1
			FROM presentacion p
			WHERE p.id_artista = NEW.id_artista
				AND p.id_presentacion <> COALESCE(NEW.id_presentacion, -1)
				AND p.estado <> 'CANCELADA'
				AND p.fecha_hora_inicio < NEW.fecha_hora_fin
				AND NEW.fecha_hora_inicio < p.fecha_hora_fin
	) THEN
		RAISE EXCEPTION 'El artista ya tiene otra presentación en ese horario';
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_presentacion ON presentacion;
CREATE TRIGGER trg_validar_presentacion
BEFORE INSERT OR UPDATE ON presentacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_presentacion();

-- Respaldo para inserciones simultáneas.

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE presentacion DROP CONSTRAINT IF EXISTS ex_presentacion_escenario;
ALTER TABLE presentacion ADD CONSTRAINT ex_presentacion_escenario
    EXCLUDE USING gist (
		id_escenario WITH =,
		tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
	) WHERE (estado <> 'CANCELADA');

ALTER TABLE presentacion DROP CONSTRAINT IF EXISTS ex_presentacion_artista;
ALTER TABLE presentacion ADD CONSTRAINT ex_presentacion_artista
    EXCLUDE USING gist (
		id_artista WITH =,
		tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
	) WHERE (estado <> 'CANCELADA');

