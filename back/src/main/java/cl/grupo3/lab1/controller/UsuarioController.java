package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import cl.grupo3.lab1.dto.ActualizarUsuarioRequest;
import cl.grupo3.lab1.dto.CrearUsuarioRequest;
import cl.grupo3.lab1.dto.UsuarioResponse;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.security.SoloAdmin;
import cl.grupo3.lab1.service.UsuarioService;
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

/** Controlador del CRUD de usuarios (/api/usuarios), solo ADMINISTRADOR. Llama a UsuarioService; no contiene SQL ni lógica. */
@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/usuarios")
@SoloAdmin
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Lista los usuarios (filtro opcional por rol)")
    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String rol) {
        return usuarioService.listar(rol);
    }

    @Operation(summary = "Obtiene un usuario por id")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Crea un usuario")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest crearUsuarioRequest) {
        return usuarioService.crear(crearUsuarioRequest);
    }

    @Operation(summary = "Actualiza un usuario")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest actualizarUsuarioRequest) {
        return usuarioService.actualizar(id, actualizarUsuarioRequest);
    }

    @Operation(summary = "Elimina un usuario")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthUser usuarioAutenticado) {
        usuarioService.eliminar(id, usuarioAutenticado.idUsuario());
    }
}
