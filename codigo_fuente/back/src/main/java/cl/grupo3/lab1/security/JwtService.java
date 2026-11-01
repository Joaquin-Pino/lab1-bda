package cl.grupo3.lab1.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Firma y valida JWT HS256.
 * Claims: sub = id_usuario (string), rol, email, iat, exp (= ahora + app.jwt.expiration-minutes).
 * Falla al arrancar si app.jwt.secret (JWT_SECRET) falta o mide menos de 32 bytes.
 */
@Component
public class JwtService {

    public static final int MIN_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno JWT_SECRET (app.jwt.secret). Defínela en back/.env");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MIN_SECRET_BYTES + " bytes (tiene " + secretBytes.length + ")");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MINUTES debe ser mayor que 0");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationSeconds = expirationMinutes * 60;
    }

    public String generateToken(Long idUsuario, String rol, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim("rol", rol)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Devuelve el usuario del token, o vacío si es inválido, está mal firmado, expiró o tiene claims incorrectos. */
    public Optional<AuthUser> parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            String rol = claims.get("rol", String.class);
            if (claims.getSubject() == null || rol == null || !Roles.TODOS.contains(rol)) {
                return Optional.empty();
            }
            return Optional.of(new AuthUser(Long.valueOf(claims.getSubject()), rol));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
