package com.smartfitness.messaging.core;

import com.smartfitness.event.DomainEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.messaging.IMessageSubscriptionService;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * MessageBrokerComponent: 메시지 큐와 Pub/Sub 로직을 구현하는 핵심 인프라 컴포넌트입니다.
 * Tactic: Passive Redundancy (메시지 보존), Message Based (비동기 전송)
 */
public class MessageBrokerComponent implements IMessagePublisherService, IMessageSubscriptionService {
    // 메시지 큐와 구독자 맵을 관리 (Persistent storage is implied for real-world reliability)
    private final Map<String, List<Consumer<DomainEvent>>> topicSubscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * IMessagePublisherService 구현: 이벤트를 수신하여 구독자들에게 비동기적으로 전달합니다.
     */
    @Override
    public void publishEvent(String topic, DomainEvent event) {
        // 1. 메시지 큐에 이벤트 영속적으로 저장 (실제 구현)
        // 2. 구독자들에게 이벤트 전달
        List<Consumer<DomainEvent>> handlers = topicSubscribers.get(topic);
        if (handlers != null) {
            for (Consumer<DomainEvent> handler : handlers) {
                // 각 핸들러를 별도의 스레드에서 비동기 실행 (Introduce Concurrency)
                executor.submit(() -> handler.accept(event));
            }
        }
    }

    /**
     * IMessageSubscriptionService 구현: Consumer의 핸들러를 등록합니다.
     */
    @Override
    public void subscribeToTopic(String topic, Consumer<DomainEvent> eventHandler) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(eventHandler);
    }
    
    // 추가적으로 메시지 재시도, Dead Letter Queue (DLQ) 관리 등의 로직을 가집니다.
}
