package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.NoLeidasResponse;
import cl.grupo3.lab1.dto.NotificacionResponse;
import cl.grupo3.lab1.dto.SaldoCreditoResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.repository.NotificacionRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Notificaciones y saldo de crédito. Cada usuario solo accede a lo suyo (también el admin):
 * una notificación ajena o inexistente responde 404.
 */
@Service
public class NotificacionService {

    static final String MSG_NO_ENCONTRADA = "Notificación no encontrada";

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<NotificacionResponse> listar(AuthUser usuario, boolean soloNoLeidas) {
        return notificacionRepository.findByUsuario(usuario.idUsuario(), soloNoLeidas);
    }

    public NotificacionResponse obtener(Long id, AuthUser usuario) {
        return notificacionRepository.findByIdAndUsuario(id, usuario.idUsuario())
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
    }

    public NoLeidasResponse contarNoLeidas(AuthUser usuario) {
        return new NoLeidasResponse(notificacionRepository.contarNoLeidas(usuario.idUsuario()));
    }

    /** Idempotente: marcar una ya leída también responde con la notificación. */
    public NotificacionResponse marcarLeida(Long id, AuthUser usuario) {
        if (notificacionRepository.marcarLeida(id, usuario.idUsuario()) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADA);
        }
        return obtener(id, usuario);
    }

    public void marcarTodasLeidas(AuthUser usuario) {
        notificacionRepository.marcarTodasLeidas(usuario.idUsuario());
    }

    public void eliminar(Long id, AuthUser usuario) {
        if (notificacionRepository.delete(id, usuario.idUsuario()) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADA);
        }
    }

    public SaldoCreditoResponse saldo(AuthUser usuario) {
        return new SaldoCreditoResponse(notificacionRepository.findSaldo(usuario.idUsuario()));
    }
}
