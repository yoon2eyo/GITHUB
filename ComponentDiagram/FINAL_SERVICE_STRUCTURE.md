# 스마트 피트니스 시스템 최종 서비스 구조

**확정 날짜**: 2025-11-11
**검증 기준**: UC 24개 + QAS 6개 완전 커버

---

## 🎯 최종 서비스 구조 (11개)

### **1. API Gateway (Entry Point)**
```
서비스명: RequestRouter
역할: 라우팅, 보안, 로드밸런싱, Rate Limiting
Layer: Entry Point
Port: HTTPS 443
```

**담당 기능**:
- 모든 클라이언트 요청의 진입점
- 인증 토큰 검증
- 서비스 라우팅
- Circuit Breaker, Rate Limiting

**관련 DD**: DD-01 (Entry Point), DD-09 (보안)

---

### **2. Auth Service**
```
서비스명: AuthenticationManager
역할: 인증, 권한, 회원가입
Layer: Business Logic Layer
DB: DB_AUTH
```

**담당 UC**:
- UC-01: 고객 계정 등록
- UC-02: 헬퍼 계정 등록
- UC-03: 지점주 계정 등록
- UC-04: 로그인
- UC-05: 본인 인증 수행
- UC-06: 안면 사진 등록

**외부 연동**:
- ICreditCardVerificationService (본인 인증)
- FaceModel Service (안면 벡터 생성)

**관련 DD**: DD-03 (Database per Service), DD-09 (보안)

---

### **3. Access Service**
```
서비스명: AccessAuthorizationManager
역할: 실시간 출입 제어
Layer: Real-Time Access Layer
DB: DB_VECTOR
```

**담당 UC**:
- UC-07: 안면인식 출입 인증
- UC-08: QR코드 수동 출입
- UC-22: 게이트 개방 실행

**핵심 컴포넌트**:
- FaceVectorCache (Data Pre-Fetching)
- GateController
- AccessEventProcessor

**외부 연동**:
- FaceModel Service (IPC/gRPC) - **동일 노드**
- Equipment (Gate Control)

**성능 목표**: 3초 이내 출입 (QAS-02)

**관련 DD**: DD-05 (IPC 최적화, Pre-Fetching)

---

### **4. FaceModel Service**
```
서비스명: VectorComparisonEngine
역할: 안면 벡터 비교 (초저지연)
Layer: Real-Time Access Layer
DB: ModelMetadataDB
```

**담당 UC**:
- UC-06: 안면 사진 등록 (벡터 생성)
- UC-07: 안면인식 출입 인증 (벡터 비교)

**핵심 컴포넌트**:
- VectorComparisonEngine (Pipeline Optimization)
- ModelLifecycleManager (Hot Swap)
- FeatureExtractor

**성능 최적화**:
- CompletableFuture 병렬 처리
- Sequential: 405ms → Parallel: 205ms (49% 개선)

**외부 연동**:
- MLInferenceEngine (모델 추론)
- Access Service (IPC) - **동일 노드**

**관련 DD**: DD-05 (Pipeline Optimization, Hot Swap)

---

### **5. Helper Service**
```
서비스명: TaskManagementManager
역할: 작업 관리, 보상 처리
Layer: Business Logic Layer
DB: DB_HELPER
Storage: S3 (세탁물 사진)
```

**담당 UC**:
- UC-12: 작업 사진 등록
- UC-13: AI 세탁물 작업 1차 판독 (Consumer)
- UC-16: 보상 잔고 갱신 (Consumer)
- UC-17: 보상 잔고 조회

**핵심 컴포넌트**:
- TaskSubmissionManager
- AITaskAnalysisConsumer (비동기)
- RewardUpdateConsumer (비동기)
- DailyLimitValidator (3회/일)

**이벤트 흐름**:
1. Helper uploads → TaskSubmittedEvent
2. AITaskAnalysisConsumer → MLInferenceEngine
3. BranchOwner confirms → TaskConfirmedEvent
4. RewardUpdateConsumer → Balance update

**관련 DD**: DD-02 (비동기 통신), DD-03 (Database per Service)

---

### **6. Search Service**
```
서비스명: BranchContentService
역할: 검색, 리뷰, 맞춤형 알림
Layer: Business Logic Layer
DB: SearchEngineDB (ElasticSearch)
```

**담당 UC**:
- UC-09: 자연어 지점 검색 (Hot Path)
- UC-10: 고객 리뷰 등록 (Cold Path)
- UC-11: 맞춤형 알림 발송 (Consumer)

**Hot/Cold Path 분리** (DD-06, DD-09):

**Hot Path** (실시간 검색):
```
Query → SimpleKeywordTokenizer → SearchEngine → Results
(NO LLM! 3초 이내 보장)
```

