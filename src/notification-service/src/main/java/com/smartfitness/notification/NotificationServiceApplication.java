package com.smartfitness.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Notification Service Application (Notification Dispatcher)
 * 
 * Event-driven Push Notification Service
 * - Subscribes to domain events via Message Broker
 * - Dispatches notifications via push gateway (FCM)
 * - Asynchronous event-driven notification delivery
 * 
 * Subscribed Events:
 * - EquipmentFaultEvent (from Monitoring Service)
 * - BranchPreferenceCreatedEvent (from Search Service)
 * 
 * Reference: 06_NotificationDispatcherComponent.puml
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

