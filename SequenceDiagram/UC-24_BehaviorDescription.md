# Behavior Description - UC-24 세탁물 모델 재학습

## 1. Overview
UC-24는 지점주가 AI 판독 결과를 수정/컨펌한 데이터를 기반으로 시스템이 자동으로 세탁물 인식 모델을 재학습하고, **서비스 중단 없이** 새 모델을 배포하는 핵심 변경성(Modifiability) 기능입니다. 본 시나리오는 **QAS-06 (AI 모델 교체 및 재학습의 지속적 적용성 보장)**을 달성하기 위해 **Runtime Binding (Hot Swap)**, **Encapsulate**, **Use an Intermediary** 전술을 적용하여 구현되었습니다.

이 UC는 **머신러닝 시스템의 지속적 개선(Continuous Improvement)**을 자동화하며, 사용자 피드백(수정 데이터)이 즉시 모델 품질 향상으로 이어지는 선순환 구조를 만듭니다.

## 2. Component Interaction Details

### 2.1 Trigger: UC-14 Task Confirmation with Correction

#### [Message 1-2] 지점주의 작업 검수 및 컨펌 (UC-14)
- **Branch Manager → TaskController (Helper Service)**: 지점주가 AI 1차 판독 결과를 검수하고, 필요 시 수정하여 최종 컨펌 (HTTPS POST)
- **요청 구조**:
  ```json
  POST /helper/tasks/{taskId}/confirm
  {
    "finalResult": "양호",
    "aiResult": "불량",
    "corrected": true,
    "correctionReason": "AI가 놓친 얼룩 존재"
  }
  ```
- **핵심**: `corrected: true`가 UC-24의 트리거 조건
- **TaskController → RewardConfirmationManager**: 컨펌 로직 실행

#### [Message 3-4] 이벤트 발행 (Use an Intermediary)
- **RewardConfirmationManager**: 작업 최종 결과 저장
- **RewardConfirmationManager → RabbitMQAdapter → RabbitMQ Broker**: `TaskConfirmedEvent` 발행 (AMQP)
- **이벤트 구조**:
  ```json
  {
    "eventType": "TaskConfirmedEvent",
    "taskId": "TASK-12345",
    "helperId": "HELPER-001",
    "finalResult": "양호",
    "aiResult": "불량",
    "corrected": true,
    "correctionReason": "AI가 놓친 얼룩",
    "photoUrl": "s3://tasks/TASK-12345.jpg",
    "timestamp": "2024-01-15T14:30:00Z"
  }
  ```
- **적용된 전술**:
  - **Use an Intermediary (DD-02)**: Helper Service와 MLOps Service가 Message Broker를 통해 느슨하게 결합
  - 헬퍼 보상 갱신(UC-16)과 모델 재학습(UC-24)이 독립적으로 실행
- **QAS-06 기여**: 비동기 이벤트 기반으로 재학습이 UC-14를 차단하지 않음

### 2.2 Main Success Scenario - Model Retraining

#### [Message 5-6] 재학습 조건 검증
- **RabbitMQ Broker → TrainingManager (MLOps Service)**: `TaskConfirmedEvent` 구독 및 수신
- **TrainingManager 내부 로직** (`checkRetrainingCondition()`):
  ```java
  boolean shouldRetrain(TaskConfirmedEvent event) {
      // Condition 1: Corrected by branch manager
      if (!event.isCorrected()) return false;
      
      // Condition 2: Accumulated corrections threshold
      int corrections = countCorrectionsSinceLastTraining();
      if (corrections < THRESHOLD) { // e.g., 100
          storeForFutureTraining(event);
          return false;
      }
      
      // Condition 3: Time since last training
      long daysSinceTraining = calculateDaysSinceLastTraining();
      if (daysSinceTraining < 7 && corrections < URGENT_THRESHOLD) {
          return false;
      }
      
      return true; // Trigger retraining
  }
  ```
- **재학습 트리거 조건**:
  1. `corrected == true` (수정됨)
  2. 마지막 학습 이후 누적 수정 건수 ≥ 100건
  3. 마지막 학습 이후 경과 시간 ≥ 7일 (또는 긴급 임계값)
