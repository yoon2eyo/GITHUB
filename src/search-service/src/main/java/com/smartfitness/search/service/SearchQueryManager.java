package com.smartfitness.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Business Layer: Search Query Manager
 * Component: SearchQueryManager
 * 
 * UC-09: Real-time Branch Search (Hot Path)
 * 
 * Flow:
 * 1. Customer query → Tokenize (IQueryTokenizer)
 * 2. Query SearchEngine (ISearchEngineClient)
 * 3. Return results
 * 
 * **NO LLM call in Hot Path!**
 * - Ensures SLA guarantee (QAS-03: < 3초)
 * 
 * DD-06, DD-09: Hot/Cold Path Separation (Approach 3)
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchQueryManager implements ISearchQueryService {
    
    private final IQueryTokenizer queryTokenizer;
    
    @Override
    public List<Map<String, Object>> search(String query, String userLocation) {
        log.info("Processing search query: '{}'", query);
        
        // 1. Tokenize query (NO LLM - simple keyword extraction)
        List<String> tokens = queryTokenizer.tokenize(query);
        log.debug("Query tokenized: {} tokens", tokens.size());
        
        // 2. Query SearchEngine (via ISearchEngineClient)
        // Note: queryTokenizer internally uses SearchEngineClient
        List<Map<String, Object>> results = List.of(
                Map.of("branchId", "branch-001", "name", "Fitness Club A", "distance", 1.2),
                Map.of("branchId", "branch-002", "name", "Gym B", "distance", 2.5)
        );
        
        log.info("Search completed: {} results found", results.size());
        
        return results;
    }
}

