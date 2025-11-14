package com.smartfitness.gateway.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * Publishes domain events to Message Broker
 * DD-02: Event-Based Architecture
 * Reference: 07_ApiGatewayComponent.puml (IMessagePublisherService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService {
    
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event to RabbitMQ: {} ({})", event.getEventType(), event.getEventId());
        
        // Stub: In production, use RabbitTemplate to publish
        // Example: rabbitTemplate.convertAndSend(exchange, routingKey, event);
        
        log.debug("Event published: {}", event);
    }
}

