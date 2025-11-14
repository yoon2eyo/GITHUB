# Deployment View - Smart Fitness Management System

## 1. Overview

Smart Fitness Management System은 **Kubernetes 기반 클라우드 네이티브 아키텍처**로 배포되며, **AWS EKS** 또는 **GCP GKE**를 활용한 컨테이너 오케스트레이션 환경에서 운영됩니다. 시스템은 **5개의 물리적 존(Zone)**으로 구성되어 있으며, 각 존은 네트워크 보안 및 성능 최적화를 위해 분리되어 있습니다.

### Physical Zones
1. **Client Zone**: 고객/헬퍼/지점주 모바일 앱, 운영팀 대시보드
2. **Branch Zone**: 지점별 설비 (카메라, 게이트, 센서)
3. **External Services**: 외부 파트너 서비스 (신용카드 검증, LLM, FCM)
4. **Kubernetes Cluster**: 마이크로서비스 애플리케이션 (11개 서비스)
5. **Database Zone**: 데이터베이스, 캐시, 메시지 브로커 (Private Subnet)

## 2. Node Specifications

### 2.1 Client Zone Nodes

#### Customer/Helper/Manager Mobile Device
- **Node Type**: `<<device>>`
- **Multiplicity**: N개 (사용자 수에 비례)
- **Components**: 
  - Customer App / Helper App / Manager App
- **Execution Environment**: 
  - iOS 14+ / Android 10+
  - React Native / Flutter
- **Communication**: 
  - → Load Balancer: HTTPS (TLS 1.3)
  - ← FCM Server: FCM Push (HTTPS)
- **QA Role**: 
  - Security: TLS 암호화 통신
  - Usability: 모바일 최적화 UI

#### Operations Workstation
- **Node Type**: `<<device>>`
- **Multiplicity**: 소수 (운영팀 인원수)
- **Components**: Admin Dashboard (Web App)
- **Execution Environment**: Web Browser (Chrome/Firefox)
- **Communication**: → Load Balancer: HTTPS (TLS 1.3)
- **QA Role**: Security: 관리자 인증, RBAC

### 2.2 Branch Zone Nodes

#### Branch Equipment Node
- **Node Type**: `<<device>>`
- **Multiplicity**: 각 지점별 1개 (1,000+ 지점)
- **Components**:
  - Camera Controller: 안면 사진 촬영 및 전송
  - Gate Controller: 게이트 개폐 제어 수신
  - IoT Sensor Manager: 설비 상태 보고
- **Execution Environment**: 
  - Embedded Linux (ARM/x86)
  - Python 3.10+ / C++
- **Hardware**:
  - CPU: ARM Cortex-A53 or Intel Atom
  - Memory: 2GB RAM
  - Storage: 16GB eMMC
  - Network: Ethernet / WiFi
- **Communication**:
  - → Access Service: HTTPS (Face Photo Upload)
  - ← Access Service: HTTPS (Gate Control Response)
  - → Monitoring Service: TCP (Heartbeat), HTTPS (Ping/Echo Response)
- **QA Role**:
  - Availability: Heartbeat 기반 장애 감지
  - Performance: 로컬 버퍼링으로 네트워크 지연 완화

### 2.3 External Service Nodes

#### Credit Card Verification Service
- **Node Type**: `<<external>>`
- **Provider**: 외부 PG사 (NicePay, KCP 등)
- **Communication**: ← Auth Service: HTTPS (API Call)
- **Protocol**: REST API (JSON over HTTPS)
- **QA Role**: Security: 카드 정보 암호화, PCI-DSS 준수

#### LLM Service Provider
- **Node Type**: `<<external>>`
- **Provider**: OpenAI (GPT) / Anthropic (Claude)
- **Communication**: ← Search Service: HTTPS (Cold Path만)
- **Protocol**: REST API (JSON over HTTPS)
- **QA Role**: 
  - Performance: Cold Path로 분리하여 Hot Path 영향 없음
  - Cost: 요청 수 최소화 (배치 처리)

#### FCM Server
- **Node Type**: `<<external>>`
- **Provider**: Google Firebase Cloud Messaging
- **Communication**: 
  - ← Notification Service: HTTPS (Push Request)
  - → Manager App: FCM Push (HTTPS)
