package cl.grupo3.lab1.repository;

import java.math.BigDecimal;
import java.util.List;

import cl.grupo3.lab1.dto.CompensacionAsistenteResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * La cancelación la hace el procedimiento sp_cancelar_presentacion. Aquí solo se llama y se lee su resultado.
 */
@Repository
public class CancelacionRepository {

    /** Resultado escalar del procedimiento (sus parámetros INOUT). */
    public record Resultado(int notificados, BigDecimal creditoTotal) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public CancelacionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Cancela la presentación, notifica y compensa. Debe ejecutarse dentro de una transacción.
     * Si no existe o no está PROGRAMADA, el procedimiento lanza RAISE EXCEPTION (409).
     */
    public Resultado cancelar(Long idPresentacion) {
        String sql = "CALL sp_cancelar_presentacion(:id, NULL::int, NULL::numeric)";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("id", idPresentacion),
                (rs, i) -> new Resultado(rs.getInt("p_notificados"), rs.getBigDecimal("p_credito_total")));
    }

    /** Asistentes notificados por la cancelación con el crédito abonado a cada uno. */
    public List<CompensacionAsistenteResponse> findAsistentesNotificados(Long idPresentacion) {
        String sql = """
                SELECT u.id_usuario, u.nombre_completo, COALESCE(c.total, 0) AS credito
                FROM notificacion n
                JOIN usuario u ON u.id_usuario = n.id_usuario
                LEFT JOIN (SELECT id_usuario, SUM(monto) AS total
                           FROM compensacion WHERE id_presentacion = :id
                           GROUP BY id_usuario) c ON c.id_usuario = n.id_usuario
                WHERE n.id_presentacion = :id
                ORDER BY u.nombre_completo, u.id_usuario
                """;
        return jdbc.query(sql, new MapSqlParameterSource("id", idPresentacion),
                (rs, i) -> new CompensacionAsistenteResponse(rs.getLong("id_usuario"),
                        rs.getString("nombre_completo"), rs.getBigDecimal("credito")));
    }
}
