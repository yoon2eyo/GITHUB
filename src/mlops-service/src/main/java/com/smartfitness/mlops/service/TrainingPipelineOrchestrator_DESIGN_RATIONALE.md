# TrainingPipelineOrchestrator 컴포넌트 Design Rationale

## 5.2.4. Design Rationale

본 절에서는 `TrainingPipelineOrchestrator` 컴포넌트의 내부 설계가 시스템의 Quality Attributes (QA)를 달성하기 위해 어떻게 정당화되었는지 설명합니다. 각 QA에 대해 직접적으로 기여하는 design elements를 나열하고, 적용된 Pattern 및 Tactic을 구체적으로 제시하며, 고려된 다른 설계 후보와 비교하여 최적의 설계임을 정당화합니다.

---

## 1. QAS-06: 수정용이성 (Modifiability) 달성 정당화

### 1.1 QA 요구사항

**목표**: AI 모델 업데이트 시 서비스 중단 없이 배포
- **Hot Swap**: AtomicReference 기반 <1ms 모델 교체
- **서비스 가용성**: 99.9% 유지
- **API 요청 실패율**: <1%
- **롤백 시간**: 1분 이내 (BG-11: 신속 복구)

### 1.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `TrainingPipelineOrchestrator` | 파이프라인 워크플로우 오케스트레이션 및 단계별 조율 | 핵심 |
| `IModelDeploymentService` / `DeploymentService` | Hot Swap 배포 전략 인터페이스 및 구현 | 핵심 |
| `IDataManagementService` / `DataManagementService` | 데이터 수집 전략 인터페이스 및 구현 | 핵심 |
| `IModelVerificationService` / `ModelVerificationService` | 모델 검증 전략 인터페이스 및 구현 | 핵심 |
| `IMessagePublisherService` | 이벤트 발행 인터페이스 | 보조 |

### 1.3 적용된 Pattern 및 Tactic

#### 1.3.1 Saga Pattern (DD-07)

**설계 결정**: `TrainingPipelineOrchestrator`를 통한 4단계 파이프라인 오케스트레이션

**구현 방식**:
- 각 단계별 트랜잭션 관리
- 단계별 체크포인트 저장
- 부분 실패 시 자동 롤백
- 단계별 상태 추적 및 복구 지원

**파이프라인 단계**:
1. **데이터 수집**: `IDataManagementService.collectTrainingData()`
2. **모델 학습**: 내부 처리 (배치 학습)
3. **모델 검증**: `IModelVerificationService.verifyModel()`
4. **Hot Swap 배포**: `IModelDeploymentService.deployModel()`

**수정용이성 효과**:
- 각 단계별 독립적 수정 가능
- 단계 추가/제거 시 오케스트레이터만 수정
- 단계별 구현체 교체 용이 (Strategy Pattern)

**정당화**:
- **대안 1: 단일 메서드로 모든 단계 처리**
  - 단계별 수정 시 전체 메서드 수정 필요
  - 단계별 롤백 로직 복잡
  - 테스트 어려움
  - **결론**: Saga Pattern이 단계별 관리에 적합

- **대안 2: 각 단계를 별도 서비스로 분리 (Choreography)**
  - 서비스 간 직접 통신으로 복잡도 증가
  - 중앙 집중식 오케스트레이션 부재
  - 트랜잭션 관리 어려움
  - **결론**: Orchestration Saga Pattern이 더 적합

- **대안 3: 2PC (Two-Phase Commit)**
  - 분산 트랜잭션의 긴 락 시간
  - 성능 저하
  - 단계별 롤백 어려움
  - **결론**: Saga Pattern이 비동기 파이프라인에 적합

**적용된 Pattern**: **Orchestration Saga Pattern**
- `TrainingPipelineOrchestrator`가 중앙 오케스트레이터 역할
- 각 단계별 서비스가 독립적으로 실행
- 단계별 체크포인트 및 롤백 지원

#### 1.3.2 Strategy Pattern

**설계 결정**: `IDataManagementService`, `IModelVerificationService`, `IModelDeploymentService` 인터페이스를 통한 전략 패턴 적용

**구현 방식**:
- `IDataManagementService` 인터페이스로 다양한 데이터 수집 전략 교체 가능
- `IModelVerificationService` 인터페이스로 다양한 검증 전략 교체 가능
- `IModelDeploymentService` 인터페이스로 다양한 배포 전략 교체 가능
- 런타임에 전략 교체 가능 (의존성 주입)

