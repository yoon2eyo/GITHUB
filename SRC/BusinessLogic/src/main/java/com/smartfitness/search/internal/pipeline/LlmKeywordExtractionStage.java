package com.smartfitness.search.internal.pipeline;

import com.smartfitness.search.ports.ILLMAnalysisService;
import java.util.List;
import java.util.Objects;

/**
 * Stage A – calls the external LLM service to transform raw text into preference keywords.
 */
public class LlmKeywordExtractionStage {
    private final ILLMAnalysisService llmAnalysisService;

    public LlmKeywordExtractionStage(ILLMAnalysisService llmAnalysisService) {
        this.llmAnalysisService = Objects.requireNonNull(llmAnalysisService, "llmAnalysisService");
    }

    public List<String> extractKeywords(String content) {
        return llmAnalysisService.analyzeTextForPreferences(content);
    }
}
