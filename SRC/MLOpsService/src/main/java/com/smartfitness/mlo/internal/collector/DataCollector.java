package com.smartfitness.mlo.internal.collector;

import com.smartfitness.mlo.ports.IAuthRepository;
import com.smartfitness.mlo.ports.IHelperRepository;
import com.smartfitness.mlo.internal.storage.IModelDataRepository;
import com.smartfitness.domain.model.UserAccount;
import com.smartfitness.domain.model.HelperTask;
import java.util.List;
import java.util.Date;

/**
 * DataCollector: Collects data for the daily batch from different services' repositories (Read-Only access).
 */
public class DataCollector {
    private final IAuthRepository authClient;
    private final IHelperRepository helperClient;
    private final IModelDataRepository modelStorage;

    public DataCollector(IAuthRepository authClient, IHelperRepository helperClient, IModelDataRepository modelStorage) {
        this.authClient = authClient;
        this.helperClient = helperClient;
        this.modelStorage = modelStorage;
    }
    
    /**
     * Executes the daily batch data collection from other service DBs (Read-Only).
     */
    public List<byte[]> collectDailyTrainingData() {
        // Collect new face vectors (via IAuthRepository)
        Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 3600 * 1000);
        List<UserAccount> newFaces = authClient.findRecentlyRegisteredUsers(oneDayAgo);
        
        // Collect new ground truth data (via IHelperRepository)
        List<HelperTask> groundTruth = helperClient.findConfirmedTasksForRetraining();
        
        // Save to MLOps' own storage (DS-05)
        saveCollectedData(newFaces, groundTruth);

        // Return all training data aggregated in DS-05
        return modelStorage.loadAllTrainingData();
    }

    private void saveCollectedData(List<UserAccount> newUsers, List<HelperTask> groundTruthTasks) {
        // Placeholder: transform and persist required training artifacts
        modelStorage.saveRawTrainingData("FaceVector", null);
        modelStorage.saveRawTrainingData("GroundTruth", null);
    }
}
