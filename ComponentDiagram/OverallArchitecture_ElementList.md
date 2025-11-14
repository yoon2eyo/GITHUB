# Overall Architecture Component Element List

## 개요
본 문서는 Overall Architecture 컴포넌트 다이어그램(`00_Overall_Architecture.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

이 다이어그램은 전체 시스템의 고수준 아키텍처를 보여주므로, 개별 컴포넌트 다이어그램과 달리 Layer별 분류 대신 요소 유형별로 분류합니다.

---

## 외부 액터 (External Actors)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Customer App** | 고객 모바일 애플리케이션<br>지점 검색, 리뷰 작성, 예약 기능 제공<br>고객의 주요 사용자 인터페이스 | UC-01~UC-03, UC-09~UC-11, UC-17~UC-19<br>QAS-04 (Security - 사용자 인증) |
| **Helper App** | 헬퍼 모바일 애플리케이션<br>작업 사진 업로드, 보상 확인 기능 제공<br>헬퍼의 주요 작업 인터페이스 | UC-12~UC-16<br>QAS-04 (Security - 헬퍼 인증) |
| **Branch Manager App** | 지점장 모바일 애플리케이션<br>지점 관리, 작업 승인, 알림 수신 기능 제공<br>지점주의 관리 인터페이스 | UC-14, UC-18, UC-19, UC-20<br>QAS-04 (Security - 관리자 권한) |
| **Operations Center** | 운영 센터 콘솔<br>시스템 모니터링, 관리자 기능 제공<br>운영팀의 관리 인터페이스 | UC-05, UC-06<br>QAS-05 (Availability - 시스템 모니터링) |

---

## 외부 파트너 시스템 (External Partner Systems)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ICreditCardVerificationService** | 신용카드 본인 인증 서비스<br>회원가입 시 신용카드 검증<br>외부 PG사 연동 | UC-04 (회원가입)<br>QAS-04 (Security - 결제 정보 보호) |
| **ILLMAnalysisService** | 외부 LLM 분석 서비스<br>리뷰 텍스트 키워드 추출 및 감정 분석<br>OpenAI/Claude API 연동 | UC-10, UC-18 (Cold Path)<br>DD-09 (Hot/Cold Path Separation) |

---

## 지점 설비 (Branch Equipment)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Branch Equipment & Cameras** | 지점 설비 및 카메라 시스템<br>얼굴 인식 카메라, 출입 게이트, 상태 센서<br>설비 하트비트 및 제어 인터페이스 | UC-07, UC-08, UC-21<br>QAS-01 (설비 고장 감지), QAS-02 (출입 제어) |

---

## 푸시 알림 게이트웨이 (Push Notification Gateway)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IPushNotificationGateway** | 푸시 알림 게이트웨이 인터페이스<br>FCM/APNs 기반 푸시 알림 전송<br>크로스 플랫폼 알림 지원 | UC-11, UC-20<br>QAS-05 (Availability - 알림 전달) |

---

## API 게이트웨이 (API Gateway)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **RequestRouter (ApiGateway)** | API 게이트웨이 및 요청 라우터<br>모든 클라이언트 요청의 단일 진입점<br>인증, 라우팅, 로드밸런싱 | UC-01~UC-24 (모든 API 요청)<br>DD-01 (MSA), QAS-04 (Security), QAS-05 (Availability) |

---

## 메시지 브로커 (Message Broker)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **RabbitMQ (Message Broker)** | 메시지 브로커 및 이벤트 버스<br>서비스 간 비동기 이벤트 라우팅<br>이벤트 기반 느슨한 결합 | DD-02 (Event-Driven)<br>UC-01~UC-24 (모든 이벤트 통신) |

---

## 실시간 접근 계층 (Real-Time Access Layer)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **AccessAuthorizationManager** | 출입 인증 관리자<br>얼굴 인식 및 QR 출입 제어<br>실시간 게이트 제어 | UC-07, UC-08<br>QAS-02 (3초 SLA), DD-05 (IPC 최적화) |
| **VectorComparisonEngine (FACE MODEL)** | 벡터 비교 엔진<br>얼굴 특징 벡터 유사도 계산<br>병렬 파이프라인 기반 고성능 비교 | UC-07<br>DD-05 (49% 성능 향상), QAS-02 (205ms) |

---

## 비즈니스 로직 계층 (Business Logic Layer)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **AuthenticationManager** | 인증 관리자<br>JWT 토큰 발급 및 검증<br>사용자 권한 관리 | UC-01~UC-05<br>QAS-04 (Security), DD-01 (MSA) |
| **BranchContentService** | 지점 콘텐츠 서비스<br>지점 검색 및 리뷰 관리<br>Hot/Cold Path 분리 | UC-09, UC-10, UC-18<br>QAS-03 (검색 SLA), DD-09 (Hot/Cold Path) |
| **TaskManagementManager** | 작업 관리자<br>헬퍼 작업 관리 및 AI 분석 트리거<br>보상 시스템 운영 | UC-12~UC-16<br>QAS-06 (Modifiability), DD-02 (Event-Driven) |
| **BranchOwnerManager** | 지점주 관리자<br>지점주 계정 및 정보 관리<br>지점 운영 데이터 제공 | UC-03, UC-18, UC-19<br>DD-03 (Database per Service) |
| **StatusReceiverManager** | 상태 수신 관리자<br>설비 모니터링 및 장애 감지<br>하트비트 및 Ping/Echo 처리 | UC-21<br>QAS-01 (15초 알림), DD-04 (Fault Detection) |
| **NotificationDispatcherConsumer** | 알림 발송 컨슈머<br>이벤트 기반 푸시 알림 발송<br>지점주 및 고객 대상 알림 | UC-11, UC-20<br>DD-02 (Event-Driven), DD-07 (Scheduling Policy) |

---

## AI 파이프라인 계층 (AI Pipeline Layer)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **MLOpsTrainingService** | MLOps 훈련 서비스<br>모델 재훈련 파이프라인 오케스트레이션<br>데이터 수집, 훈련, 검증, 배포 | UC-24<br>QAS-06 (Modifiability), DD-05 (Pipeline Optimization) |
| **MLInferenceEngine (Internal ML Platform)** | ML 추론 엔진<br>내부 ML 플랫폼 및 모델 호스팅<br>훈련 및 추론 API 제공 | UC-13, UC-24<br>QAS-06 (AI 모델 교체), DD-05 (Hot Swap) |

---

## 패키지 구조 (Package Structure)

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Real-Time Access Layer** | 실시간 출입 제어 및 얼굴 인식<br>성능이 가장 중요한 계층<br>IPC 최적화 및 캐시 적용 | QAS-02 (3초 SLA), DD-05 (Performance Optimization)<br>UC-07, UC-08 |
| **Business Logic Layer** | 핵심 비즈니스 로직 구현<br>인증, 검색, 작업 관리, 모니터링<br>MSA 기반 서비스 분리 | DD-01 (4-Layer MSA), DD-02 (Event-Driven)<br>QAS-04 (Security), QAS-05 (Availability) |
| **AI Pipeline Layer** | AI/ML 파이프라인 및 모델 관리<br>MLOps 및 실시간 추론<br>모델 교체 및 Hot Swap | QAS-06 (Modifiability), DD-05 (Pipeline Optimization)<br>UC-13, UC-24 |

---

## 요소 수량 요약

| 요소 유형 | 개수 |
|----------|------|
| **외부 액터** | 4 |
| **외부 파트너 시스템** | 2 |
| **지점 설비** | 1 |
| **푸시 알림 게이트웨이** | 1 |
| **API 게이트웨이** | 1 |
| **메시지 브로커** | 1 |
| **실시간 접근 계층 컴포넌트** | 2 |
| **비즈니스 로직 계층 컴포넌트** | 6 |
| **AI 파이프라인 계층 컴포넌트** | 2 |
| **패키지** | 3 |
| **총계** | **23** |

---

## Architectural Drivers 적용 현황

### QAS-01 (설비 고장 감지 - 설비 고장 발생 시 15초 이내 알림 발송)
- **실시간 모니터링**: Equipment → StatusReceiverManager (하트비트/Ping-Echo)
- **빠른 알림**: StatusReceiverManager → NotificationDispatcherConsumer (이벤트 기반)

### QAS-02 (성능 - 출입 인증 시스템 응답시간 3초 이내 95% 달성)
- **IPC 최적화**: AccessAuthorizationManager ↔ VectorComparisonEngine (205ms)
- **병렬 처리**: VectorComparisonEngine (49% 성능 향상)
- **캐시 적용**: Face vectors 메모리 캐시

### QAS-03 (성능 - 검색 서비스 응답시간 3초 이내 95% 달성)
- **Hot Path**: BranchContentService (LLM 배제, 로컬 토큰화)
- **ElasticSearch**: 고성능 검색 엔진 활용

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **API Gateway**: RequestRouter (통합 보안 게이트웨이)
- **인증/인가**: AuthenticationManager (JWT 및 RBAC)
- **접근 제어**: 모든 서비스에 적용된 권한 검증

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 브로커**: RabbitMQ (이벤트 내구성)
- **서비스 분리**: MSA 기반 장애 격리
- **복원력 패턴**: 서킷 브레이커, 재시도 적용

### QAS-06 (Modifiability - 새로운 AI 모델 및 분석 알고리즘의 신속한 적용)
- **Hot Swap**: MLOpsTrainingService → MLInferenceEngine (무중단 모델 교체)
- **AI 어댑터**: MLInferenceEngine (프레임워크 독립성)
- **파이프라인 유연성**: MLOpsTrainingService (단계별 교체 가능)

### DD-01 (4-Layer Hybrid MSA)
- **API Gateway**: RequestRouter (통합 진입점)
- **실시간 계층**: AccessAuthorizationManager, VectorComparisonEngine
- **비즈니스 계층**: AuthenticationManager, BranchContentService, 등
- **AI 계층**: MLOpsTrainingService, MLInferenceEngine

### DD-02 (Event-Driven Architecture)
- **메시지 브로커**: RabbitMQ (중앙 이벤트 버스)
- **비동기 통신**: 모든 서비스 간 이벤트 기반 통합
- **느슨한 결합**: 서비스 간 직접 의존성 제거

### DD-04 (Fault Detection - 설비 고장 감지 및 실시간 알림)
- **하트비트**: Equipment → StatusReceiverManager (10분 주기)
- **능동 모니터링**: StatusReceiverManager → Equipment (10초 주기)
- **이벤트 알림**: EquipmentFaultEvent → NotificationDispatcherConsumer

### DD-05 (Performance Optimization - 동시성 도입, 데이터 사전 적재)
- **IPC/gRPC**: Access ↔ FaceModel (동일 노드 고성능 통신)
- **병렬 처리**: VectorComparisonEngine (CompletableFuture)
- **데이터 사전 적재**: Face vectors 캐시 (90% 히트율)
- **Hot Swap**: Runtime binding 기반 모델 교체

### DD-09 (Hot/Cold Path Separation)
- **Hot Path**: BranchContentService (실시간 검색, LLM 배제)
- **Cold Path**: BranchContentService (LLM 키워드 분석)

---

## 시스템 통합 패턴

### 동기 통신 (Synchronous Communication)
- **클라이언트 → API Gateway**: HTTPS 기반 요청 라우팅
- **API Gateway → 서비스**: HTTP 기반 서비스 호출
- **외부 연동**: Auth → ICreditCardVerificationService (결제 검증)

### 비동기 통신 (Asynchronous Communication)
- **서비스 → 메시지 브로커**: 이벤트 발행 (TaskSubmittedEvent 등)
- **메시지 브로커 → 서비스**: 이벤트 구독 및 처리
- **알림 발송**: NotificationDispatcherConsumer → IPushNotificationGateway

### IPC 최적화 (Inter-Process Communication)
- **동일 노드 통신**: AccessAuthorizationManager ↔ VectorComparisonEngine (IPC/gRPC)
- **내부 호출**: FaceModel ↔ MLInferenceEngine (Local)

---

## 결론

Overall Architecture 컴포넌트 다이어그램의 모든 요소(23개)를 요소 유형별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **외부 액터**: 4개 (Customer/Helper/Manager/Ops)
- **코어 컴포넌트**: 13개 (Gateway/Broker + 11개 서비스)
- **외부 시스템**: 4개 (Partners/Equipment/PushGW + MLEngine)
- **패키지**: 3개 계층 패키지

이 다이어그램은 **4-Layer Hybrid MSA 아키텍처**의 전체적인 모습을 보여주며, 각 계층의 역할 분담과 통신 패턴을 명확히 나타냅니다.
