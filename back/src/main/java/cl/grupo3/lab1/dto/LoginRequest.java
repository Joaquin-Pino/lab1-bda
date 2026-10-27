package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/login. */
public record LoginRequest(
        @NotBlank(message = "es obligatorio") String email,
        @NotBlank(message = "es obligatorio") String password) {

    /** Recorta espacios antes de validar (el email se pasa a minúsculas en el servicio). */
    public LoginRequest {
        email = email == null ? null : email.strip();
    }
}
