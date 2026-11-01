package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import cl.grupo3.lab1.dto.RolRequest;
import cl.grupo3.lab1.dto.RolResponse;
import cl.grupo3.lab1.security.SoloAdmin;
import cl.grupo3.lab1.service.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controlador del CRUD de roles (/api/roles), solo ADMINISTRADOR. Llama a RolService; no contiene SQL ni lógica. */
@Tag(name = "Roles")
@RestController
@RequestMapping("/api/roles")
@SoloAdmin
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @Operation(summary = "Lista los roles")
    @GetMapping
    public List<RolResponse> listar() {
        return rolService.listar();
    }

    @Operation(summary = "Obtiene un rol por id")
    @GetMapping("/{id}")
    public RolResponse obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }

    @Operation(summary = "Crea un rol")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crear(@Valid @RequestBody RolRequest rolRequest) {
        return rolService.crear(rolRequest.tipo());
    }

    @Operation(summary = "Actualiza un rol")
    @PutMapping("/{id}")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest rolRequest) {
        return rolService.actualizar(id, rolRequest.tipo());
    }

    @Operation(summary = "Elimina un rol")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
    }
}
