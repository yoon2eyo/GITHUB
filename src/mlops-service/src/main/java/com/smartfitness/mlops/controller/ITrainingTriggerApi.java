package com.smartfitness.mlops.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Training Trigger API Interface
 * Reference: 11_MLOpsServiceComponent.puml (ITrainingTriggerApi)
 */
public interface ITrainingTriggerApi {
    ResponseEntity<Map<String, String>> triggerTraining(String modelType);
}

