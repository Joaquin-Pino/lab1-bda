package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Recinto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL del CRUD de recintos (sin ORM). Lo usa RecintoService. */
@Repository
public class RecintoRepository {

    private static final String SELECT_BASE = """
            SELECT id_recinto, nombre, direccion, aforo_maximo
            FROM recinto
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public RecintoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Recinto> findAll() {
        return jdbc.query(SELECT_BASE + "ORDER BY nombre", new MapSqlParameterSource(), RecintoRepository::mapRow);
    }

    public Optional<Recinto> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE id_recinto = :id", new MapSqlParameterSource("id", id),
                RecintoRepository::mapRow).stream().findFirst();
    }

    public Long insert(String nombre, String direccion, Integer aforoMaximo) {
        String sql = """
                INSERT INTO recinto (nombre, direccion, aforo_maximo)
                VALUES (:nombre, :direccion, :aforo)
                RETURNING id_recinto
                """;
        var params = new MapSqlParameterSource()
                .addValue("nombre", nombre)
                .addValue("direccion", direccion)
                .addValue("aforo", aforoMaximo);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** Devuelve filas afectadas. */
    public int update(Long id, String nombre, String direccion, Integer aforoMaximo) {
        String sql = """
                UPDATE recinto
                SET nombre = :nombre,
                    direccion = :direccion,
                    aforo_maximo = :aforo
                WHERE id_recinto = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("nombre", nombre)
                .addValue("direccion", direccion)
                .addValue("aforo", aforoMaximo);
        return jdbc.update(sql, params);
    }

    /** Sus escenarios se borran en cascada; si tiene festivales falla la FK (23503 -> 409). */
    public int delete(Long id) {
        return jdbc.update("DELETE FROM recinto WHERE id_recinto = :id", new MapSqlParameterSource("id", id));
    }

    private static Recinto mapRow(ResultSet rs, int i) throws SQLException {
        return new Recinto(rs.getLong("id_recinto"), rs.getString("nombre"), rs.getString("direccion"),
                rs.getInt("aforo_maximo"));
    }
}
