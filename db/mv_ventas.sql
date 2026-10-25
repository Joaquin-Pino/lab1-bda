-- 1. Crear la vista materializada
CREATE MATERIALIZED VIEW mv_ventas AS
SELECT 
    f.id_festival, -- ID del festival
    f.nombre AS festival_nombre, -- Nombre del festival
    te.nombre_tipo AS tipo_entrada, -- Nombre del tipo de entrada
    te.aforo_total, -- Aforo total
    (te.aforo_total - te.aforo_disponible) AS tickets_vendidos, -- Tickets vendidos
    ROUND(((te.aforo_total - te.aforo_disponible)::numeric / te.aforo_total) * 100, 2) AS porcentaje_ocupacion, -- Porcentaje de ocupación
    COALESCE(SUM(t.precio), 0) AS recaudacion_total -- COALESCE si el resultado de la suma es null devuelve un cero
FROM festival f
JOIN tipo_entrada te ON f.id_festival = te.id_festival
-- LEFT JOIN Mantiene los tipos de entrada aunque no hayan tickets vendidos en ese tipo
--  <> 'CANCELADO' importante para evitar ventas fantasma, entradas devueltas o echas credito
LEFT JOIN ticket t ON te.id_tipo_entrada = t.id_tipo_entrada AND t.estado <> 'CANCELADO'
GROUP BY f.id_festival, f.nombre, te.nombre_tipo, te.aforo_total, te.aforo_disponible;

-- 2. Función para refrescar los datos
CREATE OR REPLACE FUNCTION fn_refrescar_reporte_ventas()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW mv_ventas;
END;
$$ LANGUAGE plpgsql;