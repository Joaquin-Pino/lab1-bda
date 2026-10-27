package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de cancelar un festival o un tipo de entrada: cuántos asistentes se notificaron
 * y el crédito devuelto a cada uno. {@code id} es el del festival o el del tipo de entrada.
 */
public record DevolucionResponse(Long id, String estado, int asistentesNotificados,
                                 BigDecimal creditoTotalOtorgado, List<CompensacionAsistenteResponse> asistentes) {
}
