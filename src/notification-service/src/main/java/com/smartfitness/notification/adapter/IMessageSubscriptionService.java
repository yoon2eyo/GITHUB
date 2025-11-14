package com.smartfitness.notification.adapter;

/**
 * System Interface Layer: Message Subscription Service Interface
 * Reference: 06_NotificationDispatcherComponent.puml (IMessageSubscriptionService)
 */
public interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
    void unsubscribe(String eventType);
}

