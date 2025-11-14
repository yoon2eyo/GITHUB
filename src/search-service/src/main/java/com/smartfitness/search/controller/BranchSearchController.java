package com.smartfitness.search.controller;

import com.smartfitness.search.service.ISearchQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: Branch Search Controller
 * Component: BranchSearchController
 * 
 * UC-09: Real-time Branch Search (Hot Path)
 * - Customer searches branches by keyword
 * - Target: 95% < 3초 (QAS-03)
 * - NO LLM call in Hot Path → SLA guaranteed
 * 
 * DD-06, DD-09: Hot/Cold Path Separation (Approach 3)
 * 
 * Reference: 03_BranchContentServiceComponent.puml (IBranchSearchApi)
 */
@Slf4j
@RestController
@RequestMapping("/search/branches")
@RequiredArgsConstructor
public class BranchSearchController implements IBranchSearchApi {
    
    private final ISearchQueryService searchQueryService;
    
    /**
     * UC-09: Search branches (Hot Path)
     * NO LLM call → Ensures SLA
     */
    @Override
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> searchBranches(
            @RequestParam String query,
            @RequestParam(required = false) String userLocation) {
        
        log.info("Search request: query='{}', location='{}'", query, userLocation);
        
        // Hot Path: Simple keyword search (NO LLM)
        List<Map<String, Object>> results = searchQueryService.search(query, userLocation);
        
        log.info("Search completed: {} results", results.size());
        
        return ResponseEntity.ok(results);
    }
}

