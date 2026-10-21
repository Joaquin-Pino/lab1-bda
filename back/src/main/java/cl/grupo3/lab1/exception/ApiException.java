package cl.grupo3.lab1.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base con código HTTP y mensaje para el cliente (en español, sin detalles internos).
 * Usar las subclases: BadRequestException, UnauthorizedException, NotFoundException, ConflictException.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
