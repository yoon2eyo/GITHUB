package com.smartfitness.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Access Service - Real-Time Access Control
 * UC-08: Face Recognition Access
 * DD-05: IPC Optimization (Same Physical Node, Pipeline, Pre-Fetching)
 * QAS-02: 95% of access within 3 seconds
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories
@EnableCaching
public class AccessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccessServiceApplication.class, args);
    }
}

