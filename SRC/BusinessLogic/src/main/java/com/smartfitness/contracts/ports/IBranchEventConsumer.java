package com.smartfitness.contracts.ports;

import com.smartfitness.event.IDomainEvent;

/**
 * 지점 이벤트 처리 인터페이스
 * 메시지 브로커를 통한 이벤트 소비
 */
public interface IBranchEventConsumer {
    /**
     * 이벤트 처리
     * @param event 도메인 이벤트
     */
    void handleEvent(IDomainEvent event);

    /**
     * 구독 시작
     */
    void register();

    /**
     * 구독 취소
     */
    void unregister();
}
