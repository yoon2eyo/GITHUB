# 4.3.5 & 4.3.6.1 Artifact Definition Diagram

## Overview

본 문서는 Smart Fitness Management System의 **Artifact Definition Diagram**을 설명합니다. 각 Node에 배포되는 모든 파일(Artifact)을 식별하고, Component와 Artifact 간의 매핑 관계를 정의하며, QA(성능, 가용성, 유지보수성 등)를 고려한 배치 전략을 제시합니다.

Artifact Definition Diagram은 **4개 그룹**으로 나누어 작성되었습니다:
1. **Application Services** (Kubernetes Cluster)
2. **Client Applications** (Mobile Apps, Admin Dashboard)
3. **Branch Equipment** (IoT Devices)
4. **Infrastructure Services** (Database, Cache, Message Broker)

---

## 1. Artifact Definition - Application Services

**파일**: `ArtifactDefinition_01_ApplicationServices.puml`

### 1.1 Artifact 목록 (Application Services)

| Node | Artifact Name | Type | Manifest Component | Size | QA Contribution |
|------|---------------|------|-------------------|------|-----------------|
| **Load Balancer** | nginx-ingress-controller.yaml | Configuration | Nginx Ingress Controller | < 1KB | Availability: L7 Load Balancing |
| | ingress-config.yaml | Configuration | - | < 1KB | Performance: Routing rules |
| | tls-certificates.pem | Certificate | - | < 10KB | Security (QAS-04): TLS 1.3 |
| **API Gateway** | api-gateway-service.jar | Executable JAR | API Gateway Service (All Components) | 50-100MB | Availability (QAS-05): Circuit Breaker |
| | application-gateway.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | jwt-public-key.pem | Certificate | - | < 5KB | Security (QAS-04): JWT verification |
| | spring-boot-3.2.4.jar | Library | - | 30MB | Performance: Reactive WebFlux |
| | spring-cloud-gateway-4.1.jar | Library | - | 10MB | Availability: Service Discovery |
| | resilience4j-2.1.0.jar | Library | - | 5MB | Availability: Circuit Breaker |
| **Access + FaceModel** | access-service.jar | Executable JAR | Access Service (6 Components) | 50-100MB | Performance (QAS-02): Co-located |
| | facemodel-service.jar | Executable JAR | FaceModel Service (4 Components) | 50-100MB | Performance (QAS-02): IPC/gRPC |
| | application-access.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | application-facemodel.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | grpc-java-1.58.0.jar | Library | - | 20MB | Performance: IPC < 10ms |
| | lettuce-6.2.jar | Library | - | 5MB | Performance: Redis Cache |
| **Auth Service** | auth-service.jar | Executable JAR | Auth Service (8 Components) | 50-100MB | Security (QAS-04): JWT issuance |
| | application-auth.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | spring-security-6.1.jar | Library | - | 15MB | Security: Authentication |
| | jjwt-0.12.3.jar | Library | - | 1MB | Security: JWT (RS256) |
| **Helper Service** | helper-service.jar | Executable JAR | Helper Service (7 Components) | 50-100MB | Modifiability: Task management |
| | application-helper.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | aws-s3-sdk-2.20.jar | Library | - | 20MB | Scalability: S3 upload |
| **Search Service** | search-service.jar | Executable JAR | Search Service (8 Components) | 50-100MB | Performance (QAS-03): Hot/Cold Path |
| | application-search.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | elasticsearch-client-8.10.jar | Library | - | 15MB | Performance: Full-text search |
| **BranchOwner Service** | branchowner-service.jar | Executable JAR | BranchOwner Service (5 Components) | 50-100MB | Modifiability: Branch management |
| | application-branchowner.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| **Monitoring Service** | monitoring-service.jar | Executable JAR | Monitoring Service (7 Components) | 50-100MB | Availability (QAS-01): Fault detection |
| | application-monitoring.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | quartz-scheduler-2.3.jar | Library | - | 5MB | Availability: Periodic checks |
| **Notification Service** | notification-service.jar | Executable JAR | Notification Service (4 Components) | 50-100MB | Availability (QAS-01): Push alerts |
| | application-notification.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | fcm-service-account.json | Credential | - | < 5KB | Security: FCM authentication |
| | firebase-admin-sdk-9.2.jar | Library | - | 10MB | Availability: FCM Push |
| **MLOps Service** | mlops-service.jar | Executable JAR | MLOps Service (9 Components) | 50-100MB | Modifiability (QAS-06): Model training |
| | ml-inference-engine.py | Python Script | ML Inference Engine | 10-50MB | Modifiability: Model inference |
| | application-mlops.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | model-weights-v1.0.pb | Model File | - | 100-500MB | Modifiability: Hot Swap model |
| | tensorflow-2.13.jar | Library | - | 200MB | Performance: GPU acceleration |
| | pytorch-2.1.jar | Library | - | 150MB | Performance: GPU acceleration |
| | cuda-12.2.so | Library | - | 500MB | Performance: GPU driver |
| | cudnn-8.9.so | Library | - | 200MB | Performance: DNN library |

