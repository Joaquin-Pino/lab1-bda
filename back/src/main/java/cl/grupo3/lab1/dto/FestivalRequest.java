package cl.grupo3.lab1.dto;

import java.time.LocalDate;
import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST/PUT /api/festivales.
 */
public record FestivalRequest(
        @NotNull(message = "es obligatorio")
        Long idRecinto,

        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener como máximo 150 caracteres")
        String nombre,

        @NotNull(message = "es obligatorio")
        LocalDate fechaInicio,

        @NotNull(message = "es obligatorio")
        LocalDate fechaFin,

        @Pattern(regexp = "BORRADOR|PUBLICADO|CANCELADO|FINALIZADO",
                message = "debe ser BORRADOR, PUBLICADO, CANCELADO o FINALIZADO")
        String estado) {

    /** Recorta espacios y pasa el estado a mayúsculas antes de validar. */
    public FestivalRequest {
        nombre = nombre == null ? null : nombre.strip();
        estado = estado == null || estado.isBlank() ? null : estado.strip().toUpperCase(Locale.ROOT);
    }
}
