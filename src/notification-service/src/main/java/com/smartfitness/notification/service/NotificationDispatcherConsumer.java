package com.smartfitness.notification.service;

import com.smartfitness.notification.adapter.IMessageSubscriptionService;
import com.smartfitness.notification.adapter.IPushNotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Notification Dispatcher Consumer
 * Component: NotificationDispatcherConsumer
 * 
 * Subscribes to domain events and dispatches notifications
 * 
 * Subscribed Events:
 * - EquipmentFaultEvent (from Monitoring Service)
 * - BranchPreferenceCreatedEvent (from Search Service)
 * 
 * Reference: 06_NotificationDispatcherComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcherConsumer {
    
    private final IMessageSubscriptionService messageSubscriptionService;
    private final IPushNotificationGateway pushNotificationGateway;
    
    /**
     * Subscribe to notification events
     */
    public void subscribeToEvents() {
        log.info("Subscribing to notification events");
        messageSubscriptionService.subscribe("EquipmentFaultEvent", this);
        messageSubscriptionService.subscribe("BranchPreferenceCreatedEvent", this);
    }
    
    /**
     * Handle EquipmentFaultEvent
     * Send alert to branch owner
     */
    public void handleEquipmentFaultEvent(String equipmentId, String faultType, String timestamp) {
        log.info("Handling EquipmentFaultEvent: equipmentId={}, faultType={}", equipmentId, faultType);
        
        // Stub: Get branch owner ID for this equipment
        String branchOwnerId = "owner-id"; // Stub
        
        String message = String.format(
                "Equipment Fault Alert: Equipment %s has fault '%s' at %s",
                equipmentId, faultType, timestamp
        );
        
        pushNotificationGateway.sendPushNotification(branchOwnerId, "Equipment Fault Alert", message);
        
        log.info("Fault notification sent to branch owner: {}", branchOwnerId);
    }
    
    /**
     * Handle BranchPreferenceCreatedEvent
     * Notify branch owner about new preference match
     */
    public void handleBranchPreferenceCreatedEvent(String branchId, String preferenceDetails) {
        log.info("Handling BranchPreferenceCreatedEvent: branchId={}", branchId);
        
        // Stub: Get branch owner ID
        String branchOwnerId = "owner-id"; // Stub
        
        String message = String.format(
                "New Branch Preference: Your branch has new preference settings created"
        );
        
        pushNotificationGateway.sendPushNotification(branchOwnerId, "Branch Preference Update", message);
        
        log.info("Preference notification sent to branch owner: {}", branchOwnerId);
    }
}

