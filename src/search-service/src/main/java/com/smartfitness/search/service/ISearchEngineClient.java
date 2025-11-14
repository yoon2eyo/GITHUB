package com.smartfitness.search.service;

import java.util.List;
import java.util.Map;

/**
 * Business Layer: Search Engine Client Interface
 * Reference: 03_BranchContentServiceComponent.puml (ISearchEngineClient)
 */
public interface ISearchEngineClient {
    List<Map<String, Object>> query(List<String> keywords, String location);
    void index(String documentId, Map<String, Object> document);
}

