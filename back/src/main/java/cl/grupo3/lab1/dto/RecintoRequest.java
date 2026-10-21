package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST/PUT /api/recintos. */
public record RecintoRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener como máximo 150 caracteres")
        String nombre,

        @NotBlank(message = "es obligatorio")
        @Size(max = 250, message = "debe tener como máximo 250 caracteres")
        String direccion,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor que 0")
        Integer aforoMaximo) {

    /** Recorta espacios antes de validar. */
    public RecintoRequest {
        nombre = nombre == null ? null : nombre.strip();
        direccion = direccion == null ? null : direccion.strip();
    }
}