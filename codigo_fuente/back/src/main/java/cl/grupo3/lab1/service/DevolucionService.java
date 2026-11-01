package cl.grupo3.lab1.service;

import cl.grupo3.lab1.dto.DevolucionResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.TipoEntrada;
import cl.grupo3.lab1.repository.DevolucionRepository;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.repository.TipoEntradaRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Cancelación de festivales y tipos de entrada con devolución de crédito. La lógica ocurre en
 * sp_cancelar_festival / sp_cancelar_tipo_entrada; aquí solo se valida la propiedad y se arma la respuesta.
 */
@Service
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final FestivalRepository festivalRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final TransactionRunner transactionRunner;

    public DevolucionService(DevolucionRepository devolucionRepository, FestivalRepository festivalRepository,
                             TipoEntradaRepository tipoEntradaRepository, TransactionRunner transactionRunner) {
        this.devolucionRepository = devolucionRepository;
        this.festivalRepository = festivalRepository;
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.transactionRunner = transactionRunner;
    }

    /** Solo el dueño o el administrador; si no existe o es ajeno, 404. Ya cancelado o finalizado: 409. */
    public DevolucionResponse cancelarFestival(Long idFestival, AuthUser usuario) {
        return transactionRunner.run(() -> {
            festivalRepository.findById(idFestival)
                    .filter(f -> usuario.puedeOperarSobre(f.idUsuarioProductor()))
                    .orElseThrow(() -> new NotFoundException(TipoEntradaService.MSG_FESTIVAL_NO_ENCONTRADO));
            DevolucionRepository.Resultado resultado = devolucionRepository.cancelarFestival(idFestival);
            return respuesta(idFestival, "CANCELADO", resultado);
        });
    }

    /** Solo el dueño del festival o el administrador; si no existe o es ajeno, 404. */
    public DevolucionResponse cancelarTipoEntrada(Long idTipoEntrada, AuthUser usuario) {
        return transactionRunner.run(() -> {
            TipoEntrada tipo = tipoEntradaRepository.findById(idTipoEntrada)
                    .orElseThrow(() -> new NotFoundException(TipoEntradaService.MSG_NO_ENCONTRADO));
            festivalRepository.findById(tipo.idFestival())
                    .filter(f -> usuario.puedeOperarSobre(f.idUsuarioProductor()))
                    .orElseThrow(() -> new NotFoundException(TipoEntradaService.MSG_NO_ENCONTRADO));
            DevolucionRepository.Resultado resultado = devolucionRepository.cancelarTipoEntrada(idTipoEntrada);
            return respuesta(idTipoEntrada, "CANCELADO", resultado);
        });
    }

    private DevolucionResponse respuesta(Long id, String estado, DevolucionRepository.Resultado resultado) {
        return new DevolucionResponse(id, estado, resultado.notificados(), resultado.creditoTotal(),
                devolucionRepository.findAsistentesAfectados());
    }
}
