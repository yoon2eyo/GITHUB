package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.function.Consumer;

/**
 * ISubscriptionManager: 구독 관리 인터페이스
 * 
 * 책임:
 * - 구독자 등록/해제 처리
 * - TopicRegistry와 연동
 */
public interface ISubscriptionManager {
    /**
     * 토픽 구독
     * @param topic 구독할 토픽
     * @param subscriber 구독자 핸들러
     */
    void subscribe(String topic, Consumer<IDomainEvent> subscriber);
    
    /**
     * 토픽 구독 해제
     * @param topic 구독 해제할 토픽
     * @param subscriber 구독자 핸들러
     */
    void unsubscribe(String topic, Consumer<IDomainEvent> subscriber);
}