### 1.2 Component to Artifact Mapping (Application Services)

| Component | Manifest Artifact | Deploy Node | Multiplicity |
|-----------|------------------|-------------|--------------|
| **API Gateway Service** (All Components) | api-gateway-service.jar | API Gateway Node | 3 replicas |
| **Access Service** (6 Components) | access-service.jar | Access Node | 2 replicas |
| **FaceModel Service** (4 Components) | facemodel-service.jar | Access Node (Co-located) | 2 replicas |
| **Auth Service** (8 Components) | auth-service.jar | Auth Service Node | 2 replicas |
| **Helper Service** (7 Components) | helper-service.jar | Helper Service Node | 2 replicas |
| **Search Service** (8 Components) | search-service.jar | Search Service Node | 3 replicas |
| **BranchOwner Service** (5 Components) | branchowner-service.jar | BranchOwner Service Node | 2 replicas |
| **Monitoring Service** (7 Components) | monitoring-service.jar | Monitoring Service Node | 2 replicas |
| **Notification Service** (4 Components) | notification-service.jar | Notification Service Node | 3 replicas |
| **MLOps Service** (9 Components) | mlops-service.jar | MLOps Service Node | 1-2 replicas |
| **ML Inference Engine** | ml-inference-engine.py | MLOps Service Node | 1-2 replicas |

### 1.3 QA-Driven Artifact Decisions (Application Services)

#### Performance (QAS-02, QAS-03)
- **Co-located Artifacts**: `access-service.jar` + `facemodel-service.jar` 동일 Pod 배치 → IPC/gRPC (< 10ms)
- **gRPC Library**: `grpc-java-1.58.0.jar` → Protobuf 직렬화 (JSON 대비 5-10배 빠름)
- **Redis Client**: `lettuce-6.2.jar` → Reactive Non-blocking I/O (Cache hit ~1ms)
- **ElasticSearch Client**: `elasticsearch-client-8.10.jar` → Full-text search (~50ms)

#### Availability (QAS-01, QAS-05)
- **Circuit Breaker Library**: `resilience4j-2.1.0.jar` → 장애 전파 차단
- **Scheduler Library**: `quartz-scheduler-2.3.jar` → 주기적 Heartbeat/Ping 체크
- **FCM SDK**: `firebase-admin-sdk-9.2.jar` → 푸시 알림 전송 (< 15초)
- **Multi-Replica**: 각 서비스 2-3 replicas → SPOF 제거

#### Modifiability (QAS-06)
- **Externalized Config**: `application-*.yml` → 환경별 설정 분리 (dev/staging/prod)
- **Hot Swap Model**: `model-weights-v1.0.pb` → gRPC로 무중단 교체 (< 1ms)
- **Separate Python Script**: `ml-inference-engine.py` → Java와 독립 배포

#### Security (QAS-04)
- **TLS Certificates**: `tls-certificates.pem` → TLS 1.3 암호화
- **JWT Public Key**: `jwt-public-key.pem` → RS256 서명 검증
- **FCM Credential**: `fcm-service-account.json` → 안전한 FCM 인증

---

## 2. Artifact Definition - Client Applications

**파일**: `ArtifactDefinition_02_ClientApplications.puml`

### 2.1 Artifact 목록 (Client Applications)

