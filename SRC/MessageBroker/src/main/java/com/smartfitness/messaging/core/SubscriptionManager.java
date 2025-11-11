package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.function.Consumer;

/**
 * SubscriptionManager: 구독 관리 구현체
 * 
 * 책임:
 * - TopicRegistry에 구독자 등록/해제 위임
 * - 구독 관련 비즈니스 로직 (검증, 로깅 등)
 */
public class SubscriptionManager implements ISubscriptionManager {
    
    private final ITopicRegistry topicRegistry;
    
    public SubscriptionManager(ITopicRegistry topicRegistry) {
        this.topicRegistry = topicRegistry;
    }
    
    @Override
    public void subscribe(String topic, Consumer<IDomainEvent> subscriber) {
        // TODO: 구독 검증 로직 (중복 체크 등)
        topicRegistry.addSubscriber(topic, subscriber);
    }
    
    @Override
    public void unsubscribe(String topic, Consumer<IDomainEvent> subscriber) {
        topicRegistry.removeSubscriber(topic, subscriber);
    }
}
