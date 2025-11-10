package com.smartfitness.helper.ports;

import java.util.Optional;

import com.smartfitness.helper.model.HelperBalance;
import com.smartfitness.helper.model.TaskSubmission;

/**
 * IHelperRepository: Contract to access DB_HELPER (Database per Service).
 */
public interface IHelperRepository {
    void save(TaskSubmission submission);
    void updateTaskStatus(Long taskId, String status, Double score);
    void updateBalance(Long helperId, double amount);
    Optional<HelperBalance> findBalanceByHelperId(Long helperId);
}
