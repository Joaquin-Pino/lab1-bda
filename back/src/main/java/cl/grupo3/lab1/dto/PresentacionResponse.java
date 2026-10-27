package cl.grupo3.lab1.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Presentación devuelta por la API. Las horas salen con la zona de Chile y "dia" es su día de festival. */
public record PresentacionResponse(Long idPresentacion, Long idFestival, Long idEscenario, String nombreEscenario,
                                   Long idArtista, String nombreArtista, OffsetDateTime fechaHoraInicio,
                                   OffsetDateTime fechaHoraFin, LocalDate dia, String estado) {
}
