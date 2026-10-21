package cl.grupo3.lab1.model;

import java.time.OffsetDateTime;

/**
 * Una fila de la consulta de la grilla: un escenario y (si tiene) una de sus presentaciones del día.
 * Si el escenario no tiene presentaciones ese día, los campos de la presentación vienen en null.
 */
public record GrillaFila(Long idEscenario, String nombreEscenario, Long idPresentacion, Long idArtista,
                         String nombreArtista, String generoMusical, OffsetDateTime fechaHoraInicio,
                         OffsetDateTime fechaHoraFin, String estado) {
}
