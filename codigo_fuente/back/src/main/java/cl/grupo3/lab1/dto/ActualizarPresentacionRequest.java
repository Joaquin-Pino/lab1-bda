package cl.grupo3.lab1.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de PUT /api/presentaciones/{id}. No permite cambiar el festival ni el estado. */
public record ActualizarPresentacionRequest(
        @NotNull(message = "es obligatorio")
        Long idEscenario,

        @NotNull(message = "es obligatorio")
        Long idArtista,

        @NotNull(message = "es obligatorio")
        OffsetDateTime fechaHoraInicio,

        @NotNull(message = "es obligatorio")
        OffsetDateTime fechaHoraFin) {
}
