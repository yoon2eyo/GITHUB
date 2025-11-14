package com.smartfitness.facemodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * FaceModel Service - Face Vector Comparison Engine
 * DD-05: IPC-Based Performance Optimization
 * - Co-located with Access Service (Same Physical Node)
 * - Pipeline parallelization: 49% latency reduction
 * - Hot swap: Zero-downtime model updates (QAS-06)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories
@EnableAsync
public class FaceModelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaceModelServiceApplication.class, args);
    }
}

