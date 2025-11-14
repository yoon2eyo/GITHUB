package com.smartfitness.branchowner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * BranchOwner Service Application
 * 
 * UC-03: Branch Owner Account Registration
 * UC-18: Branch Info Registration
 * UC-19: Customer Review Inquiry
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BranchOwnerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BranchOwnerServiceApplication.class, args);
    }
}