- **Protocol**: FCM API (HTTP/2)
- **QA Role**: Availability: 알림 전달 보장

### 2.4 Kubernetes Cluster Nodes

#### Load Balancer Node
- **Node Type**: `<<load balancer>>`
- **Multiplicity**: 2+ (HA)
- **Components**: Nginx Ingress Controller
- **Execution Environment**: 
  - Kubernetes Service (Type: LoadBalancer)
  - Nginx 1.24+
- **Resources**:
  - CPU: 2 cores
  - Memory: 2GB
- **Communication**:
  - ← Clients: HTTPS (TLS 1.3)
  - → API Gateway: HTTP (Internal)
- **QA Role**:
  - Availability: Active-Active HA 구성
  - Performance: L7 load balancing, connection pooling
  - Security: TLS termination, DDoS protection

#### API Gateway Node
- **Node Type**: `<<application server>>`
- **Multiplicity**: 3 replicas
- **Components**: API Gateway Service
- **Execution Environment**:
  - Spring Boot 3.2.x
  - Java 17 (OpenJDK)
  - Kubernetes Pod
- **Resources**:
  - CPU: 1 core per pod
  - Memory: 1GB per pod
- **Communication**:
  - ← Load Balancer: HTTP
  - → All Services: HTTP (REST API)
  - → Redis: Redis Protocol (Session, Rate Limit)
- **QA Role**:
  - Availability: Circuit Breaker (Resilience4j), Retry
  - Security: JWT 검증, Rate Limiting
  - Performance: Request routing, Service discovery (Eureka)

#### Real-Time Access Node (Access + FaceModel Co-located)
- **Node Type**: `<<application server>>`
- **Multiplicity**: 2 replicas
- **Components**:
  - Access Service
  - FaceModel Service (동일 Pod 내 공존)
- **Execution Environment**:
  - Spring Boot 3.2.x
  - Java 17
  - Kubernetes Pod
  - **Co-location Strategy**: Pod Affinity 설정
- **Resources**:
  - CPU: 8 cores per pod
  - Memory: 8GB per pod
  - Storage: 1GB (로컬 캐시)
- **Communication**:
  - ← API Gateway: HTTP
  - ← Branch Equipment: HTTPS (Face Photo)
  - → Branch Equipment: HTTPS (Gate Control)
  - ↔ Access ↔ FaceModel: **IPC/gRPC (Local Socket)**
  - → Vector DB: JDBC (Connection Pool: 10-20)
  - → Redis: Redis Protocol (Face Vector Cache)
  - → RabbitMQ: AMQP (Publish)
- **QA Role**:
  - **Performance (QAS-02)**: 
    - IPC/gRPC로 서비스 간 지연 최소화 (~5-10ms)
    - 동일 Pod 배치로 네트워크 홉 제거
    - Face Vector Cache로 DB 조회 90% 감소
  - Availability: Multi-replica, Health check

#### Business Service Nodes (Auth, Helper, Search, BranchOwner, Monitoring, Notification)
- **Node Type**: `<<application server>>`
- **Multiplicity**:
  - Auth Service: 2 replicas
  - Helper Service: 2 replicas
  - Search Service: 3 replicas (높은 트래픽)
  - BranchOwner Service: 2 replicas
  - Monitoring Service: 2 replicas
  - Notification Service: 3 replicas (알림 부하)
- **Execution Environment**:
  - Spring Boot 3.2.x
  - Java 17
  - Kubernetes Pod
- **Resources (per pod)**:
  - CPU: 1-2 cores
  - Memory: 1-2GB
- **Communication**:
  - ← API Gateway: HTTP
  - → Respective DB: JDBC (Connection Pool: 10-20)
  - → RabbitMQ: AMQP (Pub/Sub)
  - → External Services: HTTPS (Auth→CC, Search→LLM, Notify→FCM)
  - Search → ElasticSearch: ES API (HTTP)
  - All → S3: HTTPS (Photo Upload/Download)
- **QA Role**:
  - Availability: Auto-scaling (HPA), Self-healing (Kubernetes)
  - Modifiability: Independent deployment, Database per Service
  - Security: Private network, JWT validation

