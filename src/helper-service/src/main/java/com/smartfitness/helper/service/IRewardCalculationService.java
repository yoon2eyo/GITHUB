package com.smartfitness.helper.service;

/**
 * Business Layer: Reward Calculation Service Interface
 * Reference: 04_HelperServiceComponent.puml (IRewardCalculationService)
 */
public interface IRewardCalculationService {
    int calculateReward(String taskId);
    void updateRewardBalance(String helperId, int amount);
}

