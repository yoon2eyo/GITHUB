package com.smartfitness.search.ports;

import java.util.List;

/**
 * ILLMAnalysisService: External LLM analysis contract for extracting preference keywords.
 */
public interface ILLMAnalysisService {
    List<String> analyzeTextForPreferences(String text);
}

