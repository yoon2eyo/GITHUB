package com.smartfitness.facemodel.ports;

import com.smartfitness.common.model.FaceVector;

/**
 * IFaceModelService: Provides face vector similarity calculation for Access Service.
 * Tactic: Introduce Concurrency (design intent for parallel processing).
 */
public interface IFaceModelService {
    /**
     * Calculate similarity score between requested and stored face vectors.
     * @param requestedVector face vector from request/device
     * @param storedVector stored reference face vector
     * @return similarity score [0.0, 1.0]
     */
    double calculateSimilarityScore(FaceVector requestedVector, FaceVector storedVector);
}

