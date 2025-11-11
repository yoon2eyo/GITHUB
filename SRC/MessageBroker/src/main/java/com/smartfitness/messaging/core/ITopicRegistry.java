package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * ITopicRegistry: 토픽별 구독자 관리를 위한 인터페이스
 * 
 * 책임:
 * - 토픽별 구독자 목록 저장 및 조회
 * - Thread-safe한 구독자 관리
 */
public interface ITopicRegistry {
    /**
     * 토픽에 구독자 추가
     * @param topic 토픽 이름
     * @param subscriber 구독자 핸들러
     */
    void addSubscriber(String topic, Consumer<IDomainEvent> subscriber);
    
    /**
     * 토픽에서 구독자 제거
     * @param topic 토픽 이름
     * @param subscriber 구독자 핸들러
     */
    void removeSubscriber(String topic, Consumer<IDomainEvent> subscriber);
    
    /**
     * 특정 토픽의 모든 구독자 조회
     * @param topic 토픽 이름
     * @return 구독자 목록 (null이 아닌 빈 리스트 반환 가능)
     */
    List<Consumer<IDomainEvent>> getSubscribers(String topic);
    
    /**
     * 토픽에 등록된 구독자가 있는지 확인
     * @param topic 토픽 이름
     * @return 구독자 존재 여부
     */
    boolean hasSubscribers(String topic);
}
