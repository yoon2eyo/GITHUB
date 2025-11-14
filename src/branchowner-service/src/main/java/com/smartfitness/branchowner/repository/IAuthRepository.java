package com.smartfitness.branchowner.repository;

import java.util.Map;

/**
 * System Interface Layer: Auth Repository Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IAuthRepository)
 */
public interface IAuthRepository {
    void createAccount(String ownerId, Map<String, String> ownerInfo);
    Map<String, Object> findAccountById(String ownerId);
}

