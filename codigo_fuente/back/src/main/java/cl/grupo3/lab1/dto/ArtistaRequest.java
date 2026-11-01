package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST/PUT /api/artistas. */
public record ArtistaRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener como máximo 150 caracteres")
        String nombre,

        @NotBlank(message = "es obligatorio")
        @Size(max = 80, message = "debe tener como máximo 80 caracteres")
        String generoMusical) {

    /** Recorta espacios antes de validar. */
    public ArtistaRequest {
        nombre = nombre == null ? null : nombre.strip();
        generoMusical = generoMusical == null ? null : generoMusical.strip();
    }
}
