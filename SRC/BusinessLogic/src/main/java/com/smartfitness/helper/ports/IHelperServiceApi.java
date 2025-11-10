package com.smartfitness.helper.ports;

import java.util.List;
import java.util.Optional;

import com.smartfitness.helper.model.HelperBalance;
import com.smartfitness.helper.model.TaskRegistrationResult;
import com.smartfitness.helper.model.TaskSubmission;

/**
 * IHelperServiceApi: Main API for helper and admin operations.
 */
public interface IHelperServiceApi {
    /**
     * Register a helper's task submission (UC-12).
     */
    TaskRegistrationResult registerTaskSubmission(TaskSubmission submission);

    /**
     * Get helper's current balance (UC-17).
     */
    Optional<HelperBalance> getHelperBalance(Long helperId);

    /**
     * Retrieve tasks for AI review/approval workflows (UC-14).
     */
    List<TaskSubmission> getTasksForReview(Long branchId);
}
