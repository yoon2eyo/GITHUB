package com.smartfitness.branchowner.repository;

import java.util.Map;

/**
 * System Interface Layer: Branch Repository Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchRepository)
 */
public interface IBranchRepository {
    void saveBranchOwner(String ownerId, Map<String, String> ownerInfo);
    Map<String, Object> findBranchById(String branchId);
    boolean existsById(String branchId);
    void updateBranchInfo(String branchId, String branchName);
    void updatePreferences(String branchId, String preferenceDetails);
}

