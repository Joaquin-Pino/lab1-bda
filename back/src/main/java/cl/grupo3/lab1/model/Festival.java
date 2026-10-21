package cl.grupo3.lab1.model;

import java.time.LocalDate;

/** Fila de la tabla festival (más el nombre de su recinto). */
public record Festival(Long idFestival, Long idUsuarioProductor, Long idRecinto, String nombreRecinto, String nombre,
                       LocalDate fechaInicio, LocalDate fechaFin, String estado) {
}
