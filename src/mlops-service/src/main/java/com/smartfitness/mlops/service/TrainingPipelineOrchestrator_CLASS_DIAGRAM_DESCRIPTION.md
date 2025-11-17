# TrainingPipelineOrchestrator 컴포넌트 Class Diagram Description

## 다이어그램 구조 설명

본 Class Diagram은 `TrainingPipelineOrchestrator` 컴포넌트의 정적 구조를 표현하며, AI 모델 학습 파이프라인의 복잡한 오케스트레이션을 단순하고 견고한 구조로 설계하기 위해 **Facade Pattern**, **Strategy Pattern**, **Saga Pattern**을 체계적으로 적용하였습니다. 각 패턴에 참여하는 클래스는 색상으로 구분되어 있으며, 총 4단계의 학습 파이프라인(데이터 수집 → 모델 학습 → 모델 검증 → Hot Swap 배포)을 안정적으로 수행하는 설계를 보여줍니다.

특히 성능 최적화와 유지보수성을 고려하여 multi-threading 환경에서의 thread-safe한 설계를 적용하였으며, SOLID 원칙과 GRASP 패턴을 준수하여 높은 응집도와 낮은 결합도를 유지합니다. 많은 수의 클래스와 인터페이스가 식별되었으므로 Business Layer 패키지 내에서 기능별로 그룹화하여 패키지 응집도를 높이고 결합도를 낮추는 방식으로 패키징하였습니다.

---

## Business Layer 패키지

### TrainingPipelineOrchestrator 클래스 (Facade Pattern - 분홍색)

`TrainingPipelineOrchestrator` 클래스는 학습 파이프라인의 핵심 오케스트레이터로, 복잡한 4단계 파이프라인 프로세스를 단순한 인터페이스로 제공하는 **Facade Pattern**을 적용한 메인 컴포넌트입니다.

**ITrainingPipelineService 구현**:
- `orchestrateTraining(trainingId: String)`: 외부에서 호출되는 단일 진입점으로, 내부적으로 데이터 수집, 모델 학습, 검증, 배포의 복잡한 과정을 조율합니다.

이 클래스는 4개의 Required Interface를 의존성 주입받아 사용하며, 각 단계별로 적절한 서비스를 호출합니다. 특히 성능을 고려하여 각 단계가 독립적으로 실행될 수 있도록 설계되었으며, thread-safe한 방식으로 구현되었습니다. 유지보수성을 위해 Single Responsibility Principle을 준수하여 파이프라인 오케스트레이션만 담당하고, 구체적인 로직은 Strategy 패턴을 적용한 다른 컴포넌트들에게 위임합니다.

### Strategy Pattern 인터페이스 및 구현체들 (초록색)

#### IModelVerificationService 인터페이스 및 ModelVerificationService 클래스

**IModelVerificationService**:
- `verifyModel(modelId: String): boolean`: 학습된 모델의 정확도와 성능을 검증하는 전략 인터페이스입니다.

**ModelVerificationService 구현체**:
- 정확도 검증(AccuracyVerifier)과 성능 검증(PerformanceVerifier)을 조율하는 전략 구현체입니다.
- 검증 실패 시 배포를 중단하여 품질을 보장합니다.

이 구현체는 Strategy Pattern을 통해 다양한 검증 알고리즘을 교체할 수 있으며, 성능을 위해 검증 과정을 병렬로 실행할 수 있는 구조를 가지고 있습니다.

#### IDataManagementService 인터페이스 및 DataManagementService 클래스

**IDataManagementService**:
- `collectTrainingData()`: 학습 데이터를 수집하는 전략 인터페이스입니다.
- `persistTrainingData(data: String)`: 수집된 데이터를 영구 저장하는 메서드입니다.

**DataManagementService 구현체**:
- DataCollector와 DataPersistenceManager를 사용하여 데이터를 수집하고 저장합니다.
- Event-Based Replication을 통해 원본 데이터를 보호하면서 학습 데이터를 확보합니다.

#### IModelDeploymentService 인터페이스 및 DeploymentService 클래스

**IModelDeploymentService**:
- `deployModel(modelId: String): String`: 검증된 모델을 프로덕션 환경에 배포하는 전략 인터페이스입니다.

**DeploymentService 구현체**:
- Hot Swap 방식을 통해 무중단 배포를 수행합니다.
- AtomicReference를 사용하여 thread-safe한 모델 교체를 보장합니다.

