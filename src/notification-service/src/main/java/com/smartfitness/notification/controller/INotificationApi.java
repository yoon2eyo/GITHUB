package com.smartfitness.notification.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Notification API Interface
 * Reference: 06_NotificationDispatcherComponent.puml (INotificationApi)
 */
public interface INotificationApi {
    ResponseEntity<Map<String, String>> sendNotification(String userId, String message);
}