| Node | Artifact Name | Type | Manifest Component | Size | QA Contribution |
|------|---------------|------|-------------------|------|-----------------|
| **Customer Mobile Device** | customer-app.ipa | iOS App Bundle | Customer App (iOS) | 50-100MB | Usability: Cross-platform |
| | customer-app.apk | Android App Package | Customer App (Android) | 50-100MB | Usability: Cross-platform |
| | app-config.json | Configuration | - | < 5KB | Modifiability: Dynamic config |
| | tls-certificates.pem | Certificate | - | < 10KB | Security (QAS-04): Certificate Pinning |
| | react-native-0.72.js | Library | - | 30MB | Modifiability: Fast iteration |
| | axios-1.5.js | Library | - | 1MB | Performance: HTTP client |
| | fcm-sdk-9.2.js | Library | - | 5MB | Availability: Push notification |
| **Helper Mobile Device** | helper-app.ipa | iOS App Bundle | Helper App (iOS) | 50-100MB | Usability: Cross-platform |
| | helper-app.apk | Android App Package | Helper App (Android) | 50-100MB | Usability: Cross-platform |
| | app-config.json | Configuration | - | < 5KB | Modifiability: Dynamic config |
| | tls-certificates.pem | Certificate | - | < 10KB | Security (QAS-04): Certificate Pinning |
| | react-native-image-picker-5.6.js | Library | - | 2MB | Usability: Camera integration |
| **Manager Mobile Device** | manager-app.ipa | iOS App Bundle | Manager App (iOS) | 50-100MB | Usability: Cross-platform |
| | manager-app.apk | Android App Package | Manager App (Android) | 50-100MB | Usability: Cross-platform |
| | app-config.json | Configuration | - | < 5KB | Modifiability: Dynamic config |
| | tls-certificates.pem | Certificate | - | < 10KB | Security (QAS-04): Certificate Pinning |
| | fcm-device-token.txt | Credential | - | < 1KB | Availability (QAS-01): Push token |
| | fcm-sdk-9.2.js | Library | - | 5MB | Availability: Push notification |
| **Operations Workstation** | admin-dashboard.js | JavaScript Bundle | Admin Dashboard (React SPA) | 10-50MB | Modifiability: SPA (No install) |
| | admin-dashboard.html | HTML | - | < 10KB | Usability: Entry point |
| | admin-dashboard.css | CSS | - | < 100KB | Usability: Styling |
| | dashboard-config.json | Configuration | - | < 5KB | Modifiability: Dynamic config |
| | react-18.2.js | Library | - | 20MB | Modifiability: Component-based |
| | recharts-2.8.js | Library | - | 5MB | Usability: Data visualization |

### 2.2 Component to Artifact Mapping (Client Applications)

| Component | Manifest Artifact | Deploy Node | Multiplicity |
|-----------|------------------|-------------|--------------|
| **Customer App (iOS)** | customer-app.ipa | Customer Mobile Device | 10,000+ installs |
| **Customer App (Android)** | customer-app.apk | Customer Mobile Device | 10,000+ installs |
| **Helper App (iOS)** | helper-app.ipa | Helper Mobile Device | 1,000+ installs |
| **Helper App (Android)** | helper-app.apk | Helper Mobile Device | 1,000+ installs |
| **Manager App (iOS)** | manager-app.ipa | Manager Mobile Device | 1,000+ installs |
| **Manager App (Android)** | manager-app.apk | Manager Mobile Device | 1,000+ installs |
| **Admin Dashboard (React SPA)** | admin-dashboard.js | Operations Workstation | 5-10 users |

### 2.3 QA-Driven Artifact Decisions (Client Applications)

#### Security (QAS-04)
- **Certificate Pinning**: `tls-certificates.pem` → 중간자 공격 방지
- **TLS 1.3**: 모든 HTTPS 통신 암호화

#### Usability
- **Cross-platform**: React Native (iOS/Android 동시 지원)
- **SPA (Web)**: `admin-dashboard.js` → 별도 설치 불필요
- **Camera Integration**: `react-native-image-picker-5.6.js` → 네이티브 카메라 API

#### Availability (QAS-01)
- **FCM Push**: `fcm-sdk-9.2.js` → 설비 고장 알림 실시간 수신 (< 15초)
- **Device Token**: `fcm-device-token.txt` → Push 타겟팅

#### Modifiability
- **Dynamic Config**: `app-config.json` → API 엔드포인트 변경 시 앱 재배포 불필요
- **React Native**: Hot Reload, OTA Update 지원

---

## 3. Artifact Definition - Branch Equipment

**파일**: `ArtifactDefinition_03_BranchEquipment.puml`

### 3.1 Artifact 목록 (Branch Equipment)

| Node | Artifact Name | Type | Manifest Component | Size | QA Contribution |
|------|---------------|------|-------------------|------|-----------------|
| **Branch Equipment Node** | camera-controller.py | Python Script | Camera Controller | 1-10MB | Performance (QAS-02): Local preprocessing |
| | camera-config.json | Configuration | - | < 5KB | Modifiability: Camera settings |
| | api-endpoint.conf | Configuration | - | < 1KB | Modifiability: Access Service URL |
| | opencv-4.6.so | Library | - | 50MB | Performance: Image preprocessing |
| | requests-2.28.py | Library | - | 1MB | Performance: HTTPS upload |
| | pillow-10.0.py | Library | - | 5MB | Performance: Image compression |
| | gate-controller.cpp | C++ Executable | Gate Controller | 1-10MB | Performance (QAS-02): Real-time GPIO |
| | gate-config.json | Configuration | - | < 5KB | Modifiability: GPIO mapping |
| | flask-server.py | Python Script | - | 1-10MB | Availability: HTTP server |
| | wiringpi-2.61.so | Library | - | 1MB | Performance: GPIO control |
| | flask-2.3.py | Library | - | 5MB | Availability: HTTP server |
| | sensor-manager.py | Python Script | IoT Sensor Manager | 1-10MB | Availability (QAS-01): Heartbeat |
| | sensor-config.json | Configuration | - | < 5KB | Modifiability: Heartbeat interval |
| | local-buffer.db | SQLite Database | - | 1-100MB | Availability: Network outage buffer |
| | socket-3.11.py | Library | - | < 1MB | Availability: TCP Heartbeat |
| | sqlite3-3.42.py | Library | - | 1MB | Availability: Local buffer |
| | camera-controller.service | Systemd Service | - | < 1KB | Availability: Auto-restart |
| | gate-controller.service | Systemd Service | - | < 1KB | Availability: Auto-restart |
| | sensor-manager.service | Systemd Service | - | < 1KB | Availability: Auto-restart |

