package com.smartfitness.helper.adapter;

/**
 * System Interface Layer: Message Subscription Service Interface
 * Reference: 04_HelperServiceComponent.puml (IMessageSubscriptionService)
 */
public interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
    void unsubscribe(String eventType);
}

