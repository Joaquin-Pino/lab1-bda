package cl.grupo3.lab1.dto;

import java.time.LocalDate;

/** Festival devuelto por la API. */
public record FestivalResponse(Long idFestival, Long idUsuarioProductor, Long idRecinto, String nombreRecinto,
                               String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
}
