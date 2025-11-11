package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;

/**
 * IEventPublisher: 이벤트 발행 내부 인터페이스
 * 
 * 책임:
 * - 토픽으로 이벤트 라우팅
 * - 구독자들에게 이벤트 전달
 */
public interface IEventPublisher {
    /**
     * 이벤트를 토픽의 모든 구독자에게 발행
     * @param topic 토픽 이름
     * @param event 발행할 이벤트
     */
    void publish(String topic, IDomainEvent event);
}
