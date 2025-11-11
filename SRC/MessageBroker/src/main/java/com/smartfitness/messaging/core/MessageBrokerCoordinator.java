package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.messaging.IMessageSubscriptionService;
import java.util.function.Consumer;

/**
 * MessageBrokerCoordinator: Message Broker의 Facade 패턴 구현
 * 
 * 책임:
 * - 외부 인터페이스 (IMessagePublisherService, IMessageSubscriptionService) 구현
 * - 내부 컴포넌트들 간 조정 (Coordinator)
 * - 단순히 요청을 적절한 내부 컴포넌트에 위임
 * 
 * Architecture Pattern: Facade
 * - 복잡한 내부 구조를 단순한 인터페이스로 감춤
 * - 클라이언트는 TopicRegistry, EventPublisher 등을 직접 알 필요 없음
 */
public class MessageBrokerCoordinator implements 
    IMessagePublisherService, 
    IMessageSubscriptionService,
    AutoCloseable {
    
    private final ITopicRegistry topicRegistry;
    private final IEventPublisher eventPublisher;
    private final ISubscriptionManager subscriptionManager;
    private final IAsyncEventDispatcher asyncDispatcher;
    
    /**
     * 생성자: 의존성 주입
     */
    public MessageBrokerCoordinator(
        ITopicRegistry topicRegistry,
        IEventPublisher eventPublisher,
        ISubscriptionManager subscriptionManager,
        IAsyncEventDispatcher asyncDispatcher
    ) {
        this.topicRegistry = topicRegistry;
        this.eventPublisher = eventPublisher;
        this.subscriptionManager = subscriptionManager;
        this.asyncDispatcher = asyncDispatcher;
    }
    
    /**
     * 기본 생성자: 기본 구현체들을 자동 생성
     */
    public MessageBrokerCoordinator() {
        this.topicRegistry = new TopicRegistry();
        this.asyncDispatcher = new AsyncEventDispatcher();
        this.eventPublisher = new EventPublisher(topicRegistry, asyncDispatcher);
        this.subscriptionManager = new SubscriptionManager(topicRegistry);
    }
    
    @Override
    public void publishEvent(String topic, IDomainEvent event) {
        eventPublisher.publish(topic, event);
    }
    
    @Override
    public void subscribeToTopic(String topic, Consumer<IDomainEvent> eventHandler) {
        subscriptionManager.subscribe(topic, eventHandler);
    }
    
    @Override
    public void unsubscribeFromTopic(String topic, Consumer<IDomainEvent> eventHandler) {
        subscriptionManager.unsubscribe(topic, eventHandler);
    }
    
    @Override
    public void close() {
        asyncDispatcher.shutdown();
    }
}
