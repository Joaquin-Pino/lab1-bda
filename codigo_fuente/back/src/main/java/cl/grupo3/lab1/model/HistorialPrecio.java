package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Cambio de precio con el festival y el tipo de entrada ya resueltos (vista global del administrador).
 * idUsuarioModificador y nombreUsuario son nulos si el cambio no se hizo desde la API o el usuario fue eliminado.
 */
public record HistorialPrecio(Long idAuditoria, Long idTipoEntrada, String nombreTipo, Long idFestival,
                              String nombreFestival, BigDecimal precioAnterior, BigDecimal precioNuevo,
                              Long idUsuarioModificador, String nombreUsuario, OffsetDateTime fechaModificacion) {
}