### 3.2 Component to Artifact Mapping (Branch Equipment)

| Component | Manifest Artifact | Deploy Node | Multiplicity |
|-----------|------------------|-------------|--------------|
| **Camera Controller** | camera-controller.py | Branch Equipment Node | 1,000+ devices |
| **Gate Controller** | gate-controller.cpp | Branch Equipment Node | 1,000+ devices |
| **IoT Sensor Manager** | sensor-manager.py | Branch Equipment Node | 1,000+ devices |

### 3.3 QA-Driven Artifact Decisions (Branch Equipment)

#### Performance (QAS-02)
- **Local Preprocessing**: `opencv-4.6.so` → 이미지 크기 70% 감소 (2MB → 600KB)
- **Real-time GPIO**: `gate-controller.cpp` → C++로 저지연 제어 (< 500ms)
- **Image Compression**: `pillow-10.0.py` → JPEG Quality 80 (70% 감소)

#### Availability (QAS-01)
- **Auto-restart**: `*.service` (Systemd) → 프로세스 장애 시 자동 재시작
- **Local Buffer**: `local-buffer.db` (SQLite) → 네트워크 장애 시 데이터 손실 방지
- **Heartbeat**: `sensor-manager.py` → 10분마다 상태 보고

#### Cost
- **Embedded Linux**: 무료 오픈소스 (라이선스 비용 제로)
- **Low-power Hardware**: ARM Cortex-A53 (< 10W)

#### Modifiability
- **Externalized Config**: `*-config.json` → 설정 변경 시 스크립트 재배포 불필요

---

## 4. Artifact Definition - Infrastructure Services

**파일**: `ArtifactDefinition_04_Infrastructure.puml`

### 4.1 Artifact 목록 (Infrastructure Services)

| Node | Artifact Name | Type | Manifest Component | Size | QA Contribution |
|------|---------------|------|-------------------|------|-----------------|
| **RDS Cluster** | auth-db-primary | Database Instance | Auth DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | auth-db-schema.sql | SQL Script | - | 1-10MB | Modifiability: Schema definition |
| | pgvector-extension.so | Extension | - | 5MB | Performance: Vector similarity |
| | helper-db-primary | Database Instance | Helper DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | helper-db-schema.sql | SQL Script | - | 1-10MB | Modifiability: Schema definition |
| | vector-db-primary | Database Instance | Vector DB (PostgreSQL 15.4) | 500GB | Performance (QAS-02): Face vectors |
| | vector-db-schema.sql | SQL Script | - | 1-10MB | Modifiability: Schema definition |
| | ivfflat-index.sql | SQL Script | - | < 1MB | Performance: IVFFlat index (10x faster) |
| | model-db-primary | Database Instance | Model DB (PostgreSQL 15.4) | 500GB | Modifiability: Model metadata |
| | search-db-primary | Database Instance | Search DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | branch-db-primary | Database Instance | Branch DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | monitor-db-primary | Database Instance | Monitor DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| **ElasticSearch Cluster** | elasticsearch-8.10.tar.gz | Binary | ElasticSearch Node | 500MB | Performance (QAS-03): Full-text search |
| | elasticsearch.yml | Configuration | - | < 10KB | Availability: Cluster config |
| | branch-index-mapping.json | Index Mapping | - | < 10KB | Performance: Optimized schema |
| | review-index-mapping.json | Index Mapping | - | < 10KB | Performance: Optimized schema |
| | nori-analyzer-plugin.zip | Plugin | - | 50MB | Performance: 한글 형태소 분석 |
| **RabbitMQ Cluster** | rabbitmq-server-3.12.8.tar.gz | Binary | RabbitMQ Node | 100MB | Availability (DD-02): Message-Based |
| | rabbitmq.conf | Configuration | - | < 10KB | Availability: Quorum Queue config |
| | queue-definitions.json | Queue Definition | - | < 10KB | Modifiability: Queue schema |
| | exchange-definitions.json | Exchange Definition | - | < 10KB | Modifiability: Event routing |
| | erlang-26.tar.gz | Runtime | - | 200MB | Availability: Erlang VM |
| **Redis Cache Cluster** | redis-server-7.2.tar.gz | Binary | Redis Node | 50MB | Performance (QAS-02): Sub-millisecond |
| | redis.conf | Configuration | - | < 10KB | Performance: LRU, persistence |
| | sentinel.conf | Configuration | - | < 10KB | Availability: Auto-failover |
| **S3 Storage** | task-photos/ | S3 Bucket | Task Photos | Unlimited | Scalability: Object storage |
| | face-images/ | S3 Bucket | Face Images | Unlimited | Scalability: Object storage |
| | ml-models/ | S3 Bucket | ML Model Weights | Unlimited | Scalability: Object storage |
| | lifecycle-policy.json | Lifecycle Policy | - | < 5KB | Cost: Standard → IA → Glacier (70%) |
| | bucket-policy.json | Bucket Policy | - | < 5KB | Security (QAS-04): IP Whitelist |

