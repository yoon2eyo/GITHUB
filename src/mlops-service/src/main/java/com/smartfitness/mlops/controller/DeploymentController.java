package com.smartfitness.mlops.controller;

import com.smartfitness.mlops.service.IModelDeploymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Deployment Controller
 * Component: DeploymentController
 * 
 * Deploys trained ML models to FaceModel Service
 * DD-05: Model Lifecycle Management with Hot Swap
 * 
 * Reference: 11_MLOpsServiceComponent.puml (IModelDeploymentApi)
 */
@Slf4j
@RestController
@RequestMapping("/mlops/deployment")
@RequiredArgsConstructor
public class DeploymentController implements IModelDeploymentApi {
    
    private final IModelDeploymentService modelDeploymentService;
    
    @Override
    @PostMapping("/deploy")
    public ResponseEntity<Map<String, String>> deployModel(@RequestParam String modelId) {
        log.info("Model deployment request: modelId={}", modelId);
        
        String deploymentId = modelDeploymentService.deployModel(modelId);
        
        return ResponseEntity.ok(Map.of(
                "deploymentId", deploymentId,
                "status", "DEPLOYED"
        ));
    }
    
    @Override
    @GetMapping("/status/{deploymentId}")
    public ResponseEntity<Map<String, String>> getDeploymentStatus(@PathVariable String deploymentId) {
        log.info("Get deployment status: {}", deploymentId);
        
        return ResponseEntity.ok(Map.of(
                "deploymentId", deploymentId,
                "status", "ACTIVE"
        ));
    }
}

