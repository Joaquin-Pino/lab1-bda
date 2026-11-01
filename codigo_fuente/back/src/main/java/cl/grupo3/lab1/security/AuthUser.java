package cl.grupo3.lab1.security;

/**
 * Usuario autenticado (principal del SecurityContext), construido desde el JWT sin consultar la base.
 * Obtenerlo con CurrentUser.get() o con @AuthenticationPrincipal AuthUser.
 */
public record AuthUser(Long idUsuario, String rol) {

    public boolean esAdmin() {
        return Roles.ADMINISTRADOR.equals(rol);
    }

    public boolean esProductor() {
        return Roles.PRODUCTOR.equals(rol);
    }

    public boolean esAsistente() {
        return Roles.ASISTENTE.equals(rol);
    }

    /** Regla de propiedad: el ADMINISTRADOR es dueño de todo; el resto solo de lo suyo. */
    public boolean puedeOperarSobre(Long idDueno) {
        return esAdmin() || idUsuario.equals(idDueno);
    }
}
