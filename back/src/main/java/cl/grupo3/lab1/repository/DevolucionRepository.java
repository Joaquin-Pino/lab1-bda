package cl.grupo3.lab1.repository;

import java.math.BigDecimal;
import java.util.List;

import cl.grupo3.lab1.dto.CompensacionAsistenteResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * La cancelación con devolución la hacen sp_cancelar_festival y sp_cancelar_tipo_entrada.
 * Aquí solo se llaman y se lee su detalle (tabla temporal tmp_devolucion, válida en la misma transacción).
 */
@Repository
public class DevolucionRepository {

    /** Resultado escalar del procedimiento (sus parámetros INOUT). */
    public record Resultado(int notificados, BigDecimal creditoTotal) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public DevolucionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Debe ejecutarse dentro de una transacción. Si ya está cancelado o finalizado, RAISE EXCEPTION (409). */
    public Resultado cancelarFestival(Long idFestival) {
        return llamar("CALL sp_cancelar_festival(:id, NULL::int, NULL::numeric)", idFestival);
    }

    /** Debe ejecutarse dentro de una transacción. */
    public Resultado cancelarTipoEntrada(Long idTipoEntrada) {
        return llamar("CALL sp_cancelar_tipo_entrada(:id, NULL::int, NULL::numeric)", idTipoEntrada);
    }

    private Resultado llamar(String sql, Long id) {
        return jdbc.queryForObject(sql, new MapSqlParameterSource("id", id),
                (rs, i) -> new Resultado(rs.getInt("p_notificados"), rs.getBigDecimal("p_credito_total")));
    }

    /** Asistentes afectados por la última cancelación de la transacción, con el crédito devuelto a cada uno. */
    public List<CompensacionAsistenteResponse> findAsistentesAfectados() {
        String sql = """
                SELECT u.id_usuario, u.nombre_completo, SUM(d.monto) AS credito
                FROM tmp_devolucion d
                JOIN usuario u ON u.id_usuario = d.id_usuario
                GROUP BY u.id_usuario, u.nombre_completo
                ORDER BY u.nombre_completo, u.id_usuario
                """;
        return jdbc.query(sql, (rs, i) -> new CompensacionAsistenteResponse(rs.getLong("id_usuario"),
                rs.getString("nombre_completo"), rs.getBigDecimal("credito")));
    }
}
