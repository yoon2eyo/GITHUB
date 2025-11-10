package com.smartfitness.mlo.internal.persistence;

import com.smartfitness.mlo.internal.storage.IModelDataRepository;

import java.util.List;
import java.util.Date;

/**
 * DataPersistenceManager: Manages data persistence operations.
 */
public class DataPersistenceManager {
    private final IModelDataRepository modelDataRepository;

    public DataPersistenceManager(IModelDataRepository modelDataRepository) {
        this.modelDataRepository = modelDataRepository;
    }

    public void persistData(String dataType, byte[] data) {
        modelDataRepository.saveRawTrainingData(dataType, data);
    }

    public List<byte[]> loadAllData() {
        return modelDataRepository.loadAllTrainingData();
    }

    public void cleanupOldData(int retentionDays) {
        // Placeholder: implement cleanup logic based on retention policy
        Date cutoffDate = new Date(System.currentTimeMillis() - (long)retentionDays * 24 * 60 * 60 * 1000);
        System.out.println("Cleaning up data older than: " + cutoffDate);
        // Implementation would delete old training data
    }
}