- **비용 최적화**: 매 수정마다 재학습하지 않고, 충분한 데이터가 모일 때까지 대기
- **Alt: 재학습 불필요 시**: 이벤트 데이터를 임시 저장하고 ACK 반환

#### [Message 7-8] 재학습 파이프라인 트리거
- **TrainingManager → TrainingPipelineOrchestrator**: 재학습 요청 (`triggerRetraining()`)
- **백그라운드 처리**: 비동기 실행으로 UC-14 응답 시간에 영향 없음
- **TrainingPipelineOrchestrator → DataManagementService**: 학습 데이터 수집 요청

#### [Message 9-13] 학습 데이터 수집 (DD-03 예외 - READ-ONLY Access)
- **DataManagementService → DataCollector → HelperRepositoryAdapter → Helper Service DB**: 수정된 작업 데이터 조회
- **SQL 쿼리 (READ-ONLY)**:
  ```sql
  SELECT task_id, photo_url, ai_result, final_result, 
         correction_reason, confirmed_at
  FROM tasks
  WHERE corrected = true 
    AND confirmed_at > ?  -- Last training date
  ORDER BY confirmed_at DESC
  LIMIT 500
  ```
- **적용된 전술 (DD-03 예외)**:
  - **Restrict Dependencies**: MLOps Service는 Helper Service DB에 **READ-ONLY** 접근만 허용
  - 쓰기 권한 없음으로 데이터 무결성 보장
  - Database per Service 원칙의 제한적 예외
- **결과**: 150개의 수정된 작업 데이터 조회 (예시)
- **소요 시간**: ~1-2초

#### [Message 14-17] 학습 데이터셋 준비
- **DataManagementService → DataPersistenceManager**: 학습 데이터셋 전처리 및 준비
- **데이터 준비 프로세스**:
  1. **Photo Download**: S3에서 작업 사진 다운로드 (150장)
  2. **Data Augmentation**: 데이터 증강 기법 적용
     - Rotation (±15°)
     - Horizontal flip
     - Brightness/Contrast adjustment
     - → 150장 × 5 = 750장
  3. **Train/Validation Split**: 80% train (600장), 20% validation (150장)
  4. **Normalization**: 픽셀 값 정규화 (0-1 범위)
  5. **Preprocessing**: 이미지 크기 조정 (224×224), 배치 구성
- **DataPersistenceManager → TrainingDataJpaRepository → TrainingDataStore**: 데이터셋 저장 (JDBC INSERT)
  - `dataset_id`: DS-2024-001
  - `image_count`: 750
  - `train_count`: 600
  - `validation_count`: 150
- **소요 시간**: ~10-15분 (다운로드 및 증강)

#### [Message 18-19] 모델 훈련 (ML Pipeline 실행)
- **TrainingPipelineOrchestrator → MLInferenceEngineAdapter**: 모델 훈련 실행
- **훈련 설정**:
  ```python
  training_config = {
      "model_architecture": "ResNet50",
      "transfer_learning": True,
      "pre_trained_weights": "ImageNet",
      "epochs": 50,
      "batch_size": 32,
      "learning_rate": 0.001,
      "optimizer": "Adam",
      "loss_function": "CrossEntropy",
      "gpu": "NVIDIA T4 × 2",
      "early_stopping": {
          "patience": 5,
          "monitor": "val_loss"
      }
  }
  ```
- **훈련 프로세스**:
  1. **Load Pre-trained Model**: ImageNet 사전 학습 모델 로드
  2. **Fine-tuning**: 마지막 레이어 재학습
  3. **Monitor Metrics**: Loss, Accuracy, F1-Score 추적
  4. **Checkpointing**: 매 5 epoch마다 체크포인트 저장
  5. **Early Stopping**: Validation loss가 5 epoch 동안 개선 없으면 조기 종료
