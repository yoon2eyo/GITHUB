# Monitoring Service Component Element List

## 개요
본 문서는 Monitoring Service 컴포넌트 다이어그램(`05_MonitoringServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IEquipmentStatusReceiver** | 설비 상태 수신 인터페이스를 정의<br>설비의 주기적 하트비트 및 상태 보고 수신<br>TCP/HTTPS 기반 설비와의 통신 표준화 | UC-21 (설비 상태 모니터링)<br>QAS-01 (설비 고장 감지 - 15초 이내 알림) |
| **IEquipmentCommandApi** | 설비 명령 API 인터페이스를 정의<br>설비 상태 조회 및 제어 명령 전송<br>모니터링 시스템과의 명령 통신 | UC-21<br>QAS-01 (설비 상태 확인) |
| **EquipmentStatusReceiver** | IEquipmentStatusReceiver 인터페이스의 구현<br>설비 하트비트 수신 및 처리<br>상태 데이터 수집 및 분석 트리거 | UC-21<br>QAS-01 (실시간 상태 수신) |
| **EquipmentCommandController** | IEquipmentCommandApi 인터페이스의 구현<br>설비 명령 요청 수신 및 처리<br>Ping/Echo 테스트 실행 조율 | UC-21<br>QAS-01 (설비 제어) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IHeartbeatReceiverService** | 하트비트 수신 인터페이스를 정의<br>설비의 정기적 상태 보고 처리<br>상태 저장 및 이상 감지 트리거 | UC-21<br>DD-04 (Heartbeat Tactic) |
| **HeartbeatReceiver** | IHeartbeatReceiverService 구현<br>설비 하트비트 수신 및 상태 저장<br>고장 상태 시 즉시 장애 감지 트리거 | UC-21<br>DD-04 (Heartbeat - 10분 주기) |
| **IFaultDetectionService** | 장애 감지 인터페이스를 정의<br>설비 고장 상태 분석 및 분류<br>장애 이벤트 발행 및 감사 로그 기록 | UC-21<br>QAS-01 (설비 고장 감지) |
| **FaultDetector** | IFaultDetectionService 구현<br>설비 장애 분석 및 이벤트 발행<br>EquipmentFaultEvent 생성 및 전송 | UC-21<br>QAS-01 (15초 이내 알림), DD-04 (Fault Detection) |
| **IPingEchoService** | Ping/Echo 서비스 인터페이스를 정의<br>설비 연결성 테스트 및 응답 검증<br>타임아웃 기반 장애 감지 | UC-21<br>DD-04 (Ping/Echo Tactic) |
| **EquipmentHealthChecker** | 시스템 구동형 건강 체크 조율자<br>스케줄러 기반 정기적 상태 점검<br>Ping/Echo 테스트 실행 및 결과 분석 | UC-21<br>DD-04 (Ping/Echo - 10초 주기) |
| **PingEchoExecutor** | IPingEchoService 구현<br>설비 연결성 테스트 실행<br>응답 시간 측정 및 타임아웃 처리 | UC-21<br>DD-04 (Ping/Echo Tactic) |
| **IAuditLogService** | 감사 로그 인터페이스를 정의<br>모든 모니터링 이벤트 및 조치 기록<br>감사 추적 및 컴플라이언스 지원 | QAS-04 (Security - 감사로그)<br>UC-21 (모니터링 감사) |
| **AuditLogger** | IAuditLogService 구현<br>모든 모니터링 이벤트 영속화<br>감사 로그 데이터베이스 저장 | QAS-04 (Security - 감사 추적)<br>UC-21 |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IEquipmentStatusRepository** | 설비 상태 저장소 인터페이스를 정의<br>설비 상태, 하트비트, 로그 데이터 CRUD 연산<br>JPA 기반 데이터 접근 추상화 | UC-21<br>DD-03 (Database per Service) |
| **EquipmentStatusJpaRepository** | IEquipmentStatusRepository 구현<br>JPA/Hibernate 기반 데이터 접근<br>설비 상태 및 로그 데이터 영속화 | DD-03 (Database per Service)<br>QAS-01 (데이터 영속성) |
| **MonitorDatabase** | 모니터링 서비스 전용 데이터베이스<br>설비 상태, 로그, 감사 데이터 저장<br>PostgreSQL 기반 암호화 저장 | DD-03 (Database per Service)<br>QAS-04 (Security - 데이터 암호화) |
| **IEquipmentGateway** | 설비 게이트웨이 인터페이스를 정의<br>설비와의 직접 통신 래핑<br>Ping/Echo 메시지 전송 및 응답 수신 | UC-21<br>DD-04 (Ping/Echo Tactic) |
| **EquipmentGatewayClient** | IEquipmentGateway 구현<br>설비 게이트웨이 HTTP/TCP 클라이언트<br>Ping/Echo 메시지 전송 및 응답 처리 | DD-04 (Ping/Echo)<br>UC-21 |
| **ISchedulerService** | 스케줄러 서비스 인터페이스를 정의<br>정기적 작업 실행 스케줄링<br>Quartz 기반 타이머 관리 | DD-04 (Scheduling Policy)<br>UC-21 (주기적 모니터링) |
| **QuartzScheduler** | ISchedulerService 구현<br>Quartz 스케줄러 통합<br>정기적 건강 체크 작업 실행 | DD-04 (Scheduling Policy)<br>QAS-01 (정기적 모니터링) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>장애 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-21 (EquipmentFaultEvent) |
| **RabbitMQAdapter** | IMessagePublisherService 구현<br>RabbitMQ 클라이언트 통합<br>EquipmentFaultEvent 발행 및 라우팅 | DD-02 (Event-Driven)<br>QAS-01 (실시간 알림) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 설비 및 관리자 요청 수신<br>설비 상태 수신 및 명령 API 공개 인터페이스<br>설비와의 첫 번째 상호작용 지점 | UC-21<br>QAS-01 (설비 고장 감지), QAS-04 (Security) |
| **Business Layer** | 설비 모니터링 핵심 로직 구현<br>하트비트/Ping-Echo 기반 장애 감지<br>이벤트 발행 및 감사 로그 기록 | DD-04 (Fault Detection), DD-02 (Event-Driven)<br>QAS-01 (15초 이내 알림), QAS-04 (감사로그) |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>데이터베이스, 설비 게이트웨이, 스케줄러, RabbitMQ 연동<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), DD-04 (Heartbeat/Ping-Echo) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 4 | 5 | 9 |
| **System Interface Layer** | 4 | 4 | 9 |
| **패키지** | - | - | 3 |
| **총계** | **10** | **11** | **25** |

---

## Architectural Drivers 적용 현황

### QAS-01 (설비 고장 감지 - 설비 고장 발생 시 15초 이내 알림 발송)
- **실시간 감지**: HeartbeatReceiver (10분 주기 하트비트 직접 감지)
- **능동 모니터링**: EquipmentHealthChecker (10초 주기 Ping/Echo)
- **빠른 알림**: FaultDetector → RabbitMQAdapter (EquipmentFaultEvent 즉시 발행)
- **목표 달성**: 이중 모니터링으로 15초 이내 장애 감지 보장

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **감사 추적**: AuditLogger, IAuditLogService (모든 모니터링 이벤트 로그)
- **접근 제어**: EquipmentCommandController, IEquipmentCommandApi (명령 권한 검증)
- **데이터 암호화**: MonitorDatabase (설비 상태 데이터 암호화 저장)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: RabbitMQAdapter (장애 이벤트 발행 보장)
- **데이터 영속성**: EquipmentStatusJpaRepository, IEquipmentStatusRepository
- **스케줄러 복원력**: QuartzScheduler (작업 재시작 및 복구)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: FaultDetector → RabbitMQAdapter (EquipmentFaultEvent)
- **비동기 처리**: 장애 감지를 동기 응답에서 분리하여 시스템 응답성 보장
- **느슨한 결합**: 모니터링 서비스와 알림 서비스 간 이벤트 기반 통신

### DD-03 (Database per Service)
- **독립 데이터베이스**: MonitorDatabase (다른 서비스와 완전 격리)
- **독점 접근**: EquipmentStatusJpaRepository, IEquipmentStatusRepository (Monitor Service만 접근)

### DD-04 (Fault Detection - 설비 고장 감지 및 실시간 알림)
- **Heartbeat Tactic**: HeartbeatReceiver (설비 주도 10분 주기 보고)
- **Ping/Echo Tactic**: EquipmentHealthChecker, PingEchoExecutor (시스템 주도 10초 주기 확인)
- **Maintain Audit Trail**: AuditLogger (모든 이벤트 및 조치 기록)
- **Passive Redundancy**: RabbitMQ를 통한 장애 이벤트 라우팅

### 관련 UC 목록
- **UC-21**: 설비 상태 모니터링 (Heartbeat + Ping/Echo)

---

## 이중 모니터링 전략 (DD-04 적용)

### 1. Heartbeat 기반 모니터링 (설비 주도)
- **트리거**: 설비가 10분마다 상태 보고
- **장점**: 실시간 고장 감지 (하트비트에 직접 '고장' 상태 포함)
- **단점**: 네트워크 장애 시 감지 불가

### 2. Ping/Echo 기반 모니터링 (시스템 주도)
- **트리거**: 스케줄러가 10초마다 상태 확인
- **장점**: 네트워크 장애 감지 가능 (30초 타임아웃)
- **단점**: 고장 상태 세부 정보 부족

### 상호 보완 설계
- **이중 감지**: 두 방식 모두 EquipmentFaultEvent 발행
- **빠른 응답**: Heartbeat로 즉시 감지 + Ping/Echo로 백업
- **15초 목표**: Ping/Echo의 빈번한 체크로 목표 달성 보장

---

## 결론

Monitoring Service 컴포넌트 다이어그램의 모든 요소(25개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 설비 상태/명령 API 인터페이스 (4개 요소)
- **Business Layer**: 장애 감지 및 모니터링 로직 (9개 요소)
- **System Interface Layer**: 데이터베이스/게이트웨이/스케줄러/RabbitMQ 연동 (9개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **이중 모니터링 전략**(Heartbeat + Ping/Echo)을 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
