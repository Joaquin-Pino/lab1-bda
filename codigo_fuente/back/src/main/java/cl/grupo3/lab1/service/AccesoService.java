package cl.grupo3.lab1.service;

import java.util.Map;

import cl.grupo3.lab1.dto.AccesoResponse;
import cl.grupo3.lab1.dto.ValidarAccesoRequest;
import cl.grupo3.lab1.exception.NotFoundException;
import cl.grupo3.lab1.repository.AccesoRepository;
import cl.grupo3.lab1.repository.FestivalRepository;
import cl.grupo3.lab1.security.AuthUser;
import org.springframework.stereotype.Service;

/**
 * Control de acceso. Solo el dueño del festival o el admin lee tickets de ese festival (si no, 404).
 * Que el ticket sea inválido no es un error HTTP: se responde 200 con el resultado para que el lector lo muestre.
 */
@Service
public class AccesoService {

    private static final Map<String, String> MENSAJES = Map.of(
            "VALIDO", "Acceso permitido",
            "YA_USADO", "Ticket ya utilizado",
            "ANULADO", "Ticket anulado",
            "INEXISTENTE", "El código no existe",
            "OTRO_FESTIVAL", "El ticket es de otro festival",
            "FECHA_INCORRECTA", "El ticket no es válido para hoy");

    private final AccesoRepository accesoRepository;
    private final FestivalRepository festivalRepository;
    private final TransactionRunner transactionRunner;

    public AccesoService(AccesoRepository accesoRepository, FestivalRepository festivalRepository,
                         TransactionRunner transactionRunner) {
        this.accesoRepository = accesoRepository;
        this.festivalRepository = festivalRepository;
        this.transactionRunner = transactionRunner;
    }

    public AccesoResponse validar(ValidarAccesoRequest request, AuthUser usuario) {
        return transactionRunner.run(() -> {
            festivalRepository.findById(request.idFestival())
                    .filter(f -> usuario.puedeOperarSobre(f.idUsuarioProductor()))
                    .orElseThrow(() -> new NotFoundException(FestivalService.MSG_NO_ENCONTRADO));
            AccesoRepository.Lectura lectura = accesoRepository.validar(request.codigoUnico().trim(),
                    request.idFestival(), usuario.idUsuario());
            var ticket = lectura.idTicket() == null ? null : accesoRepository.findTicket(lectura.idTicket()).orElse(null);
            return new AccesoResponse(lectura.resultado(), "VALIDO".equals(lectura.resultado()),
                    MENSAJES.get(lectura.resultado()), lectura.idTicket(),
                    ticket == null ? null : ticket.nombreTipo(), ticket == null ? null : ticket.fechaUso());
        });
    }
}
