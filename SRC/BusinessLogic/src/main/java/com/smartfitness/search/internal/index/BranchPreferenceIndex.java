package com.smartfitness.search.internal.index;

import com.smartfitness.search.model.BranchRecommendation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified in-memory index that mimics the external preference search engine until
 * the actual DS tier is integrated.
 */
public class BranchPreferenceIndex {
    private final Map<Long, List<String>> customerKeywords = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> branchKeywords = new ConcurrentHashMap<>();

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
        branchKeywords.put(branchId, List.copyOf(keywords));
    }

    public List<BranchRecommendation> queryByKeywords(List<String> keywords) {
        Objects.requireNonNull(keywords, "keywords");
        // TODO: integrate with actual search engine and return ranked recommendations.
        return Collections.emptyList();
    }
}
