# Helper Service Component Element List

## 개요
본 문서는 Helper Service 컴포넌트 다이어그램(`04_HelperServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IHelperTaskApi** | 헬퍼 작업 관리 API 인터페이스를 정의<br>작업 사진 등록, 작업 상태 조회 기능 제공<br>헬퍼 모바일 앱과의 통신 표준화 | UC-12 (작업 사진 등록)<br>QAS-04 (Security - 작업 권한 검증) |
| **IHelperRewardApi** | 헬퍼 보상 관리 API 인터페이스를 정의<br>작업 승인, 보상 확인, 잔액 조회 기능 제공<br>보상 시스템 투명성 보장 | UC-14 (작업 승인 및 보상)<br>QAS-04 (Security - 보상 권한 검증) |
| **TaskController** | IHelperTaskApi 인터페이스의 구현<br>작업 관련 요청 수신 및 응답 처리<br>사진 업로드 및 작업 등록 조율 | UC-12<br>QAS-04 (Security - 파일 업로드 검증) |
| **RewardController** | IHelperRewardApi 인터페이스의 구현<br>보상 관련 요청 수신 및 처리<br>작업 승인 및 보상 지급 조율 | UC-14<br>QAS-04 (Security - 보상 권한 검증) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ITaskSubmissionService** | 작업 제출 인터페이스를 정의<br>사진 업로드 및 작업 등록 처리<br>일일 제한 검증 및 이벤트 발행 | UC-12<br>DD-02 (Event-Driven), QAS-05 (Availability) |
| **TaskSubmissionManager** | ITaskSubmissionService 구현<br>사진 업로드 및 작업 등록 조율<br>검증 통과 시 이벤트 발행 | UC-12<br>DD-02 (Event-Driven), QAS-05 (Availability) |
| **ITaskValidationService** | 작업 검증 인터페이스를 정의<br>일일 작업 제한(3회) 검증<br>헬퍼 자격 및 상태 확인 | UC-12<br>QAS-04 (Security - 제한 준수) |
| **DailyLimitValidator** | ITaskValidationService 구현<br>헬퍼별 일일 작업 제한 검증<br>3회/일 제한 정책 적용 | UC-12<br>QAS-04 (Security - 자원 남용 방지) |
| **ITaskAnalysisService** | AI 작업 분석 인터페이스를 정의<br>업로드된 사진의 AI 분석 처리<br>품질 판정(양호/미흡/불분명) 로직 | UC-13<br>QAS-06 (Modifiability - AI 모델 교체) |
| **TaskAnalysisEngine** | ITaskAnalysisService 구현<br>사진 다운로드 및 AI 분석 실행<br>ML 추론 결과 저장 | UC-13<br>QAS-06 (Modifiability - AI 어댑터 패턴) |
| **AITaskAnalysisConsumer** | AI 분석 이벤트 소비자<br>TaskSubmittedEvent 수신 및 처리<br>비동기 AI 분석 워크플로우 시작 | UC-13<br>DD-02 (Event-Driven), QAS-05 (Availability) |
| **IRewardConfirmationService** | 보상 승인 인터페이스를 정의<br>지점주 작업 승인 처리<br>보상 계산 및 지급 트리거 | UC-14<br>DD-02 (Event-Driven) |
| **RewardConfirmationManager** | IRewardConfirmationService 구현<br>지점주 작업 승인 처리<br>보상 계산 및 이벤트 발행 | UC-14<br>DD-02 (Event-Driven) |
| **IRewardCalculationService** | 보상 계산 인터페이스를 정의<br>작업 유형별 보상 금액 산정<br>추가 보너스 및 패널티 적용 | UC-14<br>QAS-04 (Security - 정확한 계산) |
| **RewardCalculator** | IRewardCalculationService 구현<br>작업 유형별 보상 계산<br>정확도 보너스/패널티 적용 | UC-14<br>QAS-04 (Security - 계산 정확성) |
| **RewardUpdateConsumer** | 보상 업데이트 이벤트 소비자<br>TaskConfirmedEvent 수신<br>헬퍼 잔액 자동 업데이트 | UC-16<br>DD-02 (Event-Driven) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IHelperRepository** | 헬퍼 데이터 저장소 인터페이스를 정의<br>작업, 보상, 헬퍼 정보 CRUD 연산<br>JPA 기반 데이터 접근 추상화 | UC-12, UC-13, UC-14, UC-16<br>DD-03 (Database per Service) |
| **HelperJpaRepository** | IHelperRepository 구현<br>JPA/Hibernate 기반 데이터 접근<br>작업 및 보상 데이터 영속화 | DD-03 (Database per Service)<br>QAS-05 (Availability - 데이터 영속성) |
| **HelperDatabase** | 헬퍼 서비스 전용 데이터베이스<br>작업, 보상, 헬퍼 정보 저장<br>PostgreSQL 기반 암호화 저장 | DD-03 (Database per Service)<br>QAS-04 (Security - 데이터 암호화) |
| **ITaskPhotoStorage** | 작업 사진 저장소 인터페이스를 정의<br>S3 기반 사진 업로드/다운로드<br>임시 URL 생성 및 접근 제어 | UC-12, UC-13<br>QAS-04 (Security - 파일 보안) |
| **S3PhotoStorage** | ITaskPhotoStorage 구현<br>AWS S3 클라이언트 통합<br>사진 업로드/다운로드 및 보안 | QAS-04 (Security - 파일 암호화)<br>QAS-05 (Availability - 클라우드 스토리지) |
| **IMLInferenceEngine** | ML 추론 엔진 인터페이스를 정의<br>AI 모델 기반 이미지 분석<br>세탁물 품질 판정 API | UC-13<br>QAS-06 (Modifiability - 모델 교체) |
| **MLInferenceEngineAdapter** | IMLInferenceEngine 구현<br>ML 추론 엔진 어댑터<br>HTTP/gRPC 기반 AI 모델 호출 | QAS-06 (Modifiability - AI 교체)<br>DD-05 (Pipeline Optimization) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>작업 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-12, UC-14 (TaskSubmittedEvent, TaskConfirmedEvent) |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>외부 이벤트 수신 및 처리<br>다양한 이벤트 소비자 연동 | DD-02 (Event-Driven)<br>UC-13, UC-16 (이벤트 구독) |
| **RabbitMQAdapter** | IMessagePublisherService 및 IMessageSubscriptionService 구현<br>RabbitMQ 클라이언트 통합<br>이벤트 발행 및 구독 관리 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 내구성) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 헬퍼 요청 수신 및 처리<br>작업 및 보상 관리 API 공개 인터페이스<br>모바일 앱과의 첫 번째 상호작용 지점 | UC-12, UC-14<br>QAS-04 (Security), QAS-05 (Availability) |
| **Business Layer** | 헬퍼 작업 처리 핵심 로직 구현<br>작업 제출/검증, AI 분석, 보상 계산<br>이벤트 기반 비동기 처리 | DD-02 (Event-Driven), DD-05 (Pipeline Optimization)<br>QAS-06 (Modifiability), QAS-05 (Availability) |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>데이터베이스, S3, ML 엔진, RabbitMQ 연동<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), QAS-04 (Security) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 5 | 7 | 12 |
| **System Interface Layer** | 5 | 4 | 10 |
| **패키지** | - | - | 3 |
| **총계** | **12** | **13** | **29** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **파일 보안**: S3PhotoStorage, ITaskPhotoStorage (업로드 파일 검증)
- **권한 검증**: DailyLimitValidator, ITaskValidationService (자원 남용 방지)
- **보상 정확성**: RewardCalculator, IRewardCalculationService (계산 검증)
- **감사 로그**: 모든 작업 이벤트 RabbitMQ로 발행 (RabbitMQAdapter)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: RabbitMQAdapter (이벤트 발행 보장)
- **데이터 영속성**: HelperJpaRepository, IHelperRepository
- **클라우드 스토리지**: S3PhotoStorage (고가용성 파일 저장)
- **비동기 처리**: 이벤트 기반 아키텍처로 API 응답성 보장

### QAS-06 (Modifiability - 새로운 AI 모델 및 분석 알고리즘의 신속한 적용)
- **AI 어댑터 패턴**: MLInferenceEngineAdapter, IMLInferenceEngine
- **모델 교체**: TaskAnalysisEngine에서 다른 AI 모델로 쉽게 교체 가능
- **파이프라인 최적화**: DD-05 적용으로 AI 분석 성능 향상

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: TaskSubmissionManager, RewardConfirmationManager → RabbitMQAdapter
- **이벤트 구독**: AITaskAnalysisConsumer, RewardUpdateConsumer ← RabbitMQAdapter
- **이벤트 유형**: TaskSubmittedEvent, TaskConfirmedEvent

### DD-03 (Database per Service)
- **독립 데이터베이스**: HelperDatabase (다른 서비스와 완전 격리)
- **독점 접근**: HelperJpaRepository, IHelperRepository (Helper Service만 접근)

### DD-05 (Pipeline Optimization - 병렬 처리 및 파이프라인 최적화)
- **AI 파이프라인**: 사진 다운로드 → AI 분석 → 결과 저장
- **비동기 처리**: TaskSubmittedEvent 기반 이벤트 드리븐 분석
- **성능 최적화**: MLInferenceEngineAdapter를 통한 효율적인 AI 호출

### 관련 UC 목록
- **UC-12**: 작업 사진 등록 (TaskSubmissionManager)
- **UC-13**: AI 세탁물 작업 1차 판독 (AITaskAnalysisConsumer, TaskAnalysisEngine)
- **UC-14**: 작업 승인 및 보상 지급 (RewardConfirmationManager, RewardCalculator)
- **UC-16**: 보상 잔액 업데이트 (RewardUpdateConsumer)

---

## 이벤트 기반 아키텍처 흐름

### 작업 제출 → AI 분석 → 승인 → 보상 (DD-02 적용)
1. **TaskSubmittedEvent**: 헬퍼 사진 업로드 시 발행
2. **AI 분석**: AITaskAnalysisConsumer가 이벤트 수신하여 AI 분석 실행
3. **TaskConfirmedEvent**: 지점주 승인 시 발행
4. **보상 업데이트**: RewardUpdateConsumer가 잔액 자동 업데이트

### 설계 원칙
- **Message Based**: RabbitMQ를 통한 느슨한 결합
- **Passive Redundancy**: 메시지 브로커를 통한 장애 내성
- **Async Processing**: API 응답성과 시스템 확장성 보장

---

## 결론

Helper Service 컴포넌트 다이어그램의 모든 요소(29개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 헬퍼 작업/보상 API 인터페이스 (4개 요소)
- **Business Layer**: 이벤트 기반 작업 처리 로직 (12개 요소)
- **System Interface Layer**: 데이터베이스/S3/ML/RabbitMQ 연동 (10개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **이벤트 기반 아키텍처**와 **AI 분석 파이프라인**을 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