### Saga Pattern 컴포넌트들 (보라색)

#### DataCollector 클래스
학습 데이터를 수집하는 mediator 컴포넌트로, Auth Service와 Helper Service로부터 READ-ONLY 방식으로 데이터를 복제합니다.

#### DataPersistenceManager 클래스
수집된 데이터를 TrainingDataStore에 영구 저장하는 컴포넌트입니다.

#### AccuracyVerifier 클래스
모델의 정확도를 검증하는 컴포넌트로, 90% 이상의 정확도를 목표로 합니다.

#### PerformanceVerifier 클래스
모델의 성능을 검증하는 컴포넌트로, 3초 이내 응답 시간을 목표로 합니다.

이러한 Saga Pattern 컴포넌트들은 각 단계별 트랜잭션을 관리하며, 부분 실패 시 자동 롤백을 수행하여 데이터 일관성을 유지합니다.

### Repository 인터페이스들

#### IModelDataRepository
학습된 모델 데이터를 저장하고 조회하는 인터페이스입니다.

#### ITrainingDataRepository
학습 데이터를 저장하고 조회하는 인터페이스입니다.

#### IAuthRepository
인증 서비스의 안면 벡터 데이터를 READ-ONLY로 접근하는 인터페이스입니다.

#### IHelperRepository
헬퍼 서비스의 태스크 사진 데이터를 READ-ONLY로 접근하는 인터페이스입니다.

### 외부 통신 인터페이스들

#### IMLInferenceEngine
ML 추론 엔진과의 통신 인터페이스로, 모델 배포와 학습을 담당합니다.

#### IFaceModelClient
FaceModel Service에 모델 업데이트를 알리는 인터페이스입니다.

#### IMessagePublisherService
학습 완료 이벤트를 발행하는 메시징 인터페이스입니다.

---

## 설계 패턴 요약

### Facade Pattern (분홍색)
- **참여 클래스**: TrainingPipelineOrchestrator
- **목적**: 복잡한 4단계 파이프라인을 단순한 orchestrateTraining() 인터페이스로 제공
- **효과**: 클라이언트는 내부 복잡성을 모르고 간단한 API만 사용
- **기여**: QAS-06 유지보수성 향상, 클라이언트 결합도 감소

### Strategy Pattern (초록색)
- **참여 인터페이스**: IModelVerificationService, IDataManagementService, IModelDeploymentService
- **참여 클래스**: ModelVerificationService, DataManagementService, DeploymentService
- **목적**: 각 파이프라인 단계의 구체적인 알고리즘을 교체 가능하도록 설계
- **효과**: 새로운 검증/수집/배포 전략을 런타임에 교체 가능
- **기여**: QAS-06 유지보수성, 확장성 제공

### Saga Pattern (보라색)
- **참여 클래스**: DataCollector, DataPersistenceManager, AccuracyVerifier, PerformanceVerifier
- **목적**: 장기 실행되는 파이프라인의 트랜잭션 일관성 보장
- **효과**: 각 단계별 실패 시 자동 롤백으로 데이터 일관성 유지
- **기여**: QAS-05 가용성, 데이터 무결성 보장

---

## SOLID 원칙 준수

### Single Responsibility Principle (SRP)
각 클래스가 단일 책임을 가지도록 설계되었습니다:
- TrainingPipelineOrchestrator: 파이프라인 오케스트레이션만 담당
- ModelVerificationService: 모델 검증만 담당
- DataManagementService: 데이터 관리만 담당
- DeploymentService: 모델 배포만 담당

### Open/Closed Principle (OCP)
인터페이스를 통한 확장으로 새로운 구현체를 추가할 수 있습니다:
- IModelVerificationService에 새로운 검증 전략 추가 가능
- IModelDeploymentService에 새로운 배포 전략 추가 가능

### Liskov Substitution Principle (LSP)
모든 인터페이스 구현체가 계약을 완전히 준수합니다:
- ModelVerificationService는 IModelVerificationService를 완전히 대체 가능
- DataManagementService는 IDataManagementService를 완전히 대체 가능

### Interface Segregation Principle (ISP)
클라이언트별로 특화된 인터페이스를 분리하였습니다:
- IModelVerificationService: 검증 기능만 제공
- IDataManagementService: 데이터 관리 기능만 제공
- IModelDeploymentService: 배포 기능만 제공

