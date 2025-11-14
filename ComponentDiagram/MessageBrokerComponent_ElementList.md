# Message Broker Component Element List

## 개요
본 문서는 Message Broker 컴포넌트 다이어그램(`01_MessageBrokerComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IMessagePublisherService / MessagePublisherApi** | 메시지 발행 서비스 인터페이스를 정의하고 구현<br>모든 서비스의 이벤트 발행 표준화<br>토픽 기반 메시지 라우팅 및 외부 발행 요청 수신 | DD-02 (Event-Driven), UC-01~UC-24 (모든 이벤트 발행)<br>QAS-05 (Availability - 메시지 발행 보장) |
| **IMessageSubscriptionService / MessageSubscriptionApi** | 메시지 구독 서비스 인터페이스를 정의하고 구현<br>이벤트 수신 및 처리 표준화<br>컨슈머 등록 및 관리 및 외부 구독 요청 수신 | DD-02 (Event-Driven), UC-01~UC-24 (모든 이벤트 구독)<br>QAS-05 (Availability - 메시지 수신 보장) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ITopicRegistry** | 토픽 레지스트리 인터페이스를 정의<br>토픽 및 라우팅 키 관리<br>동적 토픽 등록 및 해제 | DD-02 (Event-Driven)<br>DD-07 (Scheduling Policy) |
| **IEventPublisher** | 이벤트 발행 인터페이스를 정의<br>이벤트 객체를 메시지로 변환 및 발행<br>메시지 직렬화 및 라우팅 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 이벤트 내구성) |
| **ISubscriptionManager** | 구독 관리 인터페이스를 정의<br>컨슈머 등록 및 해제 관리<br>메시지 필터링 및 전달 | DD-02 (Event-Driven)<br>DD-07 (Scheduling Policy) |
| **IRoutingKeyResolver** | 라우팅 키 리졸버 인터페이스를 정의<br>이벤트 타입에 따른 라우팅 키 결정<br>토픽 기반 메시지 분배 | DD-02 (Event-Driven)<br>DD-07 (Scheduling Policy) |
| **IMessageSerializer** | 메시지 직렬화 인터페이스를 정의<br>이벤트 객체를 JSON/XML 변환<br>메시지 포맷 표준화 | DD-02 (Event-Driven)<br>QAS-04 (Security - 메시지 무결성) |
| **TopicRegistry** | ITopicRegistry 구현<br>토픽 및 바인딩 정보 저장<br>런타임 토픽 관리 | DD-02 (Event-Driven)<br>DD-07 (동적 토픽 관리) |
| **EventPublisher** | IEventPublisher 구현<br>이벤트 발행 워크플로우 실행<br>직렬화 → 라우팅 → 전송 | DD-02 (Event-Driven)<br>QAS-05 (메시지 내구성) |
| **SubscriptionManager** | ISubscriptionManager 구현<br>컨슈머 연결 및 메시지 전달<br>구독자 상태 관리 | DD-02 (Event-Driven)<br>QAS-05 (메시지 전달 보장) |
| **RoutingKeyResolver** | IRoutingKeyResolver 구현<br>이벤트 메타데이터 기반 라우팅 결정<br>토픽 및 키 맵핑 | DD-02 (Event-Driven)<br>DD-07 (스케줄링 기반 라우팅) |
| **MessageSerializer** | IMessageSerializer 구현<br>이벤트 객체를 JSON 직렬화<br>메시지 헤더 및 본문 구성 | DD-02 (Event-Driven)<br>QAS-04 (메시지 보안) |
| **MessageBrokerCoordinator** | 메시지 브로커 코디네이터<br>발행/구독 요청 통합 조율<br>브로커 컴포넌트 간 중재 | DD-02 (Event-Driven)<br>DD-07 (중재자 패턴) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IRabbitMQConnection / RabbitMQConnectionPool** | RabbitMQ 연결 인터페이스를 정의하고 구현<br>연결 풀 및 채널 관리<br>AMQP 프로토콜 래핑 및 연결 풀 기반 채널 관리 | DD-02 (Event-Driven), QAS-05 (Availability - 연결 복원력, 연결 풀) |
| **IMessageQueueAdapter / RabbitMQAdapter** | 메시지 큐 어댑터 인터페이스를 정의하고 구현<br>RabbitMQ API 래핑<br>큐 연산 및 메시지 관리 및 RabbitMQ 클라이언트 통합 | DD-02 (Event-Driven), QAS-05 (Availability - 큐 내구성, 메시지 내구성) |
| **MessageQueue** | 메시지 큐<br>이벤트 메시지 저장 및 전달<br>RabbitMQ 기반 지속성 큐 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 보존) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 메시징 요청 수신<br>발행/구독 API 공개 인터페이스<br>서비스 간 첫 번째 메시징 상호작용 지점 | DD-02 (Event-Driven)<br>QAS-05 (Availability), UC-01~UC-24 |
| **Business Layer** | 메시지 브로커링 핵심 로직 구현<br>토픽 관리, 라우팅, 직렬화, 구독 관리<br>이벤트 기반 통합 중재 | DD-02 (Event-Driven), DD-07 (Scheduling Policy)<br>QAS-05 (Availability), DD-04 (Passive Redundancy) |
| **System Interface Layer** | RabbitMQ 시스템 연동 인터페이스<br>연결 관리, 큐 연산, AMQP 프로토콜 변환<br>메시지 인프라 추상화 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-05 (Availability), DD-04 (Heartbeat/Ping-Echo) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 5 | 6 | 11 |
| **System Interface Layer** | 2 | 3 | 5 |
| **패키지** | - | - | 3 |
| **총계** | **9** | **11** | **23** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **메시지 무결성**: MessageSerializer (JSON 직렬화 보안)
- **접근 제어**: SubscriptionManager (컨슈머 권한 검증)
- **감사 추적**: 모든 메시지 이벤트 로깅

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: MessageQueue (RabbitMQ 지속성 큐)
- **연결 복원력**: RabbitMQConnectionPool (자동 재연결)
- **큐 내구성**: RabbitMQAdapter (메시지 보존 보장)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: EventPublisher → RabbitMQAdapter (모든 도메인 이벤트)
- **이벤트 구독**: SubscriptionManager ← RabbitMQAdapter (모든 컨슈머)
- **비동기 통합**: 서비스 간 느슨한 결합 제공

### DD-04 (Fault Detection - 설비 고장 감지 및 실시간 알림)
- **Passive Redundancy**: 메시지 큐를 통한 장애 내성
- **메시지 지속성**: 큐 기반 메시지 보존
- **이벤트 기반**: EquipmentFaultEvent 등 장애 이벤트 라우팅

### DD-07 (Scheduling Policy - 피크타임 부하 분산)
- **토픽 관리**: TopicRegistry (동적 토픽 생성)
- **라우팅 제어**: RoutingKeyResolver (스케줄링 기반 라우팅)
- **구독 관리**: SubscriptionManager (부하 분산 구독)

### 관련 UC 목록
- **UC-01~UC-24**: 모든 유스케이스 (이벤트 기반 통합)

---

## 주요 이벤트 목록 (DD-02 적용)

### 코어 비즈니스 이벤트
- **TaskSubmittedEvent**: 헬퍼 작업 제출 (Helper → AI 분석)
- **TaskConfirmedEvent**: 작업 승인 (Helper → 보상 업데이트)
- **BranchPreferenceCreatedEvent**: 선호도 생성 (Search → 맞춤 알림)
- **EquipmentFaultEvent**: 설비 고장 (Monitor → 알림 발송)
- **BranchInfoCreatedEvent**: 지점 정보 생성 (BranchOwner → 선호도 매칭)

### 시스템 이벤트
- **AccessGrantedEvent**: 출입 허용 (Access → 감사)
- **AccessDeniedEvent**: 출입 거부 (Access → 감사)
- **ModelDeployedEvent**: 모델 배포 (MLOps → 시스템 알림)
- **ModelVerificationFailedEvent**: 모델 검증 실패 (MLOps → 알림)

---

## 메시지 브로커 아키텍처 원칙

### 순수 인프라 설계
- **비즈니스 로직 없음**: 모든 로직은 발행/구독 서비스에 존재
- **데이터 저장 없음**: 메시지만 전달, 저장은 각 서비스 DB
- **상태 비저장**: 재시작 시 상태 복원 불필요

### 중재자 패턴 (Use an Intermediary)
- **발행자-구독자 분리**: 직접 통신 제거
- **동적 바인딩**: 런타임 토픽/컨슈머 추가 가능
- **확장성**: 새로운 이벤트 타입 쉽게 추가

### 패시브 리던던시 (Passive Redundancy)
- **메시지 큐**: 장애 발생 시 메시지 보존
- **재연결**: 연결 끊김 시 자동 복구
- **내구성**: 메시지 손실 방지

---

## 결론

Message Broker 컴포넌트 다이어그램의 모든 요소(23개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 메시징 API 인터페이스 (4개 요소)
- **Business Layer**: 이벤트 브로커링 로직 (11개 요소)
- **System Interface Layer**: RabbitMQ 연동 (5개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **순수 인프라**로서의 **중재자 역할**과 **이벤트 기반 통합**을 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
