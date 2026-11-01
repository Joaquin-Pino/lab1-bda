package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Recintos")
@RestController
@RequestMapping("/api/recintos")
public class RecintoController {

    private final RecintoService recintoService;

    public RecintoController(RecintoService recintoService) {
        this.recintoService = recintoService;
    }

    @Operation(summary = "Lista los recintos")
    @Autenticado
    @GetMapping
    public List<RecintoResponse> listar() {
        return recintoService.listar();
    }

    @Operation(summary = "Obtiene un recinto por id")
    @Autenticado
    @GetMapping("/{id}")
    public RecintoResponse obtener(@PathVariable Long id) {
        return recintoService.obtener(id);
    }

    @Operation(summary = "Crea un recinto")
    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecintoResponse crear(@Valid @RequestBody RecintoRequest recintoRequest) {
        return recintoService.crear(recintoRequest);
    }

    @Operation(summary = "Actualiza un recinto")
    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public RecintoResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecintoRequest recintoRequest) {
        return recintoService.actualizar(id, recintoRequest);
    }

    @Operation(summary = "Elimina un recinto")
    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        recintoService.eliminar(id);
    }
}
