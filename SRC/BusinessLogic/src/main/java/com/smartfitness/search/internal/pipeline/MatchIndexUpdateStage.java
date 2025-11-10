package com.smartfitness.search.internal.pipeline;

import com.smartfitness.search.ports.ISearchRepository;
import java.util.List;
import java.util.Objects;

/**
 * Stage B – persists keyword metadata and updates the DS-07 search index.
 */
public class MatchIndexUpdateStage {
    private final ISearchRepository searchRepository;

    public MatchIndexUpdateStage(ISearchRepository searchRepository) {
        this.searchRepository = Objects.requireNonNull(searchRepository, "searchRepository");
    }

    public void persistCustomerKeywords(Long customerId, List<String> keywords) {
        searchRepository.saveCustomerPreference(customerId, keywords);
    }

    public void persistBranchKeywords(Long sourceId, List<String> keywords) {
        searchRepository.saveBranchPreference(sourceId, keywords);
    }
}
