package com.smartfitness.facemodel.service;

import com.smartfitness.facemodel.adapter.IMLInferenceEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Feature Extractor
 * Component: FeatureExtractor
 * 
 * Wraps ML Inference Engine for feature extraction
 * Uses current active model from ModelLifecycleManager
 * 
 * Reference: 12_FaceModelServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractor implements IFeatureExtractionService {
    
    private final IMLInferenceEngine mlInferenceEngine;
    
    @Override
    public float[] extractFeatures(byte[] photoBytes) {
        log.debug("Extracting features using ML inference engine");
        
        long startTime = System.currentTimeMillis();
        
        // Call ML inference engine (DD-05: Local call, ~200ms)
        float[] features = mlInferenceEngine.extractFeatures(photoBytes);
        
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Feature extraction completed: {} dims in {}ms", features.length, duration);
        
        return features;
    }
}

