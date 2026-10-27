package cl.grupo3.lab1.exception;

import org.springframework.http.HttpStatus;

/** 400: dato de entrada inválido que no cubre Bean Validation. */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
