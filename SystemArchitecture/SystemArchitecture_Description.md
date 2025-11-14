# Smart Fitness Management System - Overall Architecture

## 1. 시스템 개요

스마트 피트니스 관리 시스템은 **24시간 무인 운영**과 **AI 기반 서비스**를 제공하는 헬스장 통합 관리 플랫폼입니다. **하이브리드 MSA (4-Layer 분산 서비스 아키텍처)**를 채택하여 높은 성능, 가용성, 보안, 변경성을 동시에 달성합니다.

### 핵심 특징
- **무인 출입 관리**: 안면 인식 기반 2초 이내 출입 인증 (QAS-02)
- **AI 작업 판독**: 세탁물 작업 자동 분석 및 보상 지급
- **자연어 검색**: 3초 이내 지점 검색 응답 (QAS-03)
- **실시간 모니터링**: 15초 이내 설비 고장 감지 및 알림 (QAS-01)
- **지속적 학습**: 무중단 AI 모델 재학습 및 배포 (QAS-06)

## 2. 4-Layer 아키텍처 구조

시스템은 품질 속성(Quality Attributes)에 따라 **책임 기반**으로 4개의 계층으로 분리됩니다.

### 2.1 Infrastructure Layer (인프라 계층)

**목적**: 모든 클라이언트 요청의 단일 진입점 및 공통 횡단 관심사 처리

**주요 컴포넌트**:
- **API Gateway**: 
  - Request Router: 서비스 라우팅 및 부하 분산
  - JWT Validator: 토큰 기반 인증/인가
  - Rate Limiter: 과부하 방지 (Resilience4j)
  - Circuit Breaker: 장애 전파 차단
  - Load Balancer: 서비스 인스턴스 간 부하 분산
  - Service Discovery: Eureka 기반 서비스 등록/발견

**클라이언트**:
- Customer App: 고객 모바일 앱 (안면 출입, 지점 검색, 리뷰 등록)
- Helper App: 헬퍼 모바일 앱 (작업 등록, 보상 조회)
- Branch Manager App: 지점주 앱 (작업 검수, 지점 관리, 알림 수신)
- Operations Center: 운영팀 대시보드 (시스템 모니터링, MLOps 관리)

**통신 프로토콜**: HTTPS (외부), HTTP (내부)

### 2.2 Real-Time Access Layer (실시간 접근 계층)

**목적**: 초저지연 출입 인증 처리 (QAS-02: < 3초)

**주요 서비스**:

#### Access Service
- **책임**: 안면 인식 및 QR 코드 기반 출입 인증
- **핵심 컴포넌트**:
  - Access Control Controller: 안면 인식 요청 처리
  - QR Access Controller: QR 코드 인증
  - Authorization Manager: 출입 권한 검증
  - Gate Controller: 게이트 개폐 제어
  - Face Vector Cache: 안면 벡터 캐싱 (DD-05: Data Pre-Fetching)
- **데이터베이스**: Vector DB (안면 벡터, 출입 로그)

#### FaceModel Service
- **책임**: 안면 벡터 비교 및 유사도 계산
- **핵심 컴포넌트**:
  - Face Model IPC Handler: IPC/gRPC 인터페이스
  - Vector Comparison Engine: 병렬 벡터 매칭 (DD-05: Pipeline Optimization)
  - Feature Extractor: ML 기반 특징점 추출
  - Model Lifecycle Manager: 모델 Hot Swap 관리 (AtomicReference)
- **데이터베이스**: Vector DB (모델 메타데이터)

**성능 최적화 (DD-05)**:
- **Same Physical Node**: Access Service와 FaceModel Service를 동일 물리 노드에 배치
- **IPC/gRPC**: HTTP 대비 10-20ms 지연 감소
- **Data Pre-Fetching**: 상위 10,000개 안면 벡터를 메모리 캐싱 (90%+ 히트율)
- **Pipeline Optimization**: CompletableFuture 기반 병렬 특징점 추출 (49% 지연 감소)

**달성 목표**: 평균 응답 시간 ~230ms << 3초 (QAS-02) ✓

### 2.3 Business Logic Layer (비즈니스 로직 계층)

**목적**: 핵심 비즈니스 기능 제공 및 독립적 확장

**주요 서비스**:

#### Auth Service
- **책임**: 사용자 인증/인가, 회원 가입
- **핵심 기능**:
  - JWT 기반 토큰 발급 및 검증
  - 신용카드 본인 인증 (외부 연동)
  - 안면 사진 등록 및 벡터 생성
  - 사용자 권한 관리