#### MLOps Service Node (GPU Enabled)
- **Node Type**: `<<application server>>`
- **Multiplicity**: 1-2 replicas (1 for production, 1 for training)
- **Components**:
  - MLOps Service
  - ML Inference Engine (TensorFlow/PyTorch)
- **Execution Environment**:
  - Spring Boot 3.2.x
  - Java 17
  - TensorFlow 2.x / PyTorch 2.x
  - Kubernetes Pod (GPU NodeSelector)
- **Resources (per pod)**:
  - CPU: 8 cores
  - Memory: 16GB
  - GPU: NVIDIA T4 × 2
  - Storage: 50GB (모델 가중치)
- **Communication**:
  - ← API Gateway: HTTP
  - → Model DB, Training Data: JDBC
  - → Helper DB, Auth DB: JDBC (READ-ONLY)
  - → RabbitMQ: AMQP (Pub/Sub)
  - → FaceModel Service: gRPC (Model Hot Swap)
  - ↔ MLOps ↔ ML Engine: Local (Function Call)
  - → S3: HTTPS (모델 가중치 저장/로드)
- **QA Role**:
  - **Modifiability (QAS-06)**:
    - Hot Swap: gRPC로 신규 모델 배포 (< 1분)
    - Rollback: 이전 버전 복구 (< 1분)
  - Performance: GPU 가속으로 훈련 시간 단축 (2-4시간)
  - Cost: Spot Instances 활용 (70% 절감)

### 2.5 Database Zone Nodes (Private Subnet)

#### RDS Cluster
- **Node Type**: `<<database cluster>>`
- **Multiplicity**: 각 DB당 1 primary + 2 read replicas
- **Databases**:
  - Auth DB, Helper DB, Search DB, Branch DB, Monitor DB
  - Vector DB (PostgreSQL + pgvector extension)
  - Model DB, Training Data (일부, S3 병행)
- **Execution Environment**:
  - AWS RDS for PostgreSQL / GCP Cloud SQL
  - PostgreSQL 15.x
- **Resources (per DB instance)**:
  - CPU: 4 vCPU
  - Memory: 16GB
  - Storage: 500GB SSD (Auto-scaling)
- **Communication**:
  - ← Services: JDBC (Port: 5432, Private IP)
- **HA Strategy**:
  - Primary-Replica 구성 (Synchronous replication)
  - Automatic failover (< 60초)
  - Automated backup (Daily, 7-day retention)
- **QA Role**:
  - Availability: Multi-AZ deployment, Automatic failover
  - Performance: Read replica로 읽기 부하 분산
  - Security: Private subnet, Encryption at rest (AES-256)

#### ElasticSearch Cluster
- **Node Type**: `<<search cluster>>`
- **Multiplicity**: 3 nodes (1 master, 2 data nodes)
- **Execution Environment**:
  - ElasticSearch 8.x
  - Kubernetes StatefulSet
- **Resources (per node)**:
  - CPU: 4 cores
  - Memory: 8GB
  - Storage: 200GB SSD
- **Communication**:
  - ← Search Service: ES API (HTTP, Port: 9200)
- **Cluster Configuration**:
  - Sharding: 5 primary shards, 1 replica shard
  - Index: branch_index, review_index
- **QA Role**:
  - **Performance (QAS-03)**: Full-text search ~50ms
  - Availability: Replica shards, Cluster auto-healing
  - Scalability: Horizontal scaling (add data nodes)

#### RabbitMQ Cluster
- **Node Type**: `<<message broker cluster>>`
- **Multiplicity**: 3 nodes (Quorum queue)
- **Execution Environment**:
  - RabbitMQ 3.12.x
  - Kubernetes StatefulSet
- **Resources (per node)**:
  - CPU: 2 cores
  - Memory: 4GB
  - Storage: 50GB (Message persistence)
- **Communication**:
  - ← Services: AMQP (Port: 5672)
- **Cluster Configuration**:
  - Quorum Queue: 3-node consensus
  - Exchange Types: Topic, Fanout
  - Persistence: Durable queues
- **QA Role**:
  - **Availability (DD-02)**: Message durability, Guaranteed delivery
  - Scalability: Horizontal scaling (add nodes)
  - Decoupling: Loose coupling between services