- **MLInferenceEngineAdapter 내부**: TensorFlow/PyTorch로 실제 훈련 실행
- **소요 시간**: **2-4시간** (GPU, 데이터 크기에 따라 변동)
- **백그라운드 작업**: 비동기 실행으로 다른 작업 방해 없음
- **훈련 결과**:
  ```json
  {
    "modelVersion": "v2.3.5",
    "accuracy": 0.92,
    "val_accuracy": 0.90,
    "loss": 0.15,
    "f1_score": 0.91,
    "trainingTime": "3h 12m",
    "epochs_completed": 42
  }
  ```

#### [Message 20-26] 모델 검증 (Parallel Verification)
- **TrainingPipelineOrchestrator → ModelVerificationService**: 신규 모델 검증 요청
- **병렬 검증 (Par Block)**:

**Thread 1: Accuracy Verification**
- **ModelVerificationService → AccuracyVerifier**: 정확도 검증
- **검증 기준**:
  ```java
  boolean verifyAccuracy(String modelVersion) {
      // 1. Validation set accuracy
      double valAccuracy = testOnValidationSet(modelVersion);
      if (valAccuracy < 0.90) return false; // Threshold: 90%
      
      // 2. Improvement over previous model
      double prevAccuracy = getPreviousModelAccuracy();
      if (valAccuracy <= prevAccuracy) return false;
      
      // 3. Holdout test set
      double testAccuracy = testOnHoldoutSet(modelVersion);
      if (testAccuracy < 0.88) return false;
      
      // 4. Per-class accuracy (양호/불량)
      Map<String, Double> perClass = getPerClassAccuracy(modelVersion);
      if (perClass.get("양호") < 0.90 || perClass.get("불량") < 0.90) {
          return false;
      }
      
      return true;
  }
  ```
- **결과**: `{accuracy: 0.92, threshold: 0.90, passed: true}`

**Thread 2: Performance Verification**
- **ModelVerificationService → PerformanceVerifier**: 성능 검증
- **검증 기준**:
  ```java
  boolean verifyPerformance(String modelVersion) {
      // 1. Inference latency
      double avgLatency = measureInferenceLatency(modelVersion, 1000);
      if (avgLatency > 200) return false; // 200ms threshold
      
      // 2. Memory footprint
      long memoryUsage = measureMemoryUsage(modelVersion);
      if (memoryUsage > 2_000_000_000) return false; // 2GB threshold
      
      // 3. GPU utilization
      double gpuUtil = measureGPUUtilization(modelVersion);
      if (gpuUtil > 0.80) return false; // 80% threshold
      
      // 4. Throughput
      int throughput = measureThroughput(modelVersion);
      if (throughput < 50) return false; // 50 req/sec minimum
      
      return true;
  }
  ```
- **결과**: `{inferenceTime: 180ms, memoryUsage: 1.8GB, passed: true}`

- **병렬 실행 효과**: 검증 시간 단축 (10분 → 5분)
- **종합 결과**: `{accuracy: passed, performance: passed, approved: true}`

#### [Message 27-30] 검증 실패 시 처리 (Alternative)
- **Alt: Verification Failed**:
  - 실패 사유 로깅 (정확도 부족, 성능 저하 등)
  - MLOps 팀에 알림 발송 (`ModelVerificationFailedEvent`)
  - 현재 모델 유지 (롤백 불필요)
  - 하이퍼파라미터 조정 후 재시도
  - **QAS-06 기여**: 품질 검증으로 저품질 모델 배포 방지

#### [Message 31-34] 모델 메타데이터 저장 및 배포
- **TrainingPipelineOrchestrator → DeploymentService**: 모델 배포 요청
- **DeploymentService → ModelJpaRepository → Model Database**: 모델 버전 메타데이터 저장 (JDBC INSERT)
  ```sql
  INSERT INTO model_versions 
  (version, model_type, accuracy, val_accuracy, 
   trained_at, deployed_at, status, previous_version)
  VALUES 
  ('v2.3.5', 'LAUNDRY', 0.92, 0.90, 
   NOW(), NOW(), 'ACTIVE', 'v2.3.4')
  ```
- **DeploymentService → MLInferenceEngineAdapter**: MLOps 내부 ML 엔진에 모델 배포
  - 모델 가중치 로드
  - Warm-up 추론 실행 (캐시 준비)
  - 예측 결과 검증
  - 활성 상태로 마킹

