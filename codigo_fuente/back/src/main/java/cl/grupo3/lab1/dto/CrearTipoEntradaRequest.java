package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public record CrearTipoEntradaRequest(
        @NotNull(message = "es obligatorio")
        Long idFestival,

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "debe tener como máximo 100 caracteres")
        String nombreTipo,

        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "0.0", message = "no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "máximo 10 enteros y 2 decimales")
        BigDecimal precioActual,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Integer aforoTotal,

        LocalDate fecha) {

    public CrearTipoEntradaRequest {
        nombreTipo = nombreTipo == null ? null : nombreTipo.strip();
    }
}
