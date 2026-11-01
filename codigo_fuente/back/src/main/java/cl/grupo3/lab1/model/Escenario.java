package cl.grupo3.lab1.model;

/** Fila de la tabla escenario (más el nombre de su recinto). */
public record Escenario(Long idEscenario, Long idRecinto, String nombreRecinto, String nombre, Integer capacidadMaxima) {
}
