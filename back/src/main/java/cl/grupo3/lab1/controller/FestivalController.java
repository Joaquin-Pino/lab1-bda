package cl.grupo3.lab1.controller;

import java.util.List;

import java.time.LocalDate;
import cl.grupo3.lab1.dto.GrillaResponse;
import cl.grupo3.lab1.service.GrillaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import cl.grupo3.lab1.dto.FestivalRequest;
import cl.grupo3.lab1.dto.FestivalResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.FestivalService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de festivales (/api/festivales).
 * Ver: cualquier usuario autenticado (el servicio filtra qué festivales ve cada uno).
 * Crear, editar y borrar: productor o administrador (el servicio comprueba que sea el dueño).
 */
@RestController
@RequestMapping("/api/festivales")
public class FestivalController {

    private final FestivalService festivalService;
    private final GrillaService grillaService;

    public FestivalController(FestivalService festivalService, GrillaService grillaService) {
        this.festivalService = festivalService;
        this.grillaService = grillaService;
    }

    @Autenticado
    @GetMapping
    public List<FestivalResponse> listar(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return festivalService.listar(usuarioAutenticado);
    }

    @Autenticado
    @GetMapping("/{id}")
    public FestivalResponse obtener(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return festivalService.obtener(id, usuarioAutenticado);
    }

    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FestivalResponse crear(@Valid @RequestBody FestivalRequest festivalRequest,
                                  @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return festivalService.crear(festivalRequest, usuarioAutenticado);
    }

    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public FestivalResponse actualizar(@PathVariable Long id, @Valid @RequestBody FestivalRequest festivalRequest,
                                       @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return festivalService.actualizar(id, festivalRequest, usuarioAutenticado);
    }

    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        festivalService.eliminar(id, usuarioAutenticado);
    }

    /** Grilla del día. */
    @Autenticado
    @GetMapping("/{id}/grilla")
    public GrillaResponse grilla(@PathVariable Long id,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                 @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return grillaService.obtener(id, fecha, usuarioAutenticado);
    }
}
