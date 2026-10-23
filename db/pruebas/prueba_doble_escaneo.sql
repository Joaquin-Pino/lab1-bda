/*
Prueba de doble escaneo para sp_validar_acceso:
Si dos lectores validan el mismo código a la vez, solo uno debe entrar.

1. Base recién creada con:  docker compose down -v && docker compose up -d
   Hace falta un ticket con fecha de hoy. Desde /back, en una terminal:
   docker compose exec postgres psql -U festivales_user -d festivales
   Crear un festival de hoy y comprar una entrada (ids del seed: productor 2, asistente 4, recinto 1):

   INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
   VALUES (2, 1, 'Festival Hoy', fn_dia_festival(now()), fn_dia_festival(now()), 'PUBLICADO');
   INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible)
   SELECT max(id_festival), 'Pase Hoy', 10000, 10, 10 FROM festival;
   CALL sp_comprar_entradas(4, (SELECT max(id_tipo_entrada) FROM tipo_entrada), 1, false, NULL);
   SELECT codigo_unico, (SELECT max(id_festival) FROM festival) AS id_festival FROM ticket ORDER BY id_ticket DESC LIMIT 1;

2. Abrir dos terminales (A y B) con el mismo psql de arriba. Con <codigo> y <id_festival> del paso 1:

3. En la terminal A:  BEGIN; CALL sp_validar_acceso('<codigo>', <id_festival>, 2, NULL, NULL);
   Debe devolver VALIDO (sin hacer COMMIT todavía).

4. En la terminal B:  CALL sp_validar_acceso('<codigo>', <id_festival>, 2, NULL, NULL);
   La terminal B debe quedar esperando.

5. En la terminal A:  COMMIT;

6. La terminal B debe responder YA_USADO.

7. Verificar:
   SELECT resultado, count(*) FROM log_entrada GROUP BY resultado;   -- 1 VALIDO y 1 YA_USADO
   SELECT estado FROM ticket ORDER BY id_ticket DESC LIMIT 1;         -- UTILIZADO

8. Salir con \q y limpiar BD con: docker compose down -v && docker compose up -d
*/
