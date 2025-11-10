package com.smartfitness.event;

/**
 * UserRegisteredEvent: Emitted after successful user registration.
 */
public class UserRegisteredEvent implements DomainEvent {
    private final String userId;

    public UserRegisteredEvent(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}

