package cl.grupo3.lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de POST /api/compras. El comprador sale del token, nunca del cuerpo.
 * usarCredito es opcional: si no viene, se asume que no se usa el crédito de compensación.
 */
public record CompraRequest(
        @NotNull(message = "es obligatorio")
        Long idTipoEntrada,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos 1")
        @Max(value = 10, message = "debe ser como máximo 10")
        Integer cantidad,

        boolean usarCredito) {
}