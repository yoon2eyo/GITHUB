package com.smartfitness.notification.adapter;

/**
 * System Interface Layer: Push Notification Gateway Interface
 * Reference: 06_NotificationDispatcherComponent.puml (IPushNotificationGateway)
 */
public interface IPushNotificationGateway {
    void sendPushNotification(String userId, String title, String message);
}

