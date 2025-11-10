package com.smartfitness.persistence;

import com.smartfitness.helper.model.HelperBalance;
import com.smartfitness.helper.model.TaskSubmission;
import com.smartfitness.helper.ports.IHelperRepository;

import java.util.Optional;
import javax.sql.DataSource;

/**
 * HelperRepositoryImpl: concrete DAL for DB_HELPER (Database per Service).
 * The actual SQL/JPA wiring is omitted but the contract boundaries are enforced here.
 */
public class HelperRepositoryImpl implements IHelperRepository {
    private final DataSource dataSource;

    public HelperRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(TaskSubmission submission) {
        // TODO: Persist submission into DB_HELPER.TaskSubmission table.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateTaskStatus(Long taskId, String status, Double score) {
        // TODO: Update task status and optional AI score in DB_HELPER.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateBalance(Long helperId, double amount) {
        // By using an atomic SQL operation, we prevent race conditions.
        // The database ensures that concurrent updates to the same balance are serialized,
        // guaranteeing data consistency without requiring complex application-level locking.
        // String sql = "UPDATE helper_balances SET balance = balance + ? WHERE helper_id = ?";
        // jdbcTemplate.update(sql, amount, helperId);
        throw new UnsupportedOperationException("Not yet implemented. Atomic update logic should be placed here.");
    }

    @Override
    public Optional<HelperBalance> findBalanceByHelperId(Long helperId) {
        // TODO: Query DB_HELPER for helper balance snapshot.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
