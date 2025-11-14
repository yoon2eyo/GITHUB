package com.smartfitness.search.adapter;

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
 * Events Published (Cold Path):
 * - BranchPreferenceCreatedEvent (UC-10, UC-18)
 * - BranchInfoCreatedEvent (UC-18)
 * - SearchQueryEvent (UC-09, DD-09) - For Cold Path index improvement
 * 
 * Events Subscribed:
 * - BranchPreferenceCreatedEvent (for DD-07 scheduled matching)
 * - SearchQueryEvent (for DD-09 Cold Path index improvement)
 * 
 * DD-02: Event-Based Architecture
 * DD-07: Scheduling Policy (defer during peak time)
 * DD-09: Hot/Cold Path Separation - SearchQueryEvent for index improvement
 * 
 * Reference: 03_BranchContentServiceComponent.puml (IMessagePublisherService, IMessageSubscriptionService)
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

