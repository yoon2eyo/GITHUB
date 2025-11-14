package com.smartfitness.notification.service;

/**
 * Business Layer: Notification Dispatcher Service Interface
 * Reference: 06_NotificationDispatcherComponent.puml (INotificationDispatcherService)
 */
public interface INotificationDispatcherService {
    void sendNotification(String userId, String message);
}

