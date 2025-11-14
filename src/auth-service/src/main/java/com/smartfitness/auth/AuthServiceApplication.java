package com.smartfitness.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auth Service
 * UC-01, UC-02, UC-03: User Registration & Authentication
 * DD-08: Token-based Security (JWT)
 * Provides: Authentication, Authorization, User Management
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