**Cold Path** (콘텐츠 등록):
```
Content → LLM Analysis → Index → BranchPreferenceCreatedEvent
```

**핵심 컴포넌트**:
- SearchQueryManager (Hot Path)
- ContentRegistrationManager (Cold Path)
- PreferenceMatchConsumer (Scheduling Policy)

**외부 연동**:
- ILLMAnalysisService (외부 LLM) - Cold Path만

**성능 목표**: 95% 응답 3초 이내 (QAS-03)

**관련 DD**: DD-06 (Hot/Cold Path), DD-07 (Scheduling Policy), DD-09 (Approach 3)

---

### **7. BranchOwner Service**
```
서비스명: BranchOwnerManager
역할: 지점 관리, 작업 검수
Layer: Business Logic Layer
DB: DB_BRANCH
```

**담당 UC**:
- UC-14: 세탁물 작업 결과 검수/컨펌
- UC-15: 세탁물 판독 결과 수정
- UC-18: 지점 정보 등록
- UC-19: 고객 리뷰 조회

**핵심 컴포넌트**:
- BranchOwnerManager
- BranchInfoValidator
- RewardConfirmationManager

**이벤트 발행**:
- BranchInfoCreatedEvent (UC-18 → UC-11 trigger)
- TaskConfirmedEvent (UC-14 → UC-16 trigger)

**관련 DD**: DD-02 (이벤트 기반), DD-03 (Database per Service)

---

### **8. Monitoring Service**
```
서비스명: StatusReceiverManager
역할: 설비 모니터링, 고장 감지
Layer: Business Logic Layer
DB: DB_MONITOR
```

**담당 UC**:
- UC-20: 설비 상태 보고
- UC-21: 설비 상태 모니터링

**고장 감지 메커니즘** (DD-04):

**Path 1 - Heartbeat**:
```
Equipment (10분마다) → Monitor → EquipmentFaultEvent
```

**Path 2 - Ping/echo**:
```
Timer (10초마다) → Check (30초 미보고?) → Ping → No Response? → EquipmentFaultEvent
```

**핵심 컴포넌트**:
- HeartbeatReceiver
- EquipmentHealthChecker
- PingEchoExecutor
- FaultDetector

**성능 목표**: 15초 이내 알림 (QAS-01)

**관련 DD**: DD-04 (Heartbeat + Ping/echo)

---

### **9. Notification Service**
```
서비스명: NotificationDispatcherConsumer
역할: 알림 발송
Layer: Business Logic Layer
```

**담당 UC**:
- UC-11: 맞춤형 알림 발송 (성향 매칭)
- UC-20/21: 설비 고장 알림

**이벤트 구독**:
- EquipmentFaultEvent → BranchOwner Alert
- BranchPreferenceCreatedEvent → Customer Alert

**외부 연동**:
- IPushNotificationGateway (FCM, APNS)

**관련 DD**: DD-02 (비동기), DD-04 (고장 알림)

---

### **10. MLOps Service**
```
서비스명: MLOpsTrainingService
역할: AI 모델 학습/배포
Layer: AI Pipeline Layer
DB: ModelDatabase, TrainingDataStore
```

**담당 UC**:
- UC-23: 안면인식 모델 재학습
- UC-24: 세탁물 모델 재학습

**핵심 컴포넌트**:
- TrainingPipelineOrchestrator
- DataCollector (READ-ONLY 접근)
- DeploymentService
- ModelVerificationService

**데이터 접근** (DD-03 예외):
```
Auth DB → READ-ONLY (facial vectors)
Helper DB → READ-ONLY (confirmed laundry images)
```

**모델 배포**:
```
Training → Verification → Deploy → MLInferenceEngine → FaceModel (Hot Swap)
```

**관련 DD**: DD-03 (READ-ONLY 예외), DD-05 (Hot Swap), QAS-06 (무중단 배포)

---

### **11. MLInferenceEngine**
```
서비스명: MLInferenceEngine (Internal ML Platform)
역할: ML 추론 실행
Layer: AI Pipeline Layer
```

**담당 기능**:
- UC-06: 안면 벡터 생성
- UC-07: 안면 벡터 비교
- UC-13: 세탁물 사진 분석

**제공 API**:
```java
interface IMLInferenceEngine {
    FeatureVector extractFeatures(byte[] imageData);
    AnalysisResult analyzeImage(String photoUrl);
    ModelMetrics getModelMetrics();
    DeploymentResult deployModel(String version);
    RollbackResult rollbackModel(String version);
}
```

**사용 서비스**:
- FaceModel Service (안면 인식)
- Helper Service (세탁물 판독)
- MLOps Service (모델 배포)

**관련 DD**: DD-05 (Pipeline Optimization), QAS-06 (Hot Swap)

---

## 🏗️ Infrastructure Components (2개)

