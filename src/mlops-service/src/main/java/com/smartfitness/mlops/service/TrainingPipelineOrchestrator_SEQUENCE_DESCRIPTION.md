# TrainingPipelineOrchestrator 컴포넌트 시퀀스 다이어그램 설명

## 개요

본 문서는 `TrainingPipelineOrchestrator` 컴포넌트의 Behavior Diagram (Sequence Diagram)에 대한 상세 설명입니다. 컴포넌트를 구성하는 클래스의 인스턴스 수준에서 AI 모델 학습 파이프라인의 오케스트레이션을 설명하며, 각 시나리오의 동작을 기능적, 품질속성적, 패턴/택틱 측면에서 분석합니다.

---

## 시나리오 개요

본 시퀀스 다이어그램은 AI 모델 학습 파이프라인의 4단계 오케스트레이션 시나리오를 다룹니다:

**AI 모델 학습 파이프라인 오케스트레이션 (DD-07)**
- 외부 트리거(TrainingManager 또는 배치 타이머)에 의해 시작되는 완전 자동화된 학습 파이프라인
- 데이터 수집 → 모델 학습 → 모델 검증 → Hot Swap 배포의 4단계 프로세스
- 검증 실패 시 배포 단계가 생략되는 품질 게이트 기능

---

## 시나리오: AI 모델 학습 파이프라인 오케스트레이션

### 시나리오 시작점

**외부 트리거**: TrainingManager 또는 일일 배치 타이머가 `ITrainingPipelineService.orchestrateTraining()`을 호출합니다.

### [1.1] orchestrateTraining(trainingId)

**호출**: `TrainingManager` → `TrainingPipelineOrchestrator` : [1.1] `orchestrateTraining(trainingId)`

**기능적 측면**: 학습 파이프라인 오케스트레이션의 시작점으로, trainingId를 받아 4단계 파이프라인을 순차적으로 실행합니다.

**품질속성적 측면**: Facade Pattern을 통해 복잡한 파이프라인을 단순한 인터페이스로 제공하여 QAS-06 유지보수성 향상. 외부 트리거와의 느슨한 결합으로 QAS-05 가용성 보장.

**패턴/택틱 측면**: Facade Pattern 적용으로 클라이언트는 내부 4단계 복잡성을 모르고 단일 API만 호출.

### [1.2] collectTrainingData()

**호출**: `TrainingPipelineOrchestrator` → `IDataManagementService` : [1.2] `collectTrainingData()`

**기능적 측면**: 학습에 필요한 데이터를 Auth Service와 Helper Service로부터 READ-ONLY 방식으로 수집합니다.

**품질속성적 측면**: Event-Based Replication (DD-03)을 통해 원본 데이터 무결성을 보호하면서 QAS-05 가용성 유지. 데이터 수집 실패 시 파이프라인 중단으로 품질 보장.

**패턴/택틱 측면**: Strategy Pattern으로 데이터 수집 전략을 교체 가능. Saga Pattern의 첫 단계로 트랜잭션 관리 시작.

### [1.3] trainModel(trainingData)

**호출**: `TrainingPipelineOrchestrator` → `TrainingPipelineOrchestrator` : [1.3] `trainModel(trainingData)`

**기능적 측면**: 수집된 데이터를 기반으로 GPU 리소스를 활용한 모델 학습을 동기적으로 수행하는 내부 처리 단계입니다.

**품질속성적 측면**: GPU 리소스 최적화로 QAS-02 성능 목표 달성. 학습 과정에서 메모리/컴퓨팅 리소스 효율적 관리하며, 야간 배치 실행으로 피크 타임 부하 회피.

**패턴/택틱 측면**: 내부 구현으로 외부 인터페이스와 격리. Batch Sequential Pattern (DD-07)이 전체 파이프라인 레벨에서 적용되어 야간 시간대에 실행되므로 피크 타임 리소스 contention 방지.

### [1.4] verifyModel(modelId)

**호출**: `TrainingPipelineOrchestrator` → `IModelVerificationService` : [1.4] `verifyModel(modelId)`

**기능적 측면**: 학습된 모델의 정확도(>90%)와 성능(<3초 응답시간)을 검증하는 품질 게이트입니다.

**품질속성적 측면**: 검증 실패 시 배포 중단으로 QAS-02 성능과 QAS-05 가용성 동시 보장. 검증 임계값 준수로 프로덕션 품질 확보.

**패턴/택틱 측면**: Strategy Pattern으로 검증 알고리즘 교체 가능. Saga Pattern에서 검증 단계로 품질 보증 역할.