**수정용이성 효과**:
- 새로운 데이터 소스 추가 시 `IDataManagementService` 구현체만 추가
- 검증 로직 변경 시 `IModelVerificationService` 구현체만 변경
- 배포 전략 변경 시 `IModelDeploymentService` 구현체만 변경
- 기존 코드 수정 최소화

**정당화**:
- **대안 1: 구체 클래스에 직접 의존**
  - 전략 변경 시 `TrainingPipelineOrchestrator` 수정 필요
  - 테스트 시 Mock 객체 주입 어려움
  - **결론**: 인터페이스 기반 설계가 필수

- **대안 2: if-else 분기로 전략 선택**
  - 코드 복잡도 증가
  - 새로운 전략 추가 시 기존 코드 수정 필요
  - Open/Closed Principle 위배
  - **결론**: Strategy Pattern이 적합

**적용된 Pattern**: **Strategy Pattern**
- `IDataManagementService`: 다양한 데이터 소스 전략 추가 가능
- `IModelVerificationService`: 다양한 검증 전략 추가 가능
- `IModelDeploymentService`: 다양한 배포 전략 추가 가능 (Hot Swap, Blue/Green 등)

#### 1.3.3 Hot Swap Tactic (DD-07)

**설계 결정**: `IModelDeploymentService`를 통한 Hot Swap 배포

**구현 방식**:
- `DeploymentService`가 `IFaceModelClient`를 통해 FaceModel Service에 모델 업데이트 알림
- AtomicReference 기반 <1ms 모델 교체
- Passive Redundancy로 모델 교체 중 요청 처리 신뢰성 유지

**수정용이성 효과**:
- 모델 업데이트 시 서비스 중단 없음
- 배포 전략 변경 시 `IModelDeploymentService` 구현체만 변경
- 롤백 시간: 1분 이내

**정당화**:
- **대안 1: Blue/Green Deployment**
  - 트래픽 전환 시 서비스 중단 위험
  - QAS-06 목표 위반 가능성
  - **결론**: Hot Swap이 무중단 목표에 적합

- **대안 2: Rolling Deployment**
  - 일부 인스턴스만 업데이트
  - 버전 불일치 문제
  - **결론**: Hot Swap이 단일 인스턴스 환경에 적합

- **대안 3: 서비스 재시작**
  - 서비스 중단 발생
  - QAS-06 목표 위반
  - **결론**: Hot Swap이 필수

**적용된 Pattern**: **Hot Swap Pattern**
- AtomicReference를 통한 원자적 모델 교체
- Passive Redundancy로 모델 교체 중 요청 처리

#### 1.3.4 Event-Driven Architecture (DD-02)

**설계 결정**: `IMessagePublisherService`를 통한 이벤트 기반 통신

**구현 방식**:
- `TrainingPipelineOrchestrator`가 학습 완료 이벤트 발행
- Message Broker를 통한 비동기 이벤트 전달
- 느슨한 결합으로 후속 처리 로직 추가 시 컴포넌트 수정 불필요

**수정용이성 효과**:
- 파이프라인 단계별 독립적 수정 가능
- 후속 처리 로직 추가 시 컴포넌트 수정 불필요
- 이벤트 구독자 추가/제거 용이

**정당화**:
- **대안 1: 동기식 직접 호출**
  - 강한 결합 발생
  - 후속 처리 로직 추가 시 컴포넌트 수정 필요
  - **결론**: 이벤트 기반이 느슨한 결합에 적합

- **대안 2: 폴링 방식**
  - 지연 시간 발생
  - 리소스 낭비
  - **결론**: 이벤트 기반이 실시간성에 적합

**적용된 Pattern**: **Observer Pattern**
- `TrainingPipelineOrchestrator` (Publisher)가 이벤트 발행
- 여러 Observer가 비동기 구독

---

## 2. QAS-02: 성능 (Performance) 달성 정당화

### 2.1 QA 요구사항

**목표**: GPU 리소스 최적 활용 및 학습 시간 단축
- **CUDA 가속**: CPU 대비 10-20배 속도 향상
- **학습 시간**: 평균 4시간 (야간 배치)
- **검증 시간**: <1시간 (정확도 <30분, 성능 <10분)
- **배치 학습**: VRAM 사용량 50% 감소

