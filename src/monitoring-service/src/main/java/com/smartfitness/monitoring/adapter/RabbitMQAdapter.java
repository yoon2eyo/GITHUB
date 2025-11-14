package com.smartfitness.monitoring.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: RabbitMQ Adapter
 * Component: RabbitMQAdapter
 * 
 * Implements: IMessagePublisherService
 * Message Broker: RabbitMQ
 * 
 * Events Published:
 * - EquipmentFaultEvent (DD-04: Fault Detection)
 * 
 * DD-04 Tactic: Passive Redundancy
 * - EquipmentFaultEvent → NotificationDispatcher subscribes
 * - Ensures alert delivery even if MonitoringService fails
 * 
 * Reference: 05_MonitoringServiceComponent.puml (IMessagePublisherService)
 */
@Slf4j
@Component
public class RabbitMQAdapter implements IMessagePublisherService {
    
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