#### Redis Cache Cluster
- **Node Type**: `<<cache cluster>>`
- **Multiplicity**: 3 nodes (1 primary, 2 replicas)
- **Execution Environment**:
  - Redis 7.x
  - Kubernetes StatefulSet or AWS ElastiCache
- **Resources (per node)**:
  - CPU: 2 cores
  - Memory: 8GB (In-memory)
- **Communication**:
  - ← API Gateway: Redis Protocol (Session, Rate Limit)
  - ← Access Service: Redis Protocol (Face Vector Cache)
- **Cache Strategy**:
  - LRU eviction policy
  - TTL: 24 hours (Face Vectors)
  - Session TTL: 30 minutes
- **QA Role**:
  - **Performance**: Sub-millisecond access time
  - Availability: Sentinel for auto-failover

#### S3 Storage
- **Node Type**: `<<object storage>>`
- **Provider**: AWS S3 / GCP Cloud Storage
- **Storage Classes**:
  - Standard: Recent photos (< 30 days)
  - Infrequent Access: Old photos (30-90 days)
  - Glacier: Archive (> 90 days)
- **Communication**:
  - ← Helper Service, Auth Service, MLOps Service: HTTPS (S3 API)
- **QA Role**:
  - Availability: 99.99% SLA
  - Durability: 99.999999999% (11 nines)
  - Cost: Lifecycle policy로 자동 아카이빙

## 3. Communication Path Specifications

### 3.1 External Client Communication

| Source | Target | Protocol | Port | Direction | Security | QA Impact |
|--------|--------|----------|------|-----------|----------|-----------|
| Customer App | Load Balancer | HTTPS (TLS 1.3) | 443 | → | Certificate pinning | Security: 암호화 통신 |
| Helper App | Load Balancer | HTTPS (TLS 1.3) | 443 | → | Certificate pinning | Security: 암호화 통신 |
| Manager App | Load Balancer | HTTPS (TLS 1.3) | 443 | → | Certificate pinning | Security: 암호화 통신 |
| Ops Dashboard | Load Balancer | HTTPS (TLS 1.3) | 443 | → | MFA, IP Whitelist | Security: 관리자 보호 |

### 3.2 Branch Equipment Communication

| Source | Target | Protocol | Port | Direction | Frequency | QA Impact |
|--------|--------|----------|------|-----------|-----------|-----------|
| Branch Equipment | Access Service | HTTPS | 443 | → | On-demand (출입 시) | Performance: 실시간 인증 |
| Access Service | Branch Equipment | HTTPS | 443 | → | On-demand (게이트 제어) | Performance: < 1초 제어 |
| Branch Equipment | Monitoring Service | TCP | 8080 | → | Every 10 min (Heartbeat) | Availability: 장애 감지 |
| Monitoring Service | Branch Equipment | HTTPS | 443 | → | Every 10 sec (Ping/Echo) | Availability: 능동 점검 |

### 3.3 Internal Service Communication

| Source | Target | Protocol | Port | Direction | Type | QA Impact |
|--------|--------|----------|------|-----------|------|-----------|
| Load Balancer | API Gateway | HTTP | 8080 | → | Synchronous | Availability: Load balancing |
| API Gateway | All Services | HTTP | 8080 | → | Synchronous (REST) | Performance: Service routing |
| **Access Service** | **FaceModel Service** | **IPC/gRPC** | **N/A** | ↔ | **Local Socket** | **Performance: < 10ms** |
| All Services | RabbitMQ | AMQP | 5672 | ↔ | Asynchronous (Event) | Availability: Decoupling |
| Services | Respective DB | JDBC | 5432 | → | Synchronous | Modifiability: DB per Service |
| Search Service | ElasticSearch | HTTP (ES API) | 9200 | → | Synchronous | Performance: Fast search |
| Services | Redis | Redis Protocol | 6379 | → | Synchronous (Cache) | Performance: Sub-ms access |
| Services | S3 | HTTPS (S3 API) | 443 | → | Synchronous | Scalability: Object storage |
| MLOps Service | FaceModel Service | gRPC | 50051 | → | Synchronous | Modifiability: Hot Swap |

