package cl.grupo3.lab1.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Respuesta de GET /api/festivales/{id}/grilla?fecha=...: los escenarios del recinto del festival,
 * cada uno con sus presentaciones del día ordenadas por hora (lista vacía si no tiene).
 */
public record GrillaResponse(Long idFestival, String nombreFestival, LocalDate fecha, List<EscenarioGrilla> escenarios) {

    public record EscenarioGrilla(Long idEscenario, String nombre, List<PresentacionGrilla> presentaciones) {
    }

    public record PresentacionGrilla(Long idPresentacion, Long idArtista, String artista, String generoMusical,
                                     OffsetDateTime fechaHoraInicio, OffsetDateTime fechaHoraFin, String estado) {
    }
}
