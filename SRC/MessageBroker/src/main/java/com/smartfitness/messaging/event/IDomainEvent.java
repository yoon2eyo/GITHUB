package com.smartfitness.event;

/**
 * 시스템 전반에서 사용되는 도메인 이벤트의 기본 인터페이스
 */
public interface IDomainEvent {
    String getEventId();
    long getTimestamp();
    String getEventType();
}