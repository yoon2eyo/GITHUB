package com.smartfitness.helper.ports;

import com.smartfitness.event.IDomainEvent;

/**
 * Helper 서비스의 이벤트 소비자를 위한 인터페이스입니다.
 * 메시지 브로커로부터 특정 토픽의 이벤트를 구독하고 처리합니다.
 */
public interface IHelperEventConsumer {
    /**
     * 특정 토픽에서 받은 이벤트를 처리합니다.
     * @param event 처리할 도메인 이벤트
     */
    void handleEvent(IDomainEvent event);
    
    /**
     * 이 소비자가 구독하는 토픽의 이름을 반환합니다.
     * @return 구독 토픽 이름
     */
    String getSubscriptionTopic();
}