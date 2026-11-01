package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.AccesoResponse;
import cl.grupo3.lab1.dto.ValidarAccesoRequest;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.AccesoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Control de acceso (/api/acceso/validar). Dueño del festival o admin.
 */
@RestController
@RequestMapping("/api/acceso")
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @SoloProductorOAdmin
    @PostMapping("/validar")
    public AccesoResponse validar(@Valid @RequestBody ValidarAccesoRequest request,
                                  @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return accesoService.validar(request, usuarioAutenticado);
    }
}
