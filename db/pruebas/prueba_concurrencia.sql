/*
Prueba de concurrencia para el procedimiento sp_comprar_entradas:
Si dos compradores intentan quedarse con el último cupo, solo uno debe lograrlo

1. Base recién creada con:  docker compose down -v && docker compose up -d

2. Abrir dos terminales (A y B), desde el directorio /back y ejecutar en ambas:
docker compose exec postgres psql -U festivales_user -d festivales

3. En cualquiera de las dos, setear el cupo en uno con: 
UPDATE tipo_entrada SET aforo_disponible = 1 WHERE id_tipo_entrada = 1;

4. En la terminal A hacer: BEGIN; CALL sp_comprar_entradas(4, 1, 1, false, NULL);

5. En la terminal B hacer: BEGIN; CALL sp_comprar_entradas(3, 1, 1, false, NULL);
La terminal B debería quedar esperando.

6. En la terminal A hacer: COMMIT;

7. La terminal B debe mostrar el error: Aforo insuficiente o festival no publicado

8. En terminal B hacer: ROLLBACK;

9. Verificar en cualquiera de las termianles:
SELECT count(*) FROM ticket WHERE id_tipo_entrada = 1;
SELECT aforo_disponible FROM tipo_entrada WHERE id_tipo_entrada = 1;
Debe mostrar 1 y 0, respectivamente

10. Salir con \q y limpiar BD con: docker compose down -v && docker compose up -d

*/