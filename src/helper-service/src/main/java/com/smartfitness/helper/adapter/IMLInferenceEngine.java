package com.smartfitness.helper.adapter;

/**
 * System Interface Layer: ML Inference Engine Interface
 * Reference: 04_HelperServiceComponent.puml (IMLInferenceEngine)
 */
public interface IMLInferenceEngine {
    String analyzeImage(byte[] imageBytes);
}

