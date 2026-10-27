package cl.grupo3.lab1.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee "Authorization: Bearer token". Si es válido, autentica la petición con un AuthUser y la
 * autoridad ROLE_rol. Si falta o es inválido, deja pasar sin autenticar: en rutas protegidas
 * el RestAuthenticationEntryPoint responde 401. No consulta la base de datos.
 * No es @Component a propósito (evita que Spring Boot lo registre dos veces como filtro de servlet).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Atributo de la petición que marca un token presente pero inválido/expirado. */
    public static final String ATTR_TOKEN_INVALIDO = JwtAuthFilter.class.getName() + ".TOKEN_INVALIDO";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            jwtService.parseToken(token).ifPresentOrElse(authUser -> {
                var authentication = new UsernamePasswordAuthenticationToken(
                        authUser, null, List.of(new SimpleGrantedAuthority("ROLE_" + authUser.rol())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }, () -> request.setAttribute(ATTR_TOKEN_INVALIDO, Boolean.TRUE));
        }
        filterChain.doFilter(request, response);
    }
}
