package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Ticket junto con el tipo de entrada y el festival al que pertenece (resultado de un JOIN, no una tabla).
 * fechaUso es nula mientras el ticket no se use; fechaEntrada es nula si la entrada vale para todo el festival.
 */
public record TicketDetalle(Long idTicket, String codigoUnico, String estado, BigDecimal precio,
                            OffsetDateTime fechaUso, Long idOrden, Long idTipoEntrada, String nombreTipo,
                            LocalDate fechaEntrada, Long idFestival, String nombreFestival) {
}
