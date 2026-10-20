-- Trigger 1: Validación de traslapes en presentaciones

CREATE OR REPLACE FUNCTION fn_validar_presentacion()
RETURNS TRIGGER AS $$
BEGIN
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

