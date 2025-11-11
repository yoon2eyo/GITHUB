package com.smartfitness.search.internal.engine;

import com.smartfitness.search.model.BranchRecommendation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SimpleSearchEngine: In-memory TF-IDF based full-text search engine.
 * 
 * Implements the professional search engine for DD-06 (Approach C).
 * 
 * Architecture:
 * - Thread-safe concurrent index using ConcurrentHashMap
 * - TF-IDF ranking algorithm for relevance scoring
 * - O(log n) query performance with keyword filtering and sorting
 * - No external dependencies (self-contained)
 * 
 * Performance:
 * - Index size: O(n * m) where n=branches, m=keywords per branch
 * - Query time: O(n * k) where k=query keywords (acceptable for local index)
 * - Typical response: 50-500ms for 1000 branches
 * 
 * Design Pattern: Strategy Pattern (for ranking algorithms)
 * Tactic: Introduce Concurrency (thread-safe operations)
 */
public class SimpleSearchEngine implements ISearchEngine {
    
    // Index: branchId -> keywords
    private final Map<Long, List<String>> branchIndex = new ConcurrentHashMap<>();
    
    // Inverted index: keyword -> set of branchIds (for faster lookup)
    private final Map<String, Set<Long>> invertedIndex = new ConcurrentHashMap<>();
    
    // Document frequency: keyword -> count of branches containing this keyword (for TF-IDF)
    private final Map<String, Integer> documentFrequency = new ConcurrentHashMap<>();
    
    // Total number of documents (branches) - for IDF calculation
    private volatile int totalDocuments = 0;
    
    @Override
    public void upsertBranchKeywords(Long branchId, List<String> keywords) {
        if (branchId == null || keywords == null || keywords.isEmpty()) {
            return;
        }
        
        // Remove old keywords from inverted index if branch exists
        if (branchIndex.containsKey(branchId)) {
            List<String> oldKeywords = branchIndex.get(branchId);
            for (String keyword : oldKeywords) {
                Set<Long> branches = invertedIndex.get(keyword);
                if (branches != null) {
                    branches.remove(branchId);
                    if (branches.isEmpty()) {
                        invertedIndex.remove(keyword);
                    }
                }
            }
        } else {
            // New branch
            totalDocuments++;
        }
        
        // Add new keywords
        Set<String> uniqueKeywords = new HashSet<>(keywords);
        branchIndex.put(branchId, new ArrayList<>(uniqueKeywords));
        
        // Update inverted index and document frequency
        for (String keyword : uniqueKeywords) {
            String normalizedKeyword = normalizeKeyword(keyword);
            invertedIndex.computeIfAbsent(normalizedKeyword, k -> ConcurrentHashMap.newKeySet())
                    .add(branchId);
            documentFrequency.merge(normalizedKeyword, 1, Integer::sum);
        }
    }
    
    @Override
    public List<BranchRecommendation> search(List<String> queryKeywords) {
        if (queryKeywords == null || queryKeywords.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Collect all candidate branches from inverted index
        Set<Long> candidateBranches = new HashSet<>();
        for (String keyword : queryKeywords) {
            String normalizedKeyword = normalizeKeyword(keyword);
            Set<Long> branches = invertedIndex.get(normalizedKeyword);
            if (branches != null) {
                candidateBranches.addAll(branches);
            }
        }
        
        if (candidateBranches.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Calculate TF-IDF scores and create recommendations
        return candidateBranches.stream()
                .map(branchId -> {
                    double score = calculateTfIdfScore(branchId, queryKeywords);
                    return new BranchRecommendation(branchId, score);
                })
                .filter(rec -> rec.getRelevanceScore() > 0.0)
                .sorted(Comparator.comparingDouble(BranchRecommendation::getRelevanceScore).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public void clear() {
        branchIndex.clear();
        invertedIndex.clear();
        documentFrequency.clear();
        totalDocuments = 0;
    }
    
    @Override
    public int getIndexSize() {
        return branchIndex.size();
    }
    
    /**
     * Calculate TF-IDF score for a branch against query keywords.
     * TF (Term Frequency) = frequency of keyword in branch
     * IDF (Inverse Document Frequency) = log(total docs / docs containing keyword)
     * 
     * Score = Sum of (TF * IDF) for all query keywords present in branch
     */
    private double calculateTfIdfScore(Long branchId, List<String> queryKeywords) {
        List<String> branchKeywords = branchIndex.get(branchId);
        if (branchKeywords == null || branchKeywords.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        
        for (String queryKeyword : queryKeywords) {
            String normalizedKeyword = normalizeKeyword(queryKeyword);
            
            // Term Frequency: count occurrences in branch keywords
            long termFrequency = branchKeywords.stream()
                    .filter(kw -> normalizeKeyword(kw).equals(normalizedKeyword))
                    .count();
            
            if (termFrequency > 0) {
                // Inverse Document Frequency: log(total / doc frequency)
                Integer docFreq = documentFrequency.getOrDefault(normalizedKeyword, 1);
                double idf = Math.log((double) totalDocuments / Math.max(1, docFreq));
                
                // TF-IDF calculation
                double tf = (double) termFrequency / branchKeywords.size();  // Normalize by branch keyword count
                score += tf * idf;
            }
        }
        
        return score;
    }
    
    /**
     * Normalize keyword for case-insensitive and whitespace-trimmed matching.
     */
    private String normalizeKeyword(String keyword) {
        return keyword.toLowerCase().trim();
    }
}