- **데이터베이스**: Auth DB (사용자 정보, 안면 벡터)
- **외부 연동**: Credit Card Verification Service (HTTPS)

#### Helper Service
- **책임**: 헬퍼 작업 관리 및 보상 지급
- **핵심 기능**:
  - 작업 사진 등록 및 AI 판독 요청
  - 일일 작업 한도 검증
  - 보상 잔고 관리 및 갱신
  - AI 판독 결과 수신 (이벤트 구독)
- **데이터베이스**: Helper DB (작업 정보, 보상 내역)
- **이벤트**: TaskSubmittedEvent 발행, TaskConfirmedEvent 구독

#### Search Service
- **책임**: 자연어 지점 검색 및 콘텐츠 관리
- **핵심 기능**:
  - **Hot Path**: 실시간 지점 검색 (< 3초, QAS-03)
    - Simple Tokenizer + ElasticSearch (LLM 미사용)
  - **Cold Path**: 리뷰/지점 정보 인덱싱 (비동기)
    - LLM 기반 키워드 추출 (외부 연동)
  - 맞춤형 지점 추천 (성향 분석)
- **데이터베이스**: Search DB + ElasticSearch Cluster (DD-06)
- **외부 연동**: Commercial LLM Service (HTTPS, Cold Path만)
- **DD-09**: Hot/Cold Path 분리로 SLA 보장

#### BranchOwner Service
- **책임**: 지점 정보 관리 및 작업 검수
- **핵심 기능**:
  - 지점 정보 등록/수정
  - 작업 검수 및 컨펌
  - 고객 리뷰 조회
  - 지점 성향 데이터 생성
- **데이터베이스**: Branch DB (지점 정보, 검수 내역)
- **이벤트**: BranchInfoCreatedEvent 발행

#### Monitoring Service
- **책임**: 설비 상태 모니터링 및 장애 탐지
- **핵심 기능**:
  - **Heartbeat**: 설비가 10분마다 상태 보고 (DD-04)
  - **Ping/echo**: 시스템이 10초마다 능동 점검 (DD-04)
  - 장애 탐지 (30초 무응답 시)
  - 장애 알림 발송 (< 15초, QAS-01)
- **데이터베이스**: Monitor DB (설비 상태, 감사 로그)
- **이벤트**: EquipmentFaultEvent 발행
- **통신**: 
  - Equipment → Monitoring: TCP (Heartbeat)
  - Monitoring → Equipment: HTTPS (Ping/echo)

#### Notification Service
- **책임**: 푸시 알림 발송
- **핵심 기능**:
  - 이벤트 기반 알림 발송 (EquipmentFaultEvent 등)
  - FCM Push Notification 전송
  - 알림 이력 관리
- **외부 연동**: FCM Push Gateway (HTTPS)
- **이벤트**: 다양한 이벤트 구독 (EquipmentFaultEvent, BranchPreferenceCreatedEvent 등)

**공통 특징**:
- **DD-03**: Database per Service (각 서비스가 독립 DB 소유)
- **DD-02**: Message-Based Communication (RabbitMQ를 통한 이벤트 기반 통신)
- **느슨한 결합**: 서비스 간 직접 호출 최소화
- **독립적 배포**: 각 서비스 별도 배포 및 확장 가능

### 2.4 AI Pipeline Layer (AI 파이프라인 계층)

**목적**: AI 모델 학습/배포 자동화 및 무중단 모델 교체 (QAS-06)

**주요 서비스**:

#### MLOps Service
- **책임**: AI 모델 재학습 및 배포 관리
- **핵심 컴포넌트**:
  - Training Controller: 재학습 트리거 API
  - Deployment Controller: 모델 배포 관리 API
  - Training Pipeline Orchestrator: 학습 파이프라인 조율
  - Data Collector: 학습 데이터 수집 (DD-03 예외: READ-ONLY)
  - Model Verification Service: 모델 정확도/성능 검증
  - Deployment Service: 모델 배포 및 Hot Swap
- **데이터베이스**: 
  - Model DB (모델 버전, 메타데이터)
  - Training Data Store (학습 데이터셋)
- **외부 접근**: 
  - Helper DB (READ-ONLY): 세탁물 작업 데이터
  - Auth DB (READ-ONLY): 안면 인증 데이터
- **이벤트**: 
  - TaskConfirmedEvent 구독 (재학습 트리거)
  - ModelDeployedEvent 발행

#### ML Inference Engine
- **책임**: 실제 ML 모델 훈련 및 추론
- **핵심 컴포넌트**:
  - Model Server: 모델 서빙
  - Training Engine: TensorFlow/PyTorch 기반 훈련
  - Model Registry: 모델 가중치 저장소
