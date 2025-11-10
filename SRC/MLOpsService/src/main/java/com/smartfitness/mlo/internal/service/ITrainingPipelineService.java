package com.smartfitness.mlo.internal.service;

import java.util.List;

/**
 * ITrainingPipelineService: Orchestrates the training pipeline workflow.
 */
public interface ITrainingPipelineService {
    /**
     * Execute complete training pipeline.
     */
    String executeTrainingPipeline(List<byte[]> trainingData);
    
    /**
     * Get pipeline execution status.
     */
    String getPipelineStatus(String pipelineId);
    
    /**
     * Cancel running pipeline.
     */
    void cancelPipeline(String pipelineId);
}