### Dependency Inversion Principle (DIP)
고수준 모듈이 저수준 모듈에 직접 의존하지 않고 인터페이스에 의존합니다:
- TrainingPipelineOrchestrator는 구체적인 서비스 구현체가 아닌 인터페이스에 의존
- 모든 의존성은 생성자 주입을 통해 외부에서 제공

---

## GRASP 패턴 적용

### Information Expert
각 클래스가 자신이 담당하는 정보를 전문적으로 처리합니다:
- DataCollector: 데이터 수집 전문성
- AccuracyVerifier: 정확도 검증 전문성
- PerformanceVerifier: 성능 검증 전문성

### Low Coupling
인터페이스를 통한 느슨한 결합을 유지합니다:
- TrainingPipelineOrchestrator와 각 서비스 간 인터페이스 기반 통신
- 메시지 기반의 이벤트 발행으로 결합도 최소화

### High Cohesion
관련된 책임을 하나의 클래스로 그룹화합니다:
- 검증 관련 로직을 ModelVerificationService로 응집
- 데이터 관리 로직을 DataManagementService로 응집
- 배포 로직을 DeploymentService로 응집

### Protected Variations
예상되는 변경으로부터 보호하기 위해 인터페이스를 사용합니다:
- 검증 알고리즘 변경으로부터 보호 (Strategy Pattern)
- 배포 방식 변경으로부터 보호 (Strategy Pattern)
- 데이터 소스 변경으로부터 보호 (Repository Pattern)

---

## Package Cohesion/Coupling 분석

Business Layer 패키지 내에서 많은 수의 클래스와 인터페이스가 식별되었으므로, 기능적 응집도를 높이고 결합도를 낮추는 방식으로 패키징하였습니다:

### 기능별 그룹화
- **오케스트레이션 그룹**: TrainingPipelineOrchestrator, ITrainingPipelineService
- **검증 그룹**: IModelVerificationService, ModelVerificationService, AccuracyVerifier, PerformanceVerifier
- **데이터 관리 그룹**: IDataManagementService, DataManagementService, DataCollector, DataPersistenceManager
- **배포 그룹**: IModelDeploymentService, DeploymentService
- **저장소 그룹**: IModelDataRepository, ITrainingDataRepository, IAuthRepository, IHelperRepository
- **외부 통신 그룹**: IMLInferenceEngine, IFaceModelClient, IMessagePublisherService

### 응집도 향상
각 그룹 내의 클래스들이 기능적으로 밀접하게 관련되어 있어 응집도가 높습니다. 예를 들어 검증 그룹의 모든 클래스는 모델 검증이라는 단일 책임을 공유합니다.

### 결합도 감소
그룹 간에는 인터페이스를 통한 느슨한 결합을 유지하며, 직접적인 클래스 의존성을 최소화하였습니다. 이는 유지보수성과 테스트 용이성을 향상시킵니다.

---

## Element List

### Business Layer 패키지

#### TrainingPipelineOrchestrator 클래스 (Facade Pattern)
**역할**: 학습 파이프라인의 메인 오케스트레이터로, 4단계 파이프라인을 조율하는 핵심 컴포넌트입니다.

**요구사항 기여도**:
- **기능 요구사항**: orchestrateTraining() 메서드를 통해 데이터 수집, 모델 학습, 검증, 배포의 완전한 파이프라인 기능 제공
- **품질 요구사항**: Facade Pattern으로 복잡성 숨김, QAS-06 유지보수성 달성, thread-safe한 오케스트레이션으로 QAS-02 성능 보장
- **제약사항**: Single Responsibility 준수로 파이프라인 오케스트레이션 전담, 인터페이스 기반 설계로 외부 의존성 관리

#### ITrainingPipelineService 인터페이스
**역할**: 학습 파이프라인 오케스트레이션의 공개 인터페이스를 정의합니다.

**요구사항 기여도**:
- **기능 요구사항**: 외부 컴포넌트가 파이프라인을 시작할 수 있는 표준화된 인터페이스 제공
- **품질 요구사항**: 인터페이스 표준화로 클라이언트 개발 용이성 향상, QAS-06 유지보수성 기여
- **제약사항**: 단일 메서드만 노출하여 API 단순성 유지

#### IModelVerificationService 인터페이스 (Strategy Pattern)
**역할**: 모델 검증 전략을 정의하는 인터페이스로, 다양한 검증 알고리즘 교체를 가능하게 합니다.

