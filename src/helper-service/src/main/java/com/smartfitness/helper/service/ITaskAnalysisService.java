package com.smartfitness.helper.service;

/**
 * Business Layer: Task Analysis Service Interface
 * Reference: 04_HelperServiceComponent.puml (ITaskAnalysisService)
 */
public interface ITaskAnalysisService {
    String analyzeTask(String taskId, String photoUrl);
}

