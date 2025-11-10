package com.smartfitness.messaging.ports;

import com.smartfitness.messaging.event.IDomainEvent;

/**
 * 이벤트 발행을 위한 인터페이스
 */
public interface IMessagePublisherService {
    /**
     * 이벤트를 발행
     * @param topic 이벤트 토픽
     * @param event 발행할 이벤트
     */
    void publish(String topic, IDomainEvent event);
}