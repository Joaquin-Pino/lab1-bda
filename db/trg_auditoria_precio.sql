-- Trigger 2: Auditoría de precios

CREATE OR REPLACE FUNCTION fn_auditar_precio()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auditoria_precio_entrada (id_tipo_entrada, precio_anterior, precio_nuevo, id_usuario_modificador)
    VALUES (NEW.id_tipo_entrada, OLD.precio_actual, NEW.precio_actual, NULLIF(current_setting('app.user_id', true), '')::BIGINT);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_auditoria_precio ON tipo_entrada;
CREATE TRIGGER trg_auditoria_precio
AFTER UPDATE OF precio_actual ON tipo_entrada
FOR EACH ROW
WHEN (OLD.precio_actual IS DISTINCT FROM NEW.precio_actual)
EXECUTE FUNCTION fn_auditar_precio();