# API de catálogo, festivales, line-up y grilla (Persona 2)

Todas las rutas están bajo `/api` y **exigen** `Authorization: Bearer <token>` (se obtiene con `POST /api/auth/login`).
Los JSON van en `snake_case` (`genero_musical`, `id_recinto`), aunque el ejemplo de Swagger los muestre en camelCase.
Las fechas van en formato ISO-8601: `2026-12-12` para un día y `2026-12-12T20:00:00-03:00` para una hora con zona.
Todos los errores responden `{"error": "mensaje"}`.

## Códigos de respuesta

| Código | Cuándo |
|---|---|
| 200 | Consulta o edición correcta |
| 201 | Creado |
| 204 | Eliminado (sin cuerpo) |
| 400 | Dato inválido: campo vacío o faltante, número no positivo, estado no permitido, fin antes que inicio, fecha de la grilla fuera del festival |
| 401 | Sin token o token inválido/vencido |
| 403 | El rol no alcanza (por ejemplo, un asistente intenta crear) |
| 404 | No existe **o pertenece a otro productor** (no se revela que existe) |
| 409 | Nombre repetido, datos relacionados que impiden borrar, o regla del Trigger 1 (traslape, recinto, fechas) |

Nota: si el cuerpo de la petición es inválido, se responde 400 antes de revisar el rol (riesgo R9 del README, aceptado por el grupo). Por eso un asistente que envía un cuerpo inválido recibe 400 y no 403.

## Resumen de endpoints

| Método y ruta | ASISTENTE | PRODUCTOR | ADMINISTRADOR |
|---|---|---|---|
| `GET /api/recintos`, `GET /api/recintos/{id}` | ✓ | ✓ | ✓ |
| `POST /api/recintos`, `PUT /api/recintos/{id}`, `DELETE /api/recintos/{id}` | ✗ 403 | ✓ | ✓ |
| `GET /api/escenarios`, `GET /api/escenarios/{id}` | ✓ | ✓ | ✓ |
| `POST /api/escenarios`, `PUT /api/escenarios/{id}`, `DELETE /api/escenarios/{id}` | ✗ 403 | ✓ | ✓ |
| `GET /api/artistas`, `GET /api/artistas/{id}` | ✓ | ✓ | ✓ |
| `POST /api/artistas`, `PUT /api/artistas/{id}`, `DELETE /api/artistas/{id}` | ✗ 403 | ✓ | ✓ |
| `GET /api/festivales`, `GET /api/festivales/{id}` | Solo publicados | Publicados + los suyos | Todos |
| `POST /api/festivales` | ✗ 403 | ✓ (queda como dueño) | ✓ |
| `PUT /api/festivales/{id}`, `DELETE /api/festivales/{id}` | ✗ 403 | Solo los suyos | Todos |
| `GET /api/presentaciones?id_festival=`, `GET /api/presentaciones/{id}` | Si ve el festival | Si ve el festival | Todas |
| `POST`, `PUT`, `DELETE /api/presentaciones` | ✗ 403 | Solo en sus festivales | Todas |
| `GET /api/festivales/{id}/grilla?fecha=` | Si ve el festival | Si ve el festival | Todas |

---

## Recintos

Respuesta de cada recinto:

```json
{ "id_recinto": 1, "nombre": "Parque Central Santiago", "direccion": "Av. Libertador Bernardo O'Higgins 1000, Santiago", "aforo_maximo": 20000 }
```

| Método y ruta | Cuerpo | Respuestas |
|---|---|---|
| `GET /api/recintos` | — | 200 con la lista, ordenada por nombre |
| `GET /api/recintos/{id}` | — | 200 · 404 si no existe |
| `POST /api/recintos` | `{ "nombre": "Estadio Sur", "direccion": "Av. Sur 500", "aforo_maximo": 8000 }` | 201 · 400 si falta un campo o `aforo_maximo` ≤ 0 · 409 si el nombre ya existe |
| `PUT /api/recintos/{id}` | Igual que el POST | 200 · 400 · 404 · 409 |
| `DELETE /api/recintos/{id}` | — | 204 (borra también sus escenarios) · 404 · 409 si tiene festivales |

## Escenarios

Cada escenario pertenece a un recinto. La respuesta incluye el nombre del recinto:

```json
{ "id_escenario": 1, "id_recinto": 1, "nombre_recinto": "Parque Central Santiago", "nombre": "Escenario Principal", "capacidad_maxima": 12000 }
```

