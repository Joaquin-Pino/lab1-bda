package cl.grupo3.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import cl.grupo3.lab1.dto.ArtistaRequest;
import cl.grupo3.lab1.dto.ArtistaResponse;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.SoloProductorOAdmin;
import cl.grupo3.lab1.service.ArtistaService;
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

/** CRUD de artistas (/api/artistas). Lectura: cualquier usuario autenticado. Escritura: productor o administrador. */
@Tag(name = "Artistas")
@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @Operation(summary = "Lista los artistas")
    @Autenticado
    @GetMapping
    public List<ArtistaResponse> listar() {
        return artistaService.listar();
    }

    @Operation(summary = "Obtiene un artista por id")
    @Autenticado
    @GetMapping("/{id}")
    public ArtistaResponse obtener(@PathVariable Long id) {
        return artistaService.obtener(id);
    }

    @Operation(summary = "Crea un artista")
    @SoloProductorOAdmin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistaResponse crear(@Valid @RequestBody ArtistaRequest artistaRequest) {
        return artistaService.crear(artistaRequest);
    }

    @Operation(summary = "Actualiza un artista")
    @SoloProductorOAdmin
    @PutMapping("/{id}")
    public ArtistaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ArtistaRequest artistaRequest) {
        return artistaService.actualizar(id, artistaRequest);
    }

    @Operation(summary = "Elimina un artista")
    @SoloProductorOAdmin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        artistaService.eliminar(id);
    }
}
