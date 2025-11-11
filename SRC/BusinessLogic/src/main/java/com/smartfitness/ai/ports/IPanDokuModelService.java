package com.smartfitness.ai.ports;

import java.util.List;

/**
 * IPanDokuModelService: Internal ML model service contract (NOT external service).
 * Smart Fitness' self-developed ML processing engine for natural language processing,
 * face recognition, and model management.
 * 
 * This is an internal component providing ML capabilities across multiple services.
 * Architecture: Internal process calls (NOT gRPC/REST).
 */
public interface IPanDokuModelService {
    
    /**
     * Analyze user query and extract key terms for branch content search.
     * Used by: AIService for natural language processing.
     * 
     * @param query User's natural language query
     * @return List of extracted keywords/terms
     */
    List<String> analyzeQuery(String query);
    
    /**
     * Generate feature vector from face image for recognition.
     * Used by: RealTimeAccessService for face vectorization.
     * 
     * @param imageData Face image binary data
     * @return Feature vector (double array)
     */
    double[] generateVector(byte[] imageData);
    
    /**
     * Deploy a new or updated model version to production.
     * Used by: FaceModelServiceComponent for model lifecycle management.
     * 
     * @param modelVersion Version identifier
     * @return Deployment result/status
     */
    String deployModel(String modelVersion);
    
    /**
     * Train model with provided dataset.
     * Used by: MLOpsService for model training.
     * 
     * @param trainingDataPath Path to training dataset
     * @return Training result/metrics
     */
    String trainModel(String trainingDataPath);
    
    /**
     * Monitor model performance and retrieve metrics.
     * Used by: MLOpsService for ongoing model health checks.
     * 
     * @return Model performance metrics
     */
    String monitorModel();
    
    /**
     * Get current model status and deployment information.
     * Used by: All services for status inquiry.
     * 
     * @return Model status information
     */
    String getModelStatus();
}


