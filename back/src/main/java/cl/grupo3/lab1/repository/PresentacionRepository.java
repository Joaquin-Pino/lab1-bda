package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Presentacion;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SQL de presentaciones (line-up). Lo usa PresentacionService.
 * Los traslapes los valida el Trigger 1.
 */
@Repository
public class PresentacionRepository {

    private static final String SELECT_BASE = """
            SELECT p.id_presentacion, p.id_festival, f.id_usuario_productor,
                   p.id_escenario, e.nombre AS nombre_escenario,
                   p.id_artista, a.nombre AS nombre_artista,
                   p.fecha_hora_inicio, p.fecha_hora_fin,
                   fn_dia_festival(p.fecha_hora_inicio) AS dia, p.estado
            FROM presentacion p
            JOIN festival f ON f.id_festival = p.id_festival
            JOIN escenario e ON e.id_escenario = p.id_escenario
            JOIN artista a ON a.id_artista = p.id_artista
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public PresentacionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Presentacion> findByFestival(Long idFestival) {
        String sql = SELECT_BASE + "WHERE p.id_festival = :idFestival ORDER BY p.fecha_hora_inicio, e.nombre";
        return jdbc.query(sql, new MapSqlParameterSource("idFestival", idFestival), PresentacionRepository::mapRow);
    }

    public Optional<Presentacion> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE p.id_presentacion = :id", new MapSqlParameterSource("id", id),
                PresentacionRepository::mapRow).stream().findFirst();
    }

    public Long insert(Long idFestival, Long idEscenario, Long idArtista, OffsetDateTime inicio, OffsetDateTime fin) {
        String sql = """
                INSERT INTO presentacion (id_festival, id_escenario, id_artista, fecha_hora_inicio, fecha_hora_fin)
                VALUES (:idFestival, :idEscenario, :idArtista, :inicio, :fin)
                RETURNING id_presentacion
                """;
        var params = new MapSqlParameterSource()
                .addValue("idFestival", idFestival)
                .addValue("idEscenario", idEscenario)
                .addValue("idArtista", idArtista)
                .addValue("inicio", inicio)
                .addValue("fin", fin);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** No cambia el festival ni el estado. Devuelve filas afectadas. */
    public int update(Long id, Long idEscenario, Long idArtista, OffsetDateTime inicio, OffsetDateTime fin) {
        String sql = """
                UPDATE presentacion
                SET id_escenario = :idEscenario,
                    id_artista = :idArtista,
                    fecha_hora_inicio = :inicio,
                    fecha_hora_fin = :fin
                WHERE id_presentacion = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("idEscenario", idEscenario)
                .addValue("idArtista", idArtista)
                .addValue("inicio", inicio)
                .addValue("fin", fin);
        return jdbc.update(sql, params);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM presentacion WHERE id_presentacion = :id", new MapSqlParameterSource("id", id));
    }

    private static Presentacion mapRow(ResultSet rs, int i) throws SQLException {
        return new Presentacion(rs.getLong("id_presentacion"), rs.getLong("id_festival"),
                rs.getLong("id_usuario_productor"), rs.getLong("id_escenario"), rs.getString("nombre_escenario"),
                rs.getLong("id_artista"), rs.getString("nombre_artista"),
                Fechas.leer(rs, "fecha_hora_inicio"), Fechas.leer(rs, "fecha_hora_fin"),
                rs.getObject("dia", LocalDate.class), rs.getString("estado"));
    }
}
