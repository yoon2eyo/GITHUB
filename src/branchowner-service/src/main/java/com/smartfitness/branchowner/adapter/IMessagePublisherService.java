package com.smartfitness.branchowner.adapter;

import com.smartfitness.common.event.DomainEvent;

/**
 * System Interface Layer: Message Publisher Service Interface
 * Reference: 09_BranchOwnerServiceComponent.puml (IMessagePublisherService)
 */
public interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

