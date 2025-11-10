package com.smartfitness.messaging;

import com.smartfitness.event.IDomainEvent;

/**
 * IMessagePublisherService: Application Service가 Message Broker로 이벤트를 발행하기 위한 포트입니다.
 * Tactic: Use an Intermediary (느슨한 결합 유지)
 */
public interface IMessagePublisherService {
    /**
     * 특정 토픽으로 도메인 이벤트를 비동기적으로 발행합니다.
     * @param topic 발행할 메시지의 주제 (예: "faults", "rewards", "preferences")
     * @param event 발행할 도메인 이벤트 객체
     */
    void publishEvent(String topic, IDomainEvent event);
}
