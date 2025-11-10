package com.smartfitness.mlo.ports;

import java.util.Date;

/**
 * ITrainingTriggerService: Provided API to trigger daily batch training.
 */
public interface ITrainingTriggerService {
    /**
     * Trigger the daily batch training pipeline at the given timestamp.
     */
    void triggerDailyBatch(Date timestamp);
}

