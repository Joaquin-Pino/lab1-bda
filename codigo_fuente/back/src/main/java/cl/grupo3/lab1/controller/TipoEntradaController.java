package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.grupo3.lab1.dto.ActualizarTipoEntradaRequest;
import cl.grupo3.lab1.dto.CrearTipoEntradaRequest;
import cl.grupo3.lab1.dto.TipoEntradaResponse;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.TipoEntradaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de tipos de entrada (/api/tipos-entrada)
 * Ver: cualquier usuario autenticado (servicio filtra por visibilidad del festival)
 * Crear, editar y borrar: productor dueño del festival o administrador
 */

@Tag(name = "Tipos de entrada")
@RestController
@RequestMapping("/api/tipos-entrada")
public class TipoEntradaController {

    private final TipoEntradaService tipoEntradaService;

    public TipoEntradaController(TipoEntradaService tipoEntradaService) {
        this.tipoEntradaService = tipoEntradaService;
    }

    @Operation(summary = "Lista los tipos de entrada de un festival (idFestival)")
    @Autenticado
    @GetMapping
    public List<TipoEntradaResponse> listar(@RequestParam Long idFestival,
                                            @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return tipoEntradaService.listarPorFestival(idFestival, usuarioAutenticado);
    }

    @Operation(summary = "Obtiene un tipo de entrada por id")
    @Autenticado
    @GetMapping("/{id}")
    public TipoEntradaResponse obtener(@PathVariable Long id,
                                       @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return tipoEntradaService.obtener(id, usuarioAutenticado);
    }

    @Operation(summary = "Crea un tipo de entrada")
    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoEntradaResponse crear(@Valid @RequestBody CrearTipoEntradaRequest request,
                                     @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return tipoEntradaService.crear(request, usuarioAutenticado);
    }

    @Operation(summary = "Actualiza un tipo de entrada")
    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public TipoEntradaResponse actualizar(@PathVariable Long id,
                                          @Valid @RequestBody ActualizarTipoEntradaRequest request,
                                          @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return tipoEntradaService.actualizar(id, request, usuarioAutenticado);
    }

    @Operation(summary = "Elimina un tipo de entrada")
    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
                         @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        tipoEntradaService.eliminar(id, usuarioAutenticado);
    }
}
