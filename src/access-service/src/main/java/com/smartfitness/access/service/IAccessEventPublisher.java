package com.smartfitness.access.service;

import com.smartfitness.common.event.AccessDeniedEvent;
import com.smartfitness.common.event.AccessGrantedEvent;

/**
 * Business Layer Interface: Access Event Publisher
 * Publishes access-related domain events
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IAccessEventPublisher {
    void publishAccessGranted(AccessGrantedEvent event);
    void publishAccessDenied(AccessDeniedEvent event);
}

