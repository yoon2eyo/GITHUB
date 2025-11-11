package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.function.Consumer;

/**
 * IAsyncEventDispatcher: 비동기 이벤트 전달 인터페이스
 * 
 * 책임:
 * - 이벤트를 비동기적으로 구독자에게 전달
 * - ExecutorService를 통한 동시성 처리
 */
public interface IAsyncEventDispatcher {
    /**
     * 이벤트를 비동기로 전달
     * @param subscriber 구독자 핸들러
     * @param event 전달할 이벤트
     */
    void dispatchAsync(Consumer<IDomainEvent> subscriber, IDomainEvent event);
    
    /**
     * Dispatcher 종료 및 리소스 정리
     */
    void shutdown();
}
