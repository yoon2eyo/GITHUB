package com.smartfitness.mlo.internal.deployment;

import com.smartfitness.mlo.ports.IFaceModelClient;
import com.smartfitness.mlo.internal.storage.IModelDataRepository;
import java.util.Optional;

/**
 * DeploymentService: Executes Hot Swap deployment and rollback strategies.
 * Tactic: Hot Swap, Rollback, Automated Verification.
 */
public class DeploymentService {
    private final IModelDataRepository modelStorage;
    private final IFaceModelClient faceModelClient;

    public DeploymentService(IModelDataRepository modelStorage, IFaceModelClient faceModelClient) {
        this.modelStorage = modelStorage;
        this.faceModelClient = faceModelClient;
    }

    /**
     * Zero-downtime deployment into Real-Time tier.
     */
    public void executeZeroDowntimeDeployment(String modelId, String newVersion) {
        Optional<byte[]> modelBinaryOpt = modelStorage.loadModelBinary(modelId, newVersion);
        byte[] modelBinary = modelBinaryOpt.orElseThrow(() -> new IllegalStateException("Model binary not found"));

        try {
            faceModelClient.callHotSwapApi(modelId, newVersion, modelBinary);
        } catch (Exception e) {
            // In a real implementation, retrieve previous version to rollback
            executeRollback(modelId, "previous");
        }
    }

    private void executeRollback(String modelId, String previousVersion) {
        try {
            faceModelClient.callRollbackApi(modelId, previousVersion);
        } catch (Exception ex) {
            System.err.println("Rollback failed! Manual intervention required.");
        }
    }
}
