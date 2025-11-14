package com.smartfitness.search.service;

import java.util.List;
import java.util.Map;

/**
 * Business Layer: Search Query Service Interface
 * Reference: 03_BranchContentServiceComponent.puml (ISearchQueryService)
 */
public interface ISearchQueryService {
    List<Map<String, Object>> search(String query, String userLocation);
}

