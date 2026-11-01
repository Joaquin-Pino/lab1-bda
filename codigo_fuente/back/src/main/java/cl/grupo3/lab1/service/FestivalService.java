package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.FestivalRequest;
import cl.grupo3.lab1.dto.FestivalResponse;
import cl.grupo3.lab1.exception.ConflictException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Festival;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * CRUD de festivales.
 */
@Service
public class FestivalService {

    static final String MSG_NO_ENCONTRADO = "Festival no encontrado";
    private static final String ESTADO_INICIAL = "BORRADOR";

    private final FestivalRepository festivalRepository;
    private final TransactionRunner transactionRunner;

    public FestivalService(FestivalRepository festivalRepository, TransactionRunner transactionRunner) {
        this.festivalRepository = festivalRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<FestivalResponse> listar(AuthUser usuario) {
        return festivalRepository.findVisibles(usuario.esAdmin(), usuario.idUsuario()).stream()
                .map(FestivalService::toResponse).toList();
    }

    public FestivalResponse obtener(Long id, AuthUser usuario) {
        return festivalRepository.findVisibleById(id, usuario.esAdmin(), usuario.idUsuario())
                .map(FestivalService::toResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public FestivalResponse crear(FestivalRequest request, AuthUser usuario) {
        String estado = request.estado() == null ? ESTADO_INICIAL : request.estado();
        return transactionRunner.run(() -> {
            Long id = festivalRepository.insert(usuario.idUsuario(), request.idRecinto(), request.nombre(),
                    request.fechaInicio(), request.fechaFin(), estado);
            return obtener(id, usuario);
        });
    }

    public FestivalResponse actualizar(Long id, FestivalRequest request, AuthUser usuario) {
        return transactionRunner.run(() -> {
            Festival actual = buscarPropio(id, usuario);
            String estado = request.estado() == null ? actual.estado() : request.estado();
            festivalRepository.update(id, request.idRecinto(), request.nombre(), request.fechaInicio(),
                    request.fechaFin(), estado);
            // Si cambiaron las fechas o el recinto, el line-up existente debe seguir calzando.
            // Al lanzar la excepción dentro de la transacción, el UPDATE se deshace.
            int incoherentes = festivalRepository.contarPresentacionesIncoherentes(id);
            if (incoherentes > 0) {
                throw new ConflictException("No se puede guardar: " + incoherentes
                        + " presentación(es) quedarían fuera de las fechas o del recinto del festival");
            }
            return obtener(id, usuario);
        });
    }

    public void eliminar(Long id, AuthUser usuario) {
        transactionRunner.run(() -> {
            buscarPropio(id, usuario);
            festivalRepository.delete(id);
        });
    }

    /** Festival que el usuario puede modificar (dueño o administrador) */
    private Festival buscarPropio(Long id, AuthUser usuario) {
        return festivalRepository.findById(id)
                .filter(festival -> usuario.puedeOperarSobre(festival.idUsuarioProductor()))
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    private static FestivalResponse toResponse(Festival festival) {
        return new FestivalResponse(festival.idFestival(), festival.idUsuarioProductor(), festival.idRecinto(),
                festival.nombreRecinto(), festival.nombre(), festival.fechaInicio(), festival.fechaFin(),
                festival.estado());
    }
}
