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