### 2.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `TrainingPipelineOrchestrator` | 파이프라인 단계별 병렬 처리 조율 | 핵심 |
| `IDataManagementService` / `DataManagementService` | 데이터 전처리 및 병렬 수집 | 핵심 |
| `IModelVerificationService` / `ModelVerificationService` | 병렬 검증 (정확도 + 성능) | 핵심 |
| `DataCollector` | 병렬 데이터 수집 (Auth + Helper) | 보조 |

### 2.3 적용된 Pattern 및 Tactic

#### 2.3.1 Batch Sequential Pattern (DD-07)

**설계 결정**: 일일 야간 배치 학습으로 자원 효율성 증대

**구현 방식**:
- 야간 시간대에 배치 학습 수행
- 시스템 성능 보호 및 MLOps Tier 자원 효율성 향상
- 출입 서비스 영향 최소화

**성능 효과**:
- 피크 타임 부하 회피
- GPU 리소스 집중 활용
- 자원 효율성 향상

**정당화**:
- **대안 1: 실시간 학습 (온라인 학습)**
  - 지속적인 GPU 리소스 소모
  - 출입 서비스 성능 영향
  - **결론**: 배치 학습이 자원 효율적

- **대안 2: 주간 배치 학습**
  - 피크 타임 부하와 경합
  - 사용자 경험 저하
  - **결론**: 야간 배치가 적합

**적용된 Pattern**: **Batch Sequential Pattern**
- 일일 야간 배치로 자원 효율성 증대

#### 2.3.2 병렬 처리 Tactic

**설계 결정**: 데이터 전처리와 모델 학습을 동시에 실행

**구현 방식**:
- `DataCollector`가 Auth Service와 Helper Service에서 병렬 데이터 수집
- `ModelVerificationService`가 정확도 검증과 성능 검증을 병렬 수행
- GPU를 활용한 배치 학습

**성능 효과**:
- 데이터 수집 시간 단축: 순차 처리 대비 50% 감소
- 검증 시간 단축: 순차 처리 대비 50% 감소
- 전체 파이프라인 시간 단축

**정당화**:
- **대안 1: 순차 처리**
  - 데이터 수집 시간: Auth + Helper 순차
  - 검증 시간: 정확도 + 성능 순차
  - **결론**: 병렬 처리가 더 빠름

- **대안 2: 비동기 처리 (Future)**
  - 구현 복잡도 증가
  - 동기화 오버헤드
  - **결론**: 병렬 처리와 유사한 효과

**적용된 Pattern**: **Pipeline Pattern**
- 단계별 병렬 처리로 전체 시간 단축

---

## 3. QAS-05: 가용성 (Availability) 달성 정당화

### 3.1 QA 요구사항

**목표**: 서비스 가용성 99.9% 유지
- **무중단 배포**: Hot Swap을 통한 <1ms 모델 교체
- **장애 격리**: 파이프라인 단계별 장애 격리
- **롤백 시간**: 1분 이내
- **이벤트 보존**: Durable Queue로 이벤트 손실 방지

### 3.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `TrainingPipelineOrchestrator` | 장애 격리 및 롤백 관리 | 핵심 |
| `IModelDeploymentService` / `DeploymentService` | Hot Swap 배포 및 롤백 | 핵심 |
| `IMessagePublisherService` | Durable Queue를 통한 이벤트 보존 | 핵심 |
| `IModelVerificationService` / `ModelVerificationService` | 검증 실패 시 배포 중단 | 보조 |

### 3.3 적용된 Pattern 및 Tactic

#### 3.3.1 Fault Isolation Tactic

**설계 결정**: 파이프라인 단계별 장애 격리

**구현 방식**:
- 데이터 수집 실패 시 학습 중단, 기존 모델 유지
- 학습 실패 시 검증 단계 진입 불가, 자동 롤백
- 검증 실패 시 배포 중단, 기존 모델 유지
- 배포 실패 시 자동 롤백 (<1분)

**가용성 효과**:
- 부분 장애 시에도 서비스 지속 가능
- 기존 모델 유지로 서비스 중단 방지
- 단계별 장애 격리로 전체 파이프라인 영향 최소화

**정당화**:
- **대안 1: 전체 파이프라인 실패 시 전체 롤백**
  - 부분 실패 시에도 전체 롤백
  - 불필요한 롤백 발생
  - **결론**: 단계별 격리가 더 효율적

- **대안 2: 장애 발생 시 계속 진행**
  - 잘못된 모델 배포 위험
  - 서비스 품질 저하
  - **결론**: 단계별 검증 및 롤백이 필수

**적용된 Pattern**: **Saga Pattern**
- 단계별 트랜잭션 관리
- 부분 실패 시 자동 롤백

