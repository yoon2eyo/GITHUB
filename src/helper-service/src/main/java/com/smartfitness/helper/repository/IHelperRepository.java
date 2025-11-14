package com.smartfitness.helper.repository;

/**
 * System Interface Layer: Helper Repository Interface
 * Reference: 04_HelperServiceComponent.puml (IHelperRepository)
 */
public interface IHelperRepository {
    void saveTask(String taskId, String helperId, String branchId, String photoUrl);
    int countTodayTasks(String helperId);
    void updateTaskAnalysis(String taskId, String analysisResult);
    int getRewardBalance(String helperId);
    void incrementRewardBalance(String helperId, int amount);
}

