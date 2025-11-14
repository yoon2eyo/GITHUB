package com.smartfitness.search.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Branch Review API Interface
 * Reference: 03_BranchContentServiceComponent.puml (IBranchReviewApi)
 */
public interface IBranchReviewApi {
    ResponseEntity<Map<String, Object>> registerReview(String branchId, String customerId, String review);
    ResponseEntity<Map<String, Object>> registerBranchInfo(String branchOwnerId, Map<String, String> branchInfo);
}

