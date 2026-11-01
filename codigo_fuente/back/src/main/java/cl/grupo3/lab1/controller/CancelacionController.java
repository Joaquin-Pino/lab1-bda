package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.CancelacionResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.CancelacionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cancelación de una presentación (/api/presentaciones/{id}/cancelar). Dueño del festival o admin.
 */
@RestController
@RequestMapping("/api/presentaciones")
public class CancelacionController {

    private final CancelacionService cancelacionService;

    public CancelacionController(CancelacionService cancelacionService) {
        this.cancelacionService = cancelacionService;
    }

    @SoloProductorOAdmin
    @PostMapping("/{id}/cancelar")
    public CancelacionResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return cancelacionService.cancelar(id, usuarioAutenticado);
    }
}
