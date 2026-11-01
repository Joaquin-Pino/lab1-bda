package cl.grupo3.lab1.service;

import cl.grupo3.lab1.dto.CancelacionResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.repository.CancelacionRepository;
import cl.grupo3.lab1.repository.PresentacionRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Cancelación de presentaciones. La lógica (estado, notificaciones y crédito) ocurre en sp_cancelar_presentacion;
 * aquí solo se valida la propiedad y se arma la respuesta.
 */
@Service
public class CancelacionService {

    private final CancelacionRepository cancelacionRepository;
    private final PresentacionRepository presentacionRepository;
    private final TransactionRunner transactionRunner;

    public CancelacionService(CancelacionRepository cancelacionRepository, PresentacionRepository presentacionRepository,
                              TransactionRunner transactionRunner) {
        this.cancelacionRepository = cancelacionRepository;
        this.presentacionRepository = presentacionRepository;
        this.transactionRunner = transactionRunner;
    }

    /**
     * Solo el productor dueño del festival o el administrador; si no existe o es ajena, 404.
     * Una presentación que no está PROGRAMADA la rechaza el procedimiento (409).
     */
    public CancelacionResponse cancelar(Long idPresentacion, AuthUser usuario) {
        return transactionRunner.run(() -> {
            presentacionRepository.findById(idPresentacion)
                    .filter(p -> usuario.puedeOperarSobre(p.idUsuarioProductor()))
                    .orElseThrow(() -> new NotFoundException(PresentacionService.MSG_NO_ENCONTRADA));
            CancelacionRepository.Resultado resultado = cancelacionRepository.cancelar(idPresentacion);
            return new CancelacionResponse(idPresentacion, "CANCELADA", resultado.notificados(),
                    resultado.creditoTotal(), cancelacionRepository.findAsistentesNotificados(idPresentacion));
        });
    }
}
