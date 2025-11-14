package com.smartfitness.search.controller;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: Branch Search API Interface
 * Reference: 03_BranchContentServiceComponent.puml (IBranchSearchApi)
 */
public interface IBranchSearchApi {
    ResponseEntity<List<Map<String, Object>>> searchBranches(String query, String userLocation);
}

