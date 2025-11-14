package com.smartfitness.helper.service;

import com.smartfitness.helper.adapter.IMLInferenceEngine;
import com.smartfitness.helper.adapter.ITaskPhotoStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Task Analysis Engine
 * Component: TaskAnalysisEngine
 * 
 * UC-13: AI Photo Analysis
 * 1. Retrieve photo from storage (S3)
 * 2. Call ML Inference Engine for analysis
 * 3. Result: 양호 (Good) / 미흡 (Insufficient) / 불분명 (Unclear)
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAnalysisEngine implements ITaskAnalysisService {
    
    private final ITaskPhotoStorage taskPhotoStorage;
    private final IMLInferenceEngine mlInferenceEngine;
    
    @Override
    public String analyzeTask(String taskId, String photoUrl) {
        log.info("Starting AI analysis for task: {}", taskId);
        
        // 1. Retrieve photo from S3
        byte[] photoBytes = taskPhotoStorage.downloadPhoto(photoUrl);
        log.debug("Photo retrieved from storage: {} bytes", photoBytes.length);
        
        // 2. Call ML Inference Engine
        String analysisResult = mlInferenceEngine.analyzeImage(photoBytes);
        log.info("AI analysis completed for task {}: {}", taskId, analysisResult);
        
        // 3. Return result (양호/미흡/불분명)
        return analysisResult;
    }
}

