package cl.grupo3.lab1.exception;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Traduce errores de PostgreSQL (envueltos por Spring en DataAccessException) a HTTP + mensaje en español.
 * Usa el SQLState para el código y ServerErrorMessage.getMessage() para el texto (sin "ERROR:" ni "Where: PL/pgSQL ...").
 */
@Component
public class PgErrorMapper {

    public record MappedError(HttpStatus status, String message) {
    }

    static final String MSG_UNIQUE = "Ya existe un registro con ese valor";
    static final String MSG_FK = "Operación no permitida por datos relacionados";
    static final String MSG_CHECK = "Los datos no cumplen una restricción del sistema";
    static final String MSG_FORMATO = "Formato o rango de datos inválido";
    static final String MSG_LARGO = "Un valor excede el largo máximo permitido";
    static final String MSG_CONCURRENCIA = "Conflicto de concurrencia, intenta nuevamente";
    static final String MSG_NEGOCIO = "La operación no está permitida";

    private static final Map<String, String> MENSAJES_CONSTRAINT = Map.ofEntries(
            Map.entry("uq_usuario_email", "El email ya está registrado"),
            Map.entry("uq_escenario_nombre", "Ya existe un escenario con ese nombre en el recinto"),
            Map.entry("uq_tipo_entrada_nombre", "Ya existe un tipo de entrada con ese nombre en el festival"),
            Map.entry("ck_aforo", "El aforo disponible no puede ser negativo ni mayor al total"),
            Map.entry("ck_festival_fechas", "La fecha de fin no puede ser anterior a la de inicio"),
            Map.entry("ck_presentacion_horario", "La hora de fin debe ser posterior a la de inicio"),
            Map.entry("ck_ticket_uso", "Un ticket utilizado debe tener fecha de uso"),
            Map.entry("ex_ticket_codigo_unico", "Código de ticket duplicado"),
            Map.entry("rol_tipo_key", "Ya existe un rol con ese tipo"),
            Map.entry("rol_tipo_check", "Tipo de rol no permitido (valores válidos: ASISTENTE, PRODUCTOR, ADMINISTRADOR)"));

    private static final Set<String> SQLSTATE_FORMATO = Set.of("22P02", "22003", "22007", "22008");

    /** Devuelve el error mapeado si la causa raíz es una PSQLException con SQLState conocido; vacío si no. */
    public Optional<MappedError> map(Throwable ex) {
        PSQLException psqlException = findPsqlException(ex);
        if (psqlException == null || psqlException.getSQLState() == null) {
            return Optional.empty();
        }
        ServerErrorMessage serverError = psqlException.getServerErrorMessage();
        String constraint = serverError != null ? serverError.getConstraint() : null;
        String porConstraint = constraint != null ? MENSAJES_CONSTRAINT.get(constraint) : null;

        String sqlState = psqlException.getSQLState();
        MappedError mappedError = switch (sqlState) {
            case "23502" -> new MappedError(HttpStatus.BAD_REQUEST, mensajeNotNull(serverError));
            case "23514" -> new MappedError(HttpStatus.BAD_REQUEST, orDefault(porConstraint, MSG_CHECK));
            case "22001" -> new MappedError(HttpStatus.BAD_REQUEST, MSG_LARGO);
            case "23505", "23P01" -> new MappedError(HttpStatus.CONFLICT, orDefault(porConstraint, MSG_UNIQUE));
            case "23503" -> new MappedError(HttpStatus.CONFLICT, orDefault(porConstraint, MSG_FK));
            case "P0001" -> new MappedError(HttpStatus.CONFLICT, mensajeRaise(serverError));
            case "40001", "40P01" -> new MappedError(HttpStatus.CONFLICT, MSG_CONCURRENCIA);
            default -> SQLSTATE_FORMATO.contains(sqlState) ? new MappedError(HttpStatus.BAD_REQUEST, MSG_FORMATO) : null;
        };
        return Optional.ofNullable(mappedError);
    }

    static PSQLException findPsqlException(Throwable ex) {
        Throwable actual = ex;
        int niveles = 0;
        while (actual != null && niveles++ < 20) {
            if (actual instanceof PSQLException encontrada) {
                return encontrada;
            }
            actual = actual.getCause();
        }
        return null;
    }

    private static String mensajeNotNull(ServerErrorMessage serverError) {
        String columna = serverError != null ? serverError.getColumn() : null;
        return columna != null ? columna + ": es obligatorio" : "Falta un campo obligatorio";
    }

    private static String mensajeRaise(ServerErrorMessage serverError) {
        String mensaje = serverError != null ? serverError.getMessage() : null;
        return mensaje == null || mensaje.isBlank() ? MSG_NEGOCIO : mensaje;
    }

    private static String orDefault(String value, String fallback) {
        return value != null ? value : fallback;
    }
}
