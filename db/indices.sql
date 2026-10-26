-- Punto 8 del enunciado 
-- Indexar el código único del ticket (Hash) y el ID del festival 
-- para acelerar la  validación de accesos y los reportes. 

-- Indexar el código único del ticket (Hash)
-- Facilita la instrucción "WHERE t.codigo_unico=p_codigo" de la funcion sp_validar_acceso y la vista mv_ventas.
-- En vez de probar una por una va directo al ticket con el hash
CREATE INDEX idx_ticket_codigo_hash ON ticket USING HASH (codigo_unico);

-- Indexar el ID del festival en tipo_entrada
-- Mejora "JOIN tipo_entrada te ON f.id_festival = te.id_festival"
-- Al refrescar la lista debía recorrar toda la tabla de datos para encontrar 
-- los tipos de entradas del festival.
CREATE INDEX idx_tipo_entrada_festival ON tipo_entrada (id_festival);