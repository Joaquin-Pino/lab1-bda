package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import cl.grupo3.lab1.model.AuditoriaPrecio;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Lectura de la auditoría de precios (sin ORM). Los registros los crea el trigger trg_auditoria_precio,
 * por eso aquí solo hay consultas.
 */
@Repository
public class AuditoriaPrecioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuditoriaPrecioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Cambios de precio de un tipo de entrada, del más reciente al más antiguo. */
    public List<AuditoriaPrecio> findByTipoEntrada(Long idTipoEntrada) {
        String sql = """
                SELECT a.id_auditoria, a.id_tipo_entrada, a.precio_anterior, a.precio_nuevo,
                       a.id_usuario_modificador, u.nombre_completo AS nombre_usuario, a.fecha_modificacion
                FROM auditoria_precio_entrada a
                LEFT JOIN usuario u ON u.id_usuario = a.id_usuario_modificador
                WHERE a.id_tipo_entrada = :idTipoEntrada
                ORDER BY a.fecha_modificacion DESC, a.id_auditoria DESC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("idTipoEntrada", idTipoEntrada),
                AuditoriaPrecioRepository::mapRow);
    }

    private static AuditoriaPrecio mapRow(ResultSet rs, int i) throws SQLException {
        return new AuditoriaPrecio(rs.getLong("id_auditoria"), rs.getLong("id_tipo_entrada"),
                rs.getBigDecimal("precio_anterior"), rs.getBigDecimal("precio_nuevo"),
                rs.getObject("id_usuario_modificador", Long.class), rs.getString("nombre_usuario"),
                Fechas.leer(rs, "fecha_modificacion"));
    }
}
