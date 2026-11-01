package cl.grupo3.lab1.dto;

import java.math.BigDecimal;

/** Respuesta de GET /api/auth/me (leída de la base). */
public record PerfilResponse(Long idUsuario, String nombreCompleto, String email, String rol,
                             BigDecimal creditosCompensacion) {
}
