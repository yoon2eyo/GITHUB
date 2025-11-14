package com.smartfitness.auth.adapter;

/**
 * System Interface Layer: Message Subscription Service Interface
 * Reference: 02_AuthenticationServiceComponent.puml (IMessageSubscriptionService)
 */
public interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
    void unsubscribe(String eventType);
}

