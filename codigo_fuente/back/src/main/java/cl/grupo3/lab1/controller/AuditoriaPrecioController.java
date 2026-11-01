package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import cl.grupo3.lab1.dto.AuditoriaPrecioResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.AuditoriaPrecioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auditoría de precios de un tipo de entrada (/api/tipos-entrada/{id}/auditoria-precios).
 * Solo el productor dueño del festival o el administrador (el servicio comprueba el dueño).
 */
@Tag(name = "Auditoría de precios")
@RestController
@RequestMapping("/api/tipos-entrada")
public class AuditoriaPrecioController {

    private final AuditoriaPrecioService auditoriaPrecioService;

    public AuditoriaPrecioController(AuditoriaPrecioService auditoriaPrecioService) {
        this.auditoriaPrecioService = auditoriaPrecioService;
    }

    @Operation(summary = "Historial de cambios de precio de un tipo de entrada")
    @SoloProductorOAdmin
    @GetMapping("/{id}/auditoria-precios")
    public List<AuditoriaPrecioResponse> listar(@PathVariable Long id,
                                                @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return auditoriaPrecioService.listarPorTipoEntrada(id, usuarioAutenticado);
    }
}
