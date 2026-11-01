package cl.grupo3.lab1.dto;

/** Respuesta de login: {"token","tipo":"Bearer","expira_en_segundos","usuario"}. */
public record LoginResponse(String token, String tipo, long expiraEnSegundos, UsuarioResumen usuario) {
}
