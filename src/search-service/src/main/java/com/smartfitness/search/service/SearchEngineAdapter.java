package com.smartfitness.search.service;

import com.smartfitness.search.adapter.ISearchEngineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Business Layer: Search Engine Adapter
 * Component: SearchEngineAdapter
 * 
 * Adapts SearchEngine operations for Business Layer
 * - Query: Hot Path (real-time search)
 * - Index: Cold Path (async indexing)
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEngineAdapter implements ISearchEngineClient {
    
    private final ISearchEngineRepository searchEngineRepository;
    
    @Override
    public List<Map<String, Object>> query(List<String> keywords, String location) {
        log.debug("Querying SearchEngine: keywords={}, location={}", keywords, location);
        
        // Query ElasticSearch
        return searchEngineRepository.search(keywords, location);
    }
    
    @Override
    public void index(String documentId, Map<String, Object> document) {
        log.info("Indexing document: {}", documentId);
        
        // Index to ElasticSearch
        searchEngineRepository.index(documentId, document);
    }
}

