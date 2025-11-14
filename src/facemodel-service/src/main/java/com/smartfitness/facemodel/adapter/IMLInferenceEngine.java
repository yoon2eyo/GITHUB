package com.smartfitness.facemodel.adapter;

/**
 * System Interface Layer: ML Inference Engine Interface
 * Wraps ML model (TensorFlow, PyTorch, ONNX, etc.)
 * Reference: 12_FaceModelServiceComponent.puml
 */
public interface IMLInferenceEngine {
    boolean deployModel(String modelPath);
    boolean rollbackModel(String modelPath);
    float[] extractFeatures(byte[] imageBytes);
    boolean isModelLoaded();
    String getCurrentModelVersion();
}

