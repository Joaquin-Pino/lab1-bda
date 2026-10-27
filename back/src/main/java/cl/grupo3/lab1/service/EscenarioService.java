package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.EscenarioRequest;
import cl.grupo3.lab1.dto.EscenarioResponse;
import cl.grupo3.lab1.exception.ConflictException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Escenario;
import cl.grupo3.lab1.repository.EscenarioRepository;
import org.springframework.stereotype.Service;

/**
 * CRUD de escenarios. Lectura para todos, escritura para productor y administrador
 */
@Service
public class EscenarioService {

    static final String MSG_NO_ENCONTRADO = "Escenario no encontrado";

    private final EscenarioRepository escenarioRepository;
    private final TransactionRunner transactionRunner;

    public EscenarioService(EscenarioRepository escenarioRepository, TransactionRunner transactionRunner) {
        this.escenarioRepository = escenarioRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<EscenarioResponse> listar(Long idRecinto) {
        return escenarioRepository.findAll(idRecinto).stream().map(EscenarioService::toResponse).toList();
    }

    public EscenarioResponse obtener(Long id) {
        return escenarioRepository.findById(id).map(EscenarioService::toResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public EscenarioResponse crear(EscenarioRequest request) {
        return transactionRunner.run(() -> {
            Long id = escenarioRepository.insert(request.idRecinto(), request.nombre(), request.capacidadMaxima());
            return obtener(id);
        });
    }

    public EscenarioResponse actualizar(Long id, EscenarioRequest request) {
        return transactionRunner.run(() -> {
            Escenario actual = escenarioRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
            // Moverlo de recinto dejaría sus presentaciones en un festival de otro recinto
            if (!actual.idRecinto().equals(request.idRecinto()) && escenarioRepository.tienePresentaciones(id)) {
                throw new ConflictException("No se puede cambiar el recinto de un escenario que tiene presentaciones");
            }
            escenarioRepository.update(id, request.idRecinto(), request.nombre(), request.capacidadMaxima());
            return obtener(id);
        });
    }

    public void eliminar(Long id) {
        if (escenarioRepository.delete(id) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADO);
        }
    }

    private static EscenarioResponse toResponse(Escenario escenario) {
        return new EscenarioResponse(escenario.idEscenario(), escenario.idRecinto(), escenario.nombreRecinto(),
                escenario.nombre(), escenario.capacidadMaxima());
    }
}
