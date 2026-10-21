DROP TABLE IF EXISTS log_entrada, notificacion, auditoria_precio_entrada, ticket, orden_compra, presentacion, tipo_entrada, festival, artista, escenario, recinto, usuario, rol CASCADE;

CREATE TABLE rol (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL UNIQUE CHECK (tipo IN ('ASISTENTE', 'PRODUCTOR', 'ADMINISTRADOR'))
);

CREATE TABLE usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    id_rol INT NOT NULL REFERENCES rol(id),
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password_hash TEXT NOT NULL,
    creditos_compensacion NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (creditos_compensacion >= 0)
);
CREATE UNIQUE INDEX uq_usuario_email ON usuario (lower(email));

CREATE TABLE recinto (
    id_recinto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    direccion VARCHAR(250) NOT NULL,
    aforo_maximo INT NOT NULL CHECK (aforo_maximo > 0)
);

CREATE TABLE escenario (
    id_escenario BIGSERIAL PRIMARY KEY,
    id_recinto BIGINT NOT NULL REFERENCES recinto(id_recinto) ON DELETE CASCADE,
    nombre VARCHAR(120) NOT NULL,
    capacidad_maxima INT NOT NULL CHECK (capacidad_maxima > 0),
    CONSTRAINT uq_escenario_nombre UNIQUE (id_recinto, nombre)
);

CREATE TABLE artista (
    id_artista BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    genero_musical VARCHAR(80) NOT NULL
);

CREATE TABLE festival (
    id_festival BIGSERIAL PRIMARY KEY,
    id_usuario_productor BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_recinto BIGINT NOT NULL REFERENCES recinto(id_recinto),
    nombre VARCHAR(150) NOT NULL UNIQUE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR' CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'CANCELADO', 'FINALIZADO')),
    CONSTRAINT ck_festival_fechas CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE presentacion (
    id_presentacion BIGSERIAL PRIMARY KEY,
    id_festival BIGINT NOT NULL REFERENCES festival(id_festival) ON DELETE CASCADE,
    id_escenario BIGINT NOT NULL REFERENCES escenario(id_escenario),
    id_artista BIGINT NOT NULL REFERENCES artista(id_artista),
    fecha_hora_inicio TIMESTAMPTZ NOT NULL,
    fecha_hora_fin TIMESTAMPTZ NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA' CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'REALIZADA')),
    CONSTRAINT ck_presentacion_horario CHECK (fecha_hora_fin > fecha_hora_inicio)
);

CREATE TABLE tipo_entrada (
    id_tipo_entrada BIGSERIAL PRIMARY KEY,
    id_festival BIGINT NOT NULL REFERENCES festival(id_festival) ON DELETE CASCADE,
    nombre_tipo VARCHAR(100) NOT NULL,
    precio_actual NUMERIC(12,2) NOT NULL CHECK (precio_actual >= 0),
    aforo_total INT NOT NULL CHECK (aforo_total > 0),
    aforo_disponible INT NOT NULL,
    fecha DATE NULL, -- NULL = abono para todo el festival; con valor = entrada válida solo ese día
    CONSTRAINT uq_tipo_entrada_nombre UNIQUE (id_festival, nombre_tipo),
    CONSTRAINT ck_aforo CHECK (aforo_disponible >= 0 AND aforo_disponible <= aforo_total)
);

CREATE TABLE orden_compra (
    id_orden BIGSERIAL PRIMARY KEY,
    id_usuario_asistente BIGINT NOT NULL REFERENCES usuario(id_usuario),
    fecha_compra TIMESTAMPTZ NOT NULL DEFAULT now(),
    monto_total NUMERIC(12,2) NOT NULL CHECK (monto_total >= 0),
    monto_credito_aplicado NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (monto_credito_aplicado >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'PAGADA' CHECK (estado IN ('PAGADA', 'CANCELADA', 'PENDIENTE'))
);

CREATE TABLE ticket (
    id_ticket BIGSERIAL PRIMARY KEY,
    codigo_unico VARCHAR(40) NOT NULL,
    id_orden BIGINT NOT NULL REFERENCES orden_compra(id_orden) ON DELETE CASCADE,
    id_tipo_entrada BIGINT NOT NULL REFERENCES tipo_entrada(id_tipo_entrada),
    estado VARCHAR(20) NOT NULL DEFAULT 'VIGENTE' CHECK (estado IN ('VIGENTE', 'UTILIZADO', 'CANCELADO')),
    precio NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    fecha_uso TIMESTAMPTZ,
    CONSTRAINT ck_ticket_uso CHECK (estado <> 'UTILIZADO' OR fecha_uso IS NOT NULL),
    CONSTRAINT ex_ticket_codigo_unico EXCLUDE USING hash (codigo_unico WITH =)
);

CREATE TABLE notificacion (
    id BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_presentacion BIGINT REFERENCES presentacion(id_presentacion) ON DELETE SET NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE log_entrada (
    id BIGSERIAL PRIMARY KEY,
    id_ticket BIGINT NOT NULL REFERENCES ticket(id_ticket) ON DELETE CASCADE,
    id_usuario BIGINT REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),
    resultado VARCHAR(30) NOT NULL
);

CREATE TABLE auditoria_precio_entrada (
    id_auditoria BIGSERIAL PRIMARY KEY,
    id_tipo_entrada BIGINT NOT NULL REFERENCES tipo_entrada(id_tipo_entrada) ON DELETE CASCADE,
    precio_anterior NUMERIC(12,2) NOT NULL,
    precio_nuevo NUMERIC(12,2) NOT NULL,
    id_usuario_modificador BIGINT REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Roles base del sistema (siempre necesarios, no son datos de prueba)
INSERT INTO rol (tipo) VALUES ('ASISTENTE'), ('PRODUCTOR'), ('ADMINISTRADOR');

