package com.smartfitness.mlo.ports;

import java.util.Date;

/**
 * ITrainingTriggerApi: REST API for triggering training operations.
 */
public interface ITrainingTriggerApi {
    /**
     * Trigger daily batch training via HTTP endpoint.
     */
    void triggerDailyBatch(Date timestamp);
    
    /**
     * Trigger manual training with custom parameters.
     */
    void triggerManualTraining(String modelId, String config);
}