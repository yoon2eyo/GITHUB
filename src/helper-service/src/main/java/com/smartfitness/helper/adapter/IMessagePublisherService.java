package com.smartfitness.helper.adapter;

import com.smartfitness.common.event.DomainEvent;

/**
 * System Interface Layer: Message Publisher Service Interface
 * Reference: 04_HelperServiceComponent.puml (IMessagePublisherService)
 */
public interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

