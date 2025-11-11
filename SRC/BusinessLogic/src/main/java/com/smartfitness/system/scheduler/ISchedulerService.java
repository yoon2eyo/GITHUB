package com.smartfitness.system.scheduler;

/**
 * ISchedulerService: 스케줄링 인프라 인터페이스 (System Interface Layer)
 * 
 * Role: 주기적 작업 실행을 위한 계약 정의
 * 
 * Architecture Pattern: Hexagonal Architecture - Output Port
 * Layer: System Interface Layer
 * 
 * Implementation:
 * - Spring TaskScheduler
 * - Quartz Scheduler
 * - Java ScheduledExecutorService
 */
public interface ISchedulerService {
    /**
     * 주기적 작업 등록
     * 
     * @param task 실행할 작업
     * @param intervalMs 실행 간격 (밀리초)
     * @return 작업 ID (취소 시 사용)
     */
    String scheduleTask(Runnable task, long intervalMs);
    
    /**
     * 작업 취소
     * 
     * @param taskId 작업 ID
     */
    void cancelTask(String taskId);
    
    /**
     * 지연 후 작업 실행 (일회성)
     * 
     * @param task 실행할 작업
     * @param delayMs 지연 시간 (밀리초)
     * @return 작업 ID
     */
    String scheduleOnce(Runnable task, long delayMs);
}