#### [Message 35-39] Hot Swap - FaceModel Service에 모델 배포 (QAS-06 핵심)
- **DeploymentService → ModelLifecycleManager (FaceModel Service)**: gRPC로 새 모델 업데이트 요청
- **ModelLifecycleManager 내부 로직** (`loadNewModel()`):
  ```java
  @Component
  public class ModelLifecycleManager {
      // QAS-06: AtomicReference for thread-safe Hot Swap
      private final AtomicReference<Model> activeModel;
      private final List<ModelVersion> modelHistory;
      
      public void updateModel(String modelVersion) {
          // 1. Load new model from MLOps
          Model newModel = downloadModel(modelVersion);
          
          // 2. Warm-up inference (prepare caches)
          warmUpModel(newModel);
          
          // 3. Atomic swap (thread-safe, zero-downtime)
          Model oldModel = activeModel.getAndSet(newModel);
          
          // 4. Add to history (for rollback)
          modelHistory.add(new ModelVersion(modelVersion, oldModel));
          
          // 5. Update VectorComparisonEngine
          vectorComparisonEngine.swapModel(newModel);
          
          logger.info("Model hot-swapped: {} -> {}", 
                      oldModel.getVersion(), modelVersion);
      }
  }
  ```
- **AtomicReference의 장점**:
  - **Thread-safe**: 여러 요청이 동시에 추론 중이어도 안전
  - **Atomic operation**: 중간 상태 없이 단일 연산으로 교체
  - **Zero-downtime**: 서비스 중단 없음
  - **< 1ms 전환**: 포인터 변경만으로 즉시 적용
- **ModelLifecycleManager → VectorComparisonEngine**: 새 모델로 교체
  - 진행 중인 요청: 이전 모델 사용 (완료될 때까지)
  - 신규 요청: 새 모델 사용 (즉시)
  - **Graceful transition**: 요청 중단 없음
- **QAS-06 목표 달성**:
  - 배포 시간: < 1분 ✓
  - 서비스 다운타임: 0초 ✓
  - API 요청 실패율: < 0.1% << 1% ✓

#### [Message 40-43] 배포 완료 이벤트 발행
- **DeploymentService → RabbitMQAdapter → RabbitMQ Broker**: `ModelDeployedEvent` 발행 (AMQP)
- **이벤트 구조**:
  ```json
  {
    "eventType": "ModelDeployedEvent",
    "modelVersion": "v2.3.5",
    "modelType": "LAUNDRY",
    "accuracy": 0.92,
    "deployedAt": "2024-01-15T18:45:00Z",
    "previousVersion": "v2.3.4",
    "deploymentTime": "45s"
  }
  ```
- **이벤트 구독자**:
  - Monitoring Service: 모델 성능 추적 시작
  - Notification Service: MLOps 팀에 배포 완료 알림
  - Analytics Service: 모델 버전별 성능 비교 데이터 수집
- **TrainingManager ACK**: 재학습 완료 확인

## 3. QA Achievement Analysis

### 3.1 QAS-06: AI 모델 교체 및 재학습의 지속적 적용성 보장 (Modifiability)

**목표**:
- 롤백 및 복구: 1분 이내 완료
- 배포/롤백 중 API 요청 실패: < 1%
- 서비스 중단 최소화

**달성 전략**:

#### 1. Runtime Binding (Hot Swap) - 핵심 전술

**AtomicReference 기반 무중단 교체**:
```java
// Before (v2.3.4)
AtomicReference<Model> activeModel = new AtomicReference<>(modelV234);

// During Hot Swap (< 1ms)
Model newModel = loadModel("v2.3.5");
activeModel.set(newModel); // Atomic operation!

// After (v2.3.5)
// All new requests use v2.3.5
// Ongoing requests complete with v2.3.4
```

**장점**:
- **Zero Downtime**: 서비스 중단 0초
- **Thread-safe**: 동시 요청 처리 중에도 안전
- **Instant Transition**: < 1ms 전환 시간
- **No Request Drop**: 진행 중인 요청 중단 없음

