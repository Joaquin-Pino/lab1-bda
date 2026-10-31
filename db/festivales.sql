/* festivales.sql: script único de base de datos.
Contiene tablas, relaciones, funciones, triggers, procedimientos almacenados, vista materializada,
índices y datos de prueba (seeders). Es idempotente: parte con DROP TABLE, así que puede
re-ejecutarse sobre una base existente:
  psql -U festivales_user -d festivales -f festivales.sql
En Docker lo ejecuta init_all.sql la primera vez que se crea el volumen.
Usuarios de demo: clave 1234 (admin1/2, productor1/2, usuario1..6 @festivales.cl)
*/
\set ON_ERROR_STOP on

-- ======================================================================
-- 1. TABLAS, RELACIONES Y ROLES
-- ======================================================================

DROP TABLE IF EXISTS compensacion, log_entrada, notificacion, auditoria_precio_entrada, ticket, orden_compra, presentacion, tipo_entrada, festival, artista, escenario, recinto, usuario, rol CASCADE;

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

-- Crédito otorgado al cancelar una presentación: a lo más una compensación por ticket y día
CREATE TABLE compensacion (
    id BIGSERIAL PRIMARY KEY,
    id_ticket BIGINT NOT NULL REFERENCES ticket(id_ticket) ON DELETE CASCADE,
    dia DATE NOT NULL,
    id_presentacion BIGINT REFERENCES presentacion(id_presentacion) ON DELETE SET NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    monto NUMERIC(12,2) NOT NULL CHECK (monto >= 0),
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_compensacion_ticket_dia UNIQUE (id_ticket, dia)
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

-- ======================================================================
-- 2. FUNCIÓN fn_dia_festival
-- ======================================================================

/* Función única de "día" del festival
El día de una presentación es la fecha calendario de Chile (America/Santiago) de su
fecha_hora_inicio, sin importar la zona horaria de la sesión. Una presentación a la
1:00 AM cuenta como del día calendario siguiente.
Es la única definición de "día" para la grilla, el SP 2 (cancelación) y el control de acceso.
*/

CREATE OR REPLACE FUNCTION fn_dia_festival(p_ts TIMESTAMPTZ)
RETURNS DATE
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT (p_ts AT TIME ZONE 'America/Santiago')::date;
$$;

-- ======================================================================
-- 3. TRIGGER: validación de presentaciones
-- ======================================================================

-- Trigger 1: Validación de traslapes en presentaciones

CREATE OR REPLACE FUNCTION fn_validar_presentacion()
RETURNS TRIGGER AS $$
DECLARE
v_recinto_festival  BIGINT;
	v_recinto_escenario BIGINT;
	v_fecha_inicio      DATE;
	v_fecha_fin         DATE;
BEGIN
	-- Una presentación cancelada no ocupa horario: no se valida
	IF NEW.estado = 'CANCELADA' THEN
		RETURN NEW;
END IF;

	-- El escenario debe pertenecer al recinto del festival
SELECT id_recinto, fecha_inicio, fecha_fin
INTO v_recinto_festival, v_fecha_inicio, v_fecha_fin
FROM festival
WHERE id_festival = NEW.id_festival;

SELECT id_recinto
INTO v_recinto_escenario
FROM escenario
WHERE id_escenario = NEW.id_escenario;

-- Si el festival o el escenario no existen, las variables quedan NULL,
-- no se lanza nada aquí y luego falla la clave foránea con su propio error
IF v_recinto_escenario <> v_recinto_festival THEN
		RAISE EXCEPTION 'El escenario no pertenece al recinto del festival';
END IF;

	-- La presentación debe ser en un día del festival
	IF fn_dia_festival(NEW.fecha_hora_inicio) NOT BETWEEN v_fecha_inicio AND v_fecha_fin THEN
		RAISE EXCEPTION 'La presentación debe ser entre el % y el % (fechas del festival)',
			v_fecha_inicio, v_fecha_fin;
END IF;

	-- Mismo escenario con horario cruzado
	IF EXISTS (
		SELECT 1
			FROM presentacion p
			WHERE p.id_escenario = NEW.id_escenario
				AND p.id_presentacion <> COALESCE(NEW.id_presentacion, -1)
				AND p.estado <> 'CANCELADA'
				AND p.fecha_hora_inicio < NEW.fecha_hora_fin
				AND NEW.fecha_hora_inicio < p.fecha_hora_fin
	) THEN
		RAISE EXCEPTION 'El escenario ya tiene una presentación en ese horario';
	END IF;

	-- Mismo artista con horario cruzado
	IF EXISTS (
		SELECT 1
			FROM presentacion p
			WHERE p.id_artista = NEW.id_artista
				AND p.id_presentacion <> COALESCE(NEW.id_presentacion, -1)
				AND p.estado <> 'CANCELADA'
				AND p.fecha_hora_inicio < NEW.fecha_hora_fin
				AND NEW.fecha_hora_inicio < p.fecha_hora_fin
	) THEN
		RAISE EXCEPTION 'El artista ya tiene otra presentación en ese horario';
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_presentacion ON presentacion;
CREATE TRIGGER trg_validar_presentacion
BEFORE INSERT OR UPDATE ON presentacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_presentacion();

-- Respaldo para inserciones simultáneas.

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE presentacion DROP CONSTRAINT IF EXISTS ex_presentacion_escenario;
ALTER TABLE presentacion ADD CONSTRAINT ex_presentacion_escenario
    EXCLUDE USING gist (
		id_escenario WITH =,
		tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
	) WHERE (estado <> 'CANCELADA');

ALTER TABLE presentacion DROP CONSTRAINT IF EXISTS ex_presentacion_artista;
ALTER TABLE presentacion ADD CONSTRAINT ex_presentacion_artista
    EXCLUDE USING gist (
		id_artista WITH =,
		tstzrange(fecha_hora_inicio, fecha_hora_fin, '[)') WITH &&
	) WHERE (estado <> 'CANCELADA');

-- ======================================================================
-- 4. PROCEDIMIENTO: sp_comprar_entradas
-- ======================================================================

/* Procedimiento Almacenado 1: Comprar entradas
Validar el cupo disponible del tipo de entrada, crear la orden, generar un ticket
con código único por cada entrada y descontar el aforo en una sola transacción atómica.
*/

CREATE OR REPLACE PROCEDURE sp_comprar_entradas(
    p_id_usuario BIGINT,
    p_id_tipo_entrada BIGINT,
    p_cantidad INT,
    p_usar_credito BOOLEAN DEFAULT FALSE,
    INOUT p_id_orden BIGINT DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_precio NUMERIC;
    v_total NUMERIC;
    v_creditos NUMERIC;
    v_creditos_usar NUMERIC := 0;
BEGIN
    -- Verificar cantidad positiva
    IF p_cantidad IS NULL OR p_cantidad < 1 THEN
        RAISE EXCEPTION 'La cantidad de entradas debe ser positiva';
    END IF;

    -- Descontar aforo disponible
    UPDATE tipo_entrada te
    SET aforo_disponible = te.aforo_disponible - p_cantidad
    FROM festival f
    WHERE te.id_tipo_entrada = p_id_tipo_entrada
    AND te.id_festival = f.id_festival
    AND f.estado = 'PUBLICADO'
    AND te.aforo_disponible >= p_cantidad
    AND f.fecha_fin >= fn_dia_festival(now())
    AND (te.fecha IS NULL OR te.fecha >= fn_dia_festival(now()))
    RETURNING te.precio_actual INTO v_precio;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Aforo insuficiente o festival no publicado';
    END IF;

    -- Calcular total a pagar
    v_total := v_precio * p_cantidad;

    -- Si usa crédito
    IF p_usar_credito THEN
        SELECT creditos_compensacion INTO v_creditos
        FROM usuario
        WHERE id_usuario = p_id_usuario FOR UPDATE;

        v_creditos_usar := LEAST(COALESCE(v_creditos, 0), v_total);

        UPDATE usuario
        SET creditos_compensacion = creditos_compensacion - v_creditos_usar
        WHERE id_usuario = p_id_usuario;
    END IF;

    -- Crear orden
    INSERT INTO orden_compra(id_usuario_asistente, monto_total, monto_credito_aplicado)
    VALUES (p_id_usuario, v_total, v_creditos_usar)
    RETURNING id_orden INTO p_id_orden;

    -- Generar tickets
    INSERT INTO ticket(codigo_unico, id_orden, id_tipo_entrada, precio)
    SELECT replace(gen_random_uuid()::text, '-', ''), p_id_orden, p_id_tipo_entrada, v_precio
    FROM generate_series(1, p_cantidad);
    
END $$;

-- ======================================================================
-- 5. TRIGGER: auditoría de precios
-- ======================================================================

-- Trigger 2: Auditoría de precios

CREATE OR REPLACE FUNCTION fn_auditar_precio()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auditoria_precio_entrada (id_tipo_entrada, precio_anterior, precio_nuevo, id_usuario_modificador)
    VALUES (NEW.id_tipo_entrada, OLD.precio_actual, NEW.precio_actual, NULLIF(current_setting('app.user_id', true), '')::BIGINT);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_auditoria_precio ON tipo_entrada;
CREATE TRIGGER trg_auditoria_precio
AFTER UPDATE OF precio_actual ON tipo_entrada
FOR EACH ROW
WHEN (OLD.precio_actual IS DISTINCT FROM NEW.precio_actual)
EXECUTE FUNCTION fn_auditar_precio();

-- ======================================================================
-- 6. PROCEDIMIENTO: cancelar presentación
-- ======================================================================

/* Procedimiento Almacenado 2: Cancelar presentación
Marca la presentación como CANCELADA, notifica a los asistentes con tickets VIGENTES de órdenes PAGADAS
para ese día y les acredita crédito de compensación, todo en una sola transacción.
Reglas:
- Día = fn_dia_festival(fecha_hora_inicio). Aplican los tipos de entrada con fecha = ese día o fecha NULL (pase).
- Ticket de un día: se compensa su precio. Pase (fecha NULL): precio / días del festival.
- A lo más una compensación por ticket y día (compensacion, UNIQUE): la segunda cancelación del
  mismo día notifica igual, pero sin crédito adicional.
- Los tickets siguen VIGENTES (pueden servir para otras presentaciones).
*/

CREATE OR REPLACE PROCEDURE sp_cancelar_presentacion(
    p_id_presentacion BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_festival BIGINT;
    v_inicio TIMESTAMPTZ;
    v_estado VARCHAR;
    v_dia DATE;
    v_dias_festival INT;
    v_artista TEXT;
    v_festival TEXT;
BEGIN
    SELECT p.id_festival, p.fecha_hora_inicio, p.estado, a.nombre, f.nombre,
           (f.fecha_fin - f.fecha_inicio + 1)
    INTO v_id_festival, v_inicio, v_estado, v_artista, v_festival, v_dias_festival
    FROM presentacion p
    JOIN festival f ON f.id_festival = p.id_festival
    JOIN artista a ON a.id_artista = p.id_artista
    WHERE p.id_presentacion = p_id_presentacion
    FOR UPDATE OF p;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Presentación no encontrada';
    END IF;
    IF v_estado <> 'PROGRAMADA' THEN
        RAISE EXCEPTION 'Solo se puede cancelar una presentación programada (estado actual: %)', v_estado;
    END IF;

    UPDATE presentacion SET estado = 'CANCELADA' WHERE id_presentacion = p_id_presentacion;

    v_dia := fn_dia_festival(v_inicio);

    -- Compensación nueva (la clave única evita pagar dos veces el mismo ticket y día)
    INSERT INTO compensacion (id_ticket, dia, id_presentacion, id_usuario, monto)
    SELECT t.id_ticket, v_dia, p_id_presentacion, o.id_usuario_asistente,
           CASE WHEN te.fecha IS NULL THEN round(t.precio / v_dias_festival, 2) ELSE t.precio END
    FROM ticket t
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN orden_compra o ON o.id_orden = t.id_orden
    WHERE te.id_festival = v_id_festival
      AND (te.fecha IS NULL OR te.fecha = v_dia)
      AND t.estado = 'VIGENTE'
      AND o.estado = 'PAGADA'
    ON CONFLICT (id_ticket, dia) DO NOTHING;

    -- Abonar el crédito (una actualización por usuario)
    UPDATE usuario u
    SET creditos_compensacion = u.creditos_compensacion + c.total
    FROM (SELECT id_usuario, SUM(monto) AS total
          FROM compensacion WHERE id_presentacion = p_id_presentacion
          GROUP BY id_usuario) c
    WHERE u.id_usuario = c.id_usuario;

    -- Una notificación por asistente afectado (también los que ya habían sido compensados ese día)
    INSERT INTO notificacion (id_usuario, id_presentacion, mensaje)
    SELECT a.id_usuario, p_id_presentacion,
           format('Se canceló la presentación de %s en %s (%s).%s', v_artista, v_festival, to_char(v_dia, 'DD-MM-YYYY'),
                  CASE WHEN COALESCE(c.total, 0) > 0
                       THEN format(' Se abonaron $%s a tu crédito de compensación.', c.total)
                       ELSE '' END)
    FROM (SELECT DISTINCT o.id_usuario_asistente AS id_usuario
          FROM ticket t
          JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
          JOIN orden_compra o ON o.id_orden = t.id_orden
          WHERE te.id_festival = v_id_festival
            AND (te.fecha IS NULL OR te.fecha = v_dia)
            AND t.estado = 'VIGENTE' AND o.estado = 'PAGADA') a
    LEFT JOIN (SELECT id_usuario, SUM(monto) AS total
               FROM compensacion WHERE id_presentacion = p_id_presentacion
               GROUP BY id_usuario) c ON c.id_usuario = a.id_usuario;
    GET DIAGNOSTICS p_notificados = ROW_COUNT;

    SELECT COALESCE(SUM(monto), 0) INTO p_credito_total
    FROM compensacion WHERE id_presentacion = p_id_presentacion;
END $$;

-- ======================================================================
-- 7. PROCEDIMIENTOS: cancelar festival / tipo de entrada
-- ======================================================================

/* Procedimientos de cancelación con devolución de crédito: sp_cancelar_festival y sp_cancelar_tipo_entrada
Reemplazan al borrado cuando ya hay tickets vendidos (la FK de ticket impide el DELETE y borrar haría
desaparecer lo que los asistentes pagaron). Todo ocurre en la transacción del llamador.
Reglas:
- Se devuelve el precio de cada ticket VIGENTE de una orden PAGADA, descontando lo ya compensado
  por cancelaciones de presentaciones (nunca se devuelve más que el precio). Los UTILIZADO no se devuelven.
- El dinero se abona a usuario.creditos_compensacion y queda registrado en compensacion
  (dia = fecha de inicio del festival; si ya había una fila ese día para el ticket, se suma).
- Los tickets pasan a CANCELADO y las órdenes sin tickets activos a CANCELADA.
- Cada asistente afectado recibe una notificación (sin presentación asociada).
- El detalle por usuario queda en la tabla temporal tmp_devolucion (vive hasta el fin de la transacción);
  el backend lo lee para armar la respuesta.
*/

-- Auxiliar: devuelve los tickets vigentes del festival (o de un solo tipo de entrada si se indica)
CREATE OR REPLACE PROCEDURE sp_devolver_tickets(
    p_id_festival BIGINT,
    p_id_tipo_entrada BIGINT,
    p_motivo TEXT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_dia DATE;
BEGIN
    SELECT fecha_inicio INTO v_dia FROM festival WHERE id_festival = p_id_festival;

    DROP TABLE IF EXISTS tmp_devolucion;
    CREATE TEMP TABLE tmp_devolucion (
        id_ticket BIGINT PRIMARY KEY,
        id_orden BIGINT NOT NULL,
        id_usuario BIGINT NOT NULL,
        monto NUMERIC(12,2) NOT NULL
    ) ON COMMIT DROP;

    -- Tickets afectados con lo que falta devolver de cada uno (bloqueados para no competir con el control de acceso)
    INSERT INTO tmp_devolucion (id_ticket, id_orden, id_usuario, monto)
    SELECT t.id_ticket, t.id_orden, o.id_usuario_asistente,
           GREATEST(t.precio - COALESCE((SELECT SUM(c.monto) FROM compensacion c WHERE c.id_ticket = t.id_ticket), 0), 0)
    FROM ticket t
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN orden_compra o ON o.id_orden = t.id_orden
    WHERE te.id_festival = p_id_festival
      AND (p_id_tipo_entrada IS NULL OR te.id_tipo_entrada = p_id_tipo_entrada)
      AND t.estado = 'VIGENTE'
      AND o.estado = 'PAGADA'
    FOR UPDATE OF t;

    INSERT INTO compensacion (id_ticket, dia, id_presentacion, id_usuario, monto)
    SELECT id_ticket, v_dia, NULL, id_usuario, monto FROM tmp_devolucion WHERE monto > 0
    ON CONFLICT (id_ticket, dia) DO UPDATE SET monto = compensacion.monto + EXCLUDED.monto;

    UPDATE usuario u
    SET creditos_compensacion = u.creditos_compensacion + d.total
    FROM (SELECT id_usuario, SUM(monto) AS total FROM tmp_devolucion GROUP BY id_usuario) d
    WHERE u.id_usuario = d.id_usuario;

    UPDATE ticket SET estado = 'CANCELADO' WHERE id_ticket IN (SELECT id_ticket FROM tmp_devolucion);

    UPDATE orden_compra o
    SET estado = 'CANCELADA'
    WHERE o.id_orden IN (SELECT id_orden FROM tmp_devolucion)
      AND NOT EXISTS (SELECT 1 FROM ticket t WHERE t.id_orden = o.id_orden AND t.estado <> 'CANCELADO');

    INSERT INTO notificacion (id_usuario, mensaje)
    SELECT id_usuario,
           p_motivo || CASE WHEN SUM(monto) > 0
                            THEN format(' Se abonaron $%s a tu crédito de compensación.', SUM(monto))
                            ELSE '' END
    FROM tmp_devolucion
    GROUP BY id_usuario;
    GET DIAGNOSTICS p_notificados = ROW_COUNT;

    SELECT COALESCE(SUM(monto), 0) INTO p_credito_total FROM tmp_devolucion;
END $$;

-- Cancela el festival (estado CANCELADO), sus presentaciones programadas y devuelve el dinero a los asistentes
CREATE OR REPLACE PROCEDURE sp_cancelar_festival(
    p_id_festival BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
    v_nombre VARCHAR;
BEGIN
    SELECT estado, nombre INTO v_estado, v_nombre
    FROM festival WHERE id_festival = p_id_festival FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Festival no encontrado';
    END IF;
    IF v_estado IN ('CANCELADO', 'FINALIZADO') THEN
        RAISE EXCEPTION 'No se puede cancelar un festival %', lower(v_estado);
    END IF;

    UPDATE festival SET estado = 'CANCELADO' WHERE id_festival = p_id_festival;
    UPDATE presentacion SET estado = 'CANCELADA'
    WHERE id_festival = p_id_festival AND estado = 'PROGRAMADA';

    CALL sp_devolver_tickets(p_id_festival, NULL, format('Se canceló el festival %s.', v_nombre),
                             p_notificados, p_credito_total);
END $$;

-- Cancela un tipo de entrada: cierra su venta (aforo disponible 0) y devuelve el dinero de sus tickets vigentes
CREATE OR REPLACE PROCEDURE sp_cancelar_tipo_entrada(
    p_id_tipo_entrada BIGINT,
    INOUT p_notificados INT DEFAULT NULL,
    INOUT p_credito_total NUMERIC DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_festival BIGINT;
    v_tipo VARCHAR;
    v_festival VARCHAR;
    v_estado VARCHAR;
BEGIN
    SELECT te.id_festival, te.nombre_tipo, f.nombre, f.estado
    INTO v_id_festival, v_tipo, v_festival, v_estado
    FROM tipo_entrada te
    JOIN festival f ON f.id_festival = te.id_festival
    WHERE te.id_tipo_entrada = p_id_tipo_entrada
    FOR UPDATE OF te;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Tipo de entrada no encontrado';
    END IF;
    IF v_estado IN ('CANCELADO', 'FINALIZADO') THEN
        RAISE EXCEPTION 'No se puede cancelar una entrada de un festival %', lower(v_estado);
    END IF;

    UPDATE tipo_entrada SET aforo_disponible = 0 WHERE id_tipo_entrada = p_id_tipo_entrada;

    CALL sp_devolver_tickets(v_id_festival, p_id_tipo_entrada,
                             format('Se canceló la entrada "%s" del festival %s.', v_tipo, v_festival),
                             p_notificados, p_credito_total);
END $$;

-- ======================================================================
-- 8. PROCEDIMIENTO: sp_validar_acceso
-- ======================================================================

/* Procedimiento Almacenado 3: Validar acceso (control de entrada)
Recibe el código de un ticket y el festival donde está el lector. Si es válido lo marca como UTILIZADO
y registra todo intento sobre un ticket existente en log_entrada.
p_resultado: VALIDO | YA_USADO | ANULADO | INEXISTENTE | OTRO_FESTIVAL | FECHA_INCORRECTA
Doble escaneo: el UPDATE condicional (estado = 'VIGENTE') toma el candado de la fila, así que de dos
lectores simultáneos solo uno obtiene VALIDO; el otro espera y recibe YA_USADO.
INEXISTENTE no se registra porque log_entrada exige un ticket.
*/

CREATE OR REPLACE PROCEDURE sp_validar_acceso(
    p_codigo VARCHAR,
    p_id_festival BIGINT,
    p_id_usuario BIGINT,
    INOUT p_resultado VARCHAR DEFAULT NULL,
    INOUT p_id_ticket BIGINT DEFAULT NULL)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
    v_estado_orden VARCHAR;
    v_fecha_entrada DATE;
    v_id_festival BIGINT;
    v_inicio DATE;
    v_fin DATE;
    v_hoy DATE := fn_dia_festival(now());
BEGIN
    SELECT t.id_ticket, t.estado, o.estado, te.fecha, te.id_festival, f.fecha_inicio, f.fecha_fin
    INTO p_id_ticket, v_estado, v_estado_orden, v_fecha_entrada, v_id_festival, v_inicio, v_fin
    FROM ticket t
    JOIN orden_compra o ON o.id_orden = t.id_orden
    JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
    JOIN festival f ON f.id_festival = te.id_festival
    WHERE t.codigo_unico = p_codigo;

    IF NOT FOUND THEN
        p_resultado := 'INEXISTENTE';
        p_id_ticket := NULL;
        RETURN;
    END IF;

    IF v_id_festival <> p_id_festival THEN
        p_resultado := 'OTRO_FESTIVAL';
    ELSIF v_estado = 'CANCELADO' OR v_estado_orden <> 'PAGADA' THEN
        p_resultado := 'ANULADO';
    ELSIF v_estado = 'UTILIZADO' THEN
        p_resultado := 'YA_USADO';
    ELSIF v_hoy NOT BETWEEN v_inicio AND v_fin OR (v_fecha_entrada IS NOT NULL AND v_fecha_entrada <> v_hoy) THEN
        p_resultado := 'FECHA_INCORRECTA';
    ELSE
        UPDATE ticket SET estado = 'UTILIZADO', fecha_uso = now()
        WHERE id_ticket = p_id_ticket AND estado = 'VIGENTE';
        IF FOUND THEN
            p_resultado := 'VALIDO';
        ELSE
            p_resultado := 'YA_USADO';  -- otro lector lo validó justo antes
        END IF;
    END IF;

    INSERT INTO log_entrada (id_ticket, id_usuario, resultado) VALUES (p_id_ticket, p_id_usuario, p_resultado);
END $$;

-- ======================================================================
-- 9. VISTA MATERIALIZADA mv_ventas
-- ======================================================================

-- 1. Crear la vista materializada
CREATE MATERIALIZED VIEW mv_ventas AS
SELECT 
    f.id_festival, -- ID del festival
    f.nombre AS festival_nombre, -- Nombre del festival
    te.nombre_tipo AS tipo_entrada, -- Nombre del tipo de entrada
    te.aforo_total, -- Aforo total
    (te.aforo_total - te.aforo_disponible) AS tickets_vendidos, -- Tickets vendidos
    ROUND(((te.aforo_total - te.aforo_disponible)::numeric / te.aforo_total) * 100, 2) AS porcentaje_ocupacion, -- Porcentaje de ocupación
    COALESCE(SUM(t.precio), 0) AS recaudacion_total -- COALESCE si el resultado de la suma es null devuelve un cero
FROM festival f
JOIN tipo_entrada te ON f.id_festival = te.id_festival
-- LEFT JOIN Mantiene los tipos de entrada aunque no hayan tickets vendidos en ese tipo
--  <> 'CANCELADO' importante para evitar ventas fantasma, entradas devueltas o echas credito
LEFT JOIN ticket t ON te.id_tipo_entrada = t.id_tipo_entrada AND t.estado <> 'CANCELADO'
GROUP BY f.id_festival, f.nombre, te.nombre_tipo, te.aforo_total, te.aforo_disponible;

-- 2. Función para refrescar los datos
CREATE OR REPLACE FUNCTION fn_refrescar_reporte_ventas()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW mv_ventas;
END;
$$ LANGUAGE plpgsql;

-- ======================================================================
-- 10. ÍNDICES
-- ======================================================================

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

-- ======================================================================
-- 11. DATOS DE PRUEBA (seeders)
-- ======================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

/* Datos de DEMO (no usar en producción). Reemplaza a seed_dev.sql en init_all.sql.
Todas las contraseñas son: 1234
Las fechas son relativas al día en que se crea la base (hoy = fn_dia_festival(now())), así que
los festivales "en curso" siempre terminan 10 días después de crear la base.

Usuarios:
  ADMINISTRADOR: admin1@festivales.cl, admin2@festivales.cl
  PRODUCTOR:     productor1@festivales.cl, productor2@festivales.cl
  ASISTENTE:     usuario1..usuario6@festivales.cl  (usuario6 parte con crédito de compensación)

Escenarios de demo:
  - Festival Cordillera (en curso, productor1):
      "Pase Backstage"       -> AGOTADA   (probar que no se puede comprar)
      "Meet & Greet"         -> queda 1   (comprar 2 veces: la 1ª funciona, la 2ª es rechazada)
  - Festival Aurora (en curso, productor2): "Pase Premium" AGOTADA
  - Festival Primavera (publicado, futuro) y Festival Verano (borrador, sin ventas) para el CRUD.
*/

-- Usuarios (ORDER BY orden: ids estables 1..12 en una base recién creada)
INSERT INTO usuario (id_rol, nombre_completo, email, password_hash, creditos_compensacion)
SELECT r.id, u.nombre, u.email, crypt('1234', gen_salt('bf', 10)), u.creditos
FROM (VALUES
    (1,  'ADMINISTRADOR', 'Administrador Uno',  'admin1@festivales.cl',     0),
    (2,  'ADMINISTRADOR', 'Administrador Dos',  'admin2@festivales.cl',     0),
    (3,  'PRODUCTOR',     'Productora Uno',     'productor1@festivales.cl', 0),
    (4,  'PRODUCTOR',     'Productor Dos',      'productor2@festivales.cl', 0),
    (5,  'ASISTENTE',     'Usuario Uno',        'usuario1@festivales.cl',   0),
    (6,  'ASISTENTE',     'Usuario Dos',        'usuario2@festivales.cl',   0),
    (7,  'ASISTENTE',     'Usuario Tres',       'usuario3@festivales.cl',   0),
    (8,  'ASISTENTE',     'Usuario Cuatro',     'usuario4@festivales.cl',   0),
    (9,  'ASISTENTE',     'Usuario Cinco',      'usuario5@festivales.cl',   0),
    (10, 'ASISTENTE',     'Usuario Seis',       'usuario6@festivales.cl',   20000)
) AS u(orden, tipo, nombre, email, creditos)
JOIN rol r ON r.tipo = u.tipo
ORDER BY u.orden;

-- Recintos y escenarios
INSERT INTO recinto (nombre, direccion, aforo_maximo) VALUES
    ('Parque Central Santiago', 'Av. Libertador Bernardo O''Higgins 1000, Santiago', 20000),
    ('Estadio Costanera',       'Av. Costanera 2500, Valparaíso', 15000),
    ('Anfiteatro del Valle',    'Camino al Valle 450, Los Andes', 6000);

INSERT INTO escenario (id_recinto, nombre, capacidad_maxima)
SELECT r.id_recinto, e.nombre, e.capacidad
FROM (VALUES
    ('Parque Central Santiago', 'Escenario Principal',   12000),
    ('Parque Central Santiago', 'Escenario Alternativo',  5000),
    ('Estadio Costanera',       'Escenario Mar',          9000),
    ('Estadio Costanera',       'Escenario Puerto',       4000),
    ('Anfiteatro del Valle',    'Escenario Valle',        6000)
) AS e(recinto, nombre, capacidad)
JOIN recinto r ON r.nombre = e.recinto;

-- Artistas
INSERT INTO artista (nombre, genero_musical) VALUES
    ('Los Andes Eléctricos', 'Rock'),
    ('Luna Austral',         'Pop'),
    ('Cumbre Sonora',        'Indie'),
    ('Ritmo Pacífico',       'Reggae'),
    ('Nova Beat',            'Electrónica'),
    ('Voces del Sur',        'Folclor'),
    ('Mar de Fondo',         'Rock'),
    ('Aurora Boreal',        'Pop'),
    ('DJ Cóndor',            'Electrónica'),
    ('Sol Naciente',         'Jazz');

-- Festivales. hoy = día calendario en America/Santiago
-- Cordillera y Aurora: en curso, terminan en 10 días. Primavera: publicado a futuro. Verano: borrador.
INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
SELECT u.id_usuario, r.id_recinto, f.nombre,
       fn_dia_festival(now()) + f.ini, fn_dia_festival(now()) + f.fin, f.estado
FROM (VALUES
    ('productor1@festivales.cl', 'Parque Central Santiago', 'Festival Cordillera', -4, 10, 'PUBLICADO'),
    ('productor2@festivales.cl', 'Estadio Costanera',       'Festival Aurora',     -2, 10, 'PUBLICADO'),
    ('productor1@festivales.cl', 'Anfiteatro del Valle',    'Festival Primavera',  30, 32, 'PUBLICADO'),
    ('productor2@festivales.cl', 'Parque Central Santiago', 'Festival Verano',     60, 61, 'BORRADOR')
) AS f(productor, recinto, nombre, ini, fin, estado)
JOIN usuario u ON u.email = f.productor
JOIN recinto r ON r.nombre = f.recinto;

-- Tipos de entrada (aforo_disponible = aforo_total; las compras de más abajo lo descuentan)
-- fecha NULL = pase válido todo el festival; con fecha = solo ese día (offset desde hoy)
INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
SELECT f.id_festival, t.nombre, t.precio, t.aforo, t.aforo,
       CASE WHEN t.dia IS NULL THEN NULL ELSE fn_dia_festival(now()) + t.dia END
FROM (VALUES
    ('Festival Cordillera', 'Pase Completo',       80000.00, 500,  NULL::int),
    ('Festival Cordillera', 'General Día Actual',  35000.00, 800,  0),
    ('Festival Cordillera', 'Pase Backstage',     150000.00,   3,  NULL),
    ('Festival Cordillera', 'Meet & Greet',       120000.00,   5,  NULL),
    ('Festival Aurora',     'Pase Completo',       70000.00, 400,  NULL),
    ('Festival Aurora',     'General Día Actual',  30000.00, 600,  0),
    ('Festival Aurora',     'Pase Premium',       130000.00,   2,  NULL),
    ('Festival Primavera',  'Pase 3 Días',         90000.00, 300,  NULL),
    ('Festival Primavera',  'General Día 1',       40000.00, 500,  30)
) AS t(festival, nombre, precio, aforo, dia)
JOIN festival f ON f.nombre = t.festival;

-- Presentaciones (hora de Santiago). Cada fila: festival, escenario, artista, día (offset), hora inicio, hora fin
INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
SELECT f.id_festival, e.id_escenario, a.id_artista,
       ((fn_dia_festival(now()) + p.dia) + p.h_ini) AT TIME ZONE 'America/Santiago',
       ((fn_dia_festival(now()) + p.dia) + p.h_fin) AT TIME ZONE 'America/Santiago'
FROM (VALUES
    ('Festival Cordillera', 'Escenario Principal',   'Los Andes Eléctricos', -3, TIME '20:00', TIME '21:30'),
    ('Festival Cordillera', 'Escenario Principal',   'Luna Austral',          0, TIME '18:00', TIME '19:30'),
    ('Festival Cordillera', 'Escenario Alternativo', 'Cumbre Sonora',         0, TIME '20:00', TIME '21:30'),
    ('Festival Cordillera', 'Escenario Principal',   'Nova Beat',             1, TIME '21:00', TIME '23:00'),
    ('Festival Cordillera', 'Escenario Principal',   'Voces del Sur',         5, TIME '19:00', TIME '20:30'),
    ('Festival Cordillera', 'Escenario Principal',   'Los Andes Eléctricos',  9, TIME '21:00', TIME '23:00'),
    ('Festival Aurora',     'Escenario Mar',         'Ritmo Pacífico',        0, TIME '16:00', TIME '17:30'),
    ('Festival Aurora',     'Escenario Puerto',      'Mar de Fondo',          0, TIME '19:00', TIME '20:30'),
    ('Festival Aurora',     'Escenario Mar',         'Aurora Boreal',         2, TIME '20:00', TIME '22:00'),
    ('Festival Aurora',     'Escenario Mar',         'DJ Cóndor',             8, TIME '22:00', TIME '23:59'),
    ('Festival Primavera',  'Escenario Valle',       'Sol Naciente',         30, TIME '18:00', TIME '19:30'),
    ('Festival Primavera',  'Escenario Valle',       'Voces del Sur',        31, TIME '19:00', TIME '20:30')
) AS p(festival, escenario, artista, dia, h_ini, h_fin)
JOIN festival f ON f.nombre = p.festival
JOIN escenario e ON e.nombre = p.escenario AND e.id_recinto = f.id_recinto
JOIN artista a ON a.nombre = p.artista;

-- Compras reales a través del procedimiento almacenado (descuenta aforo y genera tickets con código único)
DO $$
DECLARE
    c RECORD;
    v_orden BIGINT;
BEGIN
    FOR c IN
        SELECT u.id_usuario, te.id_tipo_entrada, x.cantidad
        FROM (VALUES
            -- Backstage de Cordillera: 3 de 3 -> AGOTADA
            ('usuario1@festivales.cl', 'Festival Cordillera', 'Pase Backstage', 1),
            ('usuario2@festivales.cl', 'Festival Cordillera', 'Pase Backstage', 1),
            ('usuario3@festivales.cl', 'Festival Cordillera', 'Pase Backstage', 1),
            -- Meet & Greet: 4 de 5 -> queda 1
            ('usuario1@festivales.cl', 'Festival Cordillera', 'Meet & Greet', 2),
            ('usuario2@festivales.cl', 'Festival Cordillera', 'Meet & Greet', 2),
            -- Pase Premium de Aurora: 2 de 2 -> AGOTADA
            ('usuario4@festivales.cl', 'Festival Aurora',     'Pase Premium', 1),
            ('usuario5@festivales.cl', 'Festival Aurora',     'Pase Premium', 1),
            -- Compras normales
            ('usuario1@festivales.cl', 'Festival Cordillera', 'Pase Completo', 2),
            ('usuario2@festivales.cl', 'Festival Cordillera', 'General Día Actual', 3),
            ('usuario3@festivales.cl', 'Festival Cordillera', 'Pase Completo', 1),
            ('usuario4@festivales.cl', 'Festival Cordillera', 'General Día Actual', 2),
            ('usuario4@festivales.cl', 'Festival Aurora',     'Pase Completo', 2),
            ('usuario5@festivales.cl', 'Festival Aurora',     'General Día Actual', 4),
            ('usuario3@festivales.cl', 'Festival Aurora',     'Pase Completo', 1),
            ('usuario5@festivales.cl', 'Festival Primavera',  'Pase 3 Días', 2),
            ('usuario1@festivales.cl', 'Festival Primavera',  'General Día 1', 1)
        ) AS x(email, festival, tipo, cantidad)
        JOIN usuario u ON u.email = x.email
        JOIN festival f ON f.nombre = x.festival
        JOIN tipo_entrada te ON te.id_festival = f.id_festival AND te.nombre_tipo = x.tipo
    LOOP
        CALL sp_comprar_entradas(c.id_usuario, c.id_tipo_entrada, c.cantidad, FALSE, v_orden);
    END LOOP;
END $$;

-- Control de acceso: dos tickets de Cordillera ya ingresados (los demás siguen VIGENTES para escanear en la demo)
DO $$
DECLARE
    v_admin BIGINT := (SELECT id_usuario FROM usuario WHERE email = 'admin1@festivales.cl');
    v_festival BIGINT := (SELECT id_festival FROM festival WHERE nombre = 'Festival Cordillera');
    t RECORD;
    v_res VARCHAR;
    v_id BIGINT;
BEGIN
    FOR t IN
        SELECT tk.codigo_unico
        FROM ticket tk
        JOIN tipo_entrada te ON te.id_tipo_entrada = tk.id_tipo_entrada
        WHERE te.id_festival = v_festival AND te.nombre_tipo = 'Pase Completo'
        ORDER BY tk.id_ticket
        LIMIT 2
    LOOP
        CALL sp_validar_acceso(t.codigo_unico, v_festival, v_admin, v_res, v_id);
    END LOOP;
END $$;

-- Reporte de ventas actualizado con las compras de arriba
SELECT fn_refrescar_reporte_ventas();
