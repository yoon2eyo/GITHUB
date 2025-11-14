# UC-13: AI 세탁물 작업 1차 판독 - Behavior Description

## 1. Overview
- **Use Case ID / Name**: UC-13 / AI 세탁물 작업 1차 판독
- **Primary Actor**: 시스템 (비동기 이벤트 기반)
- **Trigger**: `TaskSubmittedEvent` (UC-12에서 헬퍼가 사진 업로드 시 발행)
- **Related System Features**: SF-08 (AI 판독)
- **Key Quality Attributes**: QAS-05 (Availability), QAS-06 (Modifiability)
- **Relevant Design Decisions**: DD-02 (Event Based, Message Based), DD-03 (Database per Service), DD-06 (AI Pipeline 활용)

이 유스케이스는 헬퍼가 업로드한 세탁물 작업 사진을 AI가 자동으로 판단하여 `양호/미흡/불분명` 중 하나의 결과를 생성하고, 결과를 Helper Service DB에 저장한다. UC-14와 UC-15는 본 판독 결과를 기반으로 수행된다.

---

## 2. Component Interaction Details

### 2.1 Main Success Scenario

#### [Messages 1-2] 이벤트 수신과 워커 기동
- `RabbitMQ Broker → RabbitMQAdapter` (Message 1): UC-12에서 발행된 `TaskSubmittedEvent`가 Helper Service 워커 큐로 전달된다.
- `RabbitMQAdapter → AITaskAnalysisConsumer` (Message 2): 구독자 `AITaskAnalysisConsumer.consumeTaskSubmitted(event)`가 호출되어 비즈니스 로직이 시작된다.
- **Availability 기여**: 메시지 큐 기반 비동기 처리로 API 경로와 판독 경로를 분리, 소비자 장애 시 메시지가 재시도되며 유실되지 않는다.

#### [Messages 3-6] 판독 대상 메타데이터 조회
- `AITaskAnalysisConsumer → HelperJpaRepository` (Message 3): `findTaskForAnalysis(taskId)`로 사진 키와 헬퍼 ID 등을 조회한다.
- `HelperJpaRepository → Helper DB` (Message 4) 및 응답 (Messages 5-6): JPA를 통해 Task 엔터티를 읽어온다.
- **Operations**: `HelperJpaRepository.findByTaskId()`
- **보조 시나리오**: 데이터가 없을 경우 NACK 후 `RabbitMQ` 재큐잉 (Behavior Description 4장 참조).

#### [Messages 7-14] 사진 다운로드 및 AI 추론
- `AITaskAnalysisConsumer → TaskAnalysisEngine` (Message 7): `analyzeTaskPhoto(TaskEntity)` 호출.
- `TaskAnalysisEngine → S3PhotoStorage` (Messages 8-9): `download(photoKey)`를 통해 S3에서 바이너리 이미지를 다운로드한다.
- `TaskAnalysisEngine → MLInferenceEngineAdapter` (Message 11): `inferLaundryQuality(image, metadata)` 호출.
- `MLInferenceEngineAdapter → ML Inference Engine` (Messages 12-13): HTTP POST `/api/v1/infer` 호출, ML 서비스가 분류 결과와 확률을 반환한다.
- `TaskAnalysisEngine` (Message 14-16): `determineLabel(scores)`로 최종 라벨(`양호/미흡/불분명`)과 신뢰도 계산.
- **Modifiability 기여**: AI 엔진 교체 시 `MLInferenceEngineAdapter`만 수정하면 되도록 인터페이스화.

#### [Messages 17-21] 판독 결과 저장 및 Ack
- `AITaskAnalysisConsumer → HelperJpaRepository` (Message 17): `saveAnalysis(taskId, label, confidence)` 호출.
- `HelperJpaRepository → Helper DB` (Message 18): `UPDATE helper_tasks SET ai_result = ?, ai_confidence = ?, analyzed_at = NOW()`.
- 응답 후 (Messages 19-21) RabbitMQ `ACK` 전송. 저장 성공 시에만 ACK하여 한 번 이상 처리(at-least-once)를 보장한다.

---

## 3. Quality Attribute Achievement

