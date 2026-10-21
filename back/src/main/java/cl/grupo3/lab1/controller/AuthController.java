package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.dto.LoginRequest;
import cl.grupo3.lab1.dto.LoginResponse;
import cl.grupo3.lab1.dto.PerfilResponse;
import cl.grupo3.lab1.dto.RegisterRequest;
import cl.grupo3.lab1.dto.UsuarioResumen;
import cl.grupo3.lab1.security.Autenticado;
import cl.grupo3.lab1.security.AuthUser;
import cl.grupo3.lab1.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controlador de registro, login y perfil (/api/auth). Llama a AuthService; no contiene SQL ni lógica. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    public UsuarioResumen register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("/me")
    @Autenticado
    public PerfilResponse me(@AuthenticationPrincipal AuthUser usuarioAutenticado) {
        return authService.perfil(usuarioAutenticado.idUsuario());
    }
}
