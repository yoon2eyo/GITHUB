package com.smartfitness.auth.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * Publishes/subscribes domain events
 * DD-02: Event-Based Architecture
 * Reference: 02_AuthenticationServiceComponent.puml (IMessagePublisherService, IMessageSubscriptionService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService, IMessageSubscriptionService {
    
    @Override
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event: {} ({})", event.getEventType(), event.getEventId());
        
        // Stub: In production, use RabbitTemplate
        // rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
    
    @Override
    public void subscribe(String eventType, Object consumer) {
        log.info("Subscribing to event type: {}", eventType);
        
        // Stub: In production, use @RabbitListener annotation on consumer methods
    }
    
    @Override
    public void unsubscribe(String eventType) {
        log.info("Unsubscribing from event type: {}", eventType);
        
        // Stub: Remove listener
    }
}

