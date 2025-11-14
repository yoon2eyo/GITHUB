package com.smartfitness.mlops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MLOps Service Application
 * 
 * ML Model Lifecycle Management
 * - Training Pipeline Orchestration
 * - Model Verification (Accuracy + Performance)
 * - Model Deployment (with Hot Swap support)
 * - Training Data Management
 * 
 * DD-03 Exception: READ-ONLY access to Auth & Helper DB for training data
 * DD-05: Model Lifecycle Management
 * 
 * Reference: 11_MLOpsServiceComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MLOpsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MLOpsServiceApplication.class, args);
    }
}

