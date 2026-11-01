package cl.grupo3.lab1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoEntradaResponse(Long idTipoEntrada, Long idFestival, String nombreTipo, BigDecimal precioActual,
                                  Integer aforoTotal, Integer aforoDisponible, LocalDate fecha) {
}