- **통신**: MLOps Service와 Local 통신

**무중단 배포 (QAS-06)**:
- **Runtime Binding (Hot Swap)**: AtomicReference로 모델 교체 (< 1ms)
- **gRPC to FaceModel Service**: 신규 모델을 FaceModel Service에 배포
- **Zero Downtime**: 서비스 중단 0초
- **Rollback**: 1분 이내 이전 버전 복구 가능

**학습 파이프라인**:
```
Trigger (수정 데이터 100건+)
   ↓
Data Collection (READ-ONLY from Helper/Auth DB)
   ↓
Data Preparation (Augmentation, Split)
   ↓
Model Training (2-4시간, Background)
   ↓
Model Verification (Accuracy + Performance)
   ↓
Model Deployment (< 1분)
   ↓
Hot Swap to FaceModel Service (< 1ms)
```

### 2.5 Persistence Layer (영속성 계층)

**목적**: 메시지 영속화 및 비동기 통신 보장

**주요 컴포넌트**:

#### RabbitMQ Message Broker
- **책임**: 이벤트 기반 비동기 통신 중개
- **프로토콜**: AMQP
- **핵심 이벤트**:
  - `TaskSubmittedEvent`: 작업 등록 → AI 분석 트리거
  - `TaskConfirmedEvent`: 작업 검수 완료 → 재학습 트리거, 보상 갱신
  - `EquipmentFaultEvent`: 설비 고장 감지 → 알림 발송
  - `BranchPreferenceCreatedEvent`: 지점 성향 생성 → 맞춤 알림
  - `ModelDeployedEvent`: 모델 배포 완료 → 모니터링 시작
- **장점**:
  - 느슨한 결합 (Publisher와 Subscriber 독립)
  - 메시지 영속화 (손실 방지)
  - 보장된 전달 (at-least-once delivery)
  - 확장성 (다중 Consumer 지원)

#### ElasticSearch Cluster
- **책임**: 전문 검색 엔진 (DD-06)
- **사용 서비스**: Search Service (Hot Path)
- **기능**:
  - Full-text Search (역색인)
  - TF-IDF, BM25 알고리즘
  - 퍼지 매칭 (오타 허용)
  - 실시간 색인 업데이트
- **성능**: ~50ms 검색 응답 (QAS-03 달성에 기여)

## 3. 통신 프로토콜

| 프로토콜 | 용도 | 사용 구간 |
|---------|------|----------|
| **HTTPS** | 외부 클라이언트 통신 | Client ↔ API Gateway, External APIs |
| **HTTP** | 내부 RESTful API | API Gateway ↔ Services |
| **IPC/gRPC** | 고성능 서비스 간 통신 | Access ↔ FaceModel (Same Node) |
| **AMQP** | 비동기 메시지 기반 통신 | Services ↔ RabbitMQ Broker |
| **JDBC** | 데이터베이스 접근 | Services ↔ Databases |
| **TCP** | 설비 Heartbeat | Equipment ↔ Monitoring |
| **Local** | 로컬 라이브러리 호출 | FaceModel/MLOps ↔ ML Inference Engine |

## 4. 외부 시스템 연동

### 4.1 Branch Equipment (지점 설비)
- **Camera**: 안면 사진 촬영 및 전송 (HTTPS → Access Service)
- **Gate Controller**: 게이트 개폐 제어 (HTTPS ← Access Service)
- **IoT Sensors**: 설비 상태 보고 (TCP → Monitoring Service)

### 4.2 External Partners
- **Credit Card Verification Service**: 신용카드 본인 인증 (Auth Service 연동)
- **Commercial LLM Service**: 자연어 분석 (Search Service Cold Path만 사용)
- **FCM Push Gateway**: 모바일 푸시 알림 (Notification Service 연동)

## 5. 품질 속성 달성 전략

### 5.1 Performance (QAS-02, QAS-03)
- **Real-Time Access Layer**: IPC/gRPC, Data Pre-Fetching, Pipeline Optimization
- **Search Service**: Hot/Cold Path 분리, ElasticSearch, LLM 제거 (Hot Path)
- **측정 결과**:
  - 안면 인증: ~230ms << 3초 ✓
  - 지점 검색: ~70ms << 3초 ✓

### 5.2 Availability (QAS-01, QAS-05)
- **Two-Level Fault Detection**: Heartbeat + Ping/echo (DD-04)
- **Circuit Breaker**: API Gateway 레벨에서 장애 전파 차단
- **Passive Redundancy**: 이벤트 기반 알림 (Message Broker)
- **Auto-scaling**: Kubernetes HPA로 자동 확장
- **측정 결과**:
  - 장애 감지 및 알림: P95 < 15초 ✓

