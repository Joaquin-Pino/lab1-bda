/* Procedimientos de cancelación con devolución de crédito: sp_cancelar_festival y sp_cancelar_tipo_entrada
Reemplazan al borrado cuando ya hay tickets vendidos (la FK de ticket impide el DELETE y borrar haría
desaparecer lo que los asistentes pagaron). Todo ocurre en la transacción del llamador.
Reglas:
- Se devuelve el precio de cada ticket VIGENTE de una orden PAGADA, descontando lo ya compensado
  por cancelaciones de presentaciones (nunca se devuelve más que el precio). Los UTILIZADO no se devuelven.
- El dinero se abona a usuario.creditos_compensacion y queda registrado en compensacion
  (dia = fecha de inicio del festival; si ya había una fila ese día para el ticket, se suma).
- Los tickets pasan a CANCELADO y las órdenes sin tickets activos a CANCELADA.
- Cada asistente afectado recibe una notificación (sin presentación asociada).
- El detalle por usuario queda en la tabla temporal tmp_devolucion (vive hasta el fin de la transacción);
  el backend lo lee para armar la respuesta.
*/

-- Auxiliar: devuelve los tickets vigentes del festival (o de un solo tipo de entrada si se indica)
CREATE OR REPLACE PROCEDURE sp_devolver_tickets(
    p_id_festival BIGINT,
    p_id_tipo_entrada BIGINT,
    p_motivo TEXT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_dia DATE;
BEGIN
    SELECT fecha_inicio INTO v_dia FROM festival WHERE id_festival = p_id_festival;

    DROP TABLE IF EXISTS tmp_devolucion;
    CREATE TEMP TABLE tmp_devolucion (
        id_ticket BIGINT PRIMARY KEY,
        id_orden BIGINT NOT NULL,
        id_usuario BIGINT NOT NULL,
        monto NUMERIC(12,2) NOT NULL
    ) ON COMMIT DROP;

    -- Tickets afectados con lo que falta devolver de cada uno (bloqueados para no competir con el control de acceso)
    INSERT INTO tmp_devolucion (id_ticket, id_orden, id_usuario, monto)
    SELECT t.id_ticket, t.id_orden, o.id_usuario_asistente,
           GREATEST(t.precio - COALESCE((SELECT SUM(c.monto) FROM compensacion c WHERE c.id_ticket = t.id_ticket), 0), 0)
    FROM ticket t
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN orden_compra o ON o.id_orden = t.id_orden
    WHERE te.id_festival = p_id_festival
      AND (p_id_tipo_entrada IS NULL OR te.id_tipo_entrada = p_id_tipo_entrada)
      AND t.estado = 'VIGENTE'
      AND o.estado = 'PAGADA'
    FOR UPDATE OF t;

    INSERT INTO compensacion (id_ticket, dia, id_presentacion, id_usuario, monto)
    SELECT id_ticket, v_dia, NULL, id_usuario, monto FROM tmp_devolucion WHERE monto > 0
    ON CONFLICT (id_ticket, dia) DO UPDATE SET monto = compensacion.monto + EXCLUDED.monto;

    UPDATE usuario u
    SET creditos_compensacion = u.creditos_compensacion + d.total
    FROM (SELECT id_usuario, SUM(monto) AS total FROM tmp_devolucion GROUP BY id_usuario) d
    WHERE u.id_usuario = d.id_usuario;

    UPDATE ticket SET estado = 'CANCELADO' WHERE id_ticket IN (SELECT id_ticket FROM tmp_devolucion);

    UPDATE orden_compra o
    SET estado = 'CANCELADA'
    WHERE o.id_orden IN (SELECT id_orden FROM tmp_devolucion)
      AND NOT EXISTS (SELECT 1 FROM ticket t WHERE t.id_orden = o.id_orden AND t.estado <> 'CANCELADO');

    INSERT INTO notificacion (id_usuario, mensaje)
    SELECT id_usuario,
           p_motivo || CASE WHEN SUM(monto) > 0
                            THEN format(' Se abonaron $%s a tu crédito de compensación.', SUM(monto))
                            ELSE '' END
    FROM tmp_devolucion
    GROUP BY id_usuario;
    GET DIAGNOSTICS p_notificados = ROW_COUNT;

    SELECT COALESCE(SUM(monto), 0) INTO p_credito_total FROM tmp_devolucion;
END $$;

-- Cancela el festival (estado CANCELADO), sus presentaciones programadas y devuelve el dinero a los asistentes
CREATE OR REPLACE PROCEDURE sp_cancelar_festival(
    p_id_festival BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
    v_nombre VARCHAR;
BEGIN
    SELECT estado, nombre INTO v_estado, v_nombre
    FROM festival WHERE id_festival = p_id_festival FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Festival no encontrado';
    END IF;
    IF v_estado IN ('CANCELADO', 'FINALIZADO') THEN
        RAISE EXCEPTION 'No se puede cancelar un festival %', lower(v_estado);
    END IF;

    UPDATE festival SET estado = 'CANCELADO' WHERE id_festival = p_id_festival;
    UPDATE presentacion SET estado = 'CANCELADA'
    WHERE id_festival = p_id_festival AND estado = 'PROGRAMADA';

    CALL sp_devolver_tickets(p_id_festival, NULL, format('Se canceló el festival %s.', v_nombre),
                             p_notificados, p_credito_total);
END $$;

-- Cancela un tipo de entrada: cierra su venta (aforo disponible 0) y devuelve el dinero de sus tickets vigentes
CREATE OR REPLACE PROCEDURE sp_cancelar_tipo_entrada(
    p_id_tipo_entrada BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_festival BIGINT;
    v_tipo VARCHAR;
    v_festival VARCHAR;
    v_estado VARCHAR;
BEGIN
    SELECT te.id_festival, te.nombre_tipo, f.nombre, f.estado
    INTO v_id_festival, v_tipo, v_festival, v_estado
    FROM tipo_entrada te
    JOIN festival f ON f.id_festival = te.id_festival
    WHERE te.id_tipo_entrada = p_id_tipo_entrada
    FOR UPDATE OF te;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Tipo de entrada no encontrado';
    END IF;
    IF v_estado IN ('CANCELADO', 'FINALIZADO') THEN
        RAISE EXCEPTION 'No se puede cancelar una entrada de un festival %', lower(v_estado);
    END IF;

    UPDATE tipo_entrada SET aforo_disponible = 0 WHERE id_tipo_entrada = p_id_tipo_entrada;

    CALL sp_devolver_tickets(v_id_festival, p_id_tipo_entrada,
                             format('Se canceló la entrada "%s" del festival %s.', v_tipo, v_festival),
                             p_notificados, p_credito_total);
END $$;
