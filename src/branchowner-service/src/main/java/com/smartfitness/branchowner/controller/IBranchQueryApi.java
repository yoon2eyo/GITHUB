package com.smartfitness.branchowner.controller;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: Branch Query API Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchQueryApi)
 */
public interface IBranchQueryApi {
    ResponseEntity<Map<String, Object>> getBranchInfo(String branchId);
    ResponseEntity<List<Map<String, Object>>> getCustomerReviews(String branchId);
}

