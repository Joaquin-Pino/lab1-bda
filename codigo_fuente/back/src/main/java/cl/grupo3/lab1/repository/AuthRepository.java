package cl.grupo3.lab1.repository;

import java.util.Optional;

import cl.grupo3.lab1.model.Usuario;
import cl.grupo3.lab1.security.Roles;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SQL de autenticación (sin ORM): login, registro y perfil. Devuelve model.Usuario; lo usa AuthService.
 */
@Repository
public class AuthRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuthRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Usuario con su password_hash para verificar el login; el hash nunca sale de la capa de servicio. */
    public Optional<Usuario> findCredencialesByEmail(String email) {
        String sql = """
                SELECT u.id_usuario, u.id_rol, r.tipo AS rol, u.nombre_completo, u.email, u.password_hash,
                       u.creditos_compensacion
                FROM usuario u
                JOIN rol r ON r.id = u.id_rol
                WHERE lower(u.email) = lower(:email)
                """;
        return jdbc.query(sql, new MapSqlParameterSource("email", email), (rs, i) -> new Usuario(
                rs.getLong("id_usuario"), rs.getLong("id_rol"), rs.getString("rol"), rs.getString("nombre_completo"),
                rs.getString("email"), rs.getString("password_hash"), rs.getBigDecimal("creditos_compensacion")))
                .stream().findFirst();
    }

    /** Inserta siempre con rol ASISTENTE (el rol se resuelve en SQL, no viene del cliente). */
    public Usuario insertAsistente(String nombreCompleto, String email, String passwordHash) {
        String sql = """
                INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
                VALUES ((SELECT id FROM rol WHERE tipo = 'ASISTENTE'), :nombre, :email, :hash)
                RETURNING id_usuario, id_rol, nombre_completo, email, creditos_compensacion
                """;
        var params = new MapSqlParameterSource()
                .addValue("nombre", nombreCompleto)
                .addValue("email", email)
                .addValue("hash", passwordHash);
        return jdbc.queryForObject(sql, params, (rs, i) -> new Usuario(
                rs.getLong("id_usuario"), rs.getLong("id_rol"), Roles.ASISTENTE, rs.getString("nombre_completo"),
                rs.getString("email"), null, rs.getBigDecimal("creditos_compensacion")));
    }

    /** Perfil para /api/auth/me (sin password_hash). */
    public Optional<Usuario> findPerfil(Long idUsuario) {
        String sql = """
                SELECT u.id_usuario, u.id_rol, r.tipo AS rol, u.nombre_completo, u.email, u.creditos_compensacion
                FROM usuario u
                JOIN rol r ON r.id = u.id_rol
                WHERE u.id_usuario = :id
                """;
        return jdbc.query(sql, new MapSqlParameterSource("id", idUsuario), (rs, i) -> new Usuario(
                rs.getLong("id_usuario"), rs.getLong("id_rol"), rs.getString("rol"), rs.getString("nombre_completo"),
                rs.getString("email"), null, rs.getBigDecimal("creditos_compensacion")))
                .stream().findFirst();
    }
}
