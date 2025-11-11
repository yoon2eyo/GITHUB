package com.smartfitness.monitor.internal.scheduler;

import com.smartfitness.monitor.ports.IMonitoringTriggerService;

/**
 * MonitoringScheduler: 주기적으로 설비 상태 모니터링을 트리거합니다.
 * 
 * UC-21: 설비 상태 모니터링
 * - 10초 간격으로 HeartbeatChecker.triggerMonitorCheck() 호출
 * - 30초 하트비트 타임아웃 체크 (3회 누락 감지)
 * 
 * Tactic: Scheduled Task (Availability)
 * 
 * ⚠️ STUB Implementation Notice:
 * - 현재: start() 메서드만 정의 (실제 스케줄링 미구현)
 * - 프로덕션: Spring @Scheduled 또는 Quartz Scheduler 사용
 * 
 * 🔧 실제 적용 방법 (Spring @Scheduled):
 * 
 * 1. Spring Boot 설정:
 *    @Configuration
 *    @EnableScheduling  // 스케줄링 활성화
 *    public class SchedulerConfig {
 *    }
 * 
 * 2. MonitoringScheduler 구현:
 *    @Component
 *    public class MonitoringScheduler {
 *        private final IMonitoringTriggerService triggerService;
 *        
 *        @Autowired
 *        public MonitoringScheduler(IMonitoringTriggerService triggerService) {
 *            this.triggerService = triggerService;
 *        }
 *        
 *        // 10초마다 자동 실행 (Spring이 자동 호출)
 *        @Scheduled(fixedRate = 10000)
 *        public void scheduleMonitoringCheck() {
 *            triggerService.triggerMonitorCheck(); // HeartbeatChecker 호출
 *        }
 *    }
 * 
 * 3. 실행 흐름:
 *    [Spring Container 시작]
 *         ↓
 *    [@Scheduled 메서드 자동 등록]
 *         ↓
 *    [10초마다 scheduleMonitoringCheck() 호출]
 *         ↓
 *    [triggerService.triggerMonitorCheck()]
 *         ↓
 *    [HeartbeatChecker: 모든 설비 체크]
 *         ↓
 *    [타임아웃 감지 시 EquipmentFaultDetectedEvent 발행]
 * 
 * 4. 대안 (Quartz Scheduler):
 *    - 복잡한 스케줄 규칙 필요 시 사용
 *    - Cron 표현식 지원: @Scheduled(cron = "0/10 * * * * ?")
 *    - 클러스터 환경 지원 (분산 스케줄링)
 * 
 * 5. QAS-01 요구사항 충족:
 *    - 10초 간격 모니터링 → 최대 10초 내 고장 감지
 *    - Connection Pool 적용 시 100 설비 체크: 100ms 이내
 *    - 이벤트 발행 + 알림 발송: 1초 이내
 *    - 총 소요 시간: 최대 11초 (10초 대기 + 1초 처리)
 */
public class MonitoringScheduler {
    private final IMonitoringTriggerService triggerService;
    private final long intervalMs;
    
    /**
     * Constructor with default 10-second interval
     * 
     * @param triggerService UC-21 모니터링 트리거 서비스 (HeartbeatChecker 구현체)
     */
    public MonitoringScheduler(IMonitoringTriggerService triggerService) {
        this(triggerService, 10000L); // 기본 10초 (UC-21 요구사항)
    }
    
    /**
     * Constructor with custom interval
     * 
     * @param triggerService 모니터링 트리거 서비스
     * @param intervalMs 실행 간격 (밀리초) - QAS-01 고려하여 10초 이하 권장
     */
    public MonitoringScheduler(IMonitoringTriggerService triggerService, long intervalMs) {
        this.triggerService = triggerService;
        this.intervalMs = intervalMs;
    }
    
    /**
     * 스케줄러 시작 (Stub)
     * 
     * ⚠️ 현재: 시작 메시지만 출력 (실제 스케줄링 미구현)
     * ✅ 프로덕션: Spring @Scheduled 어노테이션으로 자동 스케줄링
     * 
     * TODO: 실제로는 이 메서드 대신 @Scheduled 어노테이션 사용
     *       (Spring Container가 자동으로 주기적 실행 관리)
     */
    public void start() {
        System.out.println("Stub: MonitoringScheduler started with interval " + intervalMs + "ms");
        // TODO: 실제 스케줄링 로직
        // 예: ScheduledExecutorService.scheduleAtFixedRate(...)
    }
    
    /**
     * 스케줄러 중지 (Stub)
     */
    public void stop() {
        System.out.println("Stub: MonitoringScheduler stopped");
        // TODO: 스케줄러 종료 로직
    }
    
    /**
     * 주기적으로 호출될 메서드 (Stub)
     * 
     * 실제로는 Spring @Scheduled 어노테이션으로 자동 호출:
     * @Scheduled(fixedRate = 10000)
     * public void executeMonitoringCheck() { ... }
     */
    protected void executeMonitoringCheck() {
        System.out.println("Stub: Executing monitoring check...");
        triggerService.triggerMonitorCheck();
    }
}
