package com.smartfitness.branchowner.service;

import java.util.Map;

/**
 * Business Layer: Branch Owner Management Service Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchOwnerManagementService)
 */
public interface IBranchOwnerManagementService {
    Map<String, Object> registerBranchOwner(Map<String, String> ownerInfo);
}

