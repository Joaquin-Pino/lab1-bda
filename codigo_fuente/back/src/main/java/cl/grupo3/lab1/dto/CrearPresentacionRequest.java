package cl.grupo3.lab1.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de POST /api/presentaciones. Las horas van en ISO-8601 con zona, por ejemplo "2026-12-12T20:00:00-03:00".
 * Siempre se crea en estado PROGRAMADA.
 */
public record CrearPresentacionRequest(
        @NotNull(message = "es obligatorio")
        Long idFestival,

        @NotNull(message = "es obligatorio")
        Long idEscenario,

        @NotNull(message = "es obligatorio")
        Long idArtista,

        @NotNull(message = "es obligatorio")
        OffsetDateTime fechaHoraInicio,

        @NotNull(message = "es obligatorio")
        OffsetDateTime fechaHoraFin) {
}
