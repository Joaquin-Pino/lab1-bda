package cl.grupo3.lab1.controller;

import java.util.List;

import cl.grupo3.lab1.dto.OrdenResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloAsistente;
import cl.grupo3.lab1.service.OrdenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de órdenes (/api/ordenes). El asistente solo ve las suyas: la de otro usuario responde 404.
 */
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @SoloAsistente
    @GetMapping("/mias")
    public List<OrdenResponse> listarMias(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return ordenService.listarMias(usuarioAutenticado);
    }

    @Autenticado
    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return ordenService.obtener(id, usuarioAutenticado);
    }
}
