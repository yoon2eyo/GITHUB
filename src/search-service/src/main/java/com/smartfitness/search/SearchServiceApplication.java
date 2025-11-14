package com.smartfitness.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Search Service Application (Branch Content Service)
 * 
 * UC-09: Real-time Branch Search (Hot Path)
 * UC-10: Review Registration & Indexing (Cold Path)
 * UC-18: Branch Info Registration & Indexing (Cold Path)
 * 
 * DD-06, DD-09: Hot/Cold Path Separation
 * - Hot Path: NO LLM call → SLA guaranteed (QAS-03: < 3초)
 * - Cold Path: LLM analysis → Async indexing
 * 
 * DD-07: Scheduling Policy
 * - Defer matching during peak time
 * - Process in off-peak hours
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SearchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}

