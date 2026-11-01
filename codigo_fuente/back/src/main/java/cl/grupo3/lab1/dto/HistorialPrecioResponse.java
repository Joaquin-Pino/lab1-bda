package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Un cambio de precio de cualquier festival, con los nombres del festival y de la entrada. */
public record HistorialPrecioResponse(Long idAuditoria, Long idTipoEntrada, String nombreTipo, Long idFestival,
                                      String nombreFestival, BigDecimal precioAnterior, BigDecimal precioNuevo,
                                      Long idUsuarioModificador, String nombreUsuario,
                                      OffsetDateTime fechaModificacion) {
}