### 4.2 Component to Artifact Mapping (Infrastructure Services)

| Component | Manifest Artifact | Deploy Node | Multiplicity |
|-----------|------------------|-------------|--------------|
| **Auth DB** | auth-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Helper DB** | helper-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Vector DB** | vector-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Model DB** | model-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Search DB** | search-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Branch DB** | branch-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **Monitor DB** | monitor-db-primary | RDS Cluster | 1 Primary + 2 Replicas |
| **ElasticSearch Node** | elasticsearch-8.10.tar.gz | ElasticSearch Cluster | 3 nodes (1 Master, 2 Data) |
| **RabbitMQ Node** | rabbitmq-server-3.12.8.tar.gz | RabbitMQ Cluster | 3 nodes (Quorum Queue) |
| **Redis Node** | redis-server-7.2.tar.gz | Redis Cache Cluster | 3 nodes (1 Primary, 2 Replicas) |
| **S3 Buckets** | task-photos/, face-images/, ml-models/ | S3 Storage | N/A (Managed service) |

### 4.3 QA-Driven Artifact Decisions (Infrastructure Services)

#### Performance (QAS-02, QAS-03)
- **pgvector Extension**: `pgvector-extension.so` → 안면 벡터 유사도 검색 (~50ms)
- **IVFFlat Index**: `ivfflat-index.sql` → 벡터 검색 속도 10배 향상
- **Redis Cache**: `redis-server-7.2.tar.gz` → Sub-millisecond 응답 시간 (~1ms)
- **ElasticSearch**: `elasticsearch-8.10.tar.gz` → Full-text search (~50ms)
- **Nori Analyzer**: `nori-analyzer-plugin.zip` → 한글 형태소 분석 (검색 정확도 95%+)

#### Availability (QAS-01, QAS-05, DD-02)
- **Multi-AZ**: RDS 1 Primary + 2 Replicas → Automatic failover (< 60초)
- **Quorum Queue**: `rabbitmq.conf` → 3-node Raft consensus (1개 노드 장애 OK)
- **Redis Sentinel**: `sentinel.conf` → Automatic failover (< 5초)
- **Cluster Config**: `elasticsearch.yml` → Replica shards (노드 장애 허용)

#### Modifiability (DD-03, QAS-06)
- **Database per Service**: 8개 독립 DB → 각 서비스 스키마 독립
- **Schema Definition**: `*-db-schema.sql` → 버전 관리 (Git)
- **Queue/Exchange Definition**: `queue-definitions.json`, `exchange-definitions.json` → 이벤트 타입 추가 용이

#### Security (QAS-04)
- **Bucket Policy**: `bucket-policy.json` → IP Whitelist, Pre-signed URL
- **Encryption at rest**: RDS AES-256, S3 SSE-S3
- **Encryption in transit**: TLS 1.2 (PostgreSQL, S3)

#### Cost
- **Lifecycle Policy**: `lifecycle-policy.json` → S3 자동 아카이빙 (Standard → IA → Glacier) → 70% 비용 절감

---

## 5. Artifact Dependency Analysis

### 5.1 Inter-Artifact Dependencies

