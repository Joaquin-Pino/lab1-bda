/* Procedimiento Almacenado 2: Cancelar presentación
Marca la presentación como CANCELADA, notifica a los asistentes con tickets VIGENTES de órdenes PAGADAS
para ese día y les acredita crédito de compensación, todo en una sola transacción.
Reglas:
- Día = fn_dia_festival(fecha_hora_inicio). Aplican los tipos de entrada con fecha = ese día o fecha NULL (pase).
- Ticket de un día: se compensa su precio. Pase (fecha NULL): precio / días del festival.
- A lo más una compensación por ticket y día (compensacion, UNIQUE): la segunda cancelación del
  mismo día notifica igual, pero sin crédito adicional.
- Los tickets siguen VIGENTES (pueden servir para otras presentaciones).
*/

CREATE OR REPLACE PROCEDURE sp_cancelar_presentacion(
    p_id_presentacion BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_festival BIGINT;
    v_inicio TIMESTAMPTZ;
    v_estado VARCHAR;
    v_dia DATE;
    v_dias_festival INT;
    v_artista TEXT;
    v_festival TEXT;
BEGIN
    SELECT p.id_festival, p.fecha_hora_inicio, p.estado, a.nombre, f.nombre,
           (f.fecha_fin - f.fecha_inicio + 1)
    INTO v_id_festival, v_inicio, v_estado, v_artista, v_festival, v_dias_festival
    FROM presentacion p
    JOIN festival f ON f.id_festival = p.id_festival
    JOIN artista a ON a.id_artista = p.id_artista
    WHERE p.id_presentacion = p_id_presentacion
    FOR UPDATE OF p;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Presentación no encontrada';
    END IF;
    IF v_estado <> 'PROGRAMADA' THEN
        RAISE EXCEPTION 'Solo se puede cancelar una presentación programada (estado actual: %)', v_estado;
    END IF;

    UPDATE presentacion SET estado = 'CANCELADA' WHERE id_presentacion = p_id_presentacion;

    v_dia := fn_dia_festival(v_inicio);

    -- Compensación nueva (la clave única evita pagar dos veces el mismo ticket y día)
    INSERT INTO compensacion (id_ticket, dia, id_presentacion, id_usuario, monto)
    SELECT t.id_ticket, v_dia, p_id_presentacion, o.id_usuario_asistente,
           CASE WHEN te.fecha IS NULL THEN round(t.precio / v_dias_festival, 2) ELSE t.precio END
    FROM ticket t
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN orden_compra o ON o.id_orden = t.id_orden
    WHERE te.id_festival = v_id_festival
      AND (te.fecha IS NULL OR te.fecha = v_dia)
      AND t.estado = 'VIGENTE'
      AND o.estado = 'PAGADA'
    ON CONFLICT (id_ticket, dia) DO NOTHING;

    -- Abonar el crédito (una actualización por usuario)
    UPDATE usuario u
    SET creditos_compensacion = u.creditos_compensacion + c.total
    FROM (SELECT id_usuario, SUM(monto) AS total
          FROM compensacion WHERE id_presentacion = p_id_presentacion
          GROUP BY id_usuario) c
    WHERE u.id_usuario = c.id_usuario;

    -- Una notificación por asistente afectado (también los que ya habían sido compensados ese día)
    INSERT INTO notificacion (id_usuario, id_presentacion, mensaje)
    SELECT a.id_usuario, p_id_presentacion,
           format('Se canceló la presentación de %s en %s (%s).%s', v_artista, v_festival, to_char(v_dia, 'DD-MM-YYYY'),
                  CASE WHEN COALESCE(c.total, 0) > 0
                       THEN format(' Se abonaron $%s a tu crédito de compensación.', c.total)
                       ELSE '' END)
    FROM (SELECT DISTINCT o.id_usuario_asistente AS id_usuario
          FROM ticket t
          JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
          JOIN orden_compra o ON o.id_orden = t.id_orden
          WHERE te.id_festival = v_id_festival
            AND (te.fecha IS NULL OR te.fecha = v_dia)
            AND t.estado = 'VIGENTE' AND o.estado = 'PAGADA') a
    LEFT JOIN (SELECT id_usuario, SUM(monto) AS total
               FROM compensacion WHERE id_presentacion = p_id_presentacion
               GROUP BY id_usuario) c ON c.id_usuario = a.id_usuario;
    GET DIAGNOSTICS p_notificados = ROW_COUNT;

    SELECT COALESCE(SUM(monto), 0) INTO p_credito_total
    FROM compensacion WHERE id_presentacion = p_id_presentacion;
END $$;
