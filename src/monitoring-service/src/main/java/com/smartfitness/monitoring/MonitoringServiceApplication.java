package com.smartfitness.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Monitoring Service Application
 * 
 * DD-04: Fault Detection & Real-time Notification
 * - Heartbeat: Equipment-driven reporting (every 10 min)
 * - Ping/Echo: System-driven monitoring (every 10 sec)
 * 
 * QAS-01: Alert within 15초
 * 
 * Tactics:
 * - Heartbeat (Availability)
 * - Ping/echo (Availability)
 * - Maintain Audit Trail (Security)
 * - Passive Redundancy (Availability)
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class MonitoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitoringServiceApplication.class, args);
    }
}

