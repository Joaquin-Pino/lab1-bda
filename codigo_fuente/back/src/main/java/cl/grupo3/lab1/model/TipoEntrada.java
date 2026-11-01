package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Fila de la tabla tipo_entrada. */
public record TipoEntrada(Long idTipoEntrada, Long idFestival, String nombreTipo, BigDecimal precioActual,
                          Integer aforoTotal, Integer aforoDisponible, LocalDate fecha) {
}
