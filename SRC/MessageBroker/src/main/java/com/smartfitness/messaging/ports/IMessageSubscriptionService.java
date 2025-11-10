package com.smartfitness.messaging.ports;

/**
 * 이벤트 구독을 위한 인터페이스
 */
public interface IMessageSubscriptionService {
    /**
     * 특정 토픽에 대한 구독을 시작
     * @param topic 구독할 이벤트 토픽
     * @param handler 이벤트 처리기
     */
    void subscribe(String topic, Object handler);

    /**
     * 구독을 취소
     * @param topic 구독 취소할 토픽
     * @param handler 이벤트 처리기
     */
    void unsubscribe(String topic, Object handler);
}