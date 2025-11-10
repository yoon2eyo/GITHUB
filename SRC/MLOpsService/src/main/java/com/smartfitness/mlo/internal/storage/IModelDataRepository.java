package com.smartfitness.mlo.internal.storage;

import java.util.List;
import java.util.Optional;

/**
 * IModelDataRepository: MLOps Tier's persistence contract (DS-05).
 */
public interface IModelDataRepository {
    void saveRawTrainingData(String dataType, byte[] data);
    List<byte[]> loadAllTrainingData();
    void saveModelBinary(String modelId, String modelVersion, byte[] modelBinary);
    Optional<byte[]> loadModelBinary(String modelId, String modelVersion);
}