| Source Artifact | Target Artifact | Dependency Type | Reason |
|----------------|----------------|-----------------|---------|
| `access-service.jar` | `facemodel-service.jar` | **<<IPC call>>** | **Performance (QAS-02): IPC/gRPC < 10ms** |
| `mlops-service.jar` | `facemodel-service.jar` | **<<gRPC (Hot Swap)>>** | **Modifiability (QAS-06): Hot Swap** |
| `access-service.jar` | `grpc-java-1.58.0.jar` | <<dependency>> | IPC/gRPC communication |
| `auth-service.jar` | `jjwt-0.12.3.jar` | <<dependency>> | JWT token generation (RS256) |
| `search-service.jar` | `elasticsearch-client-8.10.jar` | <<dependency>> | Full-text search query |
| `monitoring-service.jar` | `quartz-scheduler-2.3.jar` | <<dependency>> | Periodic Heartbeat/Ping checks |
| `notification-service.jar` | `firebase-admin-sdk-9.2.jar` | <<dependency>> | FCM Push notification |
| `mlops-service.jar` | `tensorflow-2.13.jar` | <<dependency>> | Model training/inference |
| `mlops-service.jar` | `cuda-12.2.so` | <<dependency>> | GPU acceleration |
| `camera-controller.py` | `opencv-4.6.so` | <<dependency>> | Image preprocessing |
| `gate-controller.cpp` | `wiringpi-2.61.so` | <<dependency>> | GPIO control |
| `sensor-manager.py` | `local-buffer.db` | <<deploy>> | Network outage buffer |
| `vector-db-primary` | `pgvector-extension.so` | <<dependency>> | Vector similarity search |
| `elasticsearch-8.10.tar.gz` | `nori-analyzer-plugin.zip` | <<dependency>> | Korean morphological analysis |
| `rabbitmq-server-3.12.8.tar.gz` | `erlang-26.tar.gz` | <<dependency>> | Erlang runtime |

### 5.2 Component-Artifact Consistency Check

모든 Component가 1개 이상의 Artifact에 포함되었는지 확인:

| Component | Manifest Artifact | Status |
|-----------|------------------|--------|
| **API Gateway Service** (All Components) | `api-gateway-service.jar` | ✅ Consistent |
| **Access Service** (6 Components) | `access-service.jar` | ✅ Consistent |
| **FaceModel Service** (4 Components) | `facemodel-service.jar` | ✅ Consistent |
| **Auth Service** (8 Components) | `auth-service.jar` | ✅ Consistent |
| **Helper Service** (7 Components) | `helper-service.jar` | ✅ Consistent |
| **Search Service** (8 Components) | `search-service.jar` | ✅ Consistent |
| **BranchOwner Service** (5 Components) | `branchowner-service.jar` | ✅ Consistent |
| **Monitoring Service** (7 Components) | `monitoring-service.jar` | ✅ Consistent |
| **Notification Service** (4 Components) | `notification-service.jar` | ✅ Consistent |
| **MLOps Service** (9 Components) | `mlops-service.jar` | ✅ Consistent |
| **ML Inference Engine** | `ml-inference-engine.py` | ✅ Consistent |
| **Camera Controller** | `camera-controller.py` | ✅ Consistent |
| **Gate Controller** | `gate-controller.cpp` | ✅ Consistent |
| **IoT Sensor Manager** | `sensor-manager.py` | ✅ Consistent |
| **Customer App** (iOS/Android) | `customer-app.ipa` / `customer-app.apk` | ✅ Consistent |
| **Helper App** (iOS/Android) | `helper-app.ipa` / `helper-app.apk` | ✅ Consistent |
| **Manager App** (iOS/Android) | `manager-app.ipa` / `manager-app.apk` | ✅ Consistent |
| **Admin Dashboard** (React SPA) | `admin-dashboard.js` | ✅ Consistent |
| **Auth DB** | `auth-db-primary` | ✅ Consistent |
| **Helper DB** | `helper-db-primary` | ✅ Consistent |
| **Vector DB** | `vector-db-primary` | ✅ Consistent |
| **Model DB** | `model-db-primary` | ✅ Consistent |
| **Search DB** | `search-db-primary` | ✅ Consistent |
| **Branch DB** | `branch-db-primary` | ✅ Consistent |
| **Monitor DB** | `monitor-db-primary` | ✅ Consistent |
| **ElasticSearch Node** | `elasticsearch-8.10.tar.gz` | ✅ Consistent |
| **RabbitMQ Node** | `rabbitmq-server-3.12.8.tar.gz` | ✅ Consistent |
| **Redis Node** | `redis-server-7.2.tar.gz` | ✅ Consistent |
| **S3 Buckets** | `task-photos/`, `face-images/`, `ml-models/` | ✅ Consistent |

**결과**: 모든 Component가 Artifact에 정확히 매핑됨 (100% Consistency) ✅

---

## 6. Artifact Naming Conventions