### 5.3 Security (QAS-04)
- **JWT 기반 인증**: API Gateway에서 토큰 검증
- **HTTPS 암호화**: 모든 외부 통신
- **Maintain Audit Trail**: 모든 중요 작업 로그 기록
- **Role-Based Access Control**: 사용자 권한 분리
- **READ-ONLY Access**: MLOps의 DB 접근 제한 (DD-03 예외)

### 5.4 Modifiability (QAS-06)
- **Runtime Binding (Hot Swap)**: AtomicReference로 무중단 모델 교체
- **Event-Driven Architecture**: 느슨한 결합으로 서비스 독립성
- **Database per Service**: 데이터 스키마 독립적 변경
- **Encapsulate**: ML 프레임워크 캡슐화
- **측정 결과**:
  - 모델 배포: < 1분 ✓
  - 서비스 다운타임: 0초 ✓

## 6. 배포 전략

### 6.1 Containerization
- **Docker**: 모든 서비스 컨테이너화
- **Kubernetes**: 오케스트레이션 및 자동 배포
- **Helm Charts**: 배포 템플릿 관리

### 6.2 Service Instances
- **API Gateway**: 3+ replicas (HA)
- **Access Service**: 2+ replicas + co-located with FaceModel
- **FaceModel Service**: 2+ replicas (GPU optional)
- **Business Services**: 2-3 replicas each
- **MLOps Service**: 1-2 replicas (GPU required)
- **Message Broker**: 3-node cluster (HA)

### 6.3 Physical Node Strategy
- **Real-Time Access Node**: Access + FaceModel co-located (DD-05)
- **Business Logic Nodes**: Standard compute instances
- **AI Pipeline Node**: GPU-enabled instances (NVIDIA T4 × 2)
- **Database Nodes**: RDS/Cloud SQL (managed services)

## 7. 모니터링 및 운영

### 7.1 Monitoring Stack
- **Prometheus**: 메트릭 수집
- **Grafana**: 대시보드 시각화
- **ELK Stack**: 로그 집계 및 검색
- **Jaeger**: 분산 추적 (Distributed Tracing)

### 7.2 Key Metrics
- **Performance**: Latency (P50, P95, P99), Throughput (TPS)
- **Availability**: Uptime, Error Rate, Circuit Breaker Status
- **Business**: User Registrations, Access Events, Task Confirmations, Model Accuracy

## 8. 확장성

### 8.1 Horizontal Scaling
- **Stateless Services**: 모든 비즈니스 로직 서비스는 상태 비저장으로 설계
- **Load Balancing**: API Gateway의 자동 부하 분산
- **Auto-scaling**: Kubernetes HPA (CPU/Memory 기반)

### 8.2 Database Scaling
- **Read Replicas**: 읽기 부하 분산
- **Sharding**: ElasticSearch 샤딩
- **Connection Pooling**: HikariCP로 연결 효율화

### 8.3 Message Broker Scaling
- **Cluster Mode**: RabbitMQ 3-node cluster
- **Queue Partitioning**: 트래픽 유형별 큐 분리
- **Consumer Scaling**: 다중 Consumer 인스턴스

## 9. 비용 최적화

- **Spot Instances**: MLOps 훈련용 GPU (70% 절감)
- **Auto-scaling**: 피크/비피크 시간대 자동 조절
- **Cold Path**: LLM API 사용을 Hot Path에서 제거 (비용 대폭 절감)
- **Caching**: Redis/ElasticSearch로 DB 부하 감소

## 10. 보안 강화

- **Network Segmentation**: VPC, Security Groups
- **Private Subnets**: DB, Message Broker는 Private Network
- **SSL/TLS**: 모든 외부 통신 암호화
- **Secrets Management**: AWS Secrets Manager / Vault
- **Regular Audits**: 감사 로그 자동 분석

## 11. 결론

Smart Fitness Management System은 **4-Layer Hybrid MSA**를 통해:

1. **고성능**: Real-Time Access Layer로 초저지연 출입 인증 (~230ms)
2. **고가용성**: Two-Level Fault Detection으로 15초 이내 장애 감지
3. **확장성**: Database per Service로 독립적 확장
4. **변경성**: Hot Swap으로 무중단 AI 모델 배포
5. **보안**: JWT, HTTPS, Audit Trail로 포괄적 보안

이러한 아키텍처를 통해 **24시간 무인 운영**이 가능한 안정적이고 지능적인 헬스장 관리 플랫폼을 제공합니다.

