package com.smartfitness.search.internal.index;

import com.smartfitness.search.internal.engine.ISearchEngine;
import com.smartfitness.search.internal.engine.SimpleSearchEngine;
import com.smartfitness.search.model.BranchRecommendation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BranchPreferenceIndex: Wrapper around the professional search engine.
 * 
 * This provides a unified interface to:
 * - Store customer keywords (for future personalized ranking)
 * - Delegate branch keyword indexing to ISearchEngine
 * - Execute full-text search queries
 * 
 * DD-06 Implementation:
 * - Separates LLM (Cold Path) from search engine (Hot Path)
 * - Uses SimpleSearchEngine for TF-IDF ranking
 * - Thread-safe concurrent operations
 */
public class BranchPreferenceIndex {
    private final Map<Long, List<String>> customerKeywords = new ConcurrentHashMap<>();
    private final ISearchEngine searchEngine;

    public BranchPreferenceIndex() {
        this(new SimpleSearchEngine());
    }

    public BranchPreferenceIndex(ISearchEngine searchEngine) {
        this.searchEngine = Objects.requireNonNull(searchEngine, "searchEngine");
    }

    public void upsertCustomerKeywords(Long customerId, List<String> keywords) {
        if (customerId == null || keywords == null) {
            return;
        }
        customerKeywords.put(customerId, List.copyOf(keywords));
    }

    public void upsertBranchKeywords(Long branchId, List<String> keywords) {
        if (branchId == null || keywords == null) {
            return;
        }
        // Delegate to search engine for indexing
        searchEngine.upsertBranchKeywords(branchId, keywords);
    }

    /**
     * Execute keyword-based search query against indexed branches.
     * This is the main Hot Path operation using the professional search engine.
     * 
     * @param keywords Query keywords from customer or tokenizer
     * @return Ranked list of branch recommendations (TF-IDF sorted)
     */
    public List<BranchRecommendation> queryByKeywords(List<String> keywords) {
        Objects.requireNonNull(keywords, "keywords");
        // Use search engine to execute fast, local search
        return searchEngine.search(keywords);
    }
    
    /**
     * Get the underlying search engine (for testing or advanced operations).
     */
    public ISearchEngine getSearchEngine() {
        return searchEngine;
    }
}
