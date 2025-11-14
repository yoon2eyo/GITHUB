package com.smartfitness.access.adapter;

import com.smartfitness.common.dto.FaceVectorDto;

import java.util.List;

/**
 * System Interface Layer: Access Vector Repository Interface
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IAccessVectorRepository {
    List<FaceVectorDto> findTopActiveVectors(int limit);
    FaceVectorDto findByBranchId(String branchId);
    FaceVectorDto findByUserId(String userId);
    void save(FaceVectorDto vector);
}

