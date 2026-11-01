package cl.grupo3.lab1.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * La validación la hace el procedimiento sp_validar_acceso (resultado, marca de uso y log_entrada).
 * Aquí solo se llama y se leen los datos del ticket.
 */
@Repository
public class AccesoRepository {

    /** Resultado del procedimiento (sus parámetros INOUT). idTicket es nulo si el código no existe. */
    public record Lectura(String resultado, Long idTicket) {
    }

    /** Datos del ticket para mostrar en el lector. */
    public record TicketLeido(String nombreTipo, OffsetDateTime fechaUso) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public AccesoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Debe ejecutarse dentro de una transacción (el candado del ticket dura hasta el commit). */
    public Lectura validar(String codigo, Long idFestival, Long idUsuario) {
        String sql = "CALL sp_validar_acceso(:codigo, :idFestival, :idUsuario, NULL::varchar, NULL::bigint)";
        var params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("idFestival", idFestival)
                .addValue("idUsuario", idUsuario);
        return jdbc.queryForObject(sql, params,
                (rs, i) -> new Lectura(rs.getString("p_resultado"), rs.getObject("p_id_ticket", Long.class)));
    }

    public Optional<TicketLeido> findTicket(Long idTicket) {
        String sql = """
                SELECT te.nombre_tipo, t.fecha_uso
                FROM ticket t
                JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
                WHERE t.id_ticket = :id
                """;
        return jdbc.query(sql, new MapSqlParameterSource("id", idTicket),
                (rs, i) -> new TicketLeido(rs.getString("nombre_tipo"), Fechas.leer(rs, "fecha_uso")))
                .stream().findFirst();
    }
}