### **12. Message Broker**
```
구현: RabbitMQ
역할: 비동기 이벤트 허브
Layer: Persistence Layer
```

**핵심 이벤트**:
- TaskSubmittedEvent
- TaskConfirmedEvent
- BranchPreferenceCreatedEvent
- EquipmentFaultEvent
- BranchInfoCreatedEvent

**관련 DD**: DD-02 (Message Based), DD-04 (Passive Redundancy)

---

### **13. Search Engine**
```
구현: ElasticSearch (DS-07)
역할: 전문 검색 엔진
Layer: Persistence Layer
```

**저장 데이터**:
- 지점 정보 (BranchInfo)
- 고객 리뷰 (Review)
- 지점 성향 데이터 (Preference)

**관련 DD**: DD-06 (Hot Path 성능 보장)

---

## 📊 서비스별 UC 매핑 요약

| 서비스 | 담당 UC 개수 | UC 목록 |
|--------|-------------|---------|
| Auth | 6 | UC-01~06 |
| Access | 3 | UC-07, UC-08, UC-22 |
| FaceModel | 2 | UC-06, UC-07 |
| Helper | 4 | UC-12, UC-13, UC-16, UC-17 |
| Search | 3 | UC-09, UC-10, UC-11 |
| BranchOwner | 4 | UC-14, UC-15, UC-18, UC-19 |
| Monitoring | 2 | UC-20, UC-21 |
| Notification | 2 | UC-11, UC-20/21 |
| MLOps | 2 | UC-23, UC-24 |
| MLInferenceEngine | 3 | UC-06, UC-07, UC-13 |
| API Gateway | 24 | All UCs (Entry) |

**총 UC**: 24개 (100% 커버) ✅

---

## 🎯 QAS 지원 매트릭스

| QAS | 목표 | 지원 서비스 | 핵심 Tactics |
|-----|------|------------|--------------|
| QAS-01 | 고장 15초 알림 | Monitoring + Notification | Heartbeat, Ping/echo, Passive Redundancy |
| QAS-02 | 출입 3초 | Access + FaceModel | IPC, Pre-Fetching, Concurrency |
| QAS-03 | 검색 3초 | Search | Hot/Cold Path, NO LLM in Hot |
| QAS-04 | 보안 암호화 | Auth + Access | Encrypt Data, Limit Access |
| QAS-05 | 5분 복구 | All Services | Passive Redundancy, Message Broker |
| QAS-06 | 무중단 배포 | FaceModel + MLOps | Runtime Binding, Hot Swap |

**총 QAS**: 6개 (100% 지원) ✅

---

## 🏛️ 4-Layer 아키텍처 (DD-01)

```
┌─────────────────────────────────────────────────┐
│         Entry Point (Public Network)            │
│  • API Gateway (RequestRouter)                  │
└─────────────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────┐
│      Real-Time Access Layer (DD-05)             │
│  • Access Service (초저지연 출입)                │
│  • FaceModel Service (IPC 최적화)               │
└─────────────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────┐
│         Business Logic Layer                    │
│  • Auth Service                                 │
│  • Helper Service                               │
│  • Search Service (Hot/Cold Path)               │
│  • BranchOwner Service                          │
│  • Monitoring Service                           │
│  • Notification Service                         │
└─────────────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────┐
│         AI Pipeline Layer                       │
│  • MLOps Service (학습/배포)                    │
│  • MLInferenceEngine (추론)                     │
└─────────────────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────┐
│         Persistence Layer                       │
│  • Message Broker (RabbitMQ)                    │
│  • Search Engine (ElasticSearch)                │
│  • Databases (per Service)                      │
└─────────────────────────────────────────────────┘
```

---

## 📈 시스템 규모 목표

| 항목 | 규모 |
|------|------|
| **지점** | 100개 |
| **고객** | 10,000명 |
| **헬퍼** | 1,000명 |
| **지점주** | 100명 |
| **동시 출입** | 20 TPS (Peak) |
| **일일 검색** | 2,000건 |
| **일일 작업** | 300건 |

---

## ✅ 결론

**최종 서비스 구조: 11개 비즈니스 서비스 + 2개 인프라**

### 검증 완료:
- ✅ UC 24개 100% 커버
- ✅ QAS 6개 100% 지원
- ✅ DD-01 ~ DD-09 반영
- ✅ 중복 서비스 제거 (AI Service 삭제)
- ✅ 누락 서비스 없음

### 아키텍처 특징:
- ✅ MSA (Microservice Architecture)
- ✅ Event-Driven (Message Broker)
- ✅ 4-Layer 구조 (Real-Time, Business, AI, Persistence)
- ✅ Database per Service
- ✅ Hot/Cold Path 분리 (성능 최적화)
- ✅ IPC 최적화 (초저지연)

**Stub 코드 생성 준비 완료!** 🚀

