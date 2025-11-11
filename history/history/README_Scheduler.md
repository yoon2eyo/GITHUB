# Monitoring Service - Scheduler Integration

## 📋 개요

UC-21 설비 상태 모니터링을 위한 **스케줄러 인프라 구현** (Stub 수준)

**구현 완료일**: 2025-11-11  
**수준**: Stub (아키텍처 구조 정의)

---

## 🏗️ 아키텍처

### 3-Layer Architecture

```
┌─────────────────────────────────────────┐
│      Business Layer                     │
│  ┌────────────────────────────────┐    │
│  │  MonitoringScheduler           │    │
│  │  - start()                     │    │
│  │  - executeMonitoringCheck()    │    │
│  └─────────┬──────────────────────┘    │
│            │ uses                       │
│  ┌─────────▼──────────────────────┐    │
│  │  IMonitoringTriggerService     │    │
│  │  (HeartbeatChecker)            │    │
│  └────────────────────────────────┘    │
└─────────────────────────────────────────┘
              │
              │ optional uses
              ▼
┌─────────────────────────────────────────┐
│   System Interface Layer                │
│  ┌────────────────────────────────┐    │
│  │  ISchedulerService (Port)      │    │
│  │  - scheduleTask()              │    │
│  │  - cancelTask()                │    │
│  │  - scheduleOnce()              │    │
│  └─────────┬──────────────────────┘    │
│            │ implements                 │
│  ┌─────────▼──────────────────────┐    │
│  │  SchedulerServiceImpl (Stub)   │    │
│  └────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

---

## 📁 파일 구조

```
SRC/BusinessLogic/src/main/java/com/smartfitness/
├── monitor/
│   └── internal/
│       └── scheduler/
│           └── MonitoringScheduler.java      ✅ Stub
└── system/
    └── scheduler/
        ├── ISchedulerService.java            ✅ Stub
        └── SchedulerServiceImpl.java         ✅ Stub
```

---

## 💻 구현 내용

### 1. MonitoringScheduler.java (Business Layer)

**역할**: UC-21 설비 상태 모니터링을 10초 간격으로 트리거

```java
public class MonitoringScheduler {
    private final IMonitoringTriggerService triggerService;
    
    // 10초 간격 기본값
    public MonitoringScheduler(IMonitoringTriggerService triggerService) {
        this(triggerService, 10000L);
    }
    
    public void start() {
        // TODO: Spring @Scheduled(fixedRate = 10000)
    }
    
    protected void executeMonitoringCheck() {
        triggerService.triggerMonitorCheck();
    }
}
```

**특징**:
- Stub 수준: 구조만 정의
- 실제 환경: Spring `@Scheduled` 또는 Quartz
- UC-21 요구사항: 10초 간격 주기적 실행

### 2. ISchedulerService.java (System Interface Layer)

**역할**: 스케줄링 인프라 계약 정의

```java
public interface ISchedulerService {
    String scheduleTask(Runnable task, long intervalMs);
    void cancelTask(String taskId);
    String scheduleOnce(Runnable task, long delayMs);
}
```

**특징**:
- Hexagonal Architecture: Output Port
- 인프라 추상화 (Spring, Quartz 등)

### 3. SchedulerServiceImpl.java (System Interface Layer)

**역할**: 스케줄러 서비스 구현 (Stub)

```java
public class SchedulerServiceImpl implements ISchedulerService {
    @Override
    public String scheduleTask(Runnable task, long intervalMs) {
        System.out.println("Stub: Scheduled task");
        return "TASK_" + System.currentTimeMillis();
    }
    
    // ... cancelTask(), scheduleOnce()
}
```

**특징**:
- Stub: 실제 스케줄링 로직 없음
- 콘솔 출력으로 호출 확인

---

## 🔌 통합 방법

### Option 1: MonitoringScheduler 직접 사용 (권장)

```java
// HeartbeatChecker 인스턴스 생성
IMonitoringTriggerService heartbeatChecker = new HeartbeatChecker(...);

// 스케줄러 생성 및 시작
MonitoringScheduler scheduler = new MonitoringScheduler(heartbeatChecker);
scheduler.start();

