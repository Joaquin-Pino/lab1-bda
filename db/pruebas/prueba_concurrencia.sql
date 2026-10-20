/*
Iniciar contenedor de postgres para pruebas en puerto 5433:
docker run -d --name pg-lab1 -e POSTGRES_PASSWORD=lab1 -e POSTGRES_USER=lab1 -e POSTGRES_DB=lab1bd -p 5433:5432 postgres:16

Cargar esquema, procedimiento y datos de prueba (terminal debe estar en directorio db):
psql -h localhost -p 5433 -U lab1 -d lab1bd -f createDb.sql -f sp_comprar_entradas.sql -f pruebas/prueba_concurrencia.sql

Luego, en dos terminales distintas (A y B) ejecutar:
psql -h localhost -p 5433 -U lab1 -d lab1bd

En cualquiera de las dos hacer: UPDATE tipo_entrada SET aforo_disponible = 1 WHERE id_tipo_entrada = 1;

En terminal A: BEGIN; CALL sp_comprar_entradas(2, 1, 1, false, NULL);

En terminal B: BEGIN; CALL sp_comprar_entradas(3, 1, 1, false, NULL);

La terminal B debería quedar esperando hasta que en la terminal A se ejecute: COMMIT;

Luego en la terminal B debería verse el error: Aforo insuficiente o festival no publicado

En terminal B hacer: ROLLBACK;

Luego verificar en cualquier terminal: SELECT count(*) FROM ticket WHERE id_tipo_entrada = 1;

debería mostrar 1, pues solo se vendió una entrada

Para salir de psql: \q

Eliminar contenedor: docker rm -f pg-lab1
*/

INSERT INTO rol (tipo) VALUES ('ASISTENTE'), ('PRODUCTOR');

INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion) VALUES
  ((SELECT id FROM rol WHERE tipo = 'PRODUCTOR'), 'Productor Uno', 'prod@test.cl', 'x', 0),
  ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), 'Asistente Uno', 'asi1@test.cl', 'x', 5000),
  ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), 'Asistente Dos', 'asi2@test.cl', 'x', 0);

INSERT INTO recinto (nombre, direccion, aforo_maximo) VALUES ('Estadio Test', 'Calle 1', 10000);

INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado) VALUES
  ((SELECT id_usuario FROM usuario WHERE email = 'prod@test.cl'),
   (SELECT id_recinto FROM recinto WHERE nombre = 'Estadio Test'),
   'Fest Publicado', '2026-12-01', '2026-12-03', 'PUBLICADO'),
  ((SELECT id_usuario FROM usuario WHERE email = 'prod@test.cl'),
   (SELECT id_recinto FROM recinto WHERE nombre = 'Estadio Test'),
   'Fest Borrador', '2026-12-10', '2026-12-12', 'BORRADOR');

INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible) VALUES
  ((SELECT id_festival FROM festival WHERE nombre = 'Fest Publicado'), 'General', 10000, 2, 2),
  ((SELECT id_festival FROM festival WHERE nombre = 'Fest Borrador'),  'General', 10000, 5, 5);