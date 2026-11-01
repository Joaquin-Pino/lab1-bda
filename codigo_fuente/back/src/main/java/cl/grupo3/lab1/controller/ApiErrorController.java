package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.ApiError;
import cl.grupo3.lab1.exception.GlobalExceptionHandler;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reemplaza el /error de Spring Boot para que los errores que escapan de los controladores
 * (p. ej. lanzados en filtros) también respondan con {"error": "..."}.
 */
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiError> error(HttpServletRequest request) {
        Object codigoEstado = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.resolve(codigoEstado instanceof Integer codigo ? codigo : 500);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String mensaje = switch (status) {
            case BAD_REQUEST -> "Petición inválida";
            case UNAUTHORIZED -> GlobalExceptionHandler.MSG_NO_AUTENTICADO;
            case FORBIDDEN -> GlobalExceptionHandler.MSG_SIN_PERMISO;
            case NOT_FOUND -> GlobalExceptionHandler.MSG_NO_ENCONTRADO;
            case METHOD_NOT_ALLOWED -> "Método no permitido";
            default -> status.is4xxClientError() ? "Petición inválida" : GlobalExceptionHandler.MSG_INTERNO;
        };
        return ResponseEntity.status(status).body(new ApiError(mensaje));
    }
}
