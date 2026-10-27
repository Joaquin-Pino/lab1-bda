CREATE EXTENSION IF NOT EXISTS pgcrypto;

/* Datos semilla de DESARROLLO (no usar en producción)
Contraseña de todos los usuarios: Festival123!
Los hashes BCrypt ($2a$) se generan con pgcrypto y son compatibles con BCryptPasswordEncoder.
Los nombres no usan el prefijo "TEST " ni emails test.*@prueba.cl para no chocar con db/pruebas/.
*/

-- Usuarios (ORDER BY orden: ids estables 1..6 en una base recién creada)
INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion)
SELECT r.id, u.nombre, u.email, crypt('Festival123!', gen_salt('bf', 10)), u.creditos
FROM (VALUES
    (1, 'ADMINISTRADOR', 'Administrador General', 'admin@festivales.cl',      0),
    (2, 'PRODUCTOR',     'Productora Uno',        'productor1@festivales.cl', 0),
    (3, 'PRODUCTOR',     'Productor Dos',         'productor2@festivales.cl', 0),
    (4, 'ASISTENTE',     'Asistente Uno',         'asistente1@festivales.cl', 0),
    (5, 'ASISTENTE',     'Asistente Dos',         'asistente2@festivales.cl', 0),
    (6, 'ASISTENTE',     'Asistente Tres',        'asistente3@festivales.cl', 5000)
) AS u(orden, tipo, nombre, email, creditos)
JOIN rol r ON r.tipo = u.tipo
ORDER BY u.orden;

-- Recinto y escenarios
INSERT INTO recinto (nombre, direccion, aforo_maximo)
VALUES ('Parque Central Santiago', 'Av. Libertador Bernardo O''Higgins 1000, Santiago', 20000);

INSERT INTO escenario (id_recinto, nombre, capacidad_maxima)
SELECT id_recinto, e.nombre, e.capacidad
FROM recinto, (VALUES ('Escenario Principal', 12000), ('Escenario Alternativo', 5000)) AS e(nombre, capacidad)
WHERE recinto.nombre = 'Parque Central Santiago';

-- Artistas
INSERT INTO artista (nombre, genero_musical)
VALUES ('Los Andes Eléctricos', 'Rock'), ('Luna Austral', 'Pop');

-- Festival publicado del productor1 (12 y 13 de diciembre de 2026)
INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
SELECT u.id_usuario, r.id_recinto, 'Festival Cordillera 2026', DATE '2026-12-12', DATE '2026-12-13', 'PUBLICADO'
FROM usuario u, recinto r
WHERE u.email = 'productor1@festivales.cl' AND r.nombre = 'Parque Central Santiago';

-- Tipos de entrada: abono (fecha NULL) y una entrada por día
INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
SELECT f.id_festival, t.nombre, t.precio, t.aforo, t.aforo, t.fecha
FROM festival f, (VALUES
    ('Pase 2 Días',   60000.00, 5000, NULL::date),
    ('General Día 1', 35000.00, 7000, DATE '2026-12-12'),
    ('General Día 2', 35000.00, 7000, DATE '2026-12-13')
) AS t(nombre, precio, aforo, fecha)
WHERE f.nombre = 'Festival Cordillera 2026';

-- Festival en borrador del productor2
INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
SELECT u.id_usuario, r.id_recinto, 'Festival Verano 2027', DATE '2027-01-16', DATE '2027-01-17', 'BORRADOR'
FROM usuario u, recinto r
WHERE u.email = 'productor2@festivales.cl' AND r.nombre = 'Parque Central Santiago';
