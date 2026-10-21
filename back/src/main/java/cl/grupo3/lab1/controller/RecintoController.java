package cl.grupo3.lab1.controller;

import java.util.List;

import cl.grupo3.lab1.dto.RecintoRequest;
import cl.grupo3.lab1.dto.RecintoResponse;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.RecintoService;
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

/** CRUD de recintos (/api/recintos). Lectura: cualquier usuario autenticado. Escritura: productor o administrador. */
@RestController
@RequestMapping("/api/recintos")
public class RecintoController {

    private final RecintoService recintoService;

    public RecintoController(RecintoService recintoService) {
        this.recintoService = recintoService;
    }

    @Autenticado
    @GetMapping
    public List<RecintoResponse> listar() {
        return recintoService.listar();
    }

    @Autenticado
    @GetMapping("/{id}")
    public RecintoResponse obtener(@PathVariable Long id) {
        return recintoService.obtener(id);
    }

    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecintoResponse crear(@Valid @RequestBody RecintoRequest recintoRequest) {
        return recintoService.crear(recintoRequest);
    }

    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public RecintoResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecintoRequest recintoRequest) {
        return recintoService.actualizar(id, recintoRequest);
    }

    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        recintoService.eliminar(id);
    }
}
