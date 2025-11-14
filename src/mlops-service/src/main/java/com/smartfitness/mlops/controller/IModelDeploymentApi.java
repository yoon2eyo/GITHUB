package com.smartfitness.mlops.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Model Deployment API Interface
 * Reference: 11_MLOpsServiceComponent.puml (IModelDeploymentApi)
 */
public interface IModelDeploymentApi {
    ResponseEntity<Map<String, String>> deployModel(String modelId);
    ResponseEntity<Map<String, String>> getDeploymentStatus(String deploymentId);
}

