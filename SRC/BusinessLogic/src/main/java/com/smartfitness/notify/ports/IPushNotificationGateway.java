package com.smartfitness.notify.ports;

/**
 * IPushNotificationGateway: External gateway contract to send push notifications.
 */
public interface IPushNotificationGateway {
    void sendPushNotification(String recipientId, String title, String body);
}

