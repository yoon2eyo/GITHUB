package com.smartfitness.access.ports;

import java.util.Optional;

import com.smartfitness.common.model.FaceVector;

/**
 * IAccessVectorRepository: Repository contract to access stored (encrypted) face vectors.
 * Tactic: Abstract Data Sources.
 */
public interface IAccessVectorRepository {
    /**
     * Find stored vector by unique face identifier.
     * @param faceId unique face ID
     * @return optional FaceVector if present
     */
    Optional<FaceVector> findVectorById(String faceId);
}