**요구사항 기여도**:
- **기능 요구사항**: 모델 정확도와 성능 검증 기능 제공으로 품질 보장
- **품질 요구사항**: Strategy Pattern으로 검증 전략 교체 용이, QAS-06 유지보수성 달성
- **제약사항**: 검증 실패 시 배포 중단으로 품질 게이트 역할 수행

#### ModelVerificationService 클래스 (Strategy Pattern)
**역할**: 모델 검증을 수행하는 전략 구현체로, 정확도와 성능 검증을 조율합니다.

**요구사항 기여도**:
- **기능 요구사항**: AccuracyVerifier와 PerformanceVerifier를 활용한 종합적 모델 검증
- **품질 요구사항**: Strategy Pattern 구현으로 검증 로직 독립적 변경 가능, QAS-06 유지보수성 기여
- **제약사항**: 검증 임계값(정확도 90%, 응답시간 3초) 준수로 품질 표준 유지

#### IDataManagementService 인터페이스 (Strategy Pattern)
**역할**: 데이터 관리 전략을 정의하는 인터페이스로, 학습 데이터 수집 방식을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 학습 데이터 수집 및 저장 기능 제공
- **품질 요구사항**: Strategy Pattern으로 데이터 수집 방식 교체 용이, QAS-06 유지보수성 달성
- **제약사항**: READ-ONLY 접근으로 원본 데이터 보호

#### DataManagementService 클래스 (Strategy Pattern)
**역할**: 데이터 수집과 저장을 조율하는 전략 구현체입니다.

**요구사항 기여도**:
- **기능 요구사항**: DataCollector를 통한 데이터 수집과 DataPersistenceManager를 통한 저장
- **품질 요구사항**: Event-Based Replication으로 데이터 일관성 유지, QAS-05 가용성 기여
- **제약사항**: 여러 데이터 소스로부터 안정적 수집으로 충분한 학습 데이터 확보

#### IModelDeploymentService 인터페이스 (Strategy Pattern)
**역할**: 모델 배포 전략을 정의하는 인터페이스로, 다양한 배포 방식을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 검증된 모델을 프로덕션에 배포하는 기능 제공
- **품질 요구사항**: Strategy Pattern으로 배포 방식 교체 용이, QAS-06 유지보수성 달성
- **제약사항**: Hot Swap 방식으로 무중단 배포 구현

#### DeploymentService 클래스 (Strategy Pattern)
**역할**: 모델 배포를 수행하는 전략 구현체로, Hot Swap 배포를 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: AtomicReference 기반 thread-safe한 모델 교체로 Hot Swap 배포 수행
- **품질 요구사항**: 무중단 배포로 QAS-06 유지보수성과 QAS-05 가용성 동시 달성
- **제약사항**: <1ms 모델 교체로 성능 영향 최소화

#### DataCollector 클래스 (Saga Pattern)
**역할**: 학습 데이터를 수집하는 mediator 컴포넌트입니다.

**요구사항 기여도**:
- **기능 요구사항**: Auth Service와 Helper Service로부터 READ-ONLY 방식으로 데이터 복제
- **품질 요구사항**: Saga Pattern으로 데이터 수집 트랜잭션 관리, QAS-05 가용성 기여
- **제약사항**: READ-ONLY 접근으로 원본 데이터 무결성 보호

#### DataPersistenceManager 클래스 (Saga Pattern)
**역할**: 수집된 학습 데이터를 영구 저장하는 컴포넌트입니다.

**요구사항 기여도**:
- **기능 요구사항**: 수집된 데이터를 TrainingDataStore에 안전하게 저장
- **품질 요구사항**: Saga Pattern으로 저장 트랜잭션 관리, 데이터 무결성 보장
- **제약사항**: thread-safe한 저장 방식으로 동시성 문제 해결

#### AccuracyVerifier 클래스 (Saga Pattern)
**역할**: 모델 정확도를 검증하는 컴포넌트입니다.

**요구사항 기여도**:
- **기능 요구사항**: 모델의 정확도를 측정하여 품질 기준 충족 여부 판단
- **품질 요구사항**: Saga Pattern으로 검증 실패 시 롤백, QAS-05 가용성 기여
- **제약사항**: 90% 정확도 임계값 준수로 품질 보장

#### PerformanceVerifier 클래스 (Saga Pattern)
**역할**: 모델 성능을 검증하는 컴포넌트입니다.

