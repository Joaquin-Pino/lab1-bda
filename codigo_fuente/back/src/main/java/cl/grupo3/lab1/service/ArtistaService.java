package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.ArtistaRequest;
import cl.grupo3.lab1.dto.ArtistaResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Artista;
import cl.grupo3.lab1.repository.ArtistaRepository;
import org.springframework.stereotype.Service;

/**
 * CRUD de artistas. Lectura para todos, escritura para productor y administrador.
 */
@Service
public class ArtistaService {

    static final String MSG_NO_ENCONTRADO = "Artista no encontrado";

    private final ArtistaRepository artistaRepository;
    private final TransactionRunner transactionRunner;

    public ArtistaService(ArtistaRepository artistaRepository, TransactionRunner transactionRunner) {
        this.artistaRepository = artistaRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<ArtistaResponse> listar() {
        return artistaRepository.findAll().stream().map(ArtistaService::toResponse).toList();
    }

    public ArtistaResponse obtener(Long id) {
        return artistaRepository.findById(id).map(ArtistaService::toResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public ArtistaResponse crear(ArtistaRequest request) {
        return transactionRunner.run(() -> {
            Long id = artistaRepository.insert(request.nombre(), request.generoMusical());
            return obtener(id);
        });
    }

    public ArtistaResponse actualizar(Long id, ArtistaRequest request) {
        return transactionRunner.run(() -> {
            if (artistaRepository.update(id, request.nombre(), request.generoMusical()) == 0) {
                throw new NotFoundException(MSG_NO_ENCONTRADO);
            }
            return obtener(id);
        });
    }

    public void eliminar(Long id) {
        if (artistaRepository.delete(id) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADO);
        }
    }

    private static ArtistaResponse toResponse(Artista artista) {
        return new ArtistaResponse(artista.idArtista(), artista.nombre(), artista.generoMusical());
    }
}
