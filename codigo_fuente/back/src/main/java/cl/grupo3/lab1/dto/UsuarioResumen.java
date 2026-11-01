package cl.grupo3.lab1.dto;

/** Datos públicos del usuario (nunca incluye password_hash). */
public record UsuarioResumen(Long idUsuario, String nombreCompleto, String email, String rol) {
}
