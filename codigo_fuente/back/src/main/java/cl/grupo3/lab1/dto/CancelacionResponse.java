package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resultado de cancelar una presentación: cuántos asistentes se notificaron y el crédito de cada uno. */
public record CancelacionResponse(Long idPresentacion, String estado, int asistentesNotificados,
                                  BigDecimal creditoTotalOtorgado, List<CompensacionAsistenteResponse> asistentes) {
}
