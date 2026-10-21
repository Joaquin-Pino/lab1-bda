package cl.grupo3.lab1.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso estático al usuario autenticado de la petición actual. */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** Devuelve el AuthUser o lanza AuthenticationException (se responde 401). */
    public static AuthUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
            return authUser;
        }
        throw new AuthenticationCredentialsNotFoundException("No autenticado");
    }
}
