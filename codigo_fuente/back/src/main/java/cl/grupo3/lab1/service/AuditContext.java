package cl.grupo3.lab1.service;

import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Pasa el usuario autenticado a PostgreSQL para los triggers de auditoría (servicio compartido; lo usa TransactionRunner).
 *
 * Contrato: fija app.user_id con set_config(..., is_local = true), así dura solo la
 * transacción actual y no se filtra a otra petición que reutilice la conexión del pool.
 * El trigger lo lee con: NULLIF(current_setting('app.user_id', true), '')::bigint
 */
@Component
public class AuditContext {

    private final JdbcTemplate jdbcTemplate;

    public AuditContext(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Debe llamarse dentro de una transacción de Spring (misma conexión que el UPDATE auditado). */
    public void setUser(Long idUsuario) {
        Objects.requireNonNull(idUsuario, "idUsuario");
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                    "AuditContext.setUser debe llamarse dentro de una transacción (usa TransactionRunner.runAs o @Transactional)");
        }
        jdbcTemplate.queryForObject("SELECT set_config('app.user_id', ?, true)", String.class, String.valueOf(idUsuario));
    }
}
