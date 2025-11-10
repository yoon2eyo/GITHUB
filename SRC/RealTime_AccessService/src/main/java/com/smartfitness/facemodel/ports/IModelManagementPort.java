package com.smartfitness.facemodel.ports;

/**
 * IModelManagementPort: Contract for loading and rolling back models from the MLOps tier.
 * Tactic: Hot Swap, Rollback.
 */
public interface IModelManagementPort {
    /**
     * Load a new model binary into memory for immediate use.
     * @param modelBinary serialized/binary model payload
     */
    void loadNewModel(byte[] modelBinary);

    /**
     * Roll back to a previously loaded model.
     */
    void rollbackToPreviousModel();
}