### 6.1 Naming Principles
1. **구체성**: 실제 파일명/폴더명 사용 (예: `api-gateway-service.jar`, `camera-controller.py`)
2. **버전 명시**: 라이브러리는 버전 포함 (예: `spring-boot-3.2.4.jar`, `opencv-4.6.so`)
3. **기능 반영**: Artifact 이름이 제공 기능/QA 의미 (예: `access-service.jar` → Access Control)
4. **확장자 명시**: 파일 타입 명확히 (`.jar`, `.py`, `.cpp`, `.json`, `.sql`, `.pem`)

### 6.2 Naming Examples
| Category | Example | Meaning |
|----------|---------|---------|
| **Executable** | `api-gateway-service.jar` | Spring Boot executable JAR (API Gateway) |
| **Configuration** | `application-auth.yml` | Spring Boot configuration (Auth Service) |
| **Certificate** | `tls-certificates.pem` | TLS certificate (Security) |
| **Library** | `grpc-java-1.58.0.jar` | gRPC Java library v1.58.0 |
| **Script** | `camera-controller.py` | Python script (Camera Controller) |
| **Database** | `auth-db-primary` | PostgreSQL database instance (Auth Service) |
| **Schema** | `auth-db-schema.sql` | SQL schema definition (Auth DB) |
| **Bucket** | `task-photos/` | S3 bucket (Task photos) |
| **Plugin** | `nori-analyzer-plugin.zip` | ElasticSearch plugin (Korean analyzer) |

---

## 7. QA Achievement Summary by Artifact

| QA Attribute | Critical Artifacts | Contribution |
|--------------|-------------------|--------------|
| **Performance (QAS-02)** | `access-service.jar` + `facemodel-service.jar` (Co-located) | IPC/gRPC < 10ms → 평균 응답 시간 ~230ms |
| | `grpc-java-1.58.0.jar` | Protobuf 직렬화 (JSON 대비 5-10배 빠름) |
| | `lettuce-6.2.jar` | Redis Cache hit ~1ms (DB 조회 98% 감소) |
| | `opencv-4.6.so` | 이미지 전처리 (70% 크기 감소, 전송 시간 단축) |
| | `redis-server-7.2.tar.gz` | Sub-millisecond 응답 시간 (~1ms) |
| | `pgvector-extension.so` + `ivfflat-index.sql` | 벡터 유사도 검색 (~50ms) |
| **Performance (QAS-03)** | `elasticsearch-8.10.tar.gz` | Full-text search (~50ms, RDBMS 대비 10-100배 빠름) |
| | `nori-analyzer-plugin.zip` | 한글 형태소 분석 (검색 정확도 95%+) |
| **Availability (QAS-01)** | `monitoring-service.jar` + `quartz-scheduler-2.3.jar` | Heartbeat/Ping 주기적 체크 |
| | `sensor-manager.py` + `local-buffer.db` | 네트워크 장애 시 로컬 버퍼링 |
| | `*.service` (Systemd) | 프로세스 장애 시 자동 재시작 |
| | `notification-service.jar` + `firebase-admin-sdk-9.2.jar` | FCM Push 알림 (< 15초) |
| **Availability (QAS-05, DD-02)** | `auth-db-primary` (Multi-AZ) | Automatic failover (< 60초) |
| | `rabbitmq-server-3.12.8.tar.gz` + `rabbitmq.conf` | Quorum Queue (메시지 손실 방지) |
| | `redis-server-7.2.tar.gz` + `sentinel.conf` | Automatic failover (< 5초) |
| | `resilience4j-2.1.0.jar` | Circuit Breaker (장애 전파 차단) |
| **Security (QAS-04)** | `tls-certificates.pem` | TLS 1.3 암호화, Certificate Pinning |
| | `jwt-public-key.pem` + `jjwt-0.12.3.jar` | JWT 토큰 검증 (RS256) |
| | `fcm-service-account.json` | FCM 안전한 인증 |
| | `bucket-policy.json` | S3 IP Whitelist, Pre-signed URL |
| **Modifiability (QAS-06, DD-03)** | `application-*.yml` | 환경별 설정 분리 (Externalized Config) |
| | `model-weights-v1.0.pb` | Hot Swap 모델 교체 (< 1ms, 무중단) |
| | `*-db-primary` (8개 독립 DB) | Database per Service (스키마 독립) |
| | `ml-inference-engine.py` | Java와 독립 배포 |
| **Cost** | `lifecycle-policy.json` | S3 자동 아카이빙 (70% 비용 절감) |
| | Embedded Linux (`camera-controller.py`, etc.) | 라이선스 비용 제로 |

---

## 8. Artifact Size Analysis

