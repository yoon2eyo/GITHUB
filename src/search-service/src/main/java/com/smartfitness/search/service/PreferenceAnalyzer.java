package com.smartfitness.search.service;

import com.smartfitness.search.adapter.ILLMAnalysisServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Business Layer: Preference Analyzer
 * Component: PreferenceAnalyzer
 * 
 * UC-10, UC-18: Content Analysis (Cold Path)
 * 
 * Flow:
 * 1. Receives review/branch info content
 * 2. Calls external LLM service for keyword extraction
 * 3. Returns keywords/preferences
 * 
 * **Used ONLY in Cold Path** (NOT in Hot Path search)
 * - External LLM call is acceptable here
 * - Async processing, no SLA constraint
 * 
 * DD-06: Cold Path can use external LLM
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreferenceAnalyzer implements IPreferenceAnalysisService {
    
    private final ILLMAnalysisServiceClient llmAnalysisServiceClient;
    
    @Override
    public Map<String, Object> analyzePreference(String content) {
        log.info("Analyzing preference with LLM: {} chars", content.length());
        
        // Call external LLM service (Cold Path only)
        Map<String, Object> analysis = llmAnalysisServiceClient.extractKeywords(content);
        
        log.info("LLM analysis completed: {} keywords", analysis.size());
        
        return analysis;
    }
}