### [1.5] deployModel(modelId)

**호출**: `TrainingPipelineOrchestrator` → `IModelDeploymentService` : [1.5] `deployModel(modelId)`

**기능적 측면**: 검증된 모델을 Hot Swap 방식으로 FaceModel Service에 무중단 배포합니다.

**품질속성적 측면**: AtomicReference 기반 <1ms 모델 교체로 QAS-06 무중단 배포 달성. 99.9% 가용성 유지로 QAS-05 보장.

**패턴/택틱 측면**: Strategy Pattern으로 배포 전략 교체 가능. Hot Swap Tactic (DD-07) 적용으로 서비스 중단 제로.

### [1.6] publishEvent(TrainingCompletedEvent)

**호출**: `TrainingPipelineOrchestrator` → `IMessagePublisherService` : [1.6] `publishEvent(TrainingCompletedEvent)`

**기능적 측면**: 학습 파이프라인 완료를 외부 시스템에 이벤트로 알립니다.

**품질속성적 측면**: Observer Pattern으로 느슨한 결합 유지. 이벤트 기반 통신으로 QAS-05 가용성 향상.

**패턴/택틱 측면**: Observer Pattern 적용으로 발행자와 구독자의 독립성 보장. Event-Based Architecture (DD-02) 준수.

---

## 조건부 처리: 검증 결과에 따른 분기

### 검증 성공 경로 [isVerified == true]
- **기능적 측면**: 완전한 파이프라인 실행으로 신규 모델이 프로덕션에 배포됩니다.
- **품질속성적 측면**: 검증 통과로 품질 보장된 모델만 배포되어 QAS-02/QAS-05 달성.
- **패턴/택틱 측면**: Saga Pattern의 성공 케이스로 모든 단계 완료.

### 검증 실패 경로 [isVerified == false]
- **기능적 측면**: 배포 단계 생략으로 불량 모델의 프로덕션 반입을 방지합니다.
- **품질속성적 측면**: 품질 게이트로서 QAS-05 가용성과 QAS-02 성능 보호.
- **패턴/택틱 측면**: Saga Pattern의 롤백 메커니즘으로 부분 실패 처리.

---

## Design Pattern & Tactics 요약

### Saga Pattern (DD-07)
**적용**: 4단계 파이프라인 전체에 걸친 트랜잭션 관리
**기여**: 검증 실패 시 배포 생략으로 데이터 일관성 유지, QAS-05 가용성 향상

### Strategy Pattern
**적용**: 각 단계별 인터페이스 (IDataManagementService, IModelVerificationService, IModelDeploymentService)
**기여**: 알고리즘 교체 가능성으로 QAS-06 유지보수성 향상

### Facade Pattern
**적용**: TrainingPipelineOrchestrator의 orchestrateTraining() 메서드
**기여**: 복잡한 파이프라인을 단순 API로 제공, 클라이언트 결합도 감소

### Observer Pattern
**적용**: IMessagePublisherService를 통한 이벤트 발행
**기여**: 느슨한 결합으로 컴포넌트 독립성 유지, QAS-05 가용성 향상

### Hot Swap Tactic (DD-07)
**적용**: 모델 배포 단계
**기여**: <1ms 무중단 배포로 QAS-06 달성, 99.9% 가용성 유지

---

## 품질 속성 달성 분석

### QAS-02 (Performance)
- GPU 리소스 최적화된 학습 처리
- Hot Swap 배포로 <1ms 전환 시간
- 검증 임계값 준수로 성능 보장

### QAS-05 (Availability)
- Saga Pattern으로 부분 실패 처리
- Hot Swap 배포로 무중단 업데이트
- Observer Pattern으로 느슨한 결합

### QAS-06 (Modifiability)
- Strategy Pattern으로 각 단계 독립적 변경 가능
- Facade Pattern으로 인터페이스 단순성 유지
- 인터페이스 기반 설계로 구현 교체 용이

---

## 결론

TrainingPipelineOrchestrator의 시퀀스 다이어그램은 AI 모델 학습 파이프라인의 견고한 오케스트레이션을 보여줍니다. 검증 실패 시 배포를 중단하는 품질 게이트, Hot Swap 배포, 그리고 Saga/Strategy/Facade/Observer 패턴의 체계적 적용으로 모든 Quality Attributes를 동시에 만족하는 설계를 구현하였습니다. 특히 메시지 수준에서 본 분석은 각 호출이 품질 속성과 설계 패턴에 어떻게 기여하는지를 명확히 드러냅니다.
