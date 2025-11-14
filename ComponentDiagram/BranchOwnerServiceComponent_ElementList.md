# Branch Owner Service Component Element List

## 개요
본 문서는 Branch Owner Service 컴포넌트 다이어그램(`09_BranchOwnerServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IBranchOwnerApi** | 지점주 관리 API 인터페이스를 정의<br>지점주 계정 등록, 정보 관리 기능 제공<br>지점주 전용 관리 인터페이스 | UC-03 (지점주 계정 등록), UC-19 (고객 리뷰 조회)<br>QAS-04 (Security - 지점주 권한 검증) |
| **IBranchQueryApi** | 지점 조회 API 인터페이스를 정의<br>지점 정보 검색, 리뷰 조회 기능 제공<br>지점주용 지점 관리 인터페이스 | UC-18 (지점 정보 등록), UC-19 (리뷰 조회)<br>QAS-04 (Security - 지점 소유권 검증) |
| **BranchOwnerController** | IBranchOwnerApi 인터페이스의 구현<br>지점주 관련 요청 수신 및 처리<br>계정 등록 및 정보 관리 조율 | UC-03, UC-19<br>QAS-04 (Security - 권한 검증) |
| **BranchQueryController** | IBranchQueryApi 인터페이스의 구현<br>지점 조회 요청 수신 및 처리<br>지점 정보 및 리뷰 검색 조율 | UC-18, UC-19<br>QAS-04 (Security - 접근 제어) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IBranchOwnerManagementService** | 지점주 관리 서비스 인터페이스를 정의<br>지점주 계정 등록, 인증 연동, 정보 관리<br>지점주 비즈니스 로직 처리 | UC-03<br>DD-02 (Event-Driven), QAS-04 (Security) |
| **BranchOwnerManager** | IBranchOwnerManagementService 구현<br>지점주 계정 등록 및 관리 처리<br>인증 서비스 연동 및 이벤트 발행 | UC-03<br>DD-02 (Event-Driven), QAS-04 (Security) |
| **IBranchInfoService** | 지점 정보 서비스 인터페이스를 정의<br>지점 등록, 검증, 정보 조회 기능<br>지점 데이터 무결성 보장 | UC-18, UC-19<br>DD-03 (Database per Service) |
| **BranchInfoValidator** | IBranchInfoService 구현<br>지점 정보 등록 및 검증 처리<br>지점 데이터 유효성 검사 | UC-18<br>QAS-04 (Security - 데이터 검증) |
| **BranchEventProcessor** | 지점 관련 이벤트 처리자<br>외부 이벤트 수신 및 지점 데이터 업데이트<br>이벤트 기반 지점 상태 동기화 | UC-18, UC-19<br>DD-02 (Event-Driven) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IBranchRepository** | 지점 저장소 인터페이스를 정의<br>지점 정보, 리뷰 데이터 CRUD 연산<br>JPA 기반 데이터 접근 추상화 | UC-18, UC-19<br>DD-03 (Database per Service) |
| **BranchJpaRepository** | IBranchRepository 구현<br>JPA/Hibernate 기반 지점 데이터 접근<br>지점 정보 및 리뷰 영속화 | DD-03 (Database per Service)<br>QAS-05 (Availability - 데이터 영속성) |
| **BranchDatabase** | 지점 서비스 전용 데이터베이스<br>지점 정보, 리뷰, 이벤트 데이터 저장<br>PostgreSQL 기반 암호화 저장 | DD-03 (Database per Service)<br>QAS-04 (Security - 데이터 암호화) |
| **IAuthRepository** | 인증 저장소 인터페이스를 정의<br>지점주 계정 정보 조회<br>인증 서비스 데이터 접근 | UC-03<br>DD-03 (Database per Service) |
| **AuthJpaRepository** | IAuthRepository 구현<br>인증 서비스 데이터 접근<br>지점주 계정 정보 조회 | DD-03 (Database per Service)<br>QAS-04 (Security - 계정 검증) |
| **AuthDatabase** | 인증 서비스 데이터베이스<br>지점주 계정 정보 저장<br>읽기 전용 접근 | DD-03 (Database per Service)<br>QAS-04 (Security - 계정 보호) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>지점 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-03, UC-18 (지점 이벤트) |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>외부 이벤트 수신 및 처리<br>BranchEventProcessor 연동 | DD-02 (Event-Driven)<br>UC-18, UC-19 (지점 관련 이벤트) |
| **RabbitMQAdapter** | IMessagePublisherService 및 IMessageSubscriptionService 구현<br>RabbitMQ 클라이언트 통합<br>지점 이벤트 발행 및 구독 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 내구성) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 지점주 요청 수신 및 처리<br>지점주 관리 및 조회 API 공개 인터페이스<br>지점주 앱과의 첫 번째 상호작용 지점 | UC-03, UC-18, UC-19<br>QAS-04 (Security), QAS-05 (Availability) |
| **Business Layer** | 지점주 및 지점 관리 핵심 로직 구현<br>계정 등록, 지점 정보 검증, 이벤트 처리<br>지점주 비즈니스 규칙 적용 | DD-02 (Event-Driven), DD-03 (Database per Service)<br>QAS-04 (Security), UC-03, UC-18, UC-19 |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>지점 데이터베이스, 인증 데이터베이스, RabbitMQ 연동<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), QAS-04 (Security) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 2 | 3 | 5 |
| **System Interface Layer** | 4 | 3 | 9 |
| **패키지** | - | - | 3 |
| **총계** | **8** | **8** | **21** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **지점주 권한**: BranchOwnerController, IBranchOwnerApi (지점주 전용 기능)
- **지점 소유권**: BranchQueryController, IBranchQueryApi (본인 지점만 조회)
- **데이터 검증**: BranchInfoValidator, IBranchInfoService (지점 정보 유효성)
- **계정 보호**: AuthJpaRepository, IAuthRepository (안전한 계정 조회)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **데이터 영속성**: BranchJpaRepository, IBranchRepository
- **메시지 내구성**: RabbitMQAdapter (이벤트 발행/구독 보장)
- **읽기 전용 연동**: AuthDatabase (인증 서비스 안전한 조회)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: BranchOwnerManager → RabbitMQAdapter (지점 등록 이벤트)
- **이벤트 구독**: BranchEventProcessor ← RabbitMQAdapter
- **비동기 처리**: 지점 상태 변경을 이벤트로 처리

### DD-03 (Database per Service)
- **지점 전용 DB**: BranchDatabase (지점 데이터 독립 저장)
- **읽기 전용 연동**: AuthDatabase (계정 정보 안전 조회)
- **데이터 격리**: 각 서비스의 데이터 완전 분리

### 관련 UC 목록
- **UC-03**: 지점주 계정 등록 (BranchOwnerManager)
- **UC-18**: 지점 정보 등록 (BranchInfoValidator, BranchEventProcessor)
- **UC-19**: 고객 리뷰 조회 (BranchQueryController)

---

## 지점주 서비스 아키텍처 특징

### 데이터 접근 패턴
- **지점 데이터**: BranchJpaRepository → BranchDatabase (완전 제어)
- **계정 데이터**: AuthJpaRepository → AuthDatabase (읽기 전용)

### 이벤트 기반 통합
- **지점 등록**: BranchOwnerManager → BranchInfoCreatedEvent 발행
- **이벤트 처리**: BranchEventProcessor가 외부 이벤트 수신
- **데이터 동기화**: 이벤트 기반 지점 상태 업데이트

### 보안 설계
- **권한 분리**: 지점주는 본인의 지점 정보만 접근 가능
- **데이터 검증**: 모든 지점 정보 등록 시 철저한 검증
- **감사 추적**: 모든 지점주 활동 로깅

---

## 결론

Branch Owner Service 컴포넌트 다이어그램의 모든 요소(21개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 지점주/지점 조회 API 인터페이스 (4개 요소)
- **Business Layer**: 지점주/지점 관리 로직 (5개 요소)
- **System Interface Layer**: 데이터베이스/RabbitMQ 연동 (9개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **지점주 권한 관리**와 **데이터베이스 분리**를 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
