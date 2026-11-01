package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/auth/register. Cualquier "rol"/"id_rol" extra se ignora. */
public record RegisterRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener entre 1 y 150 caracteres")
        String nombreCompleto,

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        @Size(max = 160, message = "debe tener como máximo 160 caracteres")
        String email,

        @NotBlank(message = "es obligatorio")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password) {

    /** Recorta espacios antes de validar (el email se pasa a minúsculas en el servicio). */
    public RegisterRequest {
        nombreCompleto = nombreCompleto == null ? null : nombreCompleto.strip();
        email = email == null ? null : email.strip();
    }
}
