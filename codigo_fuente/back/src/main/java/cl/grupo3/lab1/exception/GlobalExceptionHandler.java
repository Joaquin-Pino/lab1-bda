package cl.grupo3.lab1.exception;

import java.util.stream.Collectors;

import cl.grupo3.lab1.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador central de errores: toda respuesta de error usa {"error": "..."}.
 * AccessDeniedException y AuthenticationException tienen handlers explícitos para que
 * el handler genérico de Exception no los convierta en 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final String MSG_NO_AUTENTICADO = "No autenticado";
    public static final String MSG_SIN_PERMISO = "No tienes permisos para esta acción";
    public static final String MSG_NO_ENCONTRADO = "Recurso no encontrado";
    public static final String MSG_INTERNO = "Error interno del servidor";

    private final PgErrorMapper pgErrorMapper;

    public GlobalExceptionHandler(PgErrorMapper pgErrorMapper) {
        this.pgErrorMapper = pgErrorMapper;
    }

    // ---- 400 ----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex) {
        String mensajesCampos = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatField)
                .sorted()
                .collect(Collectors.joining("; "));
        String mensajesGlobales = ex.getBindingResult().getGlobalErrors().stream()
                .map(errorGlobal -> errorGlobal.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String mensaje = joinNonBlank(mensajesCampos, mensajesGlobales);
        return respuestaError(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos inválidos" : mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String mensaje = ex.getConstraintViolations().stream()
                .map(violacion -> toSnakeCase(lastNode(violacion.getPropertyPath().toString())) + ": " + violacion.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return respuestaError(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos inválidos" : mensaje);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex) {
        String mensaje = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(errorParametro -> toSnakeCase(String.valueOf(resultado.getMethodParameter().getParameterName()))
                                + ": " + errorParametro.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        return respuestaError(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos inválidos" : mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return respuestaError(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return respuestaError(HttpStatus.BAD_REQUEST, toSnakeCase(ex.getName()) + ": valor inválido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        return respuestaError(HttpStatus.BAD_REQUEST, ex.getParameterName() + ": es obligatorio");
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ApiError> handleMissingValue(MissingRequestValueException ex) {
        return respuestaError(HttpStatus.BAD_REQUEST, "Falta un parámetro obligatorio");
    }

    // ---- 401 / 403 (explícitos: si no, el handler genérico respondería 500) ----

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        return respuestaError(HttpStatus.UNAUTHORIZED, MSG_NO_AUTENTICADO);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return respuestaError(HttpStatus.FORBIDDEN, MSG_SIN_PERMISO);
    }

    // ---- 404 / 405 / 415 ----

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return respuestaError(HttpStatus.NOT_FOUND, MSG_NO_ENCONTRADO);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return respuestaError(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return respuestaError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado (usa application/json)");
    }

    // ---- Excepciones propias (400/401/404/409) ----

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        return respuestaError(ex.getStatus(), ex.getMessage());
    }

    // ---- PostgreSQL ----

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex) {
        return pgErrorMapper.map(ex)
                .map(errorMapeado -> respuestaError(errorMapeado.status(), errorMapeado.message()))
                .orElseGet(() -> {
                    log.error("Error de acceso a datos no mapeado", ex);
                    return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INTERNO);
                });
    }

    // ---- 500 ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INTERNO);
    }

    static ResponseEntity<ApiError> respuestaError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(message));
    }

    private static String formatField(FieldError fieldError) {
        return toSnakeCase(fieldError.getField()) + ": " + fieldError.getDefaultMessage();
    }

    private static String lastNode(String path) {
        int ultimoPunto = path.lastIndexOf('.');
        return ultimoPunto >= 0 ? path.substring(ultimoPunto + 1) : path;
    }

    private static String joinNonBlank(String primero, String segundo) {
        if (primero.isBlank()) {
            return segundo;
        }
        return segundo.isBlank() ? primero : primero + "; " + segundo;
    }

    /** nombreCompleto -> nombre_completo (los mensajes usan los mismos nombres que el JSON). */
    static String toSnakeCase(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
