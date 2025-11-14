package com.smartfitness.access.service;

import com.smartfitness.access.adapter.RabbitMQAdapter;
import com.smartfitness.common.event.AccessDeniedEvent;
import com.smartfitness.common.event.AccessGrantedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Access Event Processor
 * Component: AccessEventProcessor
 * Publishes access events to Message Broker
 * DD-02: Event-Based Architecture
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessEventProcessor implements IAccessEventPublisher {
    
    private final RabbitMQAdapter rabbitMQAdapter;
    
    @Override
    public void publishAccessGranted(AccessGrantedEvent event) {
        log.info("Publishing AccessGrantedEvent: userId={}, branch={}", 
                event.getUserId(), event.getBranchId());
        
        rabbitMQAdapter.publishEvent(event);
    }
    
    @Override
    public void publishAccessDenied(AccessDeniedEvent event) {
        log.warn("Publishing AccessDeniedEvent: branch={}, reason={}", 
                event.getBranchId(), event.getDenialReason());
        
        rabbitMQAdapter.publishEvent(event);
    }
}

