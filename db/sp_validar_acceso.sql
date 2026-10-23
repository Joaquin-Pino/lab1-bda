/* Procedimiento Almacenado 3: Validar acceso (control de entrada)
Recibe el código de un ticket y el festival donde está el lector. Si es válido lo marca como UTILIZADO
y registra todo intento sobre un ticket existente en log_entrada.
p_resultado: VALIDO | YA_USADO | ANULADO | INEXISTENTE | OTRO_FESTIVAL | FECHA_INCORRECTA
Doble escaneo: el UPDATE condicional (estado = 'VIGENTE') toma el candado de la fila, así que de dos
lectores simultáneos solo uno obtiene VALIDO; el otro espera y recibe YA_USADO.
INEXISTENTE no se registra porque log_entrada exige un ticket.
*/

CREATE OR REPLACE PROCEDURE sp_validar_acceso(
    p_codigo VARCHAR,
    p_id_festival BIGINT,
    p_id_usuario BIGINT,
    INOUT p_resultado VARCHAR DEFAULT NULL,
    INOUT p_id_ticket BIGINT DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
    v_estado_orden VARCHAR;
    v_fecha_entrada DATE;
    v_id_festival BIGINT;
    v_inicio DATE;
    v_fin DATE;
    v_hoy DATE := fn_dia_festival(now());
BEGIN
    SELECT t.id_ticket, t.estado, o.estado, te.fecha, te.id_festival, f.fecha_inicio, f.fecha_fin
    INTO p_id_ticket, v_estado, v_estado_orden, v_fecha_entrada, v_id_festival, v_inicio, v_fin
    FROM ticket t
    JOIN orden_compra o ON o.id_orden = t.id_orden
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN festival f ON f.id_festival = te.id_festival
    WHERE t.codigo_unico = p_codigo;

    IF NOT FOUND THEN
        p_resultado := 'INEXISTENTE';
        p_id_ticket := NULL;
        RETURN;
    END IF;

    IF v_id_festival <> p_id_festival THEN
        p_resultado := 'OTRO_FESTIVAL';
    ELSIF v_estado = 'CANCELADO' OR v_estado_orden <> 'PAGADA' THEN
        p_resultado := 'ANULADO';
    ELSIF v_estado = 'UTILIZADO' THEN
        p_resultado := 'YA_USADO';
    ELSIF v_hoy NOT BETWEEN v_inicio AND v_fin OR (v_fecha_entrada IS NOT NULL AND v_fecha_entrada <> v_hoy) THEN
        p_resultado := 'FECHA_INCORRECTA';
    ELSE
        UPDATE ticket SET estado = 'UTILIZADO', fecha_uso = now()
        WHERE id_ticket = p_id_ticket AND estado = 'VIGENTE';
        IF FOUND THEN
            p_resultado := 'VALIDO';
        ELSE
            p_resultado := 'YA_USADO';  -- otro lector lo validó justo antes
        END IF;
    END IF;

    INSERT INTO log_entrada (id_ticket, id_usuario, resultado) VALUES (p_id_ticket, p_id_usuario, p_resultado);
END $$;
