package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Un cambio de precio de un tipo de entrada. */
public record AuditoriaPrecioResponse(Long idAuditoria, Long idTipoEntrada, BigDecimal precioAnterior,
                                      BigDecimal precioNuevo, Long idUsuarioModificador, String nombreUsuario,
                                      OffsetDateTime fechaModificacion) {
}