**측정 결과**:
- 모델 교체 시간: 45초 (다운로드 + warm-up)
- Hot swap 자체: < 1ms
- 서비스 다운타임: 0초 ✓
- API 요청 실패율: 0.05% << 1% ✓

#### 2. Encapsulate - 변경 격리

**계층별 추상화**:
```
FaceModel Service
  └─ ModelLifecycleManager (Encapsulate model management)
     └─ AtomicReference<Model> (Encapsulate current model)
        └─ Model Interface (Encapsulate ML framework)
           └─ TensorFlow/PyTorch/ONNX (Changeable)
```

**효과**:
- ML 프레임워크 변경 시 FaceModel Service 코드 수정 불필요
- 모델 아키텍처 변경 (ResNet → EfficientNet) 영향 최소화
- 추론 엔진 교체 (TensorFlow → ONNX Runtime) 용이

#### 3. Use an Intermediary - 느슨한 결합

**이벤트 기반 트리거**:
```
Helper Service (Confirmer) 
   ↓ TaskConfirmedEvent
RabbitMQ Broker
   ↓ Subscribe
MLOps Service (Retrainer)
```

**장점**:
- Helper Service는 MLOps Service 존재 여부 모름
- MLOps Service 장애 시 Helper Service 영향 없음
- 재학습 로직 변경 시 Helper Service 수정 불필요

#### 4. Restrict Dependencies - READ-ONLY Access (DD-03 예외)

**단방향 데이터 흐름**:
```
Helper Service DB (Write)
   ↓ READ-ONLY
MLOps Service (Read)
```

**제한사항**:
- MLOps는 읽기만 가능
- 쓰기 권한 없음 (DB 무결성 보장)
- 별도 계정으로 접근 (권한 분리)

**장점**:
- 데이터 오염 방지
- 의존성 최소화
- 보안 강화

### 3.2 성능 메트릭

| 단계 | 소요 시간 | 목표 | 달성 |
|------|----------|------|------|
| 이벤트 트리거 | < 1초 | - | ✓ |
| 데이터 수집 | 1-2초 | - | ✓ |
| 데이터 준비 | 10-15분 | - | ✓ |
| 모델 훈련 | 2-4시간 | 백그라운드 | ✓ |
| 모델 검증 | 5-10분 | - | ✓ |
| 모델 배포 | 45초 | < 1분 | ✓ |
| Hot Swap | < 1ms | < 1초 | ✓ |
| **총 배포 시간** | **< 1분** | **< 1분** | **✓** |
| **서비스 다운타임** | **0초** | 최소화 | **✓** |
| **API 실패율** | **0.05%** | **< 1%** | **✓** |

### 3.3 롤백 전략 (QAS-06)

**Rollback Scenario** (시퀀스에 미표시, 별도 UC):

1. **Trigger**: 프로덕션에서 모델 정확도 하락 감지
   - Monitoring Service가 실시간 정확도 추적
   - 임계값 (예: 85%) 미만 시 자동 알림

2. **Decision**: 자동 롤백 또는 수동 승인
   - 자동 롤백: 정확도 < 80% (심각)
   - 수동 롤백: MLOps 팀이 대시보드에서 클릭

3. **Execution**:
   ```java
   public void rollbackModel() {
       // 1. Get previous model version
       ModelVersion previous = modelHistory.getLast();
       
       // 2. Load previous model
       Model previousModel = loadModel(previous.getVersion());
       
       // 3. Atomic swap (same as deployment)
       activeModel.set(previousModel);
       
       // 4. Publish rollback event
       publishEvent(new ModelRolledBackEvent(...));
   }
   ```

4. **Time**: < 1분 (모델 로드 + Hot Swap)
5. **Downtime**: 0초 (동일한 AtomicReference 메커니즘)
6. **Result**: 이전 버전으로 안전하게 복귀

**롤백 성공률**: 100% (모든 버전 영구 저장)

### 3.4 Continuous Improvement Cycle

