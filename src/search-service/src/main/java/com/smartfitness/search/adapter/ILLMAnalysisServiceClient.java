package com.smartfitness.search.adapter;

import java.util.Map;

/**
 * System Interface Layer: LLM Analysis Service Client Interface
 * Reference: 03_BranchContentServiceComponent.puml (ILLMAnalysisServiceClient)
 */
public interface ILLMAnalysisServiceClient {
    Map<String, Object> extractKeywords(String content);
}

