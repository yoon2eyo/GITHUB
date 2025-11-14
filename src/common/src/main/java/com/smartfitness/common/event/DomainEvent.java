package com.smartfitness.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events
 * DD-02: Event-Based Architecture
 */
public interface DomainEvent {
    String getEventId();
    String getEventType();
    Instant getOccurredAt();
    String getAggregateId();
}

