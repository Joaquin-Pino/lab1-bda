package cl.grupo3.lab1.security;

import java.nio.charset.StandardCharsets;

import cl.grupo3.lab1.exception.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Hash BCrypt de contraseñas. Nunca registrar ni devolver contraseñas ni hashes. */
@Component
public class PasswordHasher {

    /** BCrypt solo considera los primeros 72 bytes. */
    public static final int MAX_BYTES = 72;

    private final PasswordEncoder passwordEncoder;
    /** Hash de referencia para gastar el mismo tiempo cuando el email no existe (evita enumerar usuarios). */
    private final String dummyHash;

    public PasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("contrasena-de-relleno");
    }

    public String hash(String contrasena) {
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BadRequestException("password: no puede superar 72 bytes");
        }
        return passwordEncoder.encode(contrasena);
    }

    /** Compara la contraseña con el hash; si hash es null igual gasta el tiempo de un BCrypt. */
    public boolean matches(String contrasena, String hash) {
        if (contrasena == null || contrasena.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return false;
        }
        if (hash == null) {
            passwordEncoder.matches(contrasena, dummyHash);
            return false;
        }
        return passwordEncoder.matches(contrasena, hash);
    }
}
