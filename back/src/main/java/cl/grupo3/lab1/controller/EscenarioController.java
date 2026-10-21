package cl.grupo3.lab1.controller;

import java.util.List;

import cl.grupo3.lab1.dto.EscenarioRequest;
import cl.grupo3.lab1.dto.EscenarioResponse;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.EscenarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/** CRUD de escenarios (/api/escenarios). Lectura: cualquier usuario autenticado. Escritura: productor o administrador. */
@RestController
@RequestMapping("/api/escenarios")
public class EscenarioController {

    private final EscenarioService escenarioService;

    public EscenarioController(EscenarioService escenarioService) {
        this.escenarioService = escenarioService;
    }

    @Autenticado
    @GetMapping
    public List<EscenarioResponse> listar(@RequestParam(name = "id_recinto", required = false) Long idRecinto) {
        return escenarioService.listar(idRecinto);
    }

    @Autenticado
    @GetMapping("/{id}")
    public EscenarioResponse obtener(@PathVariable Long id) {
        return escenarioService.obtener(id);
    }

    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EscenarioResponse crear(@Valid @RequestBody EscenarioRequest escenarioRequest) {
        return escenarioService.crear(escenarioRequest);
    }

    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public EscenarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody EscenarioRequest escenarioRequest) {
        return escenarioService.actualizar(id, escenarioRequest);
    }

    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        escenarioService.eliminar(id);
    }
}
