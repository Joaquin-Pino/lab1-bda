package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.SaldoCreditoResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.service.NotificacionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Saldo de crédito de compensación del usuario autenticado (/api/creditos/saldo).
 */
@RestController
@RequestMapping("/api/creditos")
@Autenticado
public class CreditoController {

    private final NotificacionService notificacionService;

    public CreditoController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/saldo")
    public SaldoCreditoResponse saldo(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return notificacionService.saldo(usuarioAutenticado);
    }
}
