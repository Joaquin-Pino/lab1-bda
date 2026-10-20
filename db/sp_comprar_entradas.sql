/* Procedimiento Almacenado 1: Comprar entradas
Validar el cupo disponible del tipo de entrada, crear la orden, generar un ticket
con código único por cada entrada y descontar el aforo en una sola transacción atómica.
*/

CREATE OR REPLACE PROCEDURE sp_comprar_entradas(
    p_id_usuario BIGINT,
    p_id_tipo_entrada BIGINT,
    p_cantidad INT,
    p_usar_credito BOOLEAN DEFAULT FALSE,
    INOUT p_id_orden BIGINT DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_precio NUMERIC;
    v_total NUMERIC;
    v_creditos NUMERIC;
    v_creditos_usar NUMERIC := 0;
BEGIN
    -- Verificar cantidad positiva
    IF p_cantidad IS NULL OR p_cantidad < 1 THEN
        RAISE EXCEPTION 'La cantidad de entradas debe ser positiva';
    END IF;

    -- Descontar aforo disponible
    UPDATE tipo_entrada te
    SET aforo_disponible = te.aforo_disponible - p_cantidad
    FROM festival f
    WHERE te.id_tipo_entrada = p_id_tipo_entrada
    AND te.id_festival = f.id_festival
    AND f.estado = 'PUBLICADO'
    AND te.aforo_disponible >= p_cantidad
    RETURNING te.precio_actual INTO v_precio;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Aforo insuficiente o festival no publicado';
    END IF;

    -- Calcular total a pagar
    v_total := v_precio * p_cantidad;

    -- Si usa crédito
    IF p_usar_credito THEN
        SELECT creditos_compensacion INTO v_creditos
        FROM usuario
        WHERE id_usuario = p_id_usuario FOR UPDATE;

        v_creditos_usar := LEAST(COALESCE(v_creditos, 0), v_total);

        UPDATE usuario
        SET creditos_compensacion = creditos_compensacion - v_creditos_usar
        WHERE id_usuario = p_id_usuario;
    END IF;

    -- Crear orden
    INSERT INTO orden_compra(id_usuario_asistente, monto_total, monto_credito_aplicado)
    VALUES (p_id_usuario, v_total, v_creditos_usar)
    RETURNING id_orden INTO p_id_orden;

    -- Generar tickets
    INSERT INTO ticket(codigo_unico, id_orden, id_tipo_entrada, precio)
    SELECT replace(gen_random_uuid()::text, '-', ''), p_id_orden, p_id_tipo_entrada, v_precio
    FROM generate_series(1, p_cantidad);
    
END $$;