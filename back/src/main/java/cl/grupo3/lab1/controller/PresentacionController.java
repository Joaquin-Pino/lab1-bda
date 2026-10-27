package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import cl.grupo3.lab1.dto.ActualizarPresentacionRequest;
import cl.grupo3.lab1.dto.CrearPresentacionRequest;
import cl.grupo3.lab1.dto.PresentacionResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.PresentacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Line-up (/api/presentaciones).
 * Ver: cualquier usuario autenticado que pueda ver el festival. Crear, editar y borrar: dueño del festival o admin.
 */
@Tag(name = "Presentaciones (line-up)")
@RestController
@RequestMapping("/api/presentaciones")
public class PresentacionController {

    private final PresentacionService presentacionService;

    public PresentacionController(PresentacionService presentacionService) {
        this.presentacionService = presentacionService;
    }

    /** Ejemplo: GET /api/presentaciones?id_festival=1 */
    @Operation(summary = "Lista las presentaciones de un festival (id_festival)")
    @Autenticado
    @GetMapping
    public List<PresentacionResponse> listar(@RequestParam(name = "id_festival") Long idFestival,
                                             @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return presentacionService.listarPorFestival(idFestival, usuarioAutenticado);
    }

    @Operation(summary = "Obtiene una presentación por id")
    @Autenticado
    @GetMapping("/{id}")
    public PresentacionResponse obtener(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return presentacionService.obtener(id, usuarioAutenticado);
    }

    @Operation(summary = "Crea una presentación")
    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresentacionResponse crear(@Valid @RequestBody CrearPresentacionRequest presentacionRequest,
                                      @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return presentacionService.crear(presentacionRequest, usuarioAutenticado);
    }

    @Operation(summary = "Actualiza una presentación")
    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public PresentacionResponse actualizar(@PathVariable Long id,
                                           @Valid @RequestBody ActualizarPresentacionRequest presentacionRequest,
                                           @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return presentacionService.actualizar(id, presentacionRequest, usuarioAutenticado);
    }

    @Operation(summary = "Elimina una presentación")
    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        presentacionService.eliminar(id, usuarioAutenticado);
    }
}
