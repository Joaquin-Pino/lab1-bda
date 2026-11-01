package cl.grupo3.lab1.service;

import cl.grupo3.lab1.dto.ActualizarTipoEntradaRequest;
import cl.grupo3.lab1.dto.CrearTipoEntradaRequest;
import cl.grupo3.lab1.dto.TipoEntradaResponse;
import cl.grupo3.lab1.exception.ConflictException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Festival;
import cl.grupo3.lab1.model.TipoEntrada;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.repository.TipoEntradaRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CRUD de tipos de entradas
 */
@Service
public class TipoEntradaService {

    static final String MSG_NO_ENCONTRADO = "Tipo de entrada no encontrado";
    static final String MSG_FESTIVAL_NO_ENCONTRADO = "Festival no encontrado";

    private final TipoEntradaRepository tipoEntradaRepository;
    private final FestivalRepository festivalRepository;
    private final TransactionRunner transactionRunner;

    public TipoEntradaService(TipoEntradaRepository tipoEntradaRepository, FestivalRepository festivalRepository,
                              TransactionRunner transactionRunner) {
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.festivalRepository = festivalRepository;
        this.transactionRunner = transactionRunner;
    }

    public List<TipoEntradaResponse> listarPorFestival(Long idFestival, AuthUser usuario) {
        festivalRepository.findVisibleById(idFestival, usuario.esAdmin(), usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(MSG_FESTIVAL_NO_ENCONTRADO));
        return tipoEntradaRepository.findByIdFestival(idFestival).stream()
                .map(TipoEntradaService::toResponse).toList();
    }

    public TipoEntradaResponse obtener(Long id, AuthUser usuario) {
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));

        festivalRepository.findVisibleById(tipoEntrada.idFestival(), usuario.esAdmin(), usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));

        return toResponse(tipoEntrada);
    }

    public TipoEntradaResponse crear(CrearTipoEntradaRequest request, AuthUser usuario) {
        return transactionRunner.runAs(usuario.idUsuario(), () -> {
            Festival festival = buscarFestivalPropio(request.idFestival(), usuario);

            if (request.fecha() != null && (request.fecha().isBefore(festival.fechaInicio()) ||
                    request.fecha().isAfter(festival.fechaFin()))) {
                throw new ConflictException("La fecha de la entrada debe estar entre las fechas del festival");
            }

            Long id = tipoEntradaRepository.insert(request.idFestival(), request.nombreTipo(), request.precioActual(), request.aforoTotal(),
                    request.fecha());
            return obtener(id, usuario);
        });
    }

    public TipoEntradaResponse actualizar(Long id, ActualizarTipoEntradaRequest request, AuthUser usuario) {
        return transactionRunner.runAs(usuario.idUsuario(), () -> {
            TipoEntrada actual = tipoEntradaRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
            Festival festival = buscarFestivalPropio(actual.idFestival(), usuario);

            if (request.fecha() != null && (request.fecha().isBefore(festival.fechaInicio()) ||
                request.fecha().isAfter(festival.fechaFin()))) {
                throw new ConflictException("La fecha de la entrada debe estar entre las fechas del festival");
            }

            tipoEntradaRepository.update(id, request.nombreTipo(), request.precioActual(), request.aforoTotal(),
                                         request.fecha());

            return toResponse(tipoEntradaRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO)));
        });
    }

    public void eliminar(Long id, AuthUser usuario) {
        transactionRunner.run(() -> {
            TipoEntrada actual = tipoEntradaRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
            buscarFestivalPropio(actual.idFestival(), usuario);
            tipoEntradaRepository.delete(id);
        });
    }

    private Festival buscarFestivalPropio(Long idFestival, AuthUser usuario) {
        return festivalRepository.findById(idFestival)
                .filter(f -> usuario.puedeOperarSobre(f.idUsuarioProductor()))
                .orElseThrow(() -> new NotFoundException(MSG_FESTIVAL_NO_ENCONTRADO));
    }

    private static TipoEntradaResponse toResponse(TipoEntrada tipoEntrada) {
        return new TipoEntradaResponse(tipoEntrada.idTipoEntrada(), tipoEntrada.idFestival(), tipoEntrada.nombreTipo(),
                tipoEntrada.precioActual(), tipoEntrada.aforoTotal(), tipoEntrada.aforoDisponible(), tipoEntrada.fecha());
    }
}