### 3.1 QAS-05 Availability – “주요 서비스 자동 복구 시간 보장”
- **Message Queue 기반 복원력**: ACK는 DB 업데이트 이후에만 수행하므로, 장애 발생 시 메시지는 재큐잉되어 다른 워커가 계속 처리할 수 있다.
- **Idempotent Update**: `UPDATE ... WHERE task_id = ?` 구조로 동일 메시지가 중복 처리되어도 최종 상태가 동일하게 유지된다.
- **Retry & Backoff**: `RabbitMQAdapter`는 네트워크 오류 및 AI 서비스 오류 시 최대 3회 재시도, 실패 시 Dead Letter Queue로 이동하여 운영자가 확인한다.
- **Fallback Label**: AI 추론 실패 또는 confidence < 임계치(예: 0.65)인 경우 `불분명`으로 저장하여 UC-14가 수동 검수로 이어질 수 있게 한다 (행동 설명 4.1 참조).

### 3.2 QAS-06 Modifiability – “AI 모델 교체 및 재학습의 지속적 적용성 보장”
- **Adapter 패턴**: `IMLInferenceEngine` 인터페이스와 `MLInferenceEngineAdapter`를 통해 내부 ML 엔진을 gRPC, HTTP 등 다른 구현으로 쉽게 교체 가능.
- **헬퍼 서비스 독립성**: Helper Service는 결과만 저장하며, 모델 변경/배포는 UC-24 (MLOps)에서 관리함으로써 서비스 간 결합도를 최소화한다.
- **Telemetry 저장**: `Helper DB`에는 `ai_confidence`, `analyzed_at` 등 메타데이터를 함께 기록하여 MLOps 파이프라인에서 추후 분석 및 재학습 트리거(UC-24)로 활용할 수 있다.

---

## 4. Exception Handling & Alternative Flows

### 4.1 ML Inference Failure (네트워크 오류 혹은 5xx)
1. **Messages 11-14**에서 오류 발생 시 `MLInferenceEngineAdapter`가 Circuit Breaker(Resilience4j)로 실패를 감지.
2. 실패 횟수가 임계치에 도달하면 `AITaskAnalysisConsumer`는 `HelperRepo.saveAnalysis()`를 호출하여 `ai_result = '불분명'`, `ai_confidence = 0`으로 저장하고 ACK 처리.
3. 동시에 실패 이벤트가 Dead Letter Queue로 전송되어 운영자에게 알림 (Ops Dashboard).

### 4.2 S3 다운로드 실패
1. `S3PhotoStorage`가 404 또는 Timeout을 반환할 경우 메시지는 `NACK(requeue=true)`되어 재시도된다.
2. 3회 실패 시 `TaskSubmittedEvent`는 Dead Letter Queue로 이동, 헬퍼에게 “재업로드 필요” 알림(UC-12 Alternative Flow)이 트리거된다.

### 4.3 데이터 미존재
- DB 조회 결과가 없을 경우 (Messages 3-6), Consumer는 즉시 ACK하지 않고 이벤트를 재큐잉하여 eventual consistency가 유지되도록 한다. UC-12 트랜잭션이 커밋되기 이전에 메시지가 도착하는 rare case에 대한 보호 장치이다.

---

## 5. Deployment & Runtime Considerations
- **Helper Service Worker Pods**: Kubernetes HPA 기준 2~6 replicas, 각 워커는 최대 10개의 concurrency(consumer prefetch)로 처리.
- **RabbitMQ**: Quorum Queue 구성 (3 nodes)으로 메시지 내구성 보장.
- **S3 버킷**: AWS S3 Standard, 객체 키 패턴 `tasks/{yyyy}/{MM}/{taskId}.jpg`.
- **ML Inference Engine**: Dockerized 서비스 (GPU 노드), REST API `/api/v1/infer`, 평균 응답 150ms, 타임아웃 3초.

---

## 6. Monitoring & Observability
- **Metrics**: `task.analysis.duration`, `ml.inference.latency`, `task.analysis.retry.count` Prometheus에 수집.
- **Logs**: `AITaskAnalysisConsumer`는 `taskId`, `label`, `confidence`, `modelVersion`, `processingTime`을 구조화 로그(JSON)로 남긴다.
- **Alerts**: Dead Letter Queue 누적 메시지 ≥ 10건, ML inference 실패율 ≥ 5% 이상 시 Slack 알림.

---

## 7. Conclusion
UC-13은 이벤트 기반 워커와 어댑터 구조를 활용하여 AI 판독을 안정적으로 수행한다. QAS-05 요구사항을 만족하기 위해 메시지 재처리, Circuit Breaker, Dead Letter Queue를 적용했고, QAS-06을 위해 ML 엔진과의 결합도를 낮추고 메타데이터를 축적하여 향후 UC-24 재학습 시 활용할 수 있도록 설계되었다. 이로써 헬퍼의 작업 업로드(UC-12)부터 지점주 검수(UC-14)까지 이어지는 AI 기반 세탁물 관리 플로우가 견고하게 지원된다.
