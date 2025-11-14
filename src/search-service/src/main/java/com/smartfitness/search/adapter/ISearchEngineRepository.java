package com.smartfitness.search.adapter;

import java.util.List;
import java.util.Map;

/**
 * System Interface Layer: Search Engine Repository Interface
 * Reference: 03_BranchContentServiceComponent.puml (ISearchEngineRepository)
 */
public interface ISearchEngineRepository {
    List<Map<String, Object>> search(List<String> keywords, String location);
    void index(String documentId, Map<String, Object> document);
}

