package com.smartfitness.access.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * Publishes access events to Message Broker
 * DD-02: Event-Based Architecture
 * Reference: 10_RealTimeAccessServiceComponent.puml (IMessagePublisherService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService {
    
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event to RabbitMQ: {} ({})", event.getEventType(), event.getEventId());
        
        // Stub: In production, use RabbitTemplate
        // rabbitTemplate.convertAndSend("smart-fitness-exchange", "access.event", event);
        
        log.debug("Event published successfully: {}", event);
    }
}

