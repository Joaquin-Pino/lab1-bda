package cl.grupo3.lab1.service;

import java.util.List;
import java.util.Locale;

import cl.grupo3.lab1.dto.RolResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Rol;
import cl.grupo3.lab1.repository.RolRepository;
import org.springframework.stereotype.Service;

/**
 * CRUD de roles (solo administrador); lo llama RolController y usa RolRepository. El CHECK de rol.tipo limita los
 * valores: tipo no permitido -> 23514 -> 400; tipo repetido -> 23505 -> 409; rol en uso al borrar -> 23503 -> 409.
 */
@Service
public class RolService {

    static final String MSG_NO_ENCONTRADO = "Rol no encontrado";

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(RolService::toRolResponse).toList();
    }

    public RolResponse obtener(Long id) {
        return rolRepository.findById(id).map(RolService::toRolResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public RolResponse crear(String tipo) {
        return toRolResponse(rolRepository.insert(normalizarTipo(tipo)));
    }

    public RolResponse actualizar(Long id, String tipo) {
        return rolRepository.update(id, normalizarTipo(tipo)).map(RolService::toRolResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public void eliminar(Long id) {
        if (rolRepository.delete(id) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADO);
        }
    }

    private static String normalizarTipo(String tipo) {
        return tipo.trim().toUpperCase(Locale.ROOT);
    }

    private static RolResponse toRolResponse(Rol rol) {
        return new RolResponse(rol.id(), rol.tipo());
    }
}
