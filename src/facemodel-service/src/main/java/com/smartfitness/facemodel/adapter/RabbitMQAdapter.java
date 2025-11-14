package com.smartfitness.facemodel.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * Publishes model lifecycle events (deployed, rollback, etc.)
 * DD-02: Event-Based Architecture
 * Reference: 12_FaceModelServiceComponent.puml (IMessagePublisherService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService {
    
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event to RabbitMQ: {} ({})", event.getEventType(), event.getEventId());
        
        // Stub: In production, use RabbitTemplate
        // rabbitTemplate.convertAndSend("smart-fitness-exchange", "facemodel.event", event);
        
        log.debug("Event published successfully: {}", event);
    }
    
    public void publishModelDeployedEvent(String modelVersion) {
        log.info("Publishing ModelDeployedEvent: version={}", modelVersion);
        
        // Stub: Create and publish ModelDeployedEvent
        // In production: Define ModelDeployedEvent in common module
    }
    
    public void publishModelRollbackEvent(String fromVersion, String toVersion) {
        log.warn("Publishing ModelRollbackEvent: {} -> {}", fromVersion, toVersion);
        
        // Stub: Create and publish ModelRollbackEvent
    }
}