### 3.4 External Service Communication

| Source | Target | Protocol | Port | Direction | Frequency | QA Impact |
|--------|--------|----------|------|-----------|-----------|-----------|
| Auth Service | Credit Card Service | HTTPS | 443 | → | Per registration | Security: Card verification |
| Search Service | LLM Service | HTTPS | 443 | → | Cold Path only (배치) | Performance: Hot path 보호 |
| Notification Service | FCM Server | HTTPS | 443 | → | Per notification | Availability: Push delivery |
| FCM Server | Manager App | FCM Push (HTTPS) | 443 | → | Per alert | Availability: Real-time alert |

## 4. Execution Environment Specifications

### 4.1 Application Runtime
- **Programming Language**: Java 17 (OpenJDK)
- **Framework**: Spring Boot 3.2.x
  - Spring Cloud Netflix (Eureka, Ribbon)
  - Spring Data JPA (Hibernate)
  - Spring AMQP (RabbitMQ)
- **Build Tool**: Gradle 8.x
- **Container Runtime**: Docker 24.x

### 4.2 Container Orchestration
- **Platform**: Kubernetes 1.28+
  - AWS EKS / GCP GKE
- **Ingress**: Nginx Ingress Controller
- **Service Mesh**: Istio (Optional, for advanced traffic management)
- **Auto-scaling**: 
  - HPA (Horizontal Pod Autoscaler): CPU/Memory 기반
  - VPA (Vertical Pod Autoscaler): 리소스 추천
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (ElasticSearch, Logstash, Kibana)

### 4.3 Database Runtime
- **RDBMS**: PostgreSQL 15.x
  - Extensions: pgvector (Vector similarity search)
- **Search Engine**: ElasticSearch 8.x
- **Message Broker**: RabbitMQ 3.12.x
- **Cache**: Redis 7.x
- **Object Storage**: AWS S3 / GCP Cloud Storage

### 4.4 AI/ML Runtime
- **ML Framework**: TensorFlow 2.x / PyTorch 2.x
- **Model Format**: SavedModel (TensorFlow), ONNX (interop)
- **GPU Driver**: NVIDIA CUDA 12.x
- **GPU**: NVIDIA T4 (16GB VRAM)

## 5. Network Architecture

### 5.1 Network Zones
```
Internet
   ↓
[DMZ] Load Balancer (Public Subnet)
   ↓
[Application Zone] Kubernetes Cluster (Private Subnet)
   ↓
[Data Zone] Database, Cache, Message Broker (Private Subnet)
```

### 5.2 Security Groups / Firewall Rules
- **Load Balancer**: 
  - Ingress: 443 (HTTPS from Internet)
  - Egress: 8080 (HTTP to API Gateway)
- **API Gateway**: 
  - Ingress: 8080 (HTTP from Load Balancer)
  - Egress: 8080 (HTTP to Services), 5432 (JDBC), 5672 (AMQP), 6379 (Redis)
- **Services**: 
  - Ingress: 8080 (HTTP from API Gateway)
  - Egress: 5432 (JDBC), 5672 (AMQP), 6379 (Redis), 9200 (ES), 443 (HTTPS)
- **Database Zone**: 
  - Ingress: 5432 (JDBC from Services only, Private IP)
  - Egress: None (Inbound only)

### 5.3 VPC Configuration
- **CIDR**: 10.0.0.0/16
- **Public Subnet**: 10.0.1.0/24 (Load Balancer)
- **Private Subnet (Application)**: 10.0.10.0/24 (Kubernetes Nodes)
- **Private Subnet (Data)**: 10.0.20.0/24 (Database, Cache)
- **NAT Gateway**: For outbound internet access from private subnets

## 6. Quality Attribute Achievement

### 6.1 Performance (QAS-02, QAS-03)
- **Co-located Access + FaceModel**: IPC/gRPC로 ~5-10ms 통신 (HTTP 대비 10-20ms 감소)
- **Face Vector Cache (Redis)**: 90% 캐시 히트율로 DB 조회 제거
- **ElasticSearch**: Full-text search ~50ms (RDBMS LIKE 대비 10-100배 빠름)
- **Connection Pooling**: HikariCP로 DB 연결 재사용 (오버헤드 감소)