// 주기적 실행 (실제로는 @Scheduled가 자동 호출)
// scheduler.executeMonitoringCheck(); // 10초마다
```

### Option 2: ISchedulerService 사용

```java
ISchedulerService schedulerService = new SchedulerServiceImpl();
IMonitoringTriggerService heartbeatChecker = new HeartbeatChecker(...);

// 10초 간격 작업 등록
String taskId = schedulerService.scheduleTask(
    () -> heartbeatChecker.triggerMonitorCheck(),
    10000L
);

// 취소
schedulerService.cancelTask(taskId);
```

---

## 🚀 실제 환경 구현 예시

### Spring Framework 사용 시

```java
@Component
public class MonitoringScheduler {
    private final IMonitoringTriggerService triggerService;
    
    @Autowired
    public MonitoringScheduler(IMonitoringTriggerService triggerService) {
        this.triggerService = triggerService;
    }
    
    @Scheduled(fixedRate = 10000) // 10초 간격
    public void executeMonitoringCheck() {
        triggerService.triggerMonitorCheck();
    }
}
```

```java
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring이 자동으로 @Scheduled 메서드 실행
}
```

### Quartz Scheduler 사용 시

```java
public class SchedulerServiceImpl implements ISchedulerService {
    private Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    
    @Override
    public String scheduleTask(Runnable task, long intervalMs) {
        JobDetail job = JobBuilder.newJob(RunnableJob.class)
            .withIdentity("monitoring-job")
            .build();
            
        Trigger trigger = TriggerBuilder.newTrigger()
            .withSchedule(SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMilliseconds(intervalMs)
                .repeatForever())
            .build();
            
        scheduler.scheduleJob(job, trigger);
        return job.getKey().getName();
    }
}
```

---

## 📊 명세서 준수도

### UC-21: 설비 상태 모니터링

| 요구사항 | 구현 상태 | 비고 |
|---------|---------|------|
| 10초 간격 주기적 트리거 | ✅ 설계 완료 | Stub 구현 |
| HeartbeatChecker 호출 | ✅ 완료 | `executeMonitoringCheck()` |
| 스케줄러 인프라 | ✅ Stub | 실제: Spring/Quartz |

---

## 🎯 Tactic 적용

### Scheduled Task (Availability Tactic)
- **목적**: 주기적 헬스 체크로 장애 조기 감지
- **구현**: MonitoringScheduler (10초 간격)
- **효과**: 최대 10초 내 타임아웃 감지 가능

---

## 📝 Stub 코드 특징

### ✅ 포함된 것
- 인터페이스 정의 (`ISchedulerService`)
- 클래스 구조 (`MonitoringScheduler`, `SchedulerServiceImpl`)
- 메서드 시그니처
- TODO 주석으로 실제 구현 가이드
- 아키텍처 패턴 주석

### ❌ 포함되지 않은 것
- 실제 스케줄링 로직
- Spring 어노테이션 (`@Scheduled`, `@Component`)
- Quartz Job/Trigger 설정
- 예외 처리
- 로깅

---

## 🔍 컴포넌트 다이어그램 반영

**MonitoringServiceComponent.puml**에 이미 포함됨:

```plantuml
package "System Interface Layer" {
  interface ISchedulerService
  component SchedulerServiceImpl
  ISchedulerService -- SchedulerServiceImpl
}

' Business Layer -> System Interface Layer
HeartbeatChecker ..( ISchedulerService : <<schedule>>
```

**수정 불필요** ✅

---

## ✅ 완료 체크리스트

- [x] MonitoringScheduler 클래스 생성
- [x] ISchedulerService 인터페이스 정의
- [x] SchedulerServiceImpl Stub 구현
- [x] TODO 주석으로 실제 구현 가이드
- [x] 아키텍처 패턴 주석
- [x] UC-21 요구사항 매핑
- [x] 컴포넌트 다이어그램 확인 (수정 불필요)
- [x] 문서화

---

**구현 완료**: 2025-11-11  
**수준**: Stub (아키텍처 구조 정의)  
**다음 단계**: 실제 환경에서 Spring `@Scheduled` 또는 Quartz 적용
