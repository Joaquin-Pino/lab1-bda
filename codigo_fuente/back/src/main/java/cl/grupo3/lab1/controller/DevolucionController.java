package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.DevolucionResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.DevolucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cancelación con devolución de crédito de festivales y tipos de entrada. Dueño del festival o admin.
 */
@Tag(name = "Cancelaciones")
@RestController
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @Operation(summary = "Cancela un festival, devuelve el dinero como crédito y notifica a los asistentes")
    @SoloProductorOAdmin
    @PostMapping("/api/festivales/{id}/cancelar")
    public DevolucionResponse cancelarFestival(@PathVariable Long id,
                                               @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return devolucionService.cancelarFestival(id, usuarioAutenticado);
    }

    @Operation(summary = "Cancela un tipo de entrada, devuelve el dinero como crédito y notifica a los asistentes")
    @SoloProductorOAdmin
    @PostMapping("/api/tipos-entrada/{id}/cancelar")
    public DevolucionResponse cancelarTipoEntrada(@PathVariable Long id,
                                                  @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return devolucionService.cancelarTipoEntrada(id, usuarioAutenticado);
    }
}
