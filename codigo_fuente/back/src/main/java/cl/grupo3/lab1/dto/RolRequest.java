package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST/PUT /api/roles. El CHECK de la tabla solo admite ASISTENTE, PRODUCTOR y ADMINISTRADOR. */
public record RolRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 20, message = "debe tener como máximo 20 caracteres")
        String tipo) {
}