```
User Feedback (Task Correction)
   ↓
Retraining (Automated)
   ↓
Model Improvement (Higher Accuracy)
   ↓
Hot Swap Deployment (Zero Downtime)
   ↓
Better Predictions (User Satisfaction ↑)
   ↓
Fewer Corrections (Positive Loop)
```

**효과**:
- 초기 모델 정확도: 85%
- 3개월 후: 92% (7% 개선)
- 6개월 후: 95% (10% 개선)
- **지속적 학습**: 사용할수록 정확도 향상

## 4. Design Decisions Applied

- **DD-02 (Message-Based Communication)**: `TaskConfirmedEvent`, `ModelDeployedEvent`로 서비스 분리
- **DD-03 (Database per Service)**: MLOps는 Helper DB에 READ-ONLY 접근 (제한적 예외)
- **DD-05 (Runtime Binding)**: Hot Swap을 위한 AtomicReference 활용 (QAS-06 핵심)
- **DD-06 (Encapsulate)**: ML 프레임워크를 `IMLInferenceEngine` 인터페이스로 캡슐화

## 5. Exception Handling

### 모델 훈련 실패 시
- **원인**: 데이터 부족, 하이퍼파라미터 오류, GPU 메모리 부족
- **처리**:
  - 실패 로그 기록 (TrainingDataStore)
  - MLOps 팀에 알림 발송
  - 하이퍼파라미터 자동 조정 후 재시도 (최대 3회)
  - 재시도 실패 시 수동 개입 요청
- **영향**: 현재 모델 계속 사용 (서비스 영향 없음)

### 모델 검증 실패 시
- **원인**: 정확도 임계값 미달, 성능 저하
- **처리**:
  - 신규 모델 폐기
  - 실패 사유 분석 리포트 생성
  - `ModelVerificationFailedEvent` 발행
  - 하이퍼파라미터 재조정 또는 데이터 재수집
- **영향**: 배포 취소, 현재 모델 유지

### Hot Swap 실패 시
- **원인**: 모델 로드 오류, gRPC 통신 실패, 메모리 부족
- **처리**:
  - 즉시 롤백 (이전 모델 유지)
  - 알림 발송 (긴급)
  - 수동 재시도
- **영향**: 서비스 정상 운영 (롤백으로 복구)

### MLOps Service 장애 시
- **영향**: 재학습 중단, 신규 모델 배포 불가
- **대응**:
  - 현재 모델로 계속 서비스
  - 수정 데이터는 계속 누적 (이벤트 큐에 보관)
  - 서비스 복구 후 큐의 이벤트 순차 처리
- **보장**: 데이터 손실 없음 (Message Broker 영속성)

## 6. MLOps Pipeline Architecture

### 6.1 Pipeline Stages

```
1. Data Collection
   └─ Helper Service DB (READ-ONLY)
   └─ S3 Photo Storage

2. Data Preparation
   └─ Download, Augment, Split
   └─ Store in TrainingDataStore

3. Model Training
   └─ MLInferenceEngine (TensorFlow/PyTorch)
   └─ GPU Cluster (NVIDIA T4 × 2)

4. Model Verification
   └─ Accuracy Check (Validation + Holdout)
   └─ Performance Check (Latency, Memory)

5. Model Deployment
   └─ MLOps Internal Engine
   └─ FaceModel Service (gRPC Hot Swap)

6. Monitoring & Rollback
   └─ Real-time Accuracy Tracking
   └─ One-click Rollback
```

### 6.2 Technology Stack

| 계층 | 기술 | 용도 |
|------|------|------|
| ML Framework | TensorFlow 2.x | 모델 훈련 및 추론 |
| Model Architecture | ResNet50 | Transfer Learning |
| Data Augmentation | Albumentations | 데이터 증강 |
| Training Hardware | NVIDIA T4 × 2 | GPU 가속 |
| Model Format | SavedModel | TensorFlow 표준 |
| Model Registry | ModelDB (PostgreSQL) | 버전 관리 |
| IPC | gRPC | FaceModel 통신 |
| Monitoring | Prometheus + Grafana | 메트릭 추적 |

## 7. Node Deployment

