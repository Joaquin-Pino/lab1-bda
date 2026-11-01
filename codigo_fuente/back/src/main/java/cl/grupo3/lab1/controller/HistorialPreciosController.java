package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.HistorialPrecioResponse;
import cl.grupo3.lab1.security.SoloAdmin;
import cl.grupo3.lab1.service.AuditoriaPrecioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Historial global de cambios de precio (/api/auditoria-precios). Solo el administrador.
 * Para el historial de una sola entrada ver AuditoriaPrecioController.
 */
@Tag(name = "Auditoría de precios")
@RestController
@RequestMapping("/api/auditoria-precios")
public class HistorialPreciosController {

    private final AuditoriaPrecioService auditoriaPrecioService;

    public HistorialPreciosController(AuditoriaPrecioService auditoriaPrecioService) {
        this.auditoriaPrecioService = auditoriaPrecioService;
    }

    @Operation(summary = "Historial de cambios de precio de todos los festivales (opcionalmente de uno solo)")
    @SoloAdmin
    @GetMapping
    public List<HistorialPrecioResponse> listar(@RequestParam(required = false) Long idFestival) {
        return auditoriaPrecioService.listarTodos(idFestival);
    }
}
