package cl.grupo3.lab1.service;

import java.util.Locale;
import java.util.Optional;

import cl.grupo3.lab1.dto.LoginRequest;
import cl.grupo3.lab1.dto.LoginResponse;
import cl.grupo3.lab1.dto.PerfilResponse;
import cl.grupo3.lab1.dto.RegisterRequest;
import cl.grupo3.lab1.dto.UsuarioResumen;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.exception.UnauthorizedException;
import cl.grupo3.lab1.model.Usuario;
import cl.grupo3.lab1.repository.AuthRepository;
import cl.grupo3.lab1.security.JwtService;
import cl.grupo3.lab1.security.PasswordHasher;
import org.springframework.stereotype.Service;

/**
 * Reglas de registro, login y perfil. Usa AuthRepository, PasswordHasher y JwtService; lo llama AuthController.
 * Convierte model.Usuario en los DTO de respuesta (sin password_hash).
 */
@Service
public class AuthService {

    static final String MSG_CREDENCIALES = "Credenciales inválidas";

    private final AuthRepository authRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public AuthService(AuthRepository authRepository, PasswordHasher passwordHasher, JwtService jwtService) {
        this.authRepository = authRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    /** Siempre crea ASISTENTE. Email repetido -> 23505 uq_usuario_email -> 409. */
    public UsuarioResumen register(RegisterRequest registerRequest) {
        Usuario usuario = authRepository.insertAsistente(registerRequest.nombreCompleto().trim(),
                normalizarEmail(registerRequest.email()), passwordHasher.hash(registerRequest.password()));
        return toUsuarioResumen(usuario);
    }

    /** Mismo 401 y mensaje si el email no existe o la contraseña es incorrecta. */
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<Usuario> credenciales = authRepository.findCredencialesByEmail(normalizarEmail(loginRequest.email()));
        boolean contrasenaValida = passwordHasher.matches(loginRequest.password(),
                credenciales.map(Usuario::passwordHash).orElse(null));
        if (credenciales.isEmpty() || !contrasenaValida) {
            throw new UnauthorizedException(MSG_CREDENCIALES);
        }
        Usuario usuario = credenciales.get();
        String token = jwtService.generateToken(usuario.idUsuario(), usuario.rol(), usuario.email());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), toUsuarioResumen(usuario));
    }

    public PerfilResponse perfil(Long idUsuario) {
        Usuario usuario = authRepository.findPerfil(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return new PerfilResponse(usuario.idUsuario(), usuario.nombreCompleto(), usuario.email(), usuario.rol(),
                usuario.creditosCompensacion());
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static UsuarioResumen toUsuarioResumen(Usuario usuario) {
        return new UsuarioResumen(usuario.idUsuario(), usuario.nombreCompleto(), usuario.email(), usuario.rol());
    }
}
