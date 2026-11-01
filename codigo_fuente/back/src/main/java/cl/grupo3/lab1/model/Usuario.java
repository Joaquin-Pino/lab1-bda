package cl.grupo3.lab1.model;

import java.math.BigDecimal;

/**
 * Fila de la tabla usuario (más el tipo de su rol). La crean los repositorios y la convierten a DTO los servicios.
 * passwordHash solo existe aquí (nunca en un DTO de salida); vale null cuando la consulta no lo necesita.
 */
public record Usuario(Long idUsuario, Long idRol, String rol, String nombreCompleto, String email,
                      String passwordHash, BigDecimal creditosCompensacion) {
}
