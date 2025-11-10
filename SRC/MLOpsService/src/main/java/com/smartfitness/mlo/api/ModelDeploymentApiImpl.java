package com.smartfitness.mlo.api;

import com.smartfitness.mlo.ports.IModelDeploymentApi;
import com.smartfitness.mlo.ports.IModelDeploymentService;

/**
 * ModelDeploymentApiImpl: REST API implementation for model deployment operations.
 */
public class ModelDeploymentApiImpl implements IModelDeploymentApi {
    private final IModelDeploymentService deploymentService;

    public ModelDeploymentApiImpl(IModelDeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @Override
    public void deployModel(String modelId, String version) {
        try {
            // Load model binary from storage and deploy
            System.out.println("Deploying model " + modelId + " version " + version);
            // Implementation would load model binary and call deploymentService
        } catch (Exception e) {
            System.err.println("Failed to deploy model: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void rollbackModel(String modelId) {
        try {
            deploymentService.rollbackModel(modelId, "previous");
        } catch (Exception e) {
            System.err.println("Failed to rollback model: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public String getDeploymentStatus(String modelId) {
        // Placeholder: return deployment status
        return "DEPLOYED";
    }
}