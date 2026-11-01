package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.grupo3.lab1.dto.CompraRequest;
import cl.grupo3.lab1.dto.OrdenResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloAsistente;
import cl.grupo3.lab1.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compra de entradas (/api/compras). Solo el asistente compra, siempre a su nombre.
 */
@Tag(name = "Compras")
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final OrdenService ordenService;

    public CompraController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @Operation(summary = "Compra entradas (llama a sp_comprar_entradas)")
    @SoloAsistente
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse comprar(@Valid @RequestBody CompraRequest compraRequest,
                                 @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return ordenService.comprar(compraRequest, usuarioAutenticado);
    }
}
