package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Fila de la tabla orden_compra. */
public record Orden(Long idOrden, Long idUsuarioAsistente, OffsetDateTime fechaCompra, BigDecimal montoTotal,
                    BigDecimal montoCreditoAplicado, String estado) {
}