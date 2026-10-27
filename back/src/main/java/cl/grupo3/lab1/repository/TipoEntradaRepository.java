package cl.grupo3.lab1.repository;

import cl.grupo3.lab1.model.TipoEntrada;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TipoEntradaRepository {

    private static final String SELECT_BASE = """
            SELECT id_tipo_entrada, id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha
            FROM tipo_entrada
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public TipoEntradaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TipoEntrada> findByIdFestival(Long idFestival) {
        String sql = SELECT_BASE + """
                WHERE id_festival = :id ORDER BY fecha NULLS FIRST, nombre_tipo
                """;
        return jdbc.query(sql, new MapSqlParameterSource("id", idFestival), TipoEntradaRepository::mapRow);
    }

    public Optional<TipoEntrada> findById(Long id) {
        String sql = SELECT_BASE + """
                WHERE id_tipo_entrada = :id
                """;
        return jdbc.query(sql, new MapSqlParameterSource("id", id), TipoEntradaRepository::mapRow)
                .stream().findFirst();
    }

    /** Al crear, el aforo disponible es igual al total. */
    public Long insert(Long idFestival, String nombreTipo, BigDecimal precioActual, int aforoTotal,
                       LocalDate fecha) {
        String sql = """
                INSERT INTO tipo_entrada (id_festival, nombre_tipo, precio_actual, aforo_total, aforo_disponible, fecha)
                VALUES (:idFestival, :nombreTipo, :precioActual, :aforoTotal, :aforoTotal, :fecha)
                RETURNING id_tipo_entrada
                """;
        var params = new MapSqlParameterSource()
                .addValue("idFestival", idFestival)
                .addValue("nombreTipo", nombreTipo)
                .addValue("precioActual", precioActual)
                .addValue("aforoTotal", aforoTotal)
                .addValue("fecha", fecha);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    /**
     * No cambia el festival. El aforo disponible se mueve la misma diferencia que el total, en la misma sentencia
     * (así una compra simultánea no se pierde); si quedaría negativo, ck_aforo rechaza el cambio.
     * Devuelve filas afectadas (0 = no existe).
     */
    public int update(Long id, String nombreTipo, BigDecimal precioActual, int aforoTotal, LocalDate fecha) {
        String sql = """
                UPDATE tipo_entrada
                SET nombre_tipo = :nombreTipo,
                    precio_actual = :precioActual,
                    fecha = :fecha,
                    aforo_disponible = aforo_disponible + (:aforoTotal - aforo_total),
                    aforo_total = :aforoTotal
                WHERE id_tipo_entrada = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("nombreTipo", nombreTipo)
                .addValue("precioActual", precioActual)
                .addValue("aforoTotal", aforoTotal)
                .addValue("fecha", fecha);
        return jdbc.update(sql, params);
    }

    /** Falla con violación de clave foránea (409) si ya hay tickets vendidos. Devuelve filas afectadas. */
    public int delete(Long id) {
        return jdbc.update("DELETE FROM tipo_entrada WHERE id_tipo_entrada = :id",
                new MapSqlParameterSource("id", id));
    }

    private static TipoEntrada mapRow(ResultSet rs, int i) throws SQLException {
        return new TipoEntrada(rs.getLong("id_tipo_entrada"), rs.getLong("id_festival"),
                rs.getString("nombre_tipo"), rs.getBigDecimal("precio_actual"),
                rs.getInt("aforo_total"), rs.getInt("aforo_disponible"),
                rs.getObject("fecha", LocalDate.class));
    }
}
