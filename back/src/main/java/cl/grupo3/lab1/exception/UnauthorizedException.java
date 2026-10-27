package cl.grupo3.lab1.exception;

import org.springframework.http.HttpStatus;

/** 401: credenciales inválidas (login). */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