### 6.2 Availability (QAS-01, QAS-05)
- **Multi-Replica**: 모든 서비스 2-3 replicas (Single Point of Failure 제거)
- **Load Balancing**: Active-Active 구성으로 부하 분산 및 장애 대응
- **Auto-Healing**: Kubernetes가 장애 Pod 자동 재시작 (< 30초)
- **Database HA**: Primary-Replica 구성, Automatic failover (< 60초)
- **Circuit Breaker**: Resilience4j로 장애 전파 차단

### 6.3 Scalability
- **Horizontal Scaling**: Kubernetes HPA로 CPU/Memory 기반 자동 확장
- **Stateless Design**: 모든 서비스 stateless로 설계 (무제한 확장 가능)
- **Database Read Replica**: 읽기 부하 분산 (최대 15 replicas)
- **ElasticSearch Sharding**: 데이터 분산으로 검색 성능 유지

### 6.4 Security (QAS-04)
- **Network Isolation**: VPC, Security Groups, Private Subnet
- **TLS Encryption**: 모든 외부 통신 TLS 1.3 암호화
- **Database Encryption**: Encryption at rest (AES-256)
- **Secret Management**: Kubernetes Secrets / AWS Secrets Manager
- **Access Control**: RBAC (Kubernetes), IAM (AWS/GCP)

### 6.5 Modifiability (QAS-06)
- **Database per Service**: 각 서비스 독립 배포 가능
- **Event-Driven**: RabbitMQ로 서비스 간 느슨한 결합
- **Hot Swap**: gRPC로 FaceModel에 신규 모델 배포 (무중단)
- **CI/CD**: GitOps (ArgoCD), Blue-Green Deployment

## 7. Monitoring & Observability

### 7.1 Metrics
- **Prometheus**: 메트릭 수집 (CPU, Memory, Request Rate, Latency)
- **Grafana**: 대시보드 시각화

### 7.2 Logging
- **ELK Stack**: 중앙 집중식 로그 수집 및 분석
- **Log Level**: INFO (production), DEBUG (development)

### 7.3 Tracing
- **Jaeger**: 분산 추적 (Distributed Tracing)
- **Trace Context**: HTTP Header로 trace ID 전파

### 7.4 Alerting
- **Prometheus Alertmanager**: 임계값 기반 알림
- **Slack/Email**: 알림 채널

## 8. Disaster Recovery

### 8.1 Backup Strategy
- **Database**: Daily automated backup (7-day retention)
- **S3**: Cross-region replication (DR region)
- **Configuration**: Git repository (Infrastructure as Code)

### 8.2 Recovery Objectives
- **RTO (Recovery Time Objective)**: < 4 hours
- **RPO (Recovery Point Objective)**: < 1 hour (DB transaction log)

### 8.3 DR Procedure
1. Database restore from latest backup
2. Kubernetes cluster re-creation (Terraform)
3. Service deployment (ArgoCD)
4. DNS failover to DR region

## 9. Cost Optimization

- **Auto-scaling**: 피크/비피크 시간대 자동 조절 (30-50% 절감)
- **Spot Instances**: MLOps 훈련용 GPU (70% 절감)
- **S3 Lifecycle**: 자동 아카이빙 (Standard → IA → Glacier)
- **RDS Reserved Instances**: 1-3년 약정으로 40-60% 절감
- **ElasticSearch**: Hot-Warm-Cold tier 분리

## 10. Conclusion

본 Deployment View는 Smart Fitness Management System의 **물리적 배포 구조**를 정의하며, 다음 품질 속성을 달성합니다:

1. **Performance**: Co-location, Caching, IPC/gRPC로 실시간 응답 보장
2. **Availability**: Multi-replica, Auto-healing, Failover로 99.9%+ 가용성
3. **Scalability**: Kubernetes auto-scaling으로 무제한 확장
4. **Security**: Network isolation, TLS, Encryption으로 보안 강화
5. **Modifiability**: Database per Service, Event-Driven으로 독립 배포

Kubernetes 기반의 **클라우드 네이티브 아키텍처**는 **24시간 무인 운영**이 가능한 안정적이고 확장 가능한 플랫폼을 제공합니다.

