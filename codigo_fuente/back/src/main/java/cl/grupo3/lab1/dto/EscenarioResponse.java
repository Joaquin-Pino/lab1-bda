package cl.grupo3.lab1.dto;

/** Escenario devuelto por la API. */
public record EscenarioResponse(Long idEscenario, Long idRecinto, String nombreRecinto, String nombre,
                                Integer capacidadMaxima) {
}
