# Notification Service Component Element List

## 개요
본 문서는 Notification Service 컴포넌트 다이어그램(`06_NotificationDispatcherComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **INotificationApi** | 알림 서비스 API 인터페이스를 정의<br>푸시 알림 전송 및 상태 조회 기능 제공<br>관리자용 알림 관리 인터페이스 | UC-05 (관리자 로그인), UC-20 (알림 발송)<br>QAS-04 (Security - 알림 권한 검증) |
| **NotificationController** | INotificationApi 인터페이스의 구현<br>알림 요청 수신 및 처리<br>알림 발송 조율 및 상태 응답 | UC-05, UC-20<br>QAS-04 (Security - 입력 검증) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **INotificationDispatcherService** | 알림 발송 서비스 인터페이스를 정의<br>다양한 유형의 알림 메시지 처리 및 발송<br>수신자 타겟팅 및 메시지 포맷팅 | UC-11 (맞춤형 알림), UC-20 (푸시 알림)<br>DD-02 (Event-Driven) |
| **NotificationDispatcherManager** | INotificationDispatcherService 구현<br>동기적 알림 요청 처리 및 발송<br>관리자 트리거 알림 발송 | UC-05, UC-20<br>QAS-05 (Availability - 즉시 알림) |
| **NotificationDispatcherConsumer** | 이벤트 기반 알림 소비자<br>도메인 이벤트 수신 및 자동 알림 발송<br>비동기 이벤트 처리 및 알림 트리거 | UC-11, UC-20<br>DD-02 (Event-Driven), DD-07 (Scheduling Policy) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IPushNotificationGateway** | 푸시 알림 게이트웨이 인터페이스를 정의<br>FCM/APNs 등 푸시 서비스 래핑<br>플랫폼별 메시지 전송 및 상태 추적 | UC-11, UC-20<br>QAS-05 (Availability - 푸시 전달) |
| **FcmPushGateway** | IPushNotificationGateway 구현<br>Firebase Cloud Messaging 클라이언트<br>Android/iOS 푸시 알림 전송 | QAS-05 (Availability - 크로스 플랫폼)<br>UC-11, UC-20 |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>RabbitMQ 이벤트 수신 및 처리<br>알림 트리거 이벤트 구독 | DD-02 (Event-Driven)<br>UC-11, UC-20 (이벤트 기반 알림) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>알림 이벤트 발행 및 라우팅<br>알림 상태 변경 이벤트 전송 | DD-02 (Event-Driven)<br>UC-11, UC-20 (알림 결과 이벤트) |
| **RabbitMQAdapter** | IMessageSubscriptionService 및 IMessagePublisherService 구현<br>RabbitMQ 클라이언트 통합<br>이벤트 수신 및 발행 관리 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 내구성) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 알림 요청 수신<br>알림 서비스 API 공개 인터페이스<br>관리자와의 첫 번째 상호작용 지점 | UC-05, UC-20<br>QAS-04 (Security), QAS-05 (Availability) |
| **Business Layer** | 알림 발송 핵심 로직 구현<br>동기/비동기 알림 처리<br>이벤트 기반 자동 알림 발송 | DD-02 (Event-Driven), DD-07 (Scheduling Policy)<br>QAS-05 (Availability), UC-11, UC-20 |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>FCM 푸시 게이트웨이, RabbitMQ 메시지 브로커 연동<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-05 (Availability), UC-11, UC-20 |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 1 | 1 | 2 |
| **Business Layer** | 1 | 2 | 3 |
| **System Interface Layer** | 3 | 2 | 5 |
| **패키지** | - | - | 3 |
| **총계** | **5** | **5** | **13** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **접근 제어**: NotificationController, INotificationApi (알림 발송 권한 검증)
- **메시지 검증**: 알림 내용 및 수신자 검증
- **감사 추적**: 모든 알림 이벤트 로깅

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **푸시 전달 보장**: FcmPushGateway, IPushNotificationGateway (재시도 및 실패 처리)
- **메시지 내구성**: RabbitMQAdapter (이벤트 발행/구독 보장)
- **크로스 플랫폼**: FCM을 통한 Android/iOS 지원

### DD-02 (Event-Driven Architecture)
- **이벤트 구독**: NotificationDispatcherConsumer ← RabbitMQAdapter
- **이벤트 발행**: NotificationDispatcherManager → RabbitMQAdapter (알림 결과 이벤트)
- **비동기 처리**: 도메인 이벤트 기반 자동 알림 발송

### DD-07 (Scheduling Policy - 피크타임 부하 분산)
- **스케줄링 적용**: NotificationDispatcherConsumer
- **부하 분산**: 피크타임 알림을 오프피크로 지연 발송
- **시스템 보호**: 실시간 응답성 저하 방지

### 관련 UC 목록
- **UC-05**: 관리자 로그인 (관리자 알림)
- **UC-11**: 맞춤형 알림 발송 (BranchPreferenceCreatedEvent 기반)
- **UC-20**: 푸시 알림 발송 (설비 고장 등 이벤트 기반)

---

## 이벤트 기반 알림 아키텍처

### 알림 트리거 이벤트
- **EquipmentFaultEvent**: 설비 고장 시 즉시 푸시 알림 (UC-21 → UC-20)
- **BranchPreferenceCreatedEvent**: 선호도 생성 시 맞춤형 알림 (UC-10 → UC-11)
- **TaskConfirmedEvent**: 작업 승인 시 보상 알림 (UC-14 → 관련 알림)

### 처리 흐름
1. **이벤트 수신**: NotificationDispatcherConsumer가 RabbitMQ에서 이벤트 구독
2. **알림 생성**: 이벤트 타입에 따른 알림 메시지 생성
3. **타겟팅**: 이벤트 데이터 기반 수신자 선정
4. **발송**: FcmPushGateway를 통한 푸시 알림 전송
5. **상태 추적**: 발송 결과 이벤트 발행

### 설계 원칙
- **Event-Driven**: 모든 알림이 도메인 이벤트에 의해 트리거
- **Asynchronous**: 동기 응답을 블로킹하지 않는 비동기 처리
- **Passive Redundancy**: 메시지 브로커를 통한 장애 내성

---

## 결론

Notification Service 컴포넌트 다이어그램의 모든 요소(13개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 알림 API 인터페이스 (2개 요소)
- **Business Layer**: 알림 발송 로직 (3개 요소)
- **System Interface Layer**: FCM/RabbitMQ 연동 (5개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **이벤트 기반 알림 발송** 아키텍처를 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
