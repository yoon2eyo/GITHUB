package com.smartfitness.search.service;

import java.util.Map;

/**
 * Business Layer: Content Registration Service Interface
 * Reference: 03_BranchContentServiceComponent.puml (IContentRegistrationService)
 */
public interface IContentRegistrationService {
    Map<String, Object> registerReview(String branchId, String customerId, String review);
    Map<String, Object> registerBranchInfo(String branchOwnerId, Map<String, String> branchInfo);
}

