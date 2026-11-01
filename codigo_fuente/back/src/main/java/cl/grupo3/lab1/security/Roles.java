package cl.grupo3.lab1.security;

import java.util.Set;

/** Valores de rol.tipo (deben coincidir con el CHECK de la tabla rol). */
public final class Roles {

    public static final String ASISTENTE = "ASISTENTE";
    public static final String PRODUCTOR = "PRODUCTOR";
    public static final String ADMINISTRADOR = "ADMINISTRADOR";

    public static final Set<String> TODOS = Set.of(ASISTENTE, PRODUCTOR, ADMINISTRADOR);

    private Roles() {
    }
}
