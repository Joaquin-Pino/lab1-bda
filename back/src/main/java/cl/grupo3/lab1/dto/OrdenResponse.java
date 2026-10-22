package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Orden de compra devuelta por la API, con sus tickets. */
public record OrdenResponse(Long idOrden, OffsetDateTime fechaCompra, BigDecimal montoTotal,
                            BigDecimal montoCreditoAplicado, String estado, List<TicketResponse> tickets) {
}
