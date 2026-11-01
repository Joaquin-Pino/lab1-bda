package cl.grupo3.lab1.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.grupo3.lab1.dto.CompraRequest;
import cl.grupo3.lab1.dto.OrdenResponse;
import cl.grupo3.lab1.dto.TicketDetalleResponse;
import cl.grupo3.lab1.dto.TicketResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Orden;
import cl.grupo3.lab1.model.Ticket;
import cl.grupo3.lab1.model.TicketDetalle;
import cl.grupo3.lab1.repository.OrdenRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Compra de entradas y consulta de órdenes. La compra completa (cupo, orden, tickets y crédito)
 * la lógica ocurre dentro del procedimiento sp_comprar_entradas
 */
@Service
public class OrdenService {

    static final String MSG_NO_ENCONTRADA = "Orden no encontrada";

    private final OrdenRepository ordenRepository;
    private final TransactionRunner transactionRunner;

    public OrdenService(OrdenRepository ordenRepository, TransactionRunner transactionRunner) {
        this.ordenRepository = ordenRepository;
        this.transactionRunner = transactionRunner;
    }

    /**
     * Compra entradas para el usuario autenticado
     * Cupo agotado, festival no publicado o terminado y cantidad inválida los rechaza el procedimiento (409).
     */
    public OrdenResponse comprar(CompraRequest request, AuthUser usuario) {
        return transactionRunner.run(() -> {
            Long idOrden = ordenRepository.comprar(usuario.idUsuario(), request.idTipoEntrada(),
                    request.cantidad(), request.usarCredito());
            Orden orden = ordenRepository.findById(idOrden)
                    .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
            return toResponse(orden, ordenRepository.findTicketsByOrden(idOrden));
        });
    }

    /** Órdenes del usuario con sus tickets, de la más reciente a la más antigua. */
    public List<OrdenResponse> listarMias(AuthUser usuario) {
        List<Orden> ordenes = ordenRepository.findByUsuario(usuario.idUsuario());
        Map<Long, List<Ticket>> ticketsPorOrden = ordenRepository
                .findTicketsByOrdenes(ordenes.stream().map(Orden::idOrden).toList()).stream()
                .collect(Collectors.groupingBy(Ticket::idOrden));
        return ordenes.stream()
                .map(orden -> toResponse(orden, ticketsPorOrden.getOrDefault(orden.idOrden(), List.of())))
                .toList();
    }

    /** Una orden con sus tickets. Si no existe o es de otro usuario, 404 en ambos casos (el admin las ve todas). */
    public OrdenResponse obtener(Long id, AuthUser usuario) {
        Orden orden = ordenRepository.findById(id)
                .filter(o -> usuario.puedeOperarSobre(o.idUsuarioAsistente()))
                .orElseThrow(() -> new NotFoundException(MSG_NO_ENCONTRADA));
        return toResponse(orden, ordenRepository.findTicketsByOrden(id));
    }

    /** Tickets del usuario con su festival y tipo de entrada. */
    public List<TicketDetalleResponse> listarTicketsMios(AuthUser usuario) {
        return ordenRepository.findTicketsByUsuario(usuario.idUsuario()).stream()
                .map(OrdenService::toResponse).toList();
    }

    static OrdenResponse toResponse(Orden orden, List<Ticket> tickets) {
        List<TicketResponse> ticketsResponse = tickets.stream()
                .map(OrdenService::toResponse).toList();
        return new OrdenResponse(orden.idOrden(), orden.fechaCompra(), orden.montoTotal(),
                orden.montoCreditoAplicado(), orden.estado(), ticketsResponse);
    }

    private static TicketDetalleResponse toResponse(TicketDetalle ticket) {
        return new TicketDetalleResponse(ticket.idTicket(), ticket.codigoUnico(), ticket.estado(), ticket.precio(),
                ticket.fechaUso(), ticket.idOrden(), ticket.idTipoEntrada(), ticket.nombreTipo(),
                ticket.fechaEntrada(), ticket.idFestival(), ticket.nombreFestival());
    }

    private static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.idTicket(), ticket.codigoUnico(), ticket.precio(), ticket.estado());
    }
}
