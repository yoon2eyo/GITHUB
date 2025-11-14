package com.smartfitness.notification.service;

import com.smartfitness.notification.adapter.IPushNotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Notification Dispatcher Manager
 * Component: NotificationDispatcherManager
 * 
 * Dispatches notifications to users via push gateway
 * 
 * Reference: 06_NotificationDispatcherComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcherManager implements INotificationDispatcherService {
    
    private final IPushNotificationGateway pushNotificationGateway;
    
    @Override
    public void sendNotification(String userId, String message) {
        log.info("Dispatching notification: userId={}, message={}", userId, message);
        
        pushNotificationGateway.sendPushNotification(userId, "Smart Fitness", message);
        
        log.info("Notification dispatched successfully: userId={}", userId);
    }
}

