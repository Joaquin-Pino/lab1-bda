package cl.grupo3.lab1.controller;

import java.util.List;

import cl.grupo3.lab1.dto.NoLeidasResponse;
import cl.grupo3.lab1.dto.NotificacionResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notificaciones del usuario autenticado (/api/notificaciones). Solo ve y modifica las suyas.
 */
@RestController
@RequestMapping("/api/notificaciones")
@Autenticado
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /** Ejemplo: GET /api/notificaciones?solo_no_leidas=true */
    @GetMapping
    public List<NotificacionResponse> listar(
            @RequestParam(name = "solo_no_leidas", defaultValue = "false") boolean soloNoLeidas,
            @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return notificacionService.listar(usuarioAutenticado, soloNoLeidas);
    }

    /** Contador de la campana. */
    @GetMapping("/no-leidas")
    public NoLeidasResponse contarNoLeidas(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return notificacionService.contarNoLeidas(usuarioAutenticado);
    }

    @GetMapping("/{id}")
    public NotificacionResponse obtener(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return notificacionService.obtener(id, usuarioAutenticado);
    }

    @PutMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id,
                                            @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return notificacionService.marcarLeida(id, usuarioAutenticado);
    }

    @PutMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasLeidas(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        notificacionService.marcarTodasLeidas(usuarioAutenticado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        notificacionService.eliminar(id, usuarioAutenticado);
    }
}
