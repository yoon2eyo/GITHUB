package com.smartfitness.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Search Query Event
 * 
 * DD-09: Cold Path - Search Query Improvement
 * 
 * Published after Hot Path returns search results.
 * Used by Cold Path to improve ElasticSearch index via LLM analysis.
 * 
 * UC-09: Natural Language Branch Search
 */
public class SearchQueryEvent implements DomainEvent {
    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;
    private final String query;
    private final String customerId;
    private final int resultCount;
    
    public SearchQueryEvent(String query, String customerId, int resultCount) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "SearchQueryEvent";
        this.occurredAt = Instant.now();
        this.query = query;
        this.customerId = customerId;
        this.resultCount = resultCount;
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public String getEventType() {
        return eventType;
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getAggregateId() {
        return customerId;
    }
    
    public String getQuery() {
        return query;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public int getResultCount() {
        return resultCount;
    }
    
    @Override
    public String toString() {
        return String.format("SearchQueryEvent{query='%s', customerId='%s', resultCount=%d}", 
                query, customerId, resultCount);
    }
}

