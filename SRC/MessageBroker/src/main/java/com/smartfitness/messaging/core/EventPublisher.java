package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * EventPublisher: 이벤트 발행 구현체
 * 
 * 책임:
 * - 토픽 레지스트리에서 구독자 조회
 * - AsyncDispatcher를 통한 비동기 전달
 */
public class EventPublisher implements IEventPublisher {
    
    private final ITopicRegistry topicRegistry;
    private final IAsyncEventDispatcher asyncDispatcher;
    
    public EventPublisher(ITopicRegistry topicRegistry, IAsyncEventDispatcher asyncDispatcher) {
        this.topicRegistry = topicRegistry;
        this.asyncDispatcher = asyncDispatcher;
    }
    
    @Override
    public void publish(String topic, IDomainEvent event) {
        // 토픽의 구독자 목록 조회
        List<Consumer<IDomainEvent>> subscribers = topicRegistry.getSubscribers(topic);
        
        if (subscribers.isEmpty()) {
            // 구독자 없음 (선택: 로깅 또는 무시)
            return;
        }
        
        // 각 구독자에게 비동기로 이벤트 전달
        for (Consumer<IDomainEvent> subscriber : subscribers) {
            asyncDispatcher.dispatchAsync(subscriber, event);
        }
    }
}
