package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Ticket del asistente con el festival y la entrada a la que corresponde (GET /api/tickets/mios). */
public record TicketDetalleResponse(Long idTicket, String codigoUnico, String estado, BigDecimal precio,
                                    OffsetDateTime fechaUso, Long idOrden, Long idTipoEntrada, String nombreTipo,
                                    LocalDate fechaEntrada, Long idFestival, String nombreFestival) {
}
