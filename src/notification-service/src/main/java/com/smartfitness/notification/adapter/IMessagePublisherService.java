package com.smartfitness.notification.adapter;

import com.smartfitness.common.event.DomainEvent;

/**
 * System Interface Layer: Message Publisher Service Interface
 * Reference: 06_NotificationDispatcherComponent.puml (IMessagePublisherService)
 */
public interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

