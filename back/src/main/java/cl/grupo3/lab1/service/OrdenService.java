package cl.grupo3.lab1.service;

import java.util.List;

import cl.grupo3.lab1.dto.CompraRequest;
import cl.grupo3.lab1.dto.OrdenResponse;
import cl.grupo3.lab1.dto.TicketResponse;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.model.Orden;
import cl.grupo3.lab1.model.Ticket;
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

    static OrdenResponse toResponse(Orden orden, List<Ticket> tickets) {
        List<TicketResponse> ticketsResponse = tickets.stream()
                .map(OrdenService::toResponse).toList();
        return new OrdenResponse(orden.idOrden(), orden.fechaCompra(), orden.montoTotal(),
                orden.montoCreditoAplicado(), orden.estado(), ticketsResponse);
    }

    private static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.idTicket(), ticket.codigoUnico(), ticket.precio(), ticket.estado());
    }
}
