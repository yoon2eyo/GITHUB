package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * TopicRegistry: 토픽별 구독자 관리 구현체
 * 
 * Tactic: Passive Redundancy (토픽 버퍼링으로 메시지 보존)
 * 
 * Thread Safety:
 * - ConcurrentHashMap: 동시 접근 안전성
 * - CopyOnWriteArrayList: 순회 중 수정 안전성
 */
public class TopicRegistry implements ITopicRegistry {
    
    // 토픽별 구독자 목록 (Thread-safe)
    private final Map<String, List<Consumer<IDomainEvent>>> topicSubscribers = new ConcurrentHashMap<>();
    
    @Override
    public void addSubscriber(String topic, Consumer<IDomainEvent> subscriber) {
        topicSubscribers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>())
                        .add(subscriber);
    }
    
    @Override
    public void removeSubscriber(String topic, Consumer<IDomainEvent> subscriber) {
        List<Consumer<IDomainEvent>> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            subscribers.remove(subscriber);
            // 구독자가 없으면 토픽 제거 (메모리 정리)
            if (subscribers.isEmpty()) {
                topicSubscribers.remove(topic);
            }
        }
    }
    
    @Override
    public List<Consumer<IDomainEvent>> getSubscribers(String topic) {
        return topicSubscribers.getOrDefault(topic, List.of());
    }
    
    @Override
    public boolean hasSubscribers(String topic) {
        List<Consumer<IDomainEvent>> subscribers = topicSubscribers.get(topic);
        return subscribers != null && !subscribers.isEmpty();
    }
}
