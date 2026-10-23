package cl.grupo3.lab1.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.dto.NotificacionResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * SQL de notificaciones y saldo de crédito. Toda consulta filtra por el usuario dueño.
 * Las notificaciones las crea sp_cancelar_presentacion.
 */
@Repository
public class NotificacionRepository {

    private static final String SELECT_BASE = """
            SELECT id, id_presentacion, mensaje, leida, fecha_creacion
            FROM notificacion
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public NotificacionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Notificaciones del usuario, de la más reciente a la más antigua. */
    public List<NotificacionResponse> findByUsuario(Long idUsuario, boolean soloNoLeidas) {
        String sql = SELECT_BASE + "WHERE id_usuario = :idUsuario" + (soloNoLeidas ? " AND NOT leida" : "")
                + " ORDER BY fecha_creacion DESC, id DESC";
        return jdbc.query(sql, new MapSqlParameterSource("idUsuario", idUsuario), NotificacionRepository::map);
    }

    public Optional<NotificacionResponse> findByIdAndUsuario(Long id, Long idUsuario) {
        return jdbc.query(SELECT_BASE + "WHERE id = :id AND id_usuario = :idUsuario",
                new MapSqlParameterSource("id", id).addValue("idUsuario", idUsuario),
                NotificacionRepository::map).stream().findFirst();
    }

    public long contarNoLeidas(Long idUsuario) {
        return jdbc.queryForObject("SELECT count(*) FROM notificacion WHERE id_usuario = :idUsuario AND NOT leida",
                new MapSqlParameterSource("idUsuario", idUsuario), Long.class);
    }

    /** Marca una notificación del usuario como leída. Devuelve 0 filas si no existe o es de otro. */
    public int marcarLeida(Long id, Long idUsuario) {
        return jdbc.update("UPDATE notificacion SET leida = TRUE WHERE id = :id AND id_usuario = :idUsuario",
                new MapSqlParameterSource("id", id).addValue("idUsuario", idUsuario));
    }

    public int marcarTodasLeidas(Long idUsuario) {
        return jdbc.update("UPDATE notificacion SET leida = TRUE WHERE id_usuario = :idUsuario AND NOT leida",
                new MapSqlParameterSource("idUsuario", idUsuario));
    }

    public int delete(Long id, Long idUsuario) {
        return jdbc.update("DELETE FROM notificacion WHERE id = :id AND id_usuario = :idUsuario",
                new MapSqlParameterSource("id", id).addValue("idUsuario", idUsuario));
    }

    public BigDecimal findSaldo(Long idUsuario) {
        return jdbc.query("SELECT creditos_compensacion FROM usuario WHERE id_usuario = :idUsuario",
                new MapSqlParameterSource("idUsuario", idUsuario), (rs, i) -> rs.getBigDecimal(1))
                .stream().findFirst().orElse(BigDecimal.ZERO);
    }

    private static NotificacionResponse map(ResultSet rs, int i) throws SQLException {
        return new NotificacionResponse(rs.getLong("id"), rs.getObject("id_presentacion", Long.class),
                rs.getString("mensaje"), rs.getBoolean("leida"), Fechas.leer(rs, "fecha_creacion"));
    }
}
