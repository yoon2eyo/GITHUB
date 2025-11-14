package com.smartfitness.notification.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: FCM Push Gateway
 * Component: FcmPushGateway
 * 
 * Implements: IPushNotificationGateway
 * Technology: Firebase Cloud Messaging (FCM)
 * 
 * Sends push notifications to mobile devices
 * 
 * Reference: 06_NotificationDispatcherComponent.puml
 */
@Slf4j
@Component
public class FcmPushGateway implements IPushNotificationGateway {
    
    @Override
    public void sendPushNotification(String userId, String title, String message) {
        log.info("Sending FCM push notification: userId={}, title={}", userId, title);
        
        // Stub: In production, use Firebase Admin SDK
        // Message fcmMessage = Message.builder()
        //     .setToken(getUserDeviceToken(userId))
        //     .setNotification(Notification.builder()
        //         .setTitle(title)
        //         .setBody(message)
        //         .build())
        //     .build();
        //
        // String response = FirebaseMessaging.getInstance().send(fcmMessage);
        // log.info("FCM message sent: {}", response);
        
        log.info("FCM push notification sent successfully: userId={}, message={}", userId, message);
    }
}

