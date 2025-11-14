package com.smartfitness.search.service;

import com.smartfitness.common.event.SearchQueryEvent;
import com.smartfitness.search.adapter.ILLMAnalysisServiceClient;
import com.smartfitness.search.adapter.IMessageSubscriptionService;
import com.smartfitness.search.adapter.ISearchEngineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Business Layer: Search Query Improvement Consumer
 * Component: SearchQueryImprovementConsumer
 * 
 * DD-09: Cold Path - Search Query Improvement
 * 
 * Flow:
 * 1. Subscribes to SearchQueryEvent from Message Broker
 * 2. Calls LLM Service to extract keywords from query
 * 3. Updates ElasticSearch index with improved keywords
 * 4. Improves future search accuracy
 * 
 * **Cold Path Only** - Async processing, no SLA constraint
 * - LLM call is acceptable here (not in Hot Path)
 * - Batch processing for cost efficiency (DD-09: 10% sampling)
 * 
 * DD-06, DD-09: Hot/Cold Path Separation (Approach 3)
 * - Hot Path: Immediate response (already completed)
 * - Cold Path: Index improvement for future searches
 * 
 * Reference: DD-09_Approach_A_Hybrid.puml (LLM Keyword Extractor)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchQueryImprovementConsumer {
    
    private final ILLMAnalysisServiceClient llmAnalysisServiceClient;
    private final ISearchEngineRepository searchEngineRepository;
    private final IMessageSubscriptionService messageSubscriptionService;
    
    /**
     * Subscribe to SearchQueryEvent for Cold Path processing
     */
    public void subscribeToSearchQueryEvent() {
        log.info("Subscribing to SearchQueryEvent for Cold Path improvement");
        messageSubscriptionService.subscribe("SearchQueryEvent", this);
    }
    
    /**
     * Handle SearchQueryEvent from Cold Path
     * 
     * DD-09: LLM analyzes query and improves ElasticSearch index
     * - Sampling: Only process 10% of queries for cost efficiency
     * - Async: No impact on Hot Path response time
     */
    public void handleSearchQueryEvent(SearchQueryEvent event) {
        log.info("Processing SearchQueryEvent for Cold Path improvement: query='{}', customerId='{}'", 
                event.getQuery(), event.getCustomerId());
        
        // DD-09: Sampling - Only process 10% of queries for cost efficiency
        // In production, use consistent hashing or random sampling
        if (shouldProcessQuery(event)) {
            try {
                // 1. Call LLM Service to extract keywords from query
                Map<String, Object> analysis = llmAnalysisServiceClient.extractKeywords(event.getQuery());
                log.debug("LLM analysis completed: {}", analysis);
                
                // 2. Extract keywords from LLM response
                @SuppressWarnings("unchecked")
                java.util.List<String> keywords = (java.util.List<String>) analysis.getOrDefault("keywords", java.util.List.of());
                
                // 3. Update ElasticSearch index with improved keywords
                // This improves future search accuracy for similar queries
                updateSearchIndex(event.getQuery(), keywords);
                
                log.info("Search index improved for query: '{}' with {} keywords", 
                        event.getQuery(), keywords.size());
                
            } catch (Exception e) {
                log.error("Error processing SearchQueryEvent for Cold Path improvement: {}", 
                        event.getQuery(), e);
                // Cold Path failures should not affect Hot Path
                // Log error but don't throw exception
            }
        } else {
            log.debug("Skipping SearchQueryEvent (sampling): query='{}'", event.getQuery());
        }
    }
    
    /**
     * DD-09: Sampling Policy - Only process 10% of queries
     * 
     * In production, use consistent hashing based on query hash
     * to ensure same query is always processed or skipped consistently
     */
    private boolean shouldProcessQuery(SearchQueryEvent event) {
        // Simple sampling: 10% of queries
        // In production, use: event.getQuery().hashCode() % 10 == 0
        return event.getQuery().hashCode() % 10 == 0;
    }
    
    /**
     * Update ElasticSearch index with improved keywords
     * 
     * This improves future search accuracy for similar queries
     */
    private void updateSearchIndex(String query, java.util.List<String> keywords) {
        // Update ElasticSearch synonym dictionary or query expansion index
        // This is a stub - in production, update ElasticSearch synonym filter
        log.debug("Updating search index: query='{}', keywords={}", query, keywords);
        
        // Example: Update synonym dictionary
        // searchEngineRepository.updateSynonyms(query, keywords);
        
        // For now, just log the improvement
        log.info("Search index update (stub): query='{}' → keywords={}", query, keywords);
    }
}

