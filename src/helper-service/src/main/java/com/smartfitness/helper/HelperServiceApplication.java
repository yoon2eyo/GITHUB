package com.smartfitness.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Helper Service Application
 * 
 * UC-12: Task Photo Registration (Helper uploads)
 * UC-13: AI Photo Analysis (ML Inference Engine)
 * UC-14: Reward Confirmation (BranchOwner approval)
 * UC-16: Reward Balance Update (Event-driven)
 * 
 * DD-02: Event-Based Architecture
 * - TaskSubmittedEvent → AI Analysis
 * - TaskConfirmedEvent → Reward Update
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
public class HelperServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelperServiceApplication.class, args);
    }
}

