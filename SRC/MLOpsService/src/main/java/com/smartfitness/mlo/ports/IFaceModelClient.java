package com.smartfitness.mlo.ports;

/**
 * IFaceModelClient: Real-Time Face Model Service client for hot swap/rollback.
 */
public interface IFaceModelClient {
    /**
     * Apply a new model (Hot Swap) including version metadata.
     */
    void callHotSwapApi(String modelId, String newVersion, byte[] modelBinary);

    /**
     * Rollback to a previous model version in the Real-Time tier.
     */
    void callRollbackApi(String modelId, String previousVersion);
}
