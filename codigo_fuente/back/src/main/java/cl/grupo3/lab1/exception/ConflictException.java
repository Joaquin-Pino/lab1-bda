package cl.grupo3.lab1.exception;

import org.springframework.http.HttpStatus;

/** 409: la operación choca con una regla de negocio o con el estado actual de los datos. */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
