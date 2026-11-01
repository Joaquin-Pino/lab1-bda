package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.AuditoriaPrecioResponse;
import cl.grupo3.lab1.dto.HistorialPrecioResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.AuditoriaPrecio;
import cl.grupo3.lab1.model.TipoEntrada;
import cl.grupo3.lab1.repository.AuditoriaPrecioRepository;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.repository.TipoEntradaRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Consulta de la auditoría de precios. La registra el trigger al cambiar tipo_entrada.precio_actual.
 * Solo la ve el productor dueño del festival o el administrador.
 */
@Service
public class AuditoriaPrecioService {

    static final String MSG_NO_ENCONTRADO = "Tipo de entrada no encontrado";

    private final AuditoriaPrecioRepository auditoriaPrecioRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final FestivalRepository festivalRepository;

    public AuditoriaPrecioService(AuditoriaPrecioRepository auditoriaPrecioRepository,
                                  TipoEntradaRepository tipoEntradaRepository,
                                  FestivalRepository festivalRepository) {
        this.auditoriaPrecioRepository = auditoriaPrecioRepository;
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.festivalRepository = festivalRepository;
    }

    /** Si el tipo de entrada no existe o su festival no es del usuario, 404 en ambos casos. */
    public List<AuditoriaPrecioResponse> listarPorTipoEntrada(Long idTipoEntrada, AuthUser usuario) {
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(idTipoEntrada)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
        festivalRepository.findById(tipoEntrada.idFestival())
                .filter(festival -> usuario.puedeOperarSobre(festival.idUsuarioProductor()))
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));

        return auditoriaPrecioRepository.findByTipoEntrada(idTipoEntrada).stream()
                .map(AuditoriaPrecioService::toResponse).toList();
    }

    /** Historial de todos los festivales (o de uno): solo lo llama el controlador del administrador. */
    public List<HistorialPrecioResponse> listarTodos(Long idFestival) {
        return auditoriaPrecioRepository.findTodos(idFestival).stream()
                .map(h -> new HistorialPrecioResponse(h.idAuditoria(), h.idTipoEntrada(), h.nombreTipo(),
                        h.idFestival(), h.nombreFestival(), h.precioAnterior(), h.precioNuevo(),
                        h.idUsuarioModificador(), h.nombreUsuario(), h.fechaModificacion()))
                .toList();
    }

    private static AuditoriaPrecioResponse toResponse(AuditoriaPrecio auditoria) {
        return new AuditoriaPrecioResponse(auditoria.idAuditoria(), auditoria.idTipoEntrada(),
                auditoria.precioAnterior(), auditoria.precioNuevo(), auditoria.idUsuarioModificador(),
                auditoria.nombreUsuario(), auditoria.fechaModificacion());
    }
}
