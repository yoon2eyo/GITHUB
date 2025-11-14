package com.smartfitness.facemodel.adapter;

import com.smartfitness.common.event.DomainEvent;

/**
 * System Interface Layer: Message Publisher Service Interface
 * Reference: 12_FaceModelServiceComponent.puml (IMessagePublisherService)
 */
public interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

