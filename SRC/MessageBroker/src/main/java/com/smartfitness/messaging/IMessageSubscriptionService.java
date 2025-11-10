package com.smartfitness.messaging;

import com.smartfitness.event.DomainEvent;
import java.util.function.Consumer;

/**
 * IMessageSubscriptionService: Consumer Service가 특정 토픽을 구독하기 위한 포트입니다.
 */
public interface IMessageSubscriptionService {
    /**
     * 특정 토픽을 구독하고, 메시지 수신 시 처리할 핸들러를 등록합니다.
     * @param topic 구독할 토픽
     * @param eventHandler 메시지를 수신했을 때 실행될 로직 (Consumer Service의 내부 메서드)
     */
    void subscribeToTopic(String topic, Consumer<DomainEvent> eventHandler);

    /**
     * Start listening/dispatch loop if needed by implementation.
     */
    default void startListening() {}
}
