package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Artista;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL del CRUD de artistas.*/
@Repository
public class ArtistaRepository {

    private static final String SELECT_BASE = """
            SELECT id_artista, nombre, genero_musical
            FROM artista
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ArtistaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Artista> findAll() {
        return jdbc.query(SELECT_BASE + "ORDER BY nombre", new MapSqlParameterSource(), ArtistaRepository::mapRow);
    }

    public Optional<Artista> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE id_artista = :id", new MapSqlParameterSource("id", id),
                ArtistaRepository::mapRow).stream().findFirst();
    }

    public Long insert(String nombre, String generoMusical) {
        String sql = """
                INSERT INTO artista (nombre, genero_musical)
                VALUES (:nombre, :genero)
                RETURNING id_artista
                """;
        var params = new MapSqlParameterSource()
                .addValue("nombre", nombre)
                .addValue("genero", generoMusical);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** Devuelve filas afectadas (0 = no existe). */
    public int update(Long id, String nombre, String generoMusical) {
        String sql = """
                UPDATE artista
                SET nombre = :nombre,
                    genero_musical = :genero
                WHERE id_artista = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("nombre", nombre)
                .addValue("genero", generoMusical);
        return jdbc.update(sql, params);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM artista WHERE id_artista = :id", new MapSqlParameterSource("id", id));
    }

    private static Artista mapRow(ResultSet rs, int i) throws SQLException {
        return new Artista(rs.getLong("id_artista"), rs.getString("nombre"), rs.getString("genero_musical"));
    }
}
