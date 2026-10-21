package cl.grupo3.lab1.dto;

import java.math.BigDecimal;

/** Ticket devuelto por la API (dentro de una orden). */
public record TicketResponse(Long idTicket, String codigoUnico, BigDecimal precio, String estado) {
}
