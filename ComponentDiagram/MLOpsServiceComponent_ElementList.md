# MLOps Service Component Element List

## 개요
본 문서는 MLOps Service 컴포넌트 다이어그램(`11_MLOpsServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ITrainingTriggerApi** | 훈련 트리거 API 인터페이스를 정의<br>수동 모델 재훈련 요청 기능 제공<br>관리자용 훈련 제어 인터페이스 | UC-24 (세탁물 모델 재학습)<br>QAS-06 (Modifiability - 모델 교체) |
| **IModelDeploymentApi** | 모델 배포 API 인터페이스를 정의<br>훈련 완료 모델 배포 기능 제공<br>관리자용 배포 제어 인터페이스 | UC-24<br>QAS-06 (Modifiability - Hot Swap) |
| **TrainingController** | ITrainingTriggerApi 인터페이스의 구현<br>훈련 트리거 요청 수신 및 처리<br>MLOps 파이프라인 시작 | UC-24<br>QAS-06 (신속한 모델 교체) |
| **DeploymentController** | IModelDeploymentApi 인터페이스의 구현<br>모델 배포 요청 수신 및 처리<br>Hot Swap 배포 실행 | UC-24<br>QAS-06 (무중단 배포) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ITrainingTriggerService** | 훈련 트리거 서비스 인터페이스를 정의<br>모델 재훈련 워크플로우 시작<br>훈련 조건 검증 및 트리거 | UC-24<br>DD-02 (Event-Driven), QAS-06 (Modifiability) |
| **TrainingManager** | ITrainingTriggerService 및 ITrainingEventHandler 구현<br>훈련 워크플로우 관리 및 이벤트 수신<br>재훈련 조건 자동 판단 | UC-24<br>DD-02 (Event-Driven), QAS-06 (Modifiability) |
| **IModelDeploymentService** | 모델 배포 서비스 인터페이스를 정의<br>훈련 완료 모델 검증 및 배포<br>Hot Swap을 통한 무중단 배포 | UC-24<br>QAS-06 (Modifiability - Hot Swap) |
| **DeploymentService** | IModelDeploymentService 및 IDeploymentEventHandler 구현<br>모델 배포 실행 및 상태 관리<br>Hot Swap 배포 오케스트레이션 | UC-24<br>QAS-06 (Hot Swap), DD-05 (Pipeline) |
| **ITrainingPipelineService** | 훈련 파이프라인 서비스 인터페이스를 정의<br>종단간 ML 파이프라인 오케스트레이션<br>데이터 수집 → 훈련 → 검증 → 배포 | UC-24<br>DD-05 (Pipeline Optimization) |
| **TrainingPipelineOrchestrator** | ITrainingPipelineService 구현<br>종단간 ML 파이프라인 조율<br>데이터 → 훈련 → 검증 → 배포 연결 | UC-24<br>DD-05 (Pipeline Optimization) |
| **IModelVerificationService** | 모델 검증 서비스 인터페이스를 정의<br>훈련 완료 모델 정확도 및 성능 검증<br>배포 승인 기준 평가 | UC-24<br>QAS-06 (Modifiability - 품질 보장) |
| **ModelVerificationService** | IModelVerificationService 구현<br>모델 정확도 및 성능 검증<br>배포 승인/거부 결정 | UC-24<br>QAS-06 (품질 검증) |
| **AccuracyVerifier** | 정확도 검증기<br>모델 정확도 측정 및 평가<br>기준 정확도 비교 | UC-24<br>QAS-06 (품질 메트릭) |
| **PerformanceVerifier** | 성능 검증기<br>모델 추론 성능 측정<br>지연시간 및 리소스 사용 평가 | UC-24<br>QAS-06 (성능 메트릭) |
| **IDataManagementService** | 데이터 관리 서비스 인터페이스를 정의<br>훈련 데이터 수집 및 전처리<br>데이터 저장 및 버전 관리 | UC-24<br>DD-06 (ElasticSearch 활용) |
| **DataManagementService** | IDataManagementService 구현<br>훈련 데이터 수집 및 관리<br>데이터 버전 및 저장소 관리 | UC-24<br>DD-06 (데이터 관리) |
| **DataCollector** | 데이터 수집기<br>Helper/Auth 서비스에서 수정된 작업 데이터 수집<br>재훈련용 데이터셋 구축 | UC-24<br>DD-02 (Event-Driven) |
| **DataPersistenceManager** | 데이터 저장 관리자<br>수집된 훈련 데이터 영속화<br>데이터 버전 관리 | UC-24<br>DD-03 (Database per Service) |
| **ITrainingEventHandler** | 훈련 이벤트 핸들러 인터페이스를 정의<br>외부 훈련 트리거 이벤트 수신<br>TaskConfirmedEvent 처리 | DD-02 (Event-Driven)<br>UC-24 |
| **IDeploymentEventHandler** | 배포 이벤트 핸들러 인터페이스를 정의<br>배포 완료 이벤트 처리<br>배포 상태 모니터링 | DD-02 (Event-Driven)<br>UC-24 |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IModelDataRepository** | 모델 데이터 저장소 인터페이스를 정의<br>훈련 모델 및 메타데이터 CRUD 연산<br>JPA 기반 모델 저장소 추상화 | UC-24<br>DD-03 (Database per Service) |
| **ITrainingDataRepository** | 훈련 데이터 저장소 인터페이스를 정의<br>훈련 데이터셋 저장 및 조회<br>데이터 버전 관리 | UC-24<br>DD-03 (Database per Service) |
| **IMLInferenceEngine** | ML 추론 엔진 인터페이스를 정의<br>모델 훈련 및 추론 기능 제공<br>TensorFlow/PyTorch 등 프레임워크 래핑 | UC-24<br>QAS-06 (Modifiability - AI 교체) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>MLOps 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-24 (ModelDeployedEvent 등) |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>외부 이벤트 수신 및 처리<br>TaskConfirmedEvent 구독 | DD-02 (Event-Driven)<br>UC-24 |
| **IFaceModelClient** | FaceModel 클라이언트 인터페이스를 정의<br>FaceModel Service gRPC 통신<br>Hot Swap 모델 업데이트 | UC-24<br>QAS-06 (Hot Swap) |
| **IAuthRepository** | 인증 저장소 인터페이스를 정의<br>사용자 데이터 읽기 전용 접근<br>데이터 수집용 계정 정보 조회 | UC-24<br>DD-03 (읽기 전용 연동) |
| **IHelperRepository** | 헬퍼 저장소 인터페이스를 정의<br>작업 데이터 읽기 전용 접근<br>훈련 데이터 수집용 작업 정보 조회 | UC-24<br>DD-03 (읽기 전용 연동) |
| **ModelJpaRepository** | IModelDataRepository 구현<br>JPA/Hibernate 기반 모델 데이터 접근<br>모델 메타데이터 및 바이너리 저장 | DD-03 (Database per Service)<br>QAS-06 (모델 버전 관리) |
| **TrainingDataJpaRepository** | ITrainingDataRepository 구현<br>JPA/Hibernate 기반 훈련 데이터 접근<br>데이터셋 버전 및 메타데이터 관리 | DD-03 (Database per Service)<br>UC-24 |
| **MLInferenceEngineAdapter** | IMLInferenceEngine 구현<br>ML 프레임워크 어댑터<br>TensorFlow/PyTorch 모델 훈련/추론 | QAS-06 (Modifiability - 프레임워크 교체)<br>DD-05 (Pipeline) |
| **RabbitMQAdapter** | IMessagePublisherService 및 IMessageSubscriptionService 구현<br>RabbitMQ 클라이언트 통합<br>MLOps 이벤트 발행 및 구독 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 이벤트 내구성) |
| **FaceModelClientAdapter** | IFaceModelClient 구현<br>FaceModel Service gRPC 클라이언트<br>Hot Swap을 위한 모델 업데이트 | QAS-06 (Hot Swap)<br>DD-05 (무중단 배포) |
| **AuthRepositoryAdapter** | IAuthRepository 구현<br>Auth Service 데이터 읽기 전용 접근<br>안전한 사용자 정보 조회 | DD-03 (읽기 전용)<br>QAS-04 (Security - 데이터 격리) |
| **HelperRepositoryAdapter** | IHelperRepository 구현<br>Helper Service 데이터 읽기 전용 접근<br>작업 데이터 수집을 위한 조회 | DD-03 (읽기 전용)<br>QAS-04 (Security - 데이터 격리) |
| **ModelDatabase** | 모델 데이터베이스<br>훈련 모델, 메타데이터, 버전 정보 저장<br>PostgreSQL 기반 모델 저장소 | DD-03 (Database per Service)<br>QAS-06 (모델 영속성) |
| **TrainingDataStore** | 훈련 데이터 저장소<br>수집된 훈련 데이터셋 저장<br>데이터 버전 및 메타데이터 관리 | DD-03 (Database per Service)<br>UC-24 |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 MLOps 요청 수신 및 처리<br>훈련 트리거 및 배포 API 공개 인터페이스<br>관리자와의 첫 번째 상호작용 지점 | UC-24<br>QAS-06 (Modifiability), QAS-05 (Availability) |
| **Business Layer** | MLOps 파이프라인 핵심 로직 구현<br>훈련 오케스트레이션, 모델 검증, 데이터 관리<br>이벤트 기반 자동화 워크플로우 | DD-02 (Event-Driven), DD-05 (Pipeline Optimization)<br>QAS-06 (Modifiability), UC-24 |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>모델 DB, 훈련 데이터, ML 엔진, FaceModel Service, RabbitMQ 연동<br>프로토콜 변환 및 데이터 격리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), QAS-04 (Security) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 7 | 9 | 16 |
| **System Interface Layer** | 8 | 7 | 15 |
| **패키지** | - | - | 3 |
| **총계** | **17** | **18** | **38** |

---

## Architectural Drivers 적용 현황

### QAS-06 (Modifiability - 새로운 AI 모델 및 분석 알고리즘의 신속한 적용)
- **모델 교체**: MLInferenceEngineAdapter (프레임워크 독립성)
- **Hot Swap**: DeploymentService, FaceModelClientAdapter (무중단 배포)
- **파이프라인 유연성**: TrainingPipelineOrchestrator (단계별 교체 가능)
- **품질 검증**: ModelVerificationService, AccuracyVerifier, PerformanceVerifier

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **데이터 격리**: AuthRepositoryAdapter, HelperRepositoryAdapter (읽기 전용)
- **접근 제어**: TrainingController, DeploymentController (관리자 권한)
- **감사 추적**: 모든 MLOps 이벤트 RabbitMQ로 발행

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: RabbitMQAdapter (이벤트 발행 보장)
- **데이터 영속성**: ModelJpaRepository, TrainingDataJpaRepository
- **파이프라인 복원력**: TrainingPipelineOrchestrator (실패 시 재시도)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: TrainingManager, DeploymentService, ModelVerificationService → RabbitMQAdapter
- **이벤트 구독**: TrainingManager ← RabbitMQAdapter (TaskConfirmedEvent)
- **비동기 처리**: 모델 재훈련을 동기 응답에서 분리

### DD-03 (Database per Service)
- **모델 전용 DB**: ModelDatabase (모델 데이터 독립 저장)
- **훈련 데이터 DB**: TrainingDataStore (훈련 데이터셋 저장)
- **읽기 전용 연동**: AuthRepositoryAdapter, HelperRepositoryAdapter (안전한 데이터 공유)

### DD-05 (Pipeline Optimization - 병렬 처리 및 파이프라인 최적화)
- **파이프라인 오케스트레이션**: TrainingPipelineOrchestrator
- **병렬 검증**: AccuracyVerifier, PerformanceVerifier (동시 실행)
- **데이터 병렬 처리**: DataCollector, DataPersistenceManager
- **Hot Swap 최적화**: FaceModelClientAdapter (원자적 모델 교체)

### 관련 UC 목록
- **UC-24**: 세탁물 모델 재학습 (전체 MLOps 파이프라인)

---

## MLOps 파이프라인 아키텍처 특징

### 종단간 ML 파이프라인 (DD-05 적용)
1. **트리거**: TrainingManager가 TaskConfirmedEvent 수신
2. **데이터 수집**: DataCollector가 Helper/Auth에서 수정 데이터 수집
3. **데이터 저장**: DataPersistenceManager가 TrainingDataStore에 저장
4. **훈련 실행**: TrainingPipelineOrchestrator가 MLInferenceEngineAdapter 호출
5. **검증**: ModelVerificationService가 정확도/성능 검증
6. **배포**: DeploymentService가 FaceModel Service에 Hot Swap
7. **이벤트 발행**: ModelDeployedEvent 발행

### 데이터 격리 및 보안 (DD-03, QAS-04)
- **읽기 전용 접근**: Auth/Helper 데이터에 대한 안전한 조회
- **데이터 복사**: 재훈련용 데이터셋을 독립 저장소에 복사
- **접근 통제**: Adapter 패턴을 통한 데이터 접근 제어

### 이벤트 기반 자동화 (DD-02)
- **자동 트리거**: TaskConfirmedEvent로 재훈련 자동 시작
- **상태 전파**: 각 단계 완료 시 이벤트 발행
- **모니터링**: 이벤트 기반 파이프라인 상태 추적

---

## 결론

MLOps Service 컴포넌트 다이어그램의 모든 요소(38개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: MLOps API 인터페이스 (4개 요소)
- **Business Layer**: ML 파이프라인 로직 (16개 요소)
- **System Interface Layer**: 모델/ML/RabbitMQ 연동 (15개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **MLOps 파이프라인 오케스트레이션**과 **Hot Swap 배포**를 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
