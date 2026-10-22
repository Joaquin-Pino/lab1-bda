package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import cl.grupo3.lab1.model.Orden;
import cl.grupo3.lab1.model.Ticket;
import cl.grupo3.lab1.model.TicketDetalle;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * La compra la hace el procedimiento sp_comprar_entradas. Aca solo se llama y se leen sus resultados.
 */
@Repository
public class OrdenRepository {

    private static final String SELECT_ORDEN = """
            SELECT id_orden, id_usuario_asistente, fecha_compra, monto_total, monto_credito_aplicado, estado
            FROM orden_compra
            """;

    private static final String SELECT_TICKET = """
            SELECT id_ticket, codigo_unico, id_orden, id_tipo_entrada, estado, precio, fecha_uso
            FROM ticket
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public OrdenRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Llama al procedimiento de compra y devuelve el id de la orden creada (su parámetro INOUT).
     * Debe ejecutarse dentro de una transacción. Cupo agotado o festival no publicado lanzan RAISE EXCEPTION.
     */
    public Long comprar(Long idUsuario, Long idTipoEntrada, int cantidad, boolean usarCredito) {
        String sql = "CALL sp_comprar_entradas(:idUsuario, :idTipoEntrada, :cantidad, :usarCredito, NULL::bigint)";
        var params = new MapSqlParameterSource()
                .addValue("idUsuario", idUsuario)
                .addValue("idTipoEntrada", idTipoEntrada)
                .addValue("cantidad", cantidad)
                .addValue("usarCredito", usarCredito);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    public Optional<Orden> findById(Long id) {
        return jdbc.query(SELECT_ORDEN + "WHERE id_orden = :id", new MapSqlParameterSource("id", id),
                OrdenRepository::mapOrden).stream().findFirst();
    }

    /** Órdenes de un asistente, de la más reciente a la más antigua. */
    public List<Orden> findByUsuario(Long idUsuario) {
        return jdbc.query(SELECT_ORDEN + "WHERE id_usuario_asistente = :idUsuario ORDER BY fecha_compra DESC, id_orden DESC",
                new MapSqlParameterSource("idUsuario", idUsuario), OrdenRepository::mapOrden);
    }

    public List<Ticket> findTicketsByOrden(Long idOrden) {
        return jdbc.query(SELECT_TICKET + "WHERE id_orden = :idOrden ORDER BY id_ticket",
                new MapSqlParameterSource("idOrden", idOrden), OrdenRepository::mapTicket);
    }

    /** Tickets de varias órdenes en una sola consulta (evita una consulta por orden). */
    public List<Ticket> findTicketsByOrdenes(Collection<Long> idsOrden) {
        if (idsOrden.isEmpty()) {
            return List.of();
        }
        return jdbc.query(SELECT_TICKET + "WHERE id_orden IN (:idsOrden) ORDER BY id_ticket",
                new MapSqlParameterSource("idsOrden", idsOrden), OrdenRepository::mapTicket);
    }

    /** Tickets de un asistente con su tipo de entrada y festival, de la compra más reciente a la más antigua. */
    public List<TicketDetalle> findTicketsByUsuario(Long idUsuario) {
        String sql = """
                SELECT t.id_ticket, t.codigo_unico, t.estado, t.precio, t.fecha_uso, t.id_orden,
                       te.id_tipo_entrada, te.nombre_tipo, te.fecha AS fecha_entrada,
                       f.id_festival, f.nombre AS nombre_festival
                FROM ticket t
                JOIN orden_compra o ON o.id_orden = t.id_orden
                JOIN tipo_entrada te ON te.id_tipo_entrada = t.id_tipo_entrada
                JOIN festival f ON f.id_festival = te.id_festival
                WHERE o.id_usuario_asistente = :idUsuario
                ORDER BY o.fecha_compra DESC, t.id_ticket
                """;
        return jdbc.query(sql, new MapSqlParameterSource("idUsuario", idUsuario),
                OrdenRepository::mapTicketDetalle);
    }

    private static TicketDetalle mapTicketDetalle(ResultSet rs, int i) throws SQLException {
        return new TicketDetalle(rs.getLong("id_ticket"), rs.getString("codigo_unico"), rs.getString("estado"),
                rs.getBigDecimal("precio"), Fechas.leer(rs, "fecha_uso"), rs.getLong("id_orden"),
                rs.getLong("id_tipo_entrada"), rs.getString("nombre_tipo"),
                rs.getObject("fecha_entrada", LocalDate.class), rs.getLong("id_festival"),
                rs.getString("nombre_festival"));
    }

    private static Orden mapOrden(ResultSet rs, int i) throws SQLException {
        return new Orden(rs.getLong("id_orden"), rs.getLong("id_usuario_asistente"),
                Fechas.leer(rs, "fecha_compra"), rs.getBigDecimal("monto_total"),
                rs.getBigDecimal("monto_credito_aplicado"), rs.getString("estado"));
    }

    private static Ticket mapTicket(ResultSet rs, int i) throws SQLException {
        return new Ticket(rs.getLong("id_ticket"), rs.getString("codigo_unico"), rs.getLong("id_orden"),
                rs.getLong("id_tipo_entrada"), rs.getString("estado"), rs.getBigDecimal("precio"),
                Fechas.leer(rs, "fecha_uso"));
    }
}
