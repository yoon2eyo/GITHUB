package com.smartfitness.gateway.adapter;

import com.smartfitness.common.event.DomainEvent;

/**
 * System Interface Layer: Message Publisher Service Interface
 * Reference: 07_ApiGatewayComponent.puml (IMessagePublisherService)
 */
public interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

