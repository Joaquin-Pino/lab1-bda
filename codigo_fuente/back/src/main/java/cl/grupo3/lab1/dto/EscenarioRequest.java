package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST/PUT /api/escenarios. */
public record EscenarioRequest(
        @NotNull(message = "es obligatorio")
        Long idRecinto,

        @NotBlank(message = "es obligatorio")
        @Size(max = 120, message = "debe tener como máximo 120 caracteres")
        String nombre,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor que 0")
        Integer capacidadMaxima) {

    /** Recorta espacios antes de validar. */
    public EscenarioRequest {
        nombre = nombre == null ? null : nombre.strip();
    }
}
