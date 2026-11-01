package cl.grupo3.lab1.dto;

import java.math.BigDecimal;

/** Asistente notificado por una cancelación y el crédito que se le abonó (0 si ya había sido compensado ese día). */
public record CompensacionAsistenteResponse(Long idUsuario, String nombreCompleto, BigDecimal creditoOtorgado) {
}