| Método y ruta | Cuerpo | Respuestas |
|---|---|---|
| `GET /api/escenarios` | — | 200 con todos. Filtro opcional: `?id_recinto=1` |
| `GET /api/escenarios/{id}` | — | 200 · 404 |
| `POST /api/escenarios` | `{ "id_recinto": 1, "nombre": "Carpa Electrónica", "capacidad_maxima": 900 }` | 201 · 400 · 409 si el nombre ya existe en ese recinto o el recinto no existe |
| `PUT /api/escenarios/{id}` | Igual que el POST | 200 · 400 · 404 · 409 si se cambia de recinto un escenario que ya tiene presentaciones |
| `DELETE /api/escenarios/{id}` | — | 204 · 404 · 409 si tiene presentaciones |

## Artistas

```json
{ "id_artista": 2, "nombre": "Luna Austral", "genero_musical": "Pop" }
```

| Método y ruta | Cuerpo | Respuestas |
|---|---|---|
| `GET /api/artistas` | — | 200 con la lista, ordenada por nombre |
| `GET /api/artistas/{id}` | — | 200 · 404 |
| `POST /api/artistas` | `{ "nombre": "Luna Austral", "genero_musical": "Pop" }` | 201 · 400 · 409 si el nombre ya existe |
| `PUT /api/artistas/{id}` | Igual que el POST | 200 · 400 · 404 · 409 |
| `DELETE /api/artistas/{id}` | — | 204 · 404 · 409 si tiene presentaciones |

---

## Festivales

Reglas:
- **Ver:** el asistente solo ve los festivales `PUBLICADO`; el productor ve los publicados y los suyos (también en borrador); el administrador ve todos.
- **Crear:** el dueño es siempre el usuario del token; el cuerpo no lleva dueño. `estado` es opcional y por defecto es `BORRADOR`.
- **Editar y borrar:** solo el dueño o el administrador. Un festival ajeno responde **404**.
- No se pueden cambiar las fechas ni el recinto si alguna presentación quedaría fuera de ellos (**409**).

Estados posibles: `BORRADOR`, `PUBLICADO`, `CANCELADO`, `FINALIZADO`.

```json
{
  "id_festival": 1, "id_usuario_productor": 2, "id_recinto": 1, "nombre_recinto": "Parque Central Santiago",
  "nombre": "Festival Cordillera 2026", "fecha_inicio": "2026-12-12", "fecha_fin": "2026-12-13", "estado": "PUBLICADO"
}
```

| Método y ruta | Cuerpo | Respuestas |
|---|---|---|
| `GET /api/festivales` | — | 200 con los festivales que el usuario puede ver |
| `GET /api/festivales/{id}` | — | 200 · 404 si no existe o no lo puede ver |
| `POST /api/festivales` | `{ "id_recinto": 1, "nombre": "Festival Prueba", "fecha_inicio": "2026-11-20", "fecha_fin": "2026-11-21", "estado": "BORRADOR" }` | 201 · 400 si falta un campo, el estado no es válido o `fecha_fin` es anterior a `fecha_inicio` · 409 si el nombre ya existe o el recinto no existe |
| `PUT /api/festivales/{id}` | Igual que el POST (si no se envía `estado`, se conserva el actual) | 200 · 400 · 404 si no es suyo · 409 si alguna presentación quedaría fuera de las nuevas fechas o del nuevo recinto |
| `DELETE /api/festivales/{id}` | — | 204 (borra también su line-up y tipos de entrada) · 404 si no es suyo · 409 si ya hay tickets vendidos |

---

## Presentaciones (line-up)

Reglas:
- **Ver:** cualquiera que pueda ver el festival.
- **Crear, editar y borrar:** solo el dueño del festival o el administrador (si no, **404**).
- No se puede modificar el line-up de un festival `CANCELADO` o `FINALIZADO`, ni editar una presentación `CANCELADA` (**409**).
- Siempre se crean en estado `PROGRAMADA`. **La cancelación no se hace aquí**: la hace el procedimiento de la Persona 4, que además notifica y compensa a los asistentes.
- Las horas se responden en hora de Chile. `dia` es el día de festival al que pertenece la presentación (una presentación a la 1:00 AM cuenta como del día siguiente).

```json
{
  "id_presentacion": 1, "id_festival": 1, "id_escenario": 1, "nombre_escenario": "Escenario Principal",
  "id_artista": 1, "nombre_artista": "Los Andes Eléctricos",
  "fecha_hora_inicio": "2026-12-12T20:00:00-03:00", "fecha_hora_fin": "2026-12-12T21:00:00-03:00",
  "dia": "2026-12-12", "estado": "PROGRAMADA"
}
```

