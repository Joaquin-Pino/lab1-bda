package cl.grupo3.lab1.controller;

import java.util.List;

import cl.grupo3.lab1.dto.TicketDetalleResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloAsistente;
import cl.grupo3.lab1.service.OrdenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tickets del asistente autenticado (/api/tickets/mios). Solo ve los suyos.
 */
@RestController
@RequestMapping("/api/tickets")
public class MisTicketsController {

    private final OrdenService ordenService;

    public MisTicketsController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @SoloAsistente
    @GetMapping("/mios")
    public List<TicketDetalleResponse> listarMios(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return ordenService.listarTicketsMios(usuarioAutenticado);
    }
}
