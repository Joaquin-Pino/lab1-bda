package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Rol;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL del CRUD de roles (sin ORM). Devuelve model.Rol; lo usa RolService. */
@Repository
public class RolRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RolRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Rol> findAll() {
        return jdbc.query("SELECT id, tipo FROM rol ORDER BY id", RolRepository::mapRow);
    }

    public Optional<Rol> findById(Long id) {
        return jdbc.query("SELECT id, tipo FROM rol WHERE id = :id", new MapSqlParameterSource("id", id),
                RolRepository::mapRow).stream().findFirst();
    }

    public Rol insert(String tipo) {
        return jdbc.queryForObject("INSERT INTO rol (tipo) VALUES (:tipo) RETURNING id, tipo",
                new MapSqlParameterSource("tipo", tipo), RolRepository::mapRow);
    }

    public Optional<Rol> update(Long id, String tipo) {
        return jdbc.query("UPDATE rol SET tipo = :tipo WHERE id = :id RETURNING id, tipo",
                new MapSqlParameterSource().addValue("id", id).addValue("tipo", tipo),
                RolRepository::mapRow).stream().findFirst();
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM rol WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    private static Rol mapRow(ResultSet rs, int i) throws SQLException {
        return new Rol(rs.getLong("id"), rs.getString("tipo"));
    }
}
