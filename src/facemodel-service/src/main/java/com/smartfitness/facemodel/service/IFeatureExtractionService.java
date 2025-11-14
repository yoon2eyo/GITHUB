package com.smartfitness.facemodel.service;

/**
 * Business Layer Interface: Feature Extraction Service
 * Wraps ML model inference for feature extraction
 * Reference: 12_FaceModelServiceComponent.puml
 */
public interface IFeatureExtractionService {
    float[] extractFeatures(byte[] photoBytes);
}

