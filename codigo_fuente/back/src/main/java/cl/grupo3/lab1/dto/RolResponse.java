package cl.grupo3.lab1.dto;

/** Respuesta de /api/roles: {"id","tipo"}. La arma RolService a partir de model.Rol. */
public record RolResponse(Long id, String tipo) {
}
