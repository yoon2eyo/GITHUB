package com.smartfitness.branchowner.adapter;

/**
 * System Interface Layer: Message Subscription Service Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IMessageSubscriptionService)
 */
public interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
    void unsubscribe(String eventType);
}

