package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lectura de un ticket: su código y el festival donde está el lector. */
public record ValidarAccesoRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 40, message = "máximo 40 caracteres") String codigoUnico,
        @NotNull(message = "es obligatorio") Long idFestival) {
}
