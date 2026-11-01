package cl.grupo3.lab1.exception;

import org.springframework.http.HttpStatus;

/** 404: recurso inexistente o ajeno (para no revelar que existe). */
public class NotFoundException extends ApiException {

    public NotFoundException() {
        this("Recurso no encontrado");
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
