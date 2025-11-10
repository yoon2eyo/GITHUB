package com.smartfitness.search.ports;

import com.smartfitness.search.model.BranchRecommendation;
import java.util.List;

/**
 * ISearchRepository: Abstraction over DB_SEARCH and the underlying text/semantic engine.
 */
public interface ISearchRepository {
    List<BranchRecommendation> executeMatchQuery(List<String> keywords);
    void saveCustomerPreference(Long customerId, List<String> preferences);
    void saveBranchPreference(Long branchId, List<String> preferences);
}

