package com.smartfitness.messaging;

import com.smartfitness.event.IDomainEvent;
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
    void subscribeToTopic(String topic, Consumer<IDomainEvent> eventHandler);

    /**
     * 특정 토픽의 구독을 해제하고, 해당 토픽에 등록된 이벤트 핸들러를 제거합니다.
     * @param topic 구독 해제할 토픽
     * @param eventHandler 제거할 이벤트 핸들러
     */
    void unsubscribeFromTopic(String topic, Consumer<IDomainEvent> eventHandler);

    /**
     * Start listening/dispatch loop if needed by implementation.
     */
    default void startListening() {}
}
