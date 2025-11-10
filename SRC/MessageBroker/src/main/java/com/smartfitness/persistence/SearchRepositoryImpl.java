package com.smartfitness.persistence;

import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.search.ports.ISearchRepository;

import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

/**
 * SearchRepositoryImpl: DAL for DB_SEARCH / DS-07 engine.
 */
public class SearchRepositoryImpl implements ISearchRepository {
    private final DataSource dataSource;

    public SearchRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<BranchRecommendation> executeMatchQuery(List<String> keywords) {
        // TODO: Delegate to search index / DS-07 using keywords.
        return Collections.emptyList();
    }

    @Override
    public void saveCustomerPreference(Long customerId, List<String> preferences) {
        // TODO: Persist customer preferences.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void saveBranchPreference(Long branchId, List<String> preferences) {
        // TODO: Persist branch preferences for downstream matching.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
