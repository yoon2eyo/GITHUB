package com.smartfitness.branchowner.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Branch Owner API Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchOwnerApi)
 */
public interface IBranchOwnerApi {
    ResponseEntity<Map<String, Object>> registerBranchOwner(Map<String, String> ownerInfo);
    ResponseEntity<Map<String, Object>> getBranchOwnerInfo(String ownerId);
}

