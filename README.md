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

* /db: Scripts de inicialización, vistas, funciones y datos semilla de PostgreSQL.

* docker-compose.yml: Orquestador de la infraestructura.

* .env.example: Plantilla de variables de entorno.



## Requisitos Previos

* Docker y Docker Compose instalados en el sistema host.



## Instrucciones de Despliegue

1. Clonar el repositorio.

2. Copiar el archivo de variables de entorno en la raíz del proyecto:
``` bash
   cp .env.example .env
```
3. Levantar la infraestructura completa en segundo plano:
``` bash
   docker compose up -d
```

Una vez que los contenedores estén en estado "Healthy":

* Interfaz de Usuario (Frontend): http://localhost:5173

* API REST (Backend): http://localhost:8080

* Base de Datos: localhost:5432

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

El sistema se inicializa con los siguientes usuarios de desarrollo. **Todas las cuentas comparten la misma contraseña: Festival123!**

| Rol | Nombre | Email |
| :--- | :--- | :--- |
| Administrador | Administrador General | admin@festivales.cl |
| Productor | Productora Uno | productor1@festivales.cl |
| Productor | Productor Dos | productor2@festivales.cl |
| Asistente | Asistente Uno | asistente1@festivales.cl |
| Asistente | Asistente Dos | asistente2@festivales.cl |
| Asistente | Asistente Tres | asistente3@festivales.cl |

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