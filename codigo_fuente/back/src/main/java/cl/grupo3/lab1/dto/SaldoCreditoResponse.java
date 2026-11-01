package cl.grupo3.lab1.dto;

import java.math.BigDecimal;

/** Saldo de crédito de compensación del usuario autenticado. */
public record SaldoCreditoResponse(BigDecimal creditosCompensacion) {
}