#### 3.3.2 Passive Redundancy Tactic (DD-07)

**설계 결정**: 모델 교체 중에도 요청 처리 신뢰성 유지

**구현 방식**:
- FaceModel Service에 Passive Redundancy 적용
- 모델 교체 중에도 기존 모델로 요청 처리
- AtomicReference 기반 원자적 모델 교체

**가용성 효과**:
- 모델 교체 중 서비스 중단 없음
- 24시간 무인 운영 중 심야 시간대 모델 교체 위험 최소화
- 서비스 가용성 99.9% 유지

**정당화**:
- **대안 1: Active-Active Redundancy**
  - 두 모델 동시 운영
  - 리소스 소모 증가
  - **결론**: Passive Redundancy가 효율적

- **대안 2: 모델 교체 중 요청 거부**
  - 서비스 중단 발생
  - QAS-05 목표 위반
  - **결론**: Passive Redundancy가 필수

**적용된 Pattern**: **Passive Redundancy Pattern**
- 모델 교체 중에도 기존 모델로 요청 처리

#### 3.3.3 Event Preservation Tactic (DD-02)

**설계 결정**: RabbitMQ Durable Queue를 통한 이벤트 보존

**구현 방식**:
- `IMessagePublisherService`를 통한 이벤트 발행
- RabbitMQ Durable Queue에 이벤트 저장
- Consumer 장애 시에도 이벤트 보존
- 복구 후 재처리 가능

**가용성 효과**:
- 이벤트 손실 방지
- 서비스 재시작 후에도 이벤트 처리 가능
- 데이터 일관성 보장

**정당화**:
- **대안 1: 메모리 큐**
  - 서비스 재시작 시 이벤트 손실
  - **결론**: Durable Queue가 필수

- **대안 2: DB 저장 후 이벤트 발행**
  - 추가 DB I/O
  - 성능 저하
  - **결론**: Durable Queue가 효율적

**적용된 Pattern**: **Observer Pattern**
- Durable Queue를 통한 이벤트 보존

---

## 4. 종합 설계 정당화

### 4.1 다중 QA 동시 달성

본 컴포넌트의 설계 요소들은 서로 상호 보완적으로 작동하여 여러 QA를 동시에 달성합니다:

1. **Saga Pattern (Orchestration)**
   - QAS-06 (수정용이성): 단계별 독립적 수정 가능
   - QAS-05 (가용성): 단계별 장애 격리 및 롤백

2. **Strategy Pattern**
   - QAS-06 (수정용이성): 전략 교체 용이
   - QAS-02 (성능): 최적 전략 선택 가능

3. **Hot Swap Pattern**
   - QAS-06 (수정용이성): 무중단 배포
   - QAS-05 (가용성): 서비스 연속성 보장

4. **Event-Driven Architecture**
   - QAS-06 (수정용이성): 느슨한 결합
   - QAS-05 (가용성): 이벤트 보존

### 4.2 대안 설계와의 비교

#### 대안 1: 단일 서비스로 모든 단계 처리
- **장점**: 구현 단순
- **단점**: 
  - QAS-06 위반 (수정 어려움)
  - QAS-05 위반 (장애 격리 어려움)
- **결론**: 현재 설계가 모든 QA를 만족

#### 대안 2: Choreography Saga Pattern
- **장점**: 중앙 오케스트레이터 불필요
- **단점**: 
  - 트랜잭션 관리 어려움
  - 복잡도 증가
  - 디버깅 어려움
- **결론**: Orchestration Saga Pattern이 더 적합

#### 대안 3: Blue/Green Deployment
- **장점**: 롤백 용이
- **단점**: 
  - QAS-06 위반 (서비스 중단 위험)
  - QAS-05 위반 (가용성 저하)
- **결론**: Hot Swap이 무중단 목표에 적합

---

## 결론

`TrainingPipelineOrchestrator` 컴포넌트는 **Orchestration Saga Pattern**, **Strategy Pattern**, **Hot Swap Pattern**, **Event-Driven Architecture**, **Facade Pattern**을 체계적으로 적용하여 QAS-06 (수정용이성), QAS-02 (성능), QAS-05 (가용성)을 모두 달성합니다. 각 설계 요소는 특정 QA 달성에 직접적으로 기여하며, 상호 보완적으로 작동하여 최적의 설계를 구성합니다. 고려된 대안 설계들과 비교하여 현재 설계가 모든 QA를 만족하는 최적의 설계임을 정당화합니다.

