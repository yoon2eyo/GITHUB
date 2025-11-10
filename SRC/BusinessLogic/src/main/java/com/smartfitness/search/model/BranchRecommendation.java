package com.smartfitness.search.model;

/**
 * BranchRecommendation: Recommendation item returned by search.
 */
public class BranchRecommendation {
    private final Long branchId;
    private final String name;
    private final double score;

    public BranchRecommendation(Long branchId, String name, double score) {
        this.branchId = branchId;
        this.name = name;
        this.score = score;
    }

    public Long getBranchId() { return branchId; }
    public String getName() { return name; }
    public double getScore() { return score; }
}

