package cl.grupo3.lab1.dto;

import java.time.OffsetDateTime;

/** Notificación del usuario autenticado. idPresentacion es nulo si la presentación fue eliminada. */
public record NotificacionResponse(Long id, Long idPresentacion, String mensaje, boolean leida,
                                   OffsetDateTime fechaCreacion) {
}
