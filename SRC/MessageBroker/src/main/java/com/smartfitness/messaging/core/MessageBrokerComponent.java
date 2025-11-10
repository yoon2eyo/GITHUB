package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.messaging.IMessageSubscriptionService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * In-memory Message Broker implementation backing the publisher/subscriber ports.
 * Tactics: Passive Redundancy (topic buffers), Message Based (async dispatch).
 */
public class MessageBrokerComponent implements
    IMessagePublisherService,
    IMessageSubscriptionService,
    AutoCloseable {

    private final Map<String, List<Consumer<IDomainEvent>>> topicSubscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Publish an event to all subscribers registered on the topic.
     */
    @Override
    public void publishEvent(String topic, IDomainEvent event) {
        List<Consumer<IDomainEvent>> handlers = topicSubscribers.get(topic);
        if (handlers == null) {
            return;
        }

        for (Consumer<IDomainEvent> handler : handlers) {
            executor.submit(() -> handler.accept(event));
        }
    }

    /**
     * Register a consumer for the given topic.
     */
    @Override
    public void subscribeToTopic(String topic, Consumer<IDomainEvent> eventHandler) {
        topicSubscribers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(eventHandler);
    }

    /**
     * Unregister a consumer from the given topic.
     */
    @Override
    public void unsubscribeFromTopic(String topic, Consumer<IDomainEvent> eventHandler) {
        List<Consumer<IDomainEvent>> handlers = topicSubscribers.get(topic);
        if (handlers != null) {
            handlers.remove(eventHandler);
            if (handlers.isEmpty()) {
                topicSubscribers.remove(topic);
            }
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
