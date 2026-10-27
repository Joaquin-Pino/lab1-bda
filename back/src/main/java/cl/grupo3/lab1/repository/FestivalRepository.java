package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Festival;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SQL del CRUD de festivales (sin ORM). Lo usan FestivalService, PresentacionService y GrillaService.
 * Regla de visibilidad (VISIBLE): el administrador ve todos; los demás ven los PUBLICADOS y los propios.
 * Así el asistente solo ve los publicados y el productor además ve sus borradores.
 */
@Repository
public class FestivalRepository {

    private static final String SELECT_BASE = """
            SELECT f.id_festival, f.id_usuario_productor, f.id_recinto, r.nombre AS nombre_recinto,
                   f.nombre, f.fecha_inicio, f.fecha_fin, f.estado
            FROM festival f
            JOIN recinto r ON r.id_recinto = f.id_recinto
            """;

    private static final String VISIBLE =
            "(:esAdmin OR f.estado = 'PUBLICADO' OR f.id_usuario_productor = :idUsuario)";

    private final NamedParameterJdbcTemplate jdbc;

    public FestivalRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Festival> findVisibles(boolean esAdmin, Long idUsuario) {
        String sql = SELECT_BASE + "WHERE " + VISIBLE + " ORDER BY f.fecha_inicio, f.nombre";
        var params = new MapSqlParameterSource()
                .addValue("esAdmin", esAdmin)
                .addValue("idUsuario", idUsuario);
        return jdbc.query(sql, params, FestivalRepository::mapRow);
    }

    /** Vacío si no existe o si el usuario no lo puede ver (el servicio responde 404 en ambos casos). */
    public Optional<Festival> findVisibleById(Long id, boolean esAdmin, Long idUsuario) {
        String sql = SELECT_BASE + "WHERE f.id_festival = :id AND " + VISIBLE;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("esAdmin", esAdmin)
                .addValue("idUsuario", idUsuario);
        return jdbc.query(sql, params, FestivalRepository::mapRow).stream().findFirst();
    }

    /** Sin regla de visibilidad: para comprobar el dueño antes de modificar. */
    public Optional<Festival> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE f.id_festival = :id", new MapSqlParameterSource("id", id),
                FestivalRepository::mapRow).stream().findFirst();
    }

    public Long insert(Long idUsuarioProductor, Long idRecinto, String nombre, LocalDate fechaInicio,
                       LocalDate fechaFin, String estado) {
        String sql = """
                INSERT INTO festival (id_usuario_productor, id_recinto, nombre, fecha_inicio, fecha_fin, estado)
                VALUES (:idProductor, :idRecinto, :nombre, :fechaInicio, :fechaFin, :estado)
                RETURNING id_festival
                """;
        var params = new MapSqlParameterSource()
                .addValue("idProductor", idUsuarioProductor)
                .addValue("idRecinto", idRecinto)
                .addValue("nombre", nombre)
                .addValue("fechaInicio", fechaInicio)
                .addValue("fechaFin", fechaFin)
                .addValue("estado", estado);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /** No cambia el dueño. Devuelve filas afectadas (0 = no existe). */
    public int update(Long id, Long idRecinto, String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                      String estado) {
        String sql = """
                UPDATE festival
                SET id_recinto = :idRecinto,
                    nombre = :nombre,
                    fecha_inicio = :fechaInicio,
                    fecha_fin = :fechaFin,
                    estado = :estado
                WHERE id_festival = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("idRecinto", idRecinto)
                .addValue("nombre", nombre)
                .addValue("fechaInicio", fechaInicio)
                .addValue("fechaFin", fechaFin)
                .addValue("estado", estado);
        return jdbc.update(sql, params);
    }

    /**
     * Cuenta las presentaciones (no canceladas) que quedarían incoherentes con el festival:
     * en un escenario de otro recinto o en un día fuera de sus fechas. Se usa después de editar el festival.
     */
    public int contarPresentacionesIncoherentes(Long id) {
        String sql = """
                SELECT COUNT(*)
                FROM presentacion p
                JOIN festival f ON f.id_festival = p.id_festival
                JOIN escenario e ON e.id_escenario = p.id_escenario
                WHERE p.id_festival = :id
                  AND p.estado <> 'CANCELADA'
                  AND (e.id_recinto <> f.id_recinto
                       OR fn_dia_festival(p.fecha_hora_inicio) NOT BETWEEN f.fecha_inicio AND f.fecha_fin)
                """;
        Integer total = jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), Integer.class);
        return total == null ? 0 : total;
    }


    public int delete(Long id) {
        return jdbc.update("DELETE FROM festival WHERE id_festival = :id", new MapSqlParameterSource("id", id));
    }

    private static Festival mapRow(ResultSet rs, int i) throws SQLException {
        return new Festival(rs.getLong("id_festival"), rs.getLong("id_usuario_productor"), rs.getLong("id_recinto"),
                rs.getString("nombre_recinto"), rs.getString("nombre"),
                rs.getObject("fecha_inicio", LocalDate.class), rs.getObject("fecha_fin", LocalDate.class),
                rs.getString("estado"));
    }
}
