package com.smartfitness.search.model;

/**
 * BranchRecommendation: Recommendation item returned by search.
 * 
 * Represents a ranked branch result from the search engine (DD-06).
 * Can be created with or without branch name (name is optional for internal search operations).
 */
public class BranchRecommendation {
    private final Long branchId;
    private final String name;
    private final double score;

    /**
     * Constructor with all fields (used when full branch info is available).
     */
    public BranchRecommendation(Long branchId, String name, double score) {
        this.branchId = branchId;
        this.name = name;
        this.score = score;
    }
    
    /**
     * Constructor with score only (used by search engine for TF-IDF ranking).
     * Name will be null and can be populated later.
     */
    public BranchRecommendation(Long branchId, double score) {
        this(branchId, null, score);
    }

    public Long getBranchId() { return branchId; }
    public String getName() { return name; }
    public double getScore() { return score; }
    public double getRelevanceScore() { return score; }  // Alias for consistency
}

