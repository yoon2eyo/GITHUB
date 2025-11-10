package com.smartfitness.mlo.internal.service;

import java.util.List;

/**
 * IDataManagementService: Manages data collection and persistence operations.
 */
public interface IDataManagementService {
    /**
     * Collect training data from external sources.
     */
    List<byte[]> collectTrainingData();
    
    /**
     * Persist collected data to storage.
     */
    void persistTrainingData(String dataType, byte[] data);
    
    /**
     * Load all training data for batch processing.
     */
    List<byte[]> loadAllTrainingData();
    
    /**
     * Clean up old training data based on retention policy.
     */
    void cleanupOldData(int retentionDays);
}