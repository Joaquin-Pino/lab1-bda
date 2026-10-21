package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.RecintoRequest;
import cl.grupo3.lab1.dto.RecintoResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Recinto;
import cl.grupo3.lab1.repository.RecintoRepository;
import org.springframework.stereotype.Service;

/**
 * CRUD de recintos. Lectura para todos, escritura para productor y administrador.
 */
@Service
public class RecintoService {

    static final String MSG_NO_ENCONTRADO = "Recinto no encontrado";

    private final RecintoRepository recintoRepository;
    private final TransactionRunner transactionRunner;

    public RecintoService(RecintoRepository recintoRepository, TransactionRunner transactionRunner) {
        this.recintoRepository = recintoRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<RecintoResponse> listar() {
        return recintoRepository.findAll().stream().map(RecintoService::toResponse).toList();
    }

    public RecintoResponse obtener(Long id) {
        return recintoRepository.findById(id).map(RecintoService::toResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public RecintoResponse crear(RecintoRequest request) {
        return transactionRunner.run(() -> {
            Long id = recintoRepository.insert(request.nombre(), request.direccion(), request.aforoMaximo());
            return obtener(id);
        });
    }

    public RecintoResponse actualizar(Long id, RecintoRequest request) {
        return transactionRunner.run(() -> {
            if (recintoRepository.update(id, request.nombre(), request.direccion(), request.aforoMaximo()) == 0) {
                throw new NotFoundException(MSG_NO_ENCONTRADO);
            }
            return obtener(id);
        });
    }

    public void eliminar(Long id) {
        if (recintoRepository.delete(id) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADO);
        }
    }

    private static RecintoResponse toResponse(Recinto recinto) {
        return new RecintoResponse(recinto.idRecinto(), recinto.nombre(), recinto.direccion(), recinto.aforoMaximo());
    }
}
