# TrainingPipelineOrchestrator 컴포넌트 명세서

## 개요

**TrainingPipelineOrchestrator**는 MLOps Service 내 AI 모델 재학습 파이프라인의 전체 워크플로우를 오케스트레이션하는 핵심 비즈니스 로직 컴포넌트입니다. 데이터 수집부터 모델 학습, 검증, Hot Swap 배포까지의 4단계 파이프라인을 관리하며, 이벤트 기반 트리거를 통해 자동화된 재학습 프로세스를 지원합니다.

---

## 컴포넌트 기능 요구사항

### Provided Interface: `ITrainingPipelineService`

이 컴포넌트는 다음 핵심 오퍼레이션을 제공합니다:

#### 1. `orchestrateTraining(String trainingId)`

**기능**: AI 모델 재학습 파이프라인 오케스트레이션 (UC-24, DD-07)

**처리 흐름**:
1. **데이터 수집**: `IDataManagementService`를 통해 학습 데이터 수집
   - Auth Service에서 안면 벡터 데이터 수집 (READ-ONLY)
   - Helper Service에서 태스크 사진 데이터 수집 (READ-ONLY)
   - Event-Based Replication으로 데이터 동기화 (DD-03)
2. **모델 학습**: 수집된 데이터를 기반으로 AI 모델 재학습 수행
   - 배치 학습으로 GPU 리소스 최적 활용
   - CUDA 가속을 통한 학습 시간 단축 (CPU 대비 10-20배)
   - 평균 학습 완료 시간: 4시간 (야간 배치)
3. **모델 검증**: `IModelVerificationService`를 통해 학습된 모델 검증
   - 정확도 검증: >90% 목표 (AccuracyVerifier)
   - 성능 검증: <3초 응답 시간 목표 (PerformanceVerifier)
   - 검증 실패 시 배포 중단 및 롤백
4. **모델 배포**: 검증 성공 시 `IModelDeploymentService`를 통해 Hot Swap 배포
   - AtomicReference 기반 <1ms 모델 교체 (DD-07: Hot Swap)
   - 무중단 배포로 서비스 가용성 99.9% 유지 (QAS-06)
   - FaceModel Service에 모델 업데이트 알림
5. **이벤트 발행**: `IMessagePublisherService`를 통해 학습 완료 이벤트 발행
   - `TrainingCompletedEvent` 발행 (비동기)
   - 후속 처리(모니터링, 알림 등)를 위한 이벤트 기록

**반환값**: `void` (비동기 처리)

**트리거 시나리오**:
- **일일 야간 배치**: 타이머 기반 자동 트리거 (DD-07: Batch Sequential)
- **이벤트 기반**: `TaskConfirmedEvent` 수신 시 자동 재학습 (DD-02: Event-Based Architecture)
- **수동 트리거**: `TrainingManager`를 통한 수동 학습 시작

**Saga Pattern 적용**:
- 각 단계별 트랜잭션 관리
- 부분 실패 시 자동 롤백
- 단계별 체크포인트 저장

---

## 컴포넌트 품질 요구사항

### 1. 수정용이성 (Modifiability) - QAS-06

#### 1.1 무중단 모델 배포 (DD-07: Hot Swap)

**목표**: AI 모델 업데이트 시 서비스 중단 없이 배포
- **Hot Swap**: AtomicReference 기반 <1ms 모델 교체
- **Passive Redundancy**: 모델 교체 중에도 요청 처리 신뢰성 유지
- **롤백 시간**: 1분 이내 (BG-11: 신속 복구)
- **결과**: 서비스 가용성 99.9% 유지, API 요청 실패율 <1%

#### 1.2 이벤트 기반 아키텍처 (DD-02)

**느슨한 결합**:
- 파이프라인 단계별 독립적 수정 가능
- `IDataManagementService`, `IModelVerificationService`, `IModelDeploymentService` 인터페이스 기반 설계
- 구현체 변경 시 컴포넌트 수정 최소화

**Event-Driven Architecture**:
- `TaskConfirmedEvent` 구독으로 자동 재학습 트리거
- `TrainingCompletedEvent` 발행으로 후속 처리 연동
- Message Broker를 통한 비동기 통신

#### 1.3 Saga Pattern

**분산 트랜잭션 관리**:
- 각 단계별 트랜잭션 관리
- 부분 실패 시 자동 롤백
- 단계별 체크포인트 저장으로 복구 지원

### 2. 성능 (Performance) - QAS-02

#### 2.1 GPU 리소스 최적 활용

**병렬 처리**:
- 데이터 전처리와 모델 학습을 동시에 실행
- 배치 학습으로 VRAM 사용량 50% 감소
- 메모리 최적화를 통한 리소스 효율성 향상

**CUDA 가속**:
- TensorFlow GPU 연산으로 CPU 대비 10-20배 속도 향상
- 학습 시간 단축: 평균 4시간 (야간 배치)

#### 2.2 모델 검증 성능

**검증 시간 최소화**:
- 정확도 검증: <30분
- 성능 검증: <10분
- 전체 검증 시간: <1시간

### 3. 가용성 (Availability) - QAS-05

#### 3.1 장애 격리

**파이프라인 단계별 격리**:
- 데이터 수집 실패 시 학습 중단, 기존 모델 유지
- 학습 실패 시 검증 단계 진입 불가, 자동 롤백
- 검증 실패 시 배포 중단, 기존 모델 유지
- 배포 실패 시 자동 롤백 (<1분)

#### 3.2 서비스 연속성

**무중단 배포**:
- Hot Swap을 통한 <1ms 모델 교체
- Passive Redundancy로 모델 교체 중 요청 처리
- 서비스 가용성 99.9% 유지

#### 3.3 이벤트 보존 (DD-02)

**Durable Queue**:
- 학습 완료 이벤트를 RabbitMQ Durable Queue에 저장
- Consumer 장애 시에도 이벤트 보존
- 복구 후 재처리 가능

### 4. 신뢰성 (Reliability)

#### 4.1 오류 처리

**단계별 예외 처리**:
- 각 단계 실패 시 적절한 롤백 및 알림 발행
- 로깅 및 모니터링을 통한 장애 추적
- 자동 복구 메커니즘

#### 4.2 데이터 일관성 (DD-03)

**Event-Based Replication**:
- Helper Service의 `TaskConfirmedEvent` 구독으로 직접 DB 접근 배제
- TrainingDataStore 독립 운영으로 데이터 일관성 보장
- READ-ONLY 접근으로 원본 데이터 보호

#### 4.3 모델 검증

**다중 검증**:
- 정확도 검증: >90% 목표
- 성능 검증: <3초 응답 시간 목표
- 검증 실패 시 배포 중단

---

## 참고 문서

- **DD-07**: AI 학습 판독 구조 설계 결정
- **DD-03**: 저장소 설계 결정 (Event-Based Replication)
- **DD-02**: 노드간 비동기 통신 구조 설계 결정
- **QAS-06**: 수정용이성 (무중단 모델 배포)
- **QAS-02**: 성능 최적화
- **QAS-05**: 가용성 (99.5%)
- **UC-24**: AI 모델 재학습 파이프라인
- **Component Diagram**: `11_MLOpsServiceComponent.puml`

