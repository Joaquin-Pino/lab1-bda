package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Fila de la tabla ticket. fechaUso es nula mientras el ticket no se haya utilizado. */
public record Ticket(Long idTicket, String codigoUnico, Long idOrden, Long idTipoEntrada, String estado,
                     BigDecimal precio, OffsetDateTime fechaUso) {
}