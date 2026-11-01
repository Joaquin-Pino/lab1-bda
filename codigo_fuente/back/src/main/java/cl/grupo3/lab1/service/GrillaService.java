package cl.grupo3.lab1.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cl.grupo3.lab1.dto.GrillaResponse;
import cl.grupo3.lab1.dto.GrillaResponse.EscenarioGrilla;
import cl.grupo3.lab1.dto.GrillaResponse.PresentacionGrilla;
import cl.grupo3.lab1.exception.BadRequestException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Festival;
import cl.grupo3.lab1.model.GrillaFila;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.repository.PresentacionRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Grilla del día: presentaciones de un festival en una fecha, agrupadas por escenario y ordenadas por hora.
 */
@Service
public class GrillaService {

    private final FestivalRepository festivalRepository;
    private final PresentacionRepository presentacionRepository;

    public GrillaService(FestivalRepository festivalRepository, PresentacionRepository presentacionRepository) {
        this.festivalRepository = festivalRepository;
        this.presentacionRepository = presentacionRepository;
    }

    public GrillaResponse obtener(Long idFestival, LocalDate fecha, AuthUser usuario) {
        Festival festival = festivalRepository.findVisibleById(idFestival, usuario.esAdmin(), usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(FestivalService.MSG_NO_ENCONTRADO));
        if (fecha.isBefore(festival.fechaInicio()) || fecha.isAfter(festival.fechaFin())) {
            throw new BadRequestException("fecha: debe estar entre " + festival.fechaInicio()
                    + " y " + festival.fechaFin());
        }

        Map<Long, EscenarioGrilla> porEscenario = new LinkedHashMap<>();
        for (GrillaFila fila : presentacionRepository.grilla(idFestival, fecha)) {
            EscenarioGrilla escenario = porEscenario.computeIfAbsent(fila.idEscenario(),
                    idEscenario -> new EscenarioGrilla(idEscenario, fila.nombreEscenario(), new ArrayList<>()));
            if (fila.idPresentacion() != null) {
                escenario.presentaciones().add(new PresentacionGrilla(fila.idPresentacion(), fila.idArtista(),
                        fila.nombreArtista(), fila.generoMusical(), fila.fechaHoraInicio(), fila.fechaHoraFin(),
                        fila.estado()));
            }
        }
        return new GrillaResponse(festival.idFestival(), festival.nombre(), fecha, List.copyOf(porEscenario.values()));
    }
}
