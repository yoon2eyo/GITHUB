package com.smartfitness.mlo.ports;

/**
 * Provided Port: Contract for the Real-Time Tier to receive the new model (Hot Swap).
 */
public interface IModelDeploymentService {
    void deployNewModel(String modelId, byte[] modelBinary);
    void rollbackModel(String modelId, String previousVersion);
}