| Artifact Category | Total Count | Total Size | Node Multiplicity | Estimated Total Deployment Size |
|-------------------|-------------|------------|-------------------|--------------------------------|
| **Application JARs** (Spring Boot) | 10 | 50-100MB each | 24 replicas total | ~1.2-2.4GB |
| **Mobile Apps** (iOS/Android) | 6 (3 apps × 2 platforms) | 50-100MB each | 12,000+ installs | ~600GB-1.2TB (cumulative) |
| **Web Apps** (React SPA) | 1 | 10-50MB | 5-10 users | ~50-500MB |
| **Branch Equipment Scripts** | 3 per device | 10-50MB total | 1,000+ devices | ~10-50GB |
| **Database Instances** (RDS) | 8 DBs × 3 instances | 500GB each | 24 instances | ~12TB (max capacity) |
| **ElasticSearch** | 1 binary | 500MB | 3 nodes | ~1.5GB |
| **RabbitMQ** | 1 binary | 100MB | 3 nodes | ~300MB |
| **Redis** | 1 binary | 50MB | 3 nodes | ~150MB |
| **S3 Buckets** | 3 buckets | Unlimited | N/A | ~10-100TB (estimated 1-year data) |
| **ML Models** | 1 per version | 100-500MB | 1-2 replicas | ~200MB-1GB |
| **Total (excluding user data)** | 100+ artifacts | - | - | **~14-16TB** |

---

## 9. Deployment Automation

### 9.1 Artifact Build & Deployment Pipeline
```
[CI/CD Pipeline]
1. Source Code → Git Repository (GitHub / GitLab)
2. Build:
   - Java: Gradle → JAR (Spring Boot)
   - Mobile: React Native → IPA/APK (Xcode/Android Studio)
   - Web: React → JS Bundle (Webpack)
   - Python: pip install → .py scripts
   - C++: g++ → executable
3. Test:
   - Unit Test, Integration Test, E2E Test
4. Artifact Storage:
   - JARs → AWS ECR / GCP GCR (Docker Image)
   - Mobile Apps → App Store Connect / Google Play Console
   - Web Apps → AWS S3 / GCP Cloud Storage (Static hosting)
   - Scripts → Ansible / Chef (Configuration Management)
5. Deploy:
   - Kubernetes: kubectl apply -f deployment.yaml
   - Mobile: App Store / Google Play (manual review)
   - Web: S3 bucket update → CloudFront cache invalidation
   - Branch Equipment: Ansible playbook (SSH remote install)
6. Verify:
   - Health Check (Liveness/Readiness Probe)
   - Smoke Test
   - Rollback (if failed)
```

### 9.2 Artifact Versioning Strategy
- **Semantic Versioning**: `MAJOR.MINOR.PATCH` (예: `1.2.3`)
  - MAJOR: Breaking changes (예: API 변경)
  - MINOR: Backward-compatible features (예: 신규 기능)
  - PATCH: Backward-compatible bug fixes (예: 버그 수정)
- **Git Tagging**: `v1.2.3` (Git tag)
- **Docker Image Tag**: `api-gateway:1.2.3` (ECR/GCR)
- **Model Versioning**: `model-weights-v1.0.pb` (파일명에 버전 포함)

---

## 10. Conclusion

본 Artifact Definition Diagram은 Smart Fitness Management System의 **모든 배포 파일(Artifact)을 식별**하고, **Component와 Artifact 간의 매핑 관계**를 정의하며, **QA(성능, 가용성, 유지보수성 등)를 고려한 배치 전략**을 제시합니다.

### 주요 결론:
1. **Component-Artifact Consistency**: 모든 Component가 Artifact에 정확히 매핑됨 (100% Consistency) ✅
2. **QA-Driven Artifact Design**: 각 Artifact는 특정 QA 목표 달성에 기여
   - **Performance**: `access-service.jar` + `facemodel-service.jar` (Co-located IPC) → ~230ms 응답
   - **Availability**: `monitoring-service.jar` (Heartbeat/Ping) → < 15초 알림
   - **Security**: `tls-certificates.pem` (Certificate Pinning) → 중간자 공격 방지
   - **Modifiability**: `model-weights-v1.0.pb` (Hot Swap) → 무중단 교체
3. **Artifact Naming Convention**: 구체적이고 의미 있는 이름 (실제 파일명, 버전 명시, 기능 반영)
4. **Artifact Dependency**: Inter-artifact 의존관계 명확히 정의 (라이브러리, IPC, gRPC)
5. **Deployment Automation**: CI/CD 파이프라인으로 자동 빌드/배포/검증

Artifact Definition Diagram을 통해 **실제 배포 시 필요한 모든 파일을 사전 식별**하고, **QA 목표 달성을 위한 최적의 배치 전략**을 수립하였습니다.

