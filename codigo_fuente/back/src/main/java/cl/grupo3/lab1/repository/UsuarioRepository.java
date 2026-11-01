package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Usuario;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SQL del CRUD de usuarios (sin ORM). Devuelve model.Usuario; lo usa UsuarioService.
 * Nunca selecciona password_hash (queda null en el model).
 */
@Repository
public class UsuarioRepository {

    private static final String SELECT_BASE = """
            SELECT u.id_usuario, u.nombre_completo, u.email, u.id_rol, r.tipo AS rol, u.creditos_compensacion
            FROM usuario u
            JOIN rol r ON r.id = u.id_rol
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Usuario> findAll(String rol) {
        String sql = SELECT_BASE + """
                WHERE CAST(:rol AS varchar) IS NULL OR r.tipo = CAST(:rol AS varchar)
                ORDER BY u.id_usuario
                """;
        return jdbc.query(sql, new MapSqlParameterSource("rol", rol), UsuarioRepository::mapRow);
    }

    public Optional<Usuario> findById(Long id) {
        String sql = SELECT_BASE + "WHERE u.id_usuario = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", id), UsuarioRepository::mapRow).stream().findFirst();
    }

    public Long insert(String nombreCompleto, String email, String passwordHash, Long idRol) {
        String sql = """
                INSERT INTO usuario (id_rol, nombre_completo, email, password_hash)
                VALUES (:idRol, :nombre, :email, :hash)
                RETURNING id_usuario
                """;
        var params = new MapSqlParameterSource()
                .addValue("idRol", idRol)
                .addValue("nombre", nombreCompleto)
                .addValue("email", email)
                .addValue("hash", passwordHash);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** passwordHash null = conserva la contraseña actual. No toca creditos_compensacion. Devuelve filas afectadas. */
    public int update(Long id, String nombreCompleto, String email, Long idRol, String passwordHash) {
        String sql = """
                UPDATE usuario
                SET nombre_completo = :nombre,
                    email = :email,
                    id_rol = :idRol,
                    password_hash = COALESCE(CAST(:hash AS text), password_hash)
                WHERE id_usuario = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("nombre", nombreCompleto)
                .addValue("email", email)
                .addValue("idRol", idRol)
                .addValue("hash", passwordHash);
        return jdbc.update(sql, params);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM usuario WHERE id_usuario = :id", new MapSqlParameterSource("id", id));
    }

    private static Usuario mapRow(ResultSet rs, int i) throws SQLException {
        return new Usuario(rs.getLong("id_usuario"), rs.getLong("id_rol"), rs.getString("rol"),
                rs.getString("nombre_completo"), rs.getString("email"), null, rs.getBigDecimal("creditos_compensacion"));
    }
}
