package com.smartfitness.helper.service;

import java.util.Map;

/**
 * Business Layer: Reward Confirmation Service Interface
 * Reference: 04_HelperServiceComponent.puml (IRewardConfirmationService)
 */
public interface IRewardConfirmationService {
    Map<String, Object> getRewardBalance(String helperId);
}

