package cl.grupo3.lab1.service;

import java.util.List;
import java.util.Locale;

import cl.grupo3.lab1.dto.ActualizarUsuarioRequest;
import cl.grupo3.lab1.dto.CrearUsuarioRequest;
import cl.grupo3.lab1.dto.UsuarioResponse;
import cl.grupo3.lab1.exception.ConflictException;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Usuario;
import cl.grupo3.lab1.repository.UsuarioRepository;
import cl.grupo3.lab1.security.PasswordHasher;
import org.springframework.stereotype.Service;

/**
 * CRUD de usuarios (solo administrador); lo llama UsuarioController y usa UsuarioRepository y TransactionRunner.
 * Las reglas de datos las garantiza PostgreSQL: email único (23505 -> 409), rol inexistente o usuario con datos
 * relacionados (23503 -> 409).
 */
@Service
public class UsuarioService {

    static final String MSG_NO_ENCONTRADO = "Usuario no encontrado";

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TransactionRunner transactionRunner;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher,
                          TransactionRunner transactionRunner) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.transactionRunner = transactionRunner;
    }

    public List<UsuarioResponse> listar(String rol) {
        return usuarioRepository.findAll(rol == null || rol.isBlank() ? null : rol.trim().toUpperCase(Locale.ROOT))
                .stream().map(UsuarioService::toUsuarioResponse).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return usuarioRepository.findById(id).map(UsuarioService::toUsuarioResponse)
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADO));
    }

    public UsuarioResponse crear(CrearUsuarioRequest crearUsuarioRequest) {
        String passwordHash = passwordHasher.hash(crearUsuarioRequest.password());
        return transactionRunner.run(() -> {
            Long id = usuarioRepository.insert(crearUsuarioRequest.nombreCompleto().trim(),
                    AuthService.normalizarEmail(crearUsuarioRequest.email()), passwordHash, crearUsuarioRequest.idRol());
            return obtener(id);
        });
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest actualizarUsuarioRequest) {
        String passwordHash = actualizarUsuarioRequest.password() == null
                ? null : passwordHasher.hash(actualizarUsuarioRequest.password());
        return transactionRunner.run(() -> {
            int filasActualizadas = usuarioRepository.update(id, actualizarUsuarioRequest.nombreCompleto().trim(),
                    AuthService.normalizarEmail(actualizarUsuarioRequest.email()), actualizarUsuarioRequest.idRol(),
                    passwordHash);
            if (filasActualizadas == 0) {
                throw new NotFoundException(MSG_NO_ENCONTRADO);
            }
            return obtener(id);
        });
    }

    public void eliminar(Long id, Long idSolicitante) {
        if (id.equals(idSolicitante)) {
            throw new ConflictException("No puedes eliminar tu propio usuario");
        }
        if (usuarioRepository.delete(id) == 0) {
            throw new NotFoundException(MSG_NO_ENCONTRADO);
        }
    }

    private static UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.idUsuario(), usuario.nombreCompleto(), usuario.email(), usuario.idRol(),
                usuario.rol(), usuario.creditosCompensacion());
    }
}
