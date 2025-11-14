package com.smartfitness.search.adapter;

/**
 * System Interface Layer: Message Subscription Service Interface
 * Reference: 03_BranchContentServiceComponent.puml (IMessageSubscriptionService)
 */
public interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
    void unsubscribe(String eventType);
}

