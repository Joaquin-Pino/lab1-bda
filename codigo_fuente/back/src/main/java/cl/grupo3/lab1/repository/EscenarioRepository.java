package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Escenario;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL del CRUD de escenarios (sin ORM). */
@Repository
public class EscenarioRepository {

    private static final String SELECT_BASE = """
            SELECT e.id_escenario, e.id_recinto, r.nombre AS nombre_recinto, e.nombre, e.capacidad_maxima
            FROM escenario e
            JOIN recinto r ON r.id_recinto = e.id_recinto
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public EscenarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** idRecinto null = todos los escenarios. */
    public List<Escenario> findAll(Long idRecinto) {
        String sql = SELECT_BASE + """
                WHERE CAST(:idRecinto AS bigint) IS NULL OR e.id_recinto = CAST(:idRecinto AS bigint)
                ORDER BY r.nombre, e.nombre
                """;
        return jdbc.query(sql, new MapSqlParameterSource("idRecinto", idRecinto), EscenarioRepository::mapRow);
    }

    public Optional<Escenario> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE e.id_escenario = :id", new MapSqlParameterSource("id", id),
                EscenarioRepository::mapRow).stream().findFirst();
    }

    public Long insert(Long idRecinto, String nombre, Integer capacidadMaxima) {
        String sql = """
                INSERT INTO escenario (id_recinto, nombre, capacidad_maxima)
                VALUES (:idRecinto, :nombre, :capacidad)
                RETURNING id_escenario
                """;
        var params = new MapSqlParameterSource()
                .addValue("idRecinto", idRecinto)
                .addValue("nombre", nombre)
                .addValue("capacidad", capacidadMaxima);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** Devuelve filas afectadas (0 = no existe). */
    public int update(Long id, Long idRecinto, String nombre, Integer capacidadMaxima) {
        String sql = """
                UPDATE escenario
                SET id_recinto = :idRecinto,
                    nombre = :nombre,
                    capacidad_maxima = :capacidad
                WHERE id_escenario = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("idRecinto", idRecinto)
                .addValue("nombre", nombre)
                .addValue("capacidad", capacidadMaxima);
        return jdbc.update(sql, params);
    }


    public boolean tienePresentaciones(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM presentacion WHERE id_escenario = :id)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), Boolean.class));
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM escenario WHERE id_escenario = :id", new MapSqlParameterSource("id", id));
    }

    private static Escenario mapRow(ResultSet rs, int i) throws SQLException {
        return new Escenario(rs.getLong("id_escenario"), rs.getLong("id_recinto"), rs.getString("nombre_recinto"),
                rs.getString("nombre"), rs.getInt("capacidad_maxima"));
    }
}