| Método y ruta | Cuerpo | Respuestas |
|---|---|---|
| `GET /api/presentaciones?id_festival=1` | — | 200 con el line-up ordenado por hora · 400 si falta `id_festival` · 404 si no puede ver el festival |
| `GET /api/presentaciones/{id}` | — | 200 · 404 |
| `POST /api/presentaciones` | `{ "id_festival": 1, "id_escenario": 1, "id_artista": 2, "fecha_hora_inicio": "2026-12-12T20:00:00-03:00", "fecha_hora_fin": "2026-12-12T21:00:00-03:00" }` | 201 · 400 si falta un campo o el fin es anterior al inicio · 404 si el festival no es suyo · 409 (ver abajo) |
| `PUT /api/presentaciones/{id}` | Igual que el POST, **sin** `id_festival` | 200 · 400 · 404 · 409 |
| `DELETE /api/presentaciones/{id}` | — | 204 · 404 |

**Errores 409 del Trigger 1** (vienen con un mensaje claro desde PostgreSQL):
- `El escenario ya tiene una presentación en ese horario`
- `El artista ya tiene otra presentación en ese horario`
- `El escenario no pertenece al recinto del festival`
- `La presentación debe ser entre el 2026-12-12 y el 2026-12-13 (fechas del festival)`

Una presentación que **termina justo cuando empieza otra** (por ejemplo, 20:00–21:00 y 21:00–22:00) sí se permite.

---

## Grilla del día

`GET /api/festivales/{id}/grilla?fecha=2026-12-12`

Devuelve **todos los escenarios del recinto del festival**, también los que no tienen presentaciones ese día, cada uno con sus presentaciones ordenadas por hora.

- Puede consultarla cualquiera que pueda ver el festival (404 si no).
- 400 si la fecha no tiene formato `AAAA-MM-DD` o está fuera de las fechas del festival.
- Las presentaciones canceladas aparecen con `"estado": "CANCELADA"`, para que la pantalla las muestre tachadas.

```json
{
  "id_festival": 1,
  "nombre_festival": "Festival Cordillera 2026",
  "fecha": "2026-12-12",
  "escenarios": [
    { "id_escenario": 2, "nombre": "Escenario Alternativo", "presentaciones": [] },
    {
      "id_escenario": 1,
      "nombre": "Escenario Principal",
      "presentaciones": [
        {
          "id_presentacion": 1, "id_artista": 1, "artista": "Los Andes Eléctricos", "genero_musical": "Rock",
          "fecha_hora_inicio": "2026-12-12T20:00:00-03:00", "fecha_hora_fin": "2026-12-12T21:00:00-03:00",
          "estado": "PROGRAMADA"
        },
        {
          "id_presentacion": 2, "id_artista": 2, "artista": "Luna Austral", "genero_musical": "Pop",
          "fecha_hora_inicio": "2026-12-12T21:00:00-03:00", "fecha_hora_fin": "2026-12-12T22:00:00-03:00",
          "estado": "PROGRAMADA"
        }
      ]
    }
  ]
}
```

---

## Base de datos (Trigger 1)

Archivo: `db/trg_validar_presentacion.sql`. Se ejecuta antes de cada `INSERT` o `UPDATE` en `presentacion` y rechaza:

1. Dos presentaciones del mismo escenario con horarios cruzados.
2. Un artista con dos presentaciones simultáneas.
3. Un escenario que no pertenece al recinto del festival.
4. Una presentación fuera de las fechas del festival (el día se calcula con `fn_dia_festival`, en hora de Chile).

Las presentaciones `CANCELADA` no se validan ni ocupan horario.

Además, dos restricciones `EXCLUDE USING gist` (`ex_presentacion_escenario` y `ex_presentacion_artista`, con la extensión `btree_gist`) impiden los traslapes incluso cuando dos inserciones llegan al mismo tiempo y ninguna ve a la otra.

**Pruebas:** `db/pruebas/pruebas_trg_validar_presentacion.sql` (16 casos, termina en `ROLLBACK`):

```
docker compose exec -T postgres psql -U festivales_user -d festivales -f /db/pruebas/pruebas_trg_validar_presentacion.sql
```

Resultado esperado: `RESUMEN: 16 OK, 0 FALLA`.
