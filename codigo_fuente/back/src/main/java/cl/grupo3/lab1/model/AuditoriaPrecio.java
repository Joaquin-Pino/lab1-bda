package cl.grupo3.lab1.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Fila de auditoria_precio_entrada (la crea el trigger) más el nombre de quien hizo el cambio.
 * idUsuarioModificador y nombreUsuario son nulos si el cambio no se hizo desde la API o el usuario fue eliminado.
 */
public record AuditoriaPrecio(Long idAuditoria, Long idTipoEntrada, BigDecimal precioAnterior,
                              BigDecimal precioNuevo, Long idUsuarioModificador, String nombreUsuario,
                              OffsetDateTime fechaModificacion) {
}
