package com.smartfitness.helper.service;

/**
 * Business Layer: Task Validation Service Interface
 * Reference: 04_HelperServiceComponent.puml (ITaskValidationService)
 */
public interface ITaskValidationService {
    boolean validateDailyLimit(String helperId);
}

