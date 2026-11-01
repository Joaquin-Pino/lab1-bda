package cl.grupo3.lab1.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Fila de la tabla presentacion, más los nombres del escenario y del artista,
 * el dueño del festival (para las reglas de propiedad) y el día según fn_dia_festival.
 */
public record Presentacion(Long idPresentacion, Long idFestival, Long idUsuarioProductor, Long idEscenario,
                           String nombreEscenario, Long idArtista, String nombreArtista,
                           OffsetDateTime fechaHoraInicio, OffsetDateTime fechaHoraFin, LocalDate dia, String estado) {
}