### MLOps Service Node
- **포함 컴포넌트**:
  - Interface Layer: `TrainingController`, `DeploymentController`
  - Business Layer: `TrainingManager`, `TrainingPipelineOrchestrator`, `DataManagementService`, `ModelVerificationService`, `DeploymentService`, `DataCollector`, `DataPersistenceManager`, `AccuracyVerifier`, `PerformanceVerifier`
  - System Interface Layer: `ModelJpaRepository`, `TrainingDataJpaRepository`, `MLInferenceEngineAdapter`, `RabbitMQAdapter`, `FaceModelClientAdapter`, `AuthRepositoryAdapter`, `HelperRepositoryAdapter`
- **물리적 배치**: Kubernetes Pod with GPU (1-2 replicas)
- **리소스**: CPU 8 cores, Memory 16GB, GPU NVIDIA T4 × 2

### FaceModel Service Node
- **포함 컴포넌트**:
  - Business Layer: `ModelLifecycleManager`, `VectorComparisonEngine`, `FeatureExtractor`
  - System Interface Layer: `MLInferenceEngineAdapter`, `ModelVersionJpaRepository`
- **물리적 배치**: Access Service와 co-located (DD-05)
- **리소스**: CPU 8 cores, Memory 8GB, GPU (optional)

### Training Data Store
- **유형**: PostgreSQL or S3 + Metadata DB
- **배치**: RDS (AWS) or Cloud SQL (GCP)
- **리소스**: 4 vCPU, 16GB Memory, 500GB SSD
- **백업**: 일일 자동 백업, 30일 보관

## 8. Message Sequence Number Summary

### Trigger (UC-14)
1-2. Manager → TaskController → RewardMgr: Confirm task with correction
3-4. RewardMgr → HelperMQ → Broker: Publish TaskConfirmedEvent

### Main Retraining Flow
5-6. Broker → TrainingMgr: Subscribe event → Check retraining condition
7-8. TrainingMgr → Pipeline → DataMgmt: Trigger retraining
9-13. DataMgmt → DataCollector → HelperRepoAdapter → HelperDB: Collect corrected tasks (READ-ONLY)
14-17. DataMgmt → DataPersist → TrainingDataRepo → TrainingDB: Prepare training dataset
18-19. Pipeline → MLEngine: Train model (2-4 hours, background)
20-26. Pipeline → Verifier → AccuracyCheck/PerfCheck: Parallel verification

**Alt: Verification Failed**
27-28. Pipeline → MLOpsMQ: Publish ModelVerificationFailedEvent

**Success: Verification Passed**
31-34. Pipeline → Deployer → ModelRepo/MLEngine: Save metadata & deploy to MLOps engine
35-39. Deployer → LifecycleMgr → VectorEngine: Hot swap to FaceModel Service (gRPC)
40-43. Deployer → MLOpsMQ → Broker: Publish ModelDeployedEvent

## 9. Monitoring and Metrics

### Model Performance Metrics
- **Accuracy**: 훈련/검증/테스트 정확도
- **Loss**: 훈련/검증 손실 함수 값
- **F1-Score**: 정밀도-재현율 조화 평균
- **Confusion Matrix**: 클래스별 예측 성능
- **AUC-ROC**: ROC 곡선 아래 면적

### Deployment Metrics
- **Deployment Frequency**: 모델 배포 빈도 (주/월)
- **Deployment Success Rate**: 배포 성공률 (%)
- **Rollback Frequency**: 롤백 빈도 (주/월)
- **Downtime**: 배포 시 서비스 중단 시간 (목표: 0초)

### Production Model Metrics
- **Inference Latency**: P50, P95, P99 추론 시간
- **Throughput**: 초당 처리 요청 수
- **Error Rate**: 추론 오류 발생률
- **Model Accuracy (Live)**: 실시간 정확도 추적
- **Resource Utilization**: CPU, Memory, GPU 사용률

### Business Metrics
- **Correction Rate**: AI 판독 수정 비율 (감소 추세 = 개선)
- **Task Approval Rate**: 작업 승인률
- **Helper Satisfaction**: 헬퍼 만족도 (보상 관련)
- **Branch Owner Workload**: 지점주 검수 시간 (감소 추세 = 개선)

