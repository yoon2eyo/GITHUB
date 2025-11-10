package com.smartfitness.mlo.ports;

/**
 * IModelDeploymentApi: REST API for model deployment operations.
 */
public interface IModelDeploymentApi {
    /**
     * Deploy specific model version via HTTP endpoint.
     */
    void deployModel(String modelId, String version);
    
    /**
     * Rollback to previous model version.
     */
    void rollbackModel(String modelId);
    
    /**
     * Get deployment status.
     */
    String getDeploymentStatus(String modelId);
}