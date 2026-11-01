package cl.grupo3.lab1.dto;

import java.time.OffsetDateTime;

/**
 * Resultado de una lectura. resultado: VALIDO, YA_USADO, ANULADO, INEXISTENTE, OTRO_FESTIVAL o FECHA_INCORRECTA.
 * valido solo es true si el ticket acaba de quedar UTILIZADO. Los datos del ticket son nulos si es INEXISTENTE.
 */
public record AccesoResponse(String resultado, boolean valido, String mensaje, Long idTicket, String nombreTipo,
                             OffsetDateTime fechaUso) {
}
