package com.smartfitness.mlops.controller;

import com.smartfitness.mlops.service.ITrainingTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Training Controller
 * Component: TrainingController
 * 
 * Triggers ML model training pipeline
 * 
 * Reference: 11_MLOpsServiceComponent.puml (ITrainingTriggerApi)
 */
@Slf4j
@RestController
@RequestMapping("/mlops/training")
@RequiredArgsConstructor
public class TrainingController implements ITrainingTriggerApi {
    
    private final ITrainingTriggerService trainingTriggerService;
    
    @Override
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerTraining(@RequestParam String modelType) {
        log.info("Training trigger request: modelType={}", modelType);
        
        String trainingId = trainingTriggerService.triggerTraining(modelType);
        
        return ResponseEntity.ok(Map.of(
                "trainingId", trainingId,
                "status", "STARTED"
        ));
    }
}

