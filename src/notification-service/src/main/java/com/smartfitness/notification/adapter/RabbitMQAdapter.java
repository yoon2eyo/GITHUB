package com.smartfitness.notification.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * 
 * Implements: IMessageSubscriptionService, IMessagePublisherService
 * Message Broker: RabbitMQ
 * 
 * Subscribed Events:
 * - EquipmentFaultEvent (from Monitoring Service)
 * - BranchPreferenceCreatedEvent (from Search Service)
 * 
 * Reference: 06_NotificationDispatcherComponent.puml (IMessageSubscriptionService, IMessagePublisherService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessageSubscriptionService, IMessagePublisherService {
    
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
}

