package com.smartfitness.messaging.core;

import com.smartfitness.event.IDomainEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * AsyncEventDispatcher: 비동기 이벤트 전달 구현체
 * 
 * Tactic: Introduce Concurrency (성능 향상)
 * - ExecutorService를 통한 비동기 처리
 * - 이벤트 발행자는 구독자 처리를 기다리지 않음
 * 
 * Implementation Note:
 * - 현재: CachedThreadPool (동적 스레드 생성)
 * - 프로덕션: FixedThreadPool 권장 (리소스 제어)
 */
public class AsyncEventDispatcher implements IAsyncEventDispatcher {
    
    private final ExecutorService executor;
    
    /**
     * 기본 생성자 (CachedThreadPool 사용)
     */
    public AsyncEventDispatcher() {
        this.executor = Executors.newCachedThreadPool();
    }
    
    /**
     * 커스텀 ExecutorService 사용 생성자
     * @param executor 사용할 ExecutorService
     */
    public AsyncEventDispatcher(ExecutorService executor) {
        this.executor = executor;
    }
    
    @Override
    public void dispatchAsync(Consumer<IDomainEvent> subscriber, IDomainEvent event) {
        // 별도 스레드에서 비동기 실행
        executor.submit(() -> {
            try {
                subscriber.accept(event);
            } catch (Exception e) {
                // TODO: 에러 핸들링 (DLQ, 로깅 등)
                System.err.println("Event dispatch failed: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
