package com.smartfitness.facemodel.ports;

import com.smartfitness.event.IDomainEvent;

/**
 * Face Model 이벤트 처리 인터페이스
 * 메시지 브로커 이벤트 소비
 */
public interface IFaceModelEventHandler {
    /**
     * 이벤트 처리
     * @param event 도메인 이벤트
     */
    void handleEvent(IDomainEvent event);

    /**
     * 이벤트 구독 등록
     */
    void register();

    /**
     * 이벤트 구독 취소
     */
    void unregister();
}
