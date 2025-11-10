package com.smartfitness.search.internal.pipeline;

import com.smartfitness.search.internal.index.BranchPreferenceIndex;
import com.smartfitness.search.ports.ISearchRepository;
import java.util.List;
import java.util.Objects;

/**
 * Stage B: persists keyword metadata and updates the preference search index.
 */
public class MatchIndexUpdateStage {
    private final ISearchRepository searchRepository;
    private final BranchPreferenceIndex branchPreferenceIndex;

    public MatchIndexUpdateStage(ISearchRepository searchRepository, BranchPreferenceIndex branchPreferenceIndex) {
        this.searchRepository = Objects.requireNonNull(searchRepository, "searchRepository");
        this.branchPreferenceIndex = Objects.requireNonNull(branchPreferenceIndex, "branchPreferenceIndex");
    }

    public void persistCustomerKeywords(Long customerId, List<String> keywords) {
        searchRepository.saveCustomerPreference(customerId, keywords);
        branchPreferenceIndex.upsertCustomerKeywords(customerId, keywords);
    }

    public void persistBranchKeywords(Long sourceId, List<String> keywords) {
        searchRepository.saveBranchPreference(sourceId, keywords);
        branchPreferenceIndex.upsertBranchKeywords(sourceId, keywords);
    }
}
