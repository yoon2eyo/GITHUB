package com.smartfitness.branchowner.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * 
 * Implements: IMessagePublisherService, IMessageSubscriptionService
 * Message Broker: RabbitMQ
 * 
 * Events Published:
 * - BranchOwnerRegisteredEvent (UC-03)
 * 
 * Events Subscribed:
 * - BranchInfoCreatedEvent (from Search Service)
 * - BranchPreferenceCreatedEvent (from Search Service)
 * 
 * DD-02: Event-Based Architecture
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml (IMessagePublisherService, IMessageSubscriptionService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService, IMessageSubscriptionService {
    
    @Override
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event to RabbitMQ: {} ({})", event.getEventType(), event.getEventId());
        
        // Stub: In production, use RabbitTemplate
        // rabbitTemplate.convertAndSend(
        //     "smart-fitness-exchange",
        //     event.getEventType(),
        //     event
        // );
    }
    
    @Override
    public void subscribe(String eventType, Object consumer) {
        log.info("Subscribing to event type: {}", eventType);
        
        // Stub: In production, use @RabbitListener annotation on consumer methods
        // @RabbitListener(queues = "#{eventType}")
        // public void handleEvent(DomainEvent event) { ... }
    }
    
    @Override
    public void unsubscribe(String eventType) {
        log.info("Unsubscribing from event type: {}", eventType);
        
        // Stub: Remove listener
    }
}

