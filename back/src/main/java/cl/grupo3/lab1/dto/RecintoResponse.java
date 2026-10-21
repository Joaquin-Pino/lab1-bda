package cl.grupo3.lab1.dto;

/** Recinto devuelto por la API. */
public record RecintoResponse(Long idRecinto, String nombre, String direccion, Integer aforoMaximo) {
}
