package cl.grupo3.lab1.service;

import java.util.List;
import java.util.Set;

import cl.grupo3.lab1.dto.ActualizarPresentacionRequest;
import cl.grupo3.lab1.dto.CrearPresentacionRequest;
import cl.grupo3.lab1.dto.PresentacionResponse;
import cl.grupo3.lab1.exception.ConflictException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Festival;
import cl.grupo3.lab1.model.Presentacion;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.repository.PresentacionRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Line-up de un festival.
 * - Ver: con la misma regla de visibilidad que el festival.
 * - Crear, editar y borrar: solo el dueño del festival o el administrador (si no, 404).
 * Los traslapes los valida Trigger 1.
 * La cancelación no se hace aquí: es el procedimiento de la Persona 4, que además notifica y compensa.
 */
@Service
public class PresentacionService {

    static final String MSG_NO_ENCONTRADA = "Presentación no encontrada";
    private static final Set<String> FESTIVAL_CERRADO = Set.of("CANCELADO", "FINALIZADO");

    private final PresentacionRepository presentacionRepository;
    private final FestivalRepository festivalRepository;
    private final TransactionRunner transactionRunner;

    public PresentacionService(PresentacionRepository presentacionRepository, FestivalRepository festivalRepository,
                               TransactionRunner transactionRunner) {
        this.presentacionRepository = presentacionRepository;
        this.festivalRepository = festivalRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<PresentacionResponse> listarPorFestival(Long idFestival, AuthUser usuario) {
        festivalRepository.findVisibleById(idFestival, usuario.esAdmin(), usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(FestivalService.MSG_NO_ENCONTRADO));
        return presentacionRepository.findByFestival(idFestival).stream()
                .map(PresentacionService::toResponse).toList();
    }

    public PresentacionResponse obtener(Long id, AuthUser usuario) {
        Presentacion presentacion = presentacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
        // Si el usuario no puede ver el festival, tampoco ve sus presentaciones
        festivalRepository.findVisibleById(presentacion.idFestival(), usuario.esAdmin(), usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
        return toResponse(presentacion);
    }

    public PresentacionResponse crear(CrearPresentacionRequest request, AuthUser usuario) {
        return transactionRunner.run(() -> {
            Festival festival = festivalRepository.findById(request.idFestival())
                    .filter(f -> usuario.puedeOperarSobre(f.idUsuarioProductor()))
                    .orElseThrow(() -> new NotFoundException(FestivalService.MSG_NO_ENCONTRADO));
            if (FESTIVAL_CERRADO.contains(festival.estado())) {
                throw new ConflictException("No se puede modificar el line-up de un festival cancelado o finalizado");
            }
            Long id = presentacionRepository.insert(request.idFestival(), request.idEscenario(), request.idArtista(),
                    request.fechaHoraInicio(), request.fechaHoraFin());
            return obtener(id, usuario);
        });
    }

    public PresentacionResponse actualizar(Long id, ActualizarPresentacionRequest request, AuthUser usuario) {
        return transactionRunner.run(() -> {
            Presentacion actual = buscarPropia(id, usuario);
            if ("CANCELADA".equals(actual.estado())) {
                throw new ConflictException("No se puede modificar una presentación cancelada");
            }
            presentacionRepository.update(id, request.idEscenario(), request.idArtista(),
                    request.fechaHoraInicio(), request.fechaHoraFin());
            return obtener(id, usuario);
        });
    }

    public void eliminar(Long id, AuthUser usuario) {
        transactionRunner.run(() -> {
            buscarPropia(id, usuario);
            presentacionRepository.delete(id);
        });
    }

    /** Presentación de un festival que el usuario puede modificar. */
    private Presentacion buscarPropia(Long id, AuthUser usuario) {
        return presentacionRepository.findById(id)
                .filter(p -> usuario.puedeOperarSobre(p.idUsuarioProductor()))
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
    }

    private static PresentacionResponse toResponse(Presentacion p) {
        return new PresentacionResponse(p.idPresentacion(), p.idFestival(), p.idEscenario(), p.nombreEscenario(),
                p.idArtista(), p.nombreArtista(), p.fechaHoraInicio(), p.fechaHoraFin(), p.dia(), p.estado());
    }
}