**요구사항 기여도**:
- **기능 요구사항**: 모델의 응답 시간을 측정하여 성능 기준 충족 여부 판단
- **품질 요구사항**: Saga Pattern으로 검증 실패 시 롤백, QAS-02 성능 목표 달성 지원
- **제약사항**: 3초 응답 시간 임계값 준수로 실시간성 보장

#### IMessagePublisherService 인터페이스
**역할**: 학습 완료 이벤트를 발행하는 메시징 인터페이스입니다.

**요구사항 기여도**:
- **기능 요구사항**: 파이프라인 완료 이벤트를 외부 시스템에 알림
- **품질 요구사항**: Observer Pattern으로 느슨한 결합 유지, QAS-05 가용성 기여
- **제약사항**: Durable Queue 사용으로 이벤트 손실 방지

#### IModelDataRepository 인터페이스
**역할**: 학습된 모델 데이터를 저장하고 조회하는 저장소 인터페이스입니다.

**요구사항 기여도**:
- **기능 요구사항**: 모델 데이터의 영구 저장과 조회 기능 제공
- **품질 요구사항**: Repository Pattern으로 데이터 접근 추상화, QAS-06 유지보수성 기여
- **제약사항**: thread-safe한 저장 방식으로 동시성 지원

#### ITrainingDataRepository 인터페이스
**역할**: 학습 데이터를 저장하고 조회하는 저장소 인터페이스입니다.

**요구사항 기여도**:
- **기능 요구사항**: 학습 데이터의 영구 저장과 조회 기능 제공
- **품질 요구사항**: Repository Pattern으로 데이터 접근 추상화, QAS-06 유지보수성 기여
- **제약사항**: 대용량 데이터 처리로 충분한 학습 데이터 확보 지원

#### IAuthRepository 인터페이스
**역할**: 인증 서비스의 안면 벡터 데이터를 READ-ONLY로 접근하는 저장소입니다.

**요구사항 기여도**:
- **기능 요구사항**: 학습용 안면 벡터 데이터 제공
- **품질 요구사항**: READ-ONLY 접근으로 원본 데이터 보호, QAS-05 가용성 기여
- **제약사항**: Event-Based Replication으로 실시간 데이터 동기화

#### IHelperRepository 인터페이스
**역할**: 헬퍼 서비스의 태스크 사진 데이터를 READ-ONLY로 접근하는 저장소입니다.

**요구사항 기여도**:
- **기능 요구사항**: 학습용 태스크 사진 데이터 제공
- **품질 요구사항**: READ-ONLY 접근으로 원본 데이터 보호, QAS-05 가용성 기여
- **제약사항**: Event-Based Replication으로 실시간 데이터 동기화

#### IMLInferenceEngine 인터페이스
**역할**: ML 추론 엔진과의 통신 인터페이스로 모델 배포와 학습을 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: GPU 리소스 최적화된 모델 학습과 배포
- **품질 요구사항**: 외부 엔진 추상화로 QAS-06 유지보수성 기여, QAS-02 성능 최적화 지원
- **제약사항**: GPU 리소스 풀 관리로 효율적 자원 활용

#### IFaceModelClient 인터페이스
**역할**: FaceModel Service에 모델 업데이트를 알리는 클라이언트 인터페이스입니다.

**요구사항 기여도**:
- **기능 요구사항**: 새로운 모델 배포 시 FaceModel Service에 실시간 알림
- **품질 요구사항**: Observer Pattern으로 서비스 간 느슨한 결합, QAS-05 가용성 기여
- **제약사항**: Hot Swap 배포와 연동하여 무중단 업데이트 지원

---

## 결론

TrainingPipelineOrchestrator 컴포넌트의 Class Diagram은 **Facade Pattern**, **Strategy Pattern**, **Saga Pattern**을 체계적으로 적용하여 복잡한 AI 모델 학습 파이프라인을 견고하고 유지보수 가능한 구조로 설계하였습니다. SOLID 원칙과 GRASP 패턴을 준수하여 높은 응집도와 낮은 결합도를 유지하며, 성능과 유지보수성 모두를 고려한 최적의 설계를 보여줍니다. 특히 많은 수의 클래스와 인터페이스를 기능별로 그룹화하여 패키지 응집도를 높이는 방식으로 패키징함으로써, 대규모 시스템에서도 효과적인 구조를 유지할 수 있음을 입증합니다.
