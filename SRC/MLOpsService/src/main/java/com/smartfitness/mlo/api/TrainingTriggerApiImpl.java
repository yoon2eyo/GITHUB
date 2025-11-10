package com.smartfitness.mlo.api;

import com.smartfitness.mlo.ports.ITrainingTriggerApi;
import com.smartfitness.mlo.ports.ITrainingTriggerService;

import java.util.Date;

/**
 * TrainingTriggerApiImpl: REST API implementation for training operations.
 */
public class TrainingTriggerApiImpl implements ITrainingTriggerApi {
    private final ITrainingTriggerService trainingService;

    public TrainingTriggerApiImpl(ITrainingTriggerService trainingService) {
        this.trainingService = trainingService;
    }

    @Override
    public void triggerDailyBatch(Date timestamp) {
        try {
            trainingService.triggerDailyBatch(timestamp);
        } catch (Exception e) {
            System.err.println("Failed to trigger daily batch: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void triggerManualTraining(String modelId, String config) {
        // Placeholder: implement manual training trigger
        System.out.println("Manual training triggered for model: " + modelId + " with config: " + config);
    }
}