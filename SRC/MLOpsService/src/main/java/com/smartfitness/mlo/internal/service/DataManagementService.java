package com.smartfitness.mlo.internal.service;

import com.smartfitness.mlo.internal.collector.DataCollector;
import com.smartfitness.mlo.internal.persistence.DataPersistenceManager;

import java.util.List;

/**
 * DataManagementService: Composite service for data collection and persistence.
 * Facade pattern implementation for data management operations.
 */
public class DataManagementService implements IDataManagementService {
    private final DataCollector dataCollector;
    private final DataPersistenceManager persistenceManager;

    public DataManagementService(DataCollector dataCollector,
                                 DataPersistenceManager persistenceManager) {
        this.dataCollector = dataCollector;
        this.persistenceManager = persistenceManager;
    }

    @Override
    public List<byte[]> collectTrainingData() {
        return dataCollector.collectDailyTrainingData();
    }

    @Override
    public void persistTrainingData(String dataType, byte[] data) {
        persistenceManager.persistData(dataType, data);
    }

    @Override
    public List<byte[]> loadAllTrainingData() {
        return persistenceManager.loadAllData();
    }

    @Override
    public void cleanupOldData(int retentionDays) {
        persistenceManager.cleanupOldData(retentionDays);
    }
}