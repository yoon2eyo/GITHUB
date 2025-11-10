package com.smartfitness.search.ports;

import com.smartfitness.search.model.SearchQuery;
import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.search.model.ContentType;
import java.util.List;

/**
 * ISearchServiceApi: Main API for semantic search and content registration.
 */
public interface ISearchServiceApi {
    /**
     * Process a semantic search query and return recommended branches (UC-09).
     */
    List<BranchRecommendation> searchBranches(SearchQuery query, Long customerId);

    /**
     * Register user content (review/posts) for preference extraction (UC-10, UC-18).
     */
    void registerContent(String content, Long sourceId, ContentType type);
}