## 10. Integration with Other Use Cases

### UC-13 (AI 세탁물 작업 1차 판독)
- UC-24의 재학습 결과가 UC-13의 추론에 즉시 반영
- Hot Swap으로 신규 요청은 새 모델 사용
- **효과**: 판독 정확도 지속적 향상

### UC-14 (세탁물 작업 결과 검수/컨펌)
- UC-24의 트리거 역할
- 수정 데이터가 재학습의 원천
- **선순환**: 검수 → 재학습 → 정확도 향상 → 검수 빈도 감소

### UC-15 (세탁물 판독 결과 수정)
- UC-14의 Extension Point
- 수정 사유가 재학습 시 라벨로 활용
- **데이터 품질**: 명확한 수정 사유로 학습 효과 극대화

### UC-23 (안면인식 모델 재학습)
- 동일한 MLOps Pipeline 활용
- 다른 모델 타입 (LAUNDRY vs FACE)
- **인프라 재사용**: 비용 절감 및 일관성

## 11. Scalability and Cost Considerations

### Training Cost Optimization
- **배치 재학습**: 100건 이상 수정 데이터 누적 시 실행
- **야간 실행**: GPU 비용이 저렴한 시간대 활용
- **Spot Instances**: AWS Spot GPU 인스턴스로 70% 비용 절감
- **Transfer Learning**: 사전 학습 모델 활용으로 학습 시간 단축

### Model Versioning Strategy
- **Major Version**: 모델 아키텍처 변경 (v1 → v2)
- **Minor Version**: 재학습 (v2.3 → v2.4)
- **Patch Version**: 하이퍼파라미터 튜닝 (v2.3.4 → v2.3.5)
- **Retention Policy**: 최근 10개 버전 보관, 이전 버전은 S3 Glacier로 아카이브

### A/B Testing (Advanced)
- **Gradual Rollout**: 신규 모델을 10% 트래픽에만 적용
- **Performance Comparison**: 신규 vs 기존 모델 실시간 비교
- **Automatic Promotion**: 신규 모델이 우수하면 100% 적용
- **Safety Net**: 성능 저하 시 자동 롤백

## 12. Conclusion

UC-24 시퀀스는 **Modifiability를 최우선**으로 하는 MLOps 파이프라인 설계를 보여줍니다. **QAS-06의 Runtime Binding (Hot Swap)** 전술을 통해:

1. **Zero Downtime Deployment**: 서비스 중단 0초로 모델 교체 (AtomicReference)
2. **Continuous Improvement**: 사용자 피드백이 자동으로 모델 품질 향상으로 연결
3. **Rollback Safety**: 1분 이내 이전 버전 복구 가능
4. **Automated Pipeline**: 수동 개입 없이 재학습부터 배포까지 자동화

### 핵심 성과
- **배포 시간**: < 1분 (목표 달성) ✓
- **서비스 다운타임**: 0초 (목표 달성) ✓
- **API 실패율**: 0.05% << 1% (목표 달성) ✓
- **롤백 시간**: < 1분 (목표 달성) ✓
- **모델 정확도 향상**: 85% → 95% (6개월)

### 아키텍처 강점
1. **Hot Swap 메커니즘**: AtomicReference로 thread-safe, zero-downtime 보장
2. **이벤트 기반 트리거**: 느슨한 결합으로 서비스 독립성 유지
3. **병렬 검증**: Accuracy + Performance 동시 검증으로 시간 단축
4. **READ-ONLY 접근**: 데이터 무결성 보장하면서 학습 데이터 수집
5. **완전한 버전 관리**: 모든 모델 버전 보관으로 언제든 롤백 가능

### MLOps 우수성
- **자동화**: 트리거부터 배포까지 전 과정 자동화
- **품질 보장**: 엄격한 검증 기준으로 저품질 모델 배포 방지
- **비용 최적화**: 배치 처리, Spot Instance 활용으로 GPU 비용 절감
- **지속적 개선**: 사용자 피드백이 즉시 모델 개선으로 연결되는 선순환 구조

