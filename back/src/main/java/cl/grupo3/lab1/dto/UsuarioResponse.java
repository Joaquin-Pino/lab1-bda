package cl.grupo3.lab1.dto;

import java.math.BigDecimal;

/** Usuario visto por el administrador (nunca incluye password_hash). */
public record UsuarioResponse(Long idUsuario, String nombreCompleto, String email, Long idRol, String rol,
                              BigDecimal creditosCompensacion) {
}
