package cl.grupo3.lab1.service;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * "Función de transacción" reutilizable (forma programática); servicio compartido que usan los demás servicios.
 *
 * <pre>
 * // Sin auditoría
 * Long id = transactionRunner.run(() -> festivalRepository.insertar(...));
 * // Con usuario para el trigger de auditoría (misma transacción que el UPDATE)
 * transactionRunner.runAs(CurrentUser.get().idUsuario(), () -> tipoEntradaRepository.actualizarPrecio(id, precio));
 * </pre>
 *
 * Forma declarativa equivalente: anotar el método del servicio con @Transactional y,
 * si hay auditoría, llamar a auditContext.setUser(id) como primera instrucción.
 * Si ya hay una transacción activa, estos métodos se unen a ella (PROPAGATION_REQUIRED).
 */
@Component
public class TransactionRunner {

    private final TransactionTemplate transactionTemplate;
    private final AuditContext auditContext;

    public TransactionRunner(PlatformTransactionManager transactionManager, AuditContext auditContext) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.auditContext = auditContext;
    }

    public <T> T run(Supplier<T> operacion) {
        return transactionTemplate.execute(status -> operacion.get());
    }

    public void run(Runnable operacion) {
        transactionTemplate.executeWithoutResult(status -> operacion.run());
    }

    /** Abre la transacción, fija app.user_id y luego ejecuta la operación. */
    public <T> T runAs(Long idUsuario, Supplier<T> operacion) {
        return transactionTemplate.execute(status -> {
            auditContext.setUser(idUsuario);
            return operacion.get();
        });
    }

    public void runAs(Long idUsuario, Runnable operacion) {
        transactionTemplate.executeWithoutResult(status -> {
            auditContext.setUser(idUsuario);
            operacion.run();
        });
    }
}
