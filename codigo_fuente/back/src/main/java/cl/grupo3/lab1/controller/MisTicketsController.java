package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tickets")
@RestController
@RequestMapping("/api/tickets")
public class MisTicketsController {

    private final OrdenService ordenService;

    public MisTicketsController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @Operation(summary = "Lista los tickets del asistente autenticado")
    @SoloAsistente
    @GetMapping("/mios")
    public List<TicketDetalleResponse> listarMios(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return ordenService.listarTicketsMios(usuarioAutenticado);
    }
}
