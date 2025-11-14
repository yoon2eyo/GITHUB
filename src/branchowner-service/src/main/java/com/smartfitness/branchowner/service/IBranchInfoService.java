package com.smartfitness.branchowner.service;

import java.util.Map;

/**
 * Business Layer: Branch Info Service Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchInfoService)
 */
public interface IBranchInfoService {
    Map<String, Object> getBranchInfo(String branchId);
    boolean validateBranchInfo(String branchId);
}

