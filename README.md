# Sistema de Gestión de Festivales



Plataforma full-stack para la administración de eventos, venta de tickets, control de acceso y visualización de reportes de ocupación para festivales de música.



## Stack Tecnológico

* Backend: Java (Spring Boot), Spring Security (JWT), Spring Data JDBC (consultas nativas).

* Frontend: React, Vite, Recharts (gráficos), Bootstrap.

* Base de Datos: PostgreSQL 17.

* Infraestructura: Docker y Docker Compose.



## Estructura del Proyecto

* /back: Código fuente de la API REST en Spring Boot.

* /front: Código fuente de la interfaz de usuario en React.

* /db: festivales.sql (script único: tablas, triggers, procedimientos, vista materializada y datos de prueba), init_all.sql (punto de entrada de Docker) y pruebas/ (tests SQL).

* compose.yaml: Orquestador de la infraestructura.

* .env.example: Plantilla de variables de entorno.



## Requisitos Previos

* Docker y Docker Compose instalados en el sistema host.



## Instrucciones de Despliegue

1. Clonar el repositorio.

2. Copiar el archivo de variables de entorno en la raíz del proyecto:
``` bash
   cp .env.example .env
```
3. Generar la clave secreta con la que el backend firma los JWT y pegarla en `.env` como `JWT_SECRET`.
   Debe tener al menos 32 caracteres (si falta o es más corta, el backend no arranca: `Could not resolve placeholder 'JWT_SECRET'`).
   Los tokens de sesión los genera el backend al hacer login; lo que se configura aquí es solo el secreto de firma.
``` bash
   openssl rand -base64 48
```
   Sin `openssl`, cualquiera de estas opciones sirve:
``` bash
   python3 -c "import secrets; print(secrets.token_urlsafe(48))"
   node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
```
   Para escribirla directamente en el `.env` (reemplaza la línea `JWT_SECRET=`):
``` bash
   sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$(openssl rand -base64 48)|" .env
```
   Cada integrante genera la suya; `.env` está en `.gitignore` y no se sube al repositorio.
   Si cambias el secreto, los tokens emitidos antes dejan de ser válidos y hay que volver a iniciar sesión.

4. Levantar la infraestructura completa en segundo plano:
``` bash
   docker compose up -d
```

Una vez que los contenedores estén en estado "Healthy":

* Interfaz de Usuario (Frontend): http://localhost:5173

* API REST (Backend): http://localhost:8080

* Base de Datos: localhost:5432 (valor de DB_PORT en .env)

Una vez finalizado el uso puede:

* Detener los servicios:
``` bash
   docker compose down
```
* Reiniciar el sistema destruyendo la base de datos (útil para recargar los scripts SQL de inicialización):
``` bash
   docker compose down -v

   docker compose up -d
```

## Solución de problemas: puertos ocupados

Para levantar los contenedores deben estar libres estos puertos del equipo (los valores salen de `.env`):

| Servicio | Puerto | Variable |
| :--- | :--- | :--- |
| PostgreSQL | 5432 | `DB_PORT` |
| Backend | 8080 | `BACKEND_PORT` |
| Frontend | 5173 | `FRONTEND_PORT` |

Si `docker compose up -d` termina con `failed to bind host port ... address already in use`, otro programa está usando ese puerto.

1. Ver qué proceso lo ocupa (cambiar `5432` por el puerto del error):
``` bash
   # Linux
   sudo ss -ltnp | grep :5432
   # macOS
   lsof -iTCP:5432 -sTCP:LISTEN
   # Windows (PowerShell)
   netstat -ano | findstr :5432
```

2. Liberarlo:
   * **PostgreSQL instalado en el equipo (caso más común con el 5432).** Detener el servicio; matar el proceso no basta porque el sistema lo vuelve a levantar:
``` bash
   # Linux (systemd)
   sudo systemctl stop postgresql
   # opcional: que no arranque solo al encender el equipo
   sudo systemctl disable postgresql
   # macOS (Homebrew)
   brew services stop postgresql
```
   En Windows: `services.msc`, buscar "postgresql", clic derecho y "Detener".
   * **Un contenedor de Docker de otro proyecto.** Ver cuál es y detenerlo:
``` bash
   docker ps --format '{{.Names}}\t{{.Ports}}'
   docker stop <nombre>
```
   * **Otro programa (8080, 5173).** Cerrarlo desde su aplicación, o terminar el proceso con el PID que muestra el paso 1:
``` bash
   # Linux / macOS
   kill <PID>
   # Windows (PowerShell)
   taskkill /PID <PID> /F
```

3. Volver a levantar:
``` bash
   docker compose up -d
```

**Alternativa: cambiar el puerto en vez de liberarlo.** Para el frontend (`FRONTEND_PORT`) y el backend (`BACKEND_PORT`) basta editar el valor en `.env`.
No cambies `DB_PORT` a otro valor: el backend usa ese mismo número para conectarse a la base dentro de Docker, donde siempre es 5432, y dejaría de conectar.
Para la base de datos, libera el 5432 como se explica arriba.

Si el error es `container name "/festivales-..." is already in use`, quedó un contenedor antiguo con ese nombre (por ejemplo de una versión anterior del proyecto).
Eliminarlo no borra los datos, que están en el volumen:
``` bash
   docker rm -f festivales-db festivales-backend festivales-frontend
   docker compose up -d
```

## Usuarios de Prueba (Datos Semilla)

El script único `db/festivales.sql` crea el esquema y carga los datos de demo. **Todas las cuentas usan la contraseña: 1234**

| Rol | Email |
| :--- | :--- |
| Administrador | admin1@festivales.cl, admin2@festivales.cl |
| Productor | productor1@festivales.cl, productor2@festivales.cl |
| Asistente | usuario1@festivales.cl … usuario6@festivales.cl |

Datos de demo: Festival Cordillera y Festival Aurora en curso (terminan en 10 días), Primavera publicado y Verano en borrador.
"Pase Backstage" (Cordillera) y "Pase Premium" (Aurora) están agotadas; "Meet & Greet" (Cordillera) tiene 1 cupo.

## Características Técnicas Implementadas

* Seguridad: Autenticación y autorización basada en JSON Web Tokens (JWT) generados desde el backend.

* Transacciones Atómicas (Procedimientos Almacenados):

  - sp_comprar_entradas: Valida cupos y genera tickets únicos UUID.

  - sp_cancelar_presentacion: Cancela shows, notifica masivamente y abona compensaciones a las billeteras de los asistentes.

  - sp_validar_acceso: Control de acceso con validación estricta de doble escaneo y fechas de ticket.

* Triggers y Reglas de Negocio:

  - trg_validar_presentacion: Evita traslapes de horario en un mismo escenario o para un mismo artista.

  - trg_auditoria_precio: Mantiene el registro histórico de modificaciones tarifarias.

* Reportes Optimizados:

  - Vista materializada (mv_ventas) para el cálculo rápido de ocupación y recaudación financiera.

  - Refresco de datos mediante función PL/pgSQL gatillada desde el dashboard de administración.

* Índices Estructurales:

  - Índices HASH sobre códigos UUID para búsquedas de igualdad estrictas.

  - Índices B-Tree sobre claves foráneas estratégicas para evitar Sequential Scans durante reportes y devoluciones.