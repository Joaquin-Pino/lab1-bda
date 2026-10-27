package cl.grupo3.lab1.dto;

/**
 * Formato único de error de la API: {"error": "mensaje"}.
 */
public record ApiError(String error) {
}
