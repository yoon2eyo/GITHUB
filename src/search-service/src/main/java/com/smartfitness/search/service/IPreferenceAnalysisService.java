package com.smartfitness.search.service;

import java.util.Map;

/**
 * Business Layer: Preference Analysis Service Interface
 * Reference: 03_BranchContentServiceComponent.puml (IPreferenceAnalysisService)
 */
public interface IPreferenceAnalysisService {
    Map<String, Object> analyzePreference(String content);
}

