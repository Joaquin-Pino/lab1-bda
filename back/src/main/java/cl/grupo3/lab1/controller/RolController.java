package cl.grupo3.lab1.controller;

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
@RestController
@RequestMapping("/api/roles")
@SoloAdmin
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<RolResponse> listar() {
        return rolService.listar();
    }

    @GetMapping("/{id}")
    public RolResponse obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crear(@Valid @RequestBody RolRequest rolRequest) {
        return rolService.crear(rolRequest.tipo());
    }

    @PutMapping("/{id}")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest rolRequest) {
        return rolService.actualizar(id, rolRequest.tipo());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
    }
}
