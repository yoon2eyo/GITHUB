package com.smartfitness.system.scheduler;

/**
 * SchedulerServiceImpl: 스케줄러 서비스 구현체 (Stub)
 * 
 * Architecture Pattern: Hexagonal Architecture - Adapter
 * Layer: System Interface Layer
 * 
 * ⚠️ STUB Implementation Notice:
 * - 현재: 콘솔 출력만 수행 (실제 스케줄링 미구현)
 * - 프로덕션: Spring TaskScheduler 또는 Quartz Scheduler 사용
 * 
 * 🔧 실제 적용 방법 (Spring TaskScheduler):
 * 
 * 1. Spring Boot 의존성 (자동 포함):
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter</artifactId>
 *    </dependency>
 * 
 * 2. SchedulerServiceImpl 구현:
 *    @Component
 *    public class SchedulerServiceImpl implements ISchedulerService {
 *        private final TaskScheduler taskScheduler;
 *        private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
 *        
 *        @Autowired
 *        public SchedulerServiceImpl(TaskScheduler taskScheduler) {
 *            this.taskScheduler = taskScheduler;
 *        }
 *        
 *        @Override
 *        public String scheduleTask(Runnable task, long intervalMs) {
 *            String taskId = "TASK_" + System.currentTimeMillis();
 *            
 *            // 주기적 실행 (intervalMs 마다 task 실행)
 *            ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
 *                task, 
 *                intervalMs
 *            );
 *            
 *            scheduledTasks.put(taskId, future); // 취소용 저장
 *            return taskId;
 *        }
 *        
 *        @Override
 *        public void cancelTask(String taskId) {
 *            ScheduledFuture<?> future = scheduledTasks.remove(taskId);
 *            if (future != null) {
 *                future.cancel(false); // 실행 중인 작업 중단
 *            }
 *        }
 *        
 *        @Override
 *        public String scheduleOnce(Runnable task, long delayMs) {
 *            String taskId = "TASK_ONCE_" + System.currentTimeMillis();
 *            
 *            // delayMs 후 1회 실행
 *            taskScheduler.schedule(
 *                task,
 *                new Date(System.currentTimeMillis() + delayMs)
 *            );
 *            
 *            return taskId;
 *        }
 *    }
 * 
 * 3. TaskScheduler Bean 설정:
 *    @Configuration
 *    public class SchedulerConfig {
 *        @Bean
 *        public TaskScheduler taskScheduler() {
 *            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
 *            scheduler.setPoolSize(10);              // 동시 실행 작업 수
 *            scheduler.setThreadNamePrefix("scheduler-");
 *            scheduler.setAwaitTerminationSeconds(20);
 *            scheduler.setWaitForTasksToCompleteOnShutdown(true);
 *            scheduler.initialize();
 *            return scheduler;
 *        }
 *    }
 * 
 * 4. 사용 예시 (UC-21 모니터링):
 *    String taskId = schedulerService.scheduleTask(
 *        () -> heartbeatChecker.triggerMonitorCheck(),
 *        10000L  // 10초마다 실행
 *    );
 * 
 * 5. 실행 흐름:
 *    [scheduleTask() 호출]
 *         ↓
 *    [TaskScheduler에 작업 등록]
 *         ↓
 *    [10초마다 자동 실행]
 *         ↓
 *    [task.run() 호출]
 *         ↓
 *    [triggerMonitorCheck() 실행]
 * 
 * 6. 대안 (Quartz Scheduler):
 *    - 복잡한 스케줄 규칙: Cron 표현식 지원
 *    - 지속성: DB에 작업 저장 (재시작 후 복구)
 *    - 클러스터: 여러 서버에서 스케줄 공유
 */
public class SchedulerServiceImpl implements ISchedulerService {
    
    @Override
    public String scheduleTask(Runnable task, long intervalMs) {
        // ⚠️ Stub: 실제로는 TaskScheduler.scheduleAtFixedRate() 호출
        // TODO: 
        // ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, intervalMs);
        // scheduledTasks.put(taskId, future);
        
        String taskId = "TASK_" + System.currentTimeMillis();
        System.out.println("Stub: Scheduled task [" + taskId + "] with interval " + intervalMs + "ms");
        return taskId;
    }
    
    @Override
    public void cancelTask(String taskId) {
        // ⚠️ Stub: 실제로는 ScheduledFuture.cancel() 호출
        // TODO:
        // ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        // if (future != null) future.cancel(false);
        
        System.out.println("Stub: Cancelled task [" + taskId + "]");
    }
    
    @Override
    public String scheduleOnce(Runnable task, long delayMs) {
        // ⚠️ Stub: 실제로는 TaskScheduler.schedule() 호출
        // TODO:
        // Date startTime = new Date(System.currentTimeMillis() + delayMs);
        // taskScheduler.schedule(task, startTime);
        
        String taskId = "TASK_ONCE_" + System.currentTimeMillis();
        System.out.println("Stub: Scheduled one-time task [" + taskId + "] with delay " + delayMs + "ms");
        return taskId;
    }
}
