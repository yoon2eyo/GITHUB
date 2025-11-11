package com.smartfitness.search.internal.engine;

import com.smartfitness.search.model.BranchRecommendation;
import java.util.List;

/**
 * ISearchEngine: Professional full-text search engine interface.
 * 
 * This is the core contract for implementing fast branch matching with keyword-based indexing.
 * Supports:
 * - Upsert (index/update): Add or update branch keywords in the search index
 * - Query (search): Execute keyword-based searches with TF-IDF ranking
 * - Thread-safe concurrent access
 * 
 * DD-06 Implementation: Approach C (전문 검색 엔진)
 * - Separates LLM (slow, external) from search (fast, local)
 * - Guarantees QAS-03 (3 seconds) by avoiding external calls in Hot Path
 * - Supports async pre-indexing via PreferenceMatchConsumer
 */
public interface ISearchEngine {
    
    /**
     * Upsert (insert or update) keywords for a branch into the search index.
     * This is called during async pre-indexing (Cold Path) via PreferenceMatchConsumer.
     * 
     * @param branchId Unique branch identifier
     * @param keywords List of extracted keywords/terms from LLM analysis
     */
    void upsertBranchKeywords(Long branchId, List<String> keywords);
    
    /**
     * Execute a keyword-based search query against indexed branches.
     * This is the main Hot Path operation - must be fast and avoid external calls.
     * 
     * @param queryKeywords Customer-provided or tokenized query keywords
     * @return Ranked list of branch recommendations, sorted by relevance (TF-IDF)
     */
    List<BranchRecommendation> search(List<String> queryKeywords);
    
    /**
     * Clear all indexed data (used for testing/reset).
     */
    void clear();
    
    /**
     * Get current index size (number of branches indexed).
     */
    int getIndexSize();
}
