# 4.3.6 Node별 Artifact 배포 정보

## Overview
본 문서는 Smart Fitness Management System의 각 노드(N-01 ~ N-15)에 배포되는 모든 Artifact 정보를 정리한 표이다. 각 Artifact는 해당 노드에서 실행되는 컴포넌트를 포함하며, 시스템의 품질 속성(QA) 달성에 기여한다.

---

## Node별 Artifact 배포 표

| Node ID | Node Name | Artifact ID | Artifact Name | Artifact Type | Manifest Component | Size | QA Contribution |
|:--------|:----------|:------------|:--------------|:--------------|:-------------------|:-----|:-----------------|
| **N-01** | **Load Balancer** | - | nginx-ingress-controller.yaml | Configuration | Nginx Ingress Controller | < 1KB | Availability (QAS-05): L7 Load Balancing |
| | | - | ingress-config.yaml | Configuration | - | < 1KB | Performance: Routing rules |
| | | - | tls-certificates.pem | Certificate | - | < 10KB | Security (QAS-04): TLS 1.3 |
| **N-02** | **API Gateway** | **A-01** | APIGateway.jar | Java Executable JAR | API Gateway Service (All Components) | 50-100MB | Availability (QAS-05): Circuit Breaker |
| | | **A-02** | gateway-config.yml | Configuration | RoutingConfiguration, RateLimitConfiguration, CircuitBreakerConfiguration, SecurityConfiguration | < 5KB | Modifiability: Externalized config |
| | | - | spring-cloud-gateway-4.1.jar | Library | - | 10MB | Availability: Service Discovery |
| | | - | resilience4j-2.1.0.jar | Library | - | 5MB | Availability: Circuit Breaker |
| | | - | jwt-public-key.pem | Certificate | - | < 5KB | Security (QAS-04): JWT verification |
| **N-03** | **Real-Time Access** | **A-03** | AccessService.jar | Java Executable JAR | Access Service (6 Components) | 50-100MB | Performance (QAS-02): Co-located |
| | | **A-04** | access-config.yml | Configuration | DataSourceConfiguration, CacheConfiguration, KafkaProducerConfiguration, FaceModelClientConfiguration | < 5KB | Modifiability: Externalized config |
| | | - | facemodel-service.jar | Java Executable JAR | FaceModel Service (4 Components) | 50-100MB | Performance (QAS-02): IPC/gRPC |
| | | - | application-facemodel.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | | - | grpc-java-1.58.0.jar | Library | - | 20MB | Performance: IPC < 10ms |
| | | - | lettuce-6.2.jar | Library | - | 5MB | Performance: Redis Cache |
| **N-04** | **Auth Service** | - | auth-service.jar | Java Executable JAR | Auth Service (8 Components) | 50-100MB | Security (QAS-04): JWT issuance |
| | | - | application-auth.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | | - | spring-security-6.1.jar | Library | - | 15MB | Security: Authentication |
| | | - | jjwt-0.12.3.jar | Library | - | 1MB | Security: JWT (RS256) |
| **N-05** | **Helper Service** | - | helper-service.jar | Java Executable JAR | Helper Service (7 Components) | 50-100MB | Modifiability: Task management |
| | | - | application-helper.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | | - | aws-s3-sdk-2.20.jar | Library | - | 20MB | Scalability: S3 upload |
| **N-06** | **Search Service** | **A-05** | SearchService.jar | Java Executable JAR | Search Service (8 Components) | 50-100MB | Performance (QAS-03): Hot/Cold Path |
| | | **A-06** | search-config.yml | Configuration | ElasticSearchConfiguration, KafkaConfiguration, LLMConfiguration | < 5KB | Modifiability: Externalized config |
| | | - | elasticsearch-client-8.10.jar | Library | - | 15MB | Performance: Full-text search |
| **N-07** | **BranchOwner Service** | - | branchowner-service.jar | Java Executable JAR | BranchOwner Service (5 Components) | 50-100MB | Modifiability: Branch management |
| | | - | application-branchowner.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| **N-08** | **Monitoring Service** | **A-10** | MonitoringService.jar | Java Executable JAR | Monitoring Service (7 Components) | 50-100MB | Availability (QAS-01): Fault detection |
| | | **A-11** | monitoring-config.yml | Configuration | ScheduledTaskConfiguration, RedisConfiguration, KafkaProducerConfiguration, PingEchoConfiguration | < 5KB | Modifiability: Externalized config |
| | | - | quartz-scheduler-2.3.jar | Library | - | 5MB | Availability: Periodic checks |
| **N-09** | **Notification Service** | - | notification-service.jar | Java Executable JAR | Notification Service (4 Components) | 50-100MB | Availability (QAS-01): Push alerts |
| | | - | application-notification.yml | Configuration | - | < 5KB | Modifiability: Externalized config |
| | | - | fcm-service-account.json | Credential | - | < 5KB | Security: FCM authentication |
| | | - | firebase-admin-sdk-9.2.jar | Library | - | 10MB | Availability: FCM Push |
| **N-10** | **MLOps Service** | **A-07** | MLOpsService.whl | Python Wheel Package | MLOps Service (9 Components) | 10-50MB | Modifiability (QAS-06): Model training |
| | | **A-08** | training_scripts/ | Python Scripts Directory | ML Inference Engine | 10-50MB | Modifiability: Model inference |
| | | **A-09** | mlops-config.yml | Configuration | TrainingConfiguration, DeploymentConfiguration, ModelVerificationConfiguration | < 5KB | Modifiability: Externalized config |
| | | - | model-weights-v1.0.pb | Model File | - | 100-500MB | Modifiability: Hot Swap model |
| | | - | tensorflow-2.13.jar | Library | - | 200MB | Performance: GPU acceleration |
| | | - | pytorch-2.1.jar | Library | - | 150MB | Performance: GPU acceleration |
| | | - | cuda-12.2.so | Library | - | 500MB | Performance: GPU driver |
| | | - | cudnn-8.9.so | Library | - | 200MB | Performance: DNN library |
| **N-11** | **RDS Cluster** | **A-15** | database-schema.sql | SQL DDL Script | user_table, branch_table, reservation_table, payment_table, access_log_table, device_status_table | 1-10MB | Modifiability: Schema definition |
| | | **A-16** | initial-data.sql | SQL DML Script | branch_initial_data, admin_user_data | < 1MB | Modifiability: Initial data |
| | | - | auth-db-primary | Database Instance | Auth DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | | - | helper-db-primary | Database Instance | Helper DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | | - | vector-db-primary | Database Instance | Vector DB (PostgreSQL 15.4) | 500GB | Performance (QAS-02): Face vectors |
| | | - | model-db-primary | Database Instance | Model DB (PostgreSQL 15.4) | 500GB | Modifiability: Model metadata |
| | | - | search-db-primary | Database Instance | Search DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | | - | branch-db-primary | Database Instance | Branch DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | | - | monitor-db-primary | Database Instance | Monitor DB (PostgreSQL 15.4) | 500GB | Modifiability (DD-03): DB per Service |
| | | - | pgvector-extension.so | Extension | - | 5MB | Performance: Vector similarity |
| | | - | ivfflat-index.sql | SQL Script | - | < 1MB | Performance: IVFFlat index (10x faster) |
| **N-12** | **ElasticSearch Cluster** | **A-18** | branch-index-mapping.json | ElasticSearch Index Mapping | branch_id, branch_name, address, facilities, keywords, location | < 10KB | Performance: Optimized schema |
| | | **A-19** | review-index-mapping.json | ElasticSearch Index Mapping | review_id, branch_id, rating, review_text, keywords, created_at | < 10KB | Performance: Optimized schema |
| | | **A-20** | nori-analyzer-plugin.zip | ElasticSearch Plugin | NoriTokenizer, NoriPartOfSpeechFilter, NoriReadingFormFilter, KoreanStopFilter | 50MB | Performance: 한글 형태소 분석 |
| | | **A-21** | elasticsearch.yml | Configuration | ClusterConfiguration, NetworkConfiguration, DiscoveryConfiguration, SecurityConfiguration | < 10KB | Availability: Cluster config |
| | | - | elasticsearch-8.10.tar.gz | Binary | ElasticSearch Node | 500MB | Performance (QAS-03): Full-text search |
| **N-13** | **RabbitMQ Cluster** | - | rabbitmq-server-3.12.8.tar.gz | Binary | RabbitMQ Node | 100MB | Availability (DD-02): Message-Based |
| | | - | rabbitmq.conf | Configuration | - | < 10KB | Availability: Quorum Queue config |
| | | - | queue-definitions.json | Queue Definition | - | < 10KB | Modifiability: Queue schema |
| | | - | exchange-definitions.json | Exchange Definition | - | < 10KB | Modifiability: Event routing |
| | | - | erlang-26.tar.gz | Runtime | - | 200MB | Availability: Erlang VM |
| **N-14** | **Redis Cache Cluster** | **A-17** | redis.conf | Configuration | ClusterConfiguration, MemoryPolicyConfiguration, PersistenceConfiguration, AuthenticationConfiguration | < 10KB | Performance: LRU, persistence |
| | | - | redis-server-7.2.tar.gz | Binary | Redis Node | 50MB | Performance (QAS-02): Sub-millisecond |
| | | - | sentinel.conf | Configuration | - | < 10KB | Availability: Auto-failover |
| **N-15** | **S3 Storage** | **A-22** | face-recognition-model.h5 | TensorFlow/Keras Model Binary | FaceRecognitionModel, ModelMetadata | 100-500MB | Modifiability (QAS-06): Hot Swap model |
| | | - | task-photos/ | S3 Bucket | Task Photos | Unlimited | Scalability: Object storage |
| | | - | face-images/ | S3 Bucket | Face Images | Unlimited | Scalability: Object storage |
| | | - | ml-models/ | S3 Bucket | ML Model Weights | Unlimited | Scalability: Object storage |
| | | - | lifecycle-policy.json | Lifecycle Policy | - | < 5KB | Cost: Standard → IA → Glacier (70%) |
| | | - | bucket-policy.json | Bucket Policy | - | < 5KB | Security (QAS-04): IP Whitelist |

---

## Artifact 의존성 요약

### 주요 Artifact 의존성

| Source Artifact | Target Artifact | Dependency Type | Reason |
|----------------|----------------|-----------------|---------|
| A-03 (AccessService.jar) | facemodel-service.jar | **<<IPC call>>** | **Performance (QAS-02): IPC/gRPC < 10ms** |
| A-07 (MLOpsService.whl) | facemodel-service.jar | **<<gRPC (Hot Swap)>>** | **Modifiability (QAS-06): Hot Swap** |
| A-16 (initial-data.sql) | A-15 (database-schema.sql) | <<depends on>> | Schema must exist before data insertion |
| A-18 (branch-index-mapping.json) | A-20 (nori-analyzer-plugin.zip) | <<depends on>> | Nori analyzer required for Korean text analysis |
| A-19 (review-index-mapping.json) | A-20 (nori-analyzer-plugin.zip) | <<depends on>> | Nori analyzer required for Korean text analysis |
| A-22 (face-recognition-model.h5) | N-10 (MLOps Service) → N-03 (Access Service) | <<deploy>> | Model deployed via gRPC Hot Swap |

---

## 노드별 Artifact 개수 요약

| Node ID | Node Name | Artifact Count | 주요 Artifact |
|:--------|:----------|:---------------|:--------------|
| N-01 | Load Balancer | 3 | nginx-ingress-controller.yaml, tls-certificates.pem |
| N-02 | API Gateway | 5 | A-01 (APIGateway.jar), A-02 (gateway-config.yml) |
| N-03 | Real-Time Access | 6 | A-03 (AccessService.jar), facemodel-service.jar, grpc-java-1.58.0.jar |
| N-04 | Auth Service | 4 | auth-service.jar, spring-security-6.1.jar, jjwt-0.12.3.jar |
| N-05 | Helper Service | 3 | helper-service.jar, aws-s3-sdk-2.20.jar |
| N-06 | Search Service | 3 | A-05 (SearchService.jar), A-06 (search-config.yml), elasticsearch-client-8.10.jar |
| N-07 | BranchOwner Service | 2 | branchowner-service.jar, application-branchowner.yml |
| N-08 | Monitoring Service | 3 | A-10 (MonitoringService.jar), A-11 (monitoring-config.yml), quartz-scheduler-2.3.jar |
| N-09 | Notification Service | 4 | notification-service.jar, firebase-admin-sdk-9.2.jar, fcm-service-account.json |
| N-10 | MLOps Service | 8 | A-07 (MLOpsService.whl), A-08 (training_scripts/), A-09 (mlops-config.yml), tensorflow-2.13.jar |
| N-11 | RDS Cluster | 11 | A-15 (database-schema.sql), A-16 (initial-data.sql), 8개 DB 인스턴스, pgvector-extension.so |
| N-12 | ElasticSearch Cluster | 5 | A-18, A-19, A-20, A-21, elasticsearch-8.10.tar.gz |
| N-13 | RabbitMQ Cluster | 5 | rabbitmq-server-3.12.8.tar.gz, rabbitmq.conf, erlang-26.tar.gz |
| N-14 | Redis Cache Cluster | 3 | A-17 (redis.conf), redis-server-7.2.tar.gz, sentinel.conf |
| N-15 | S3 Storage | 6 | A-22 (face-recognition-model.h5), 3개 S3 Bucket, lifecycle-policy.json, bucket-policy.json |
| **Total** | **15 Nodes** | **72 Artifacts** | - |

---

## QA 기여 요약

### Performance (QAS-02, QAS-03)
- **N-03**: grpc-java-1.58.0.jar (IPC < 10ms), lettuce-6.2.jar (Redis Cache ~1ms)
- **N-06**: elasticsearch-client-8.10.jar (Full-text search ~50ms)
- **N-11**: pgvector-extension.so (Vector similarity ~50ms)
- **N-12**: nori-analyzer-plugin.zip (한글 형태소 분석)
- **N-14**: redis-server-7.2.tar.gz (Sub-millisecond cache)

### Availability (QAS-01, QAS-05, DD-02)
- **N-01**: nginx-ingress-controller.yaml (L7 Load Balancing)
- **N-02**: resilience4j-2.1.0.jar (Circuit Breaker)
- **N-08**: quartz-scheduler-2.3.jar (Periodic Heartbeat/Ping checks)
- **N-09**: firebase-admin-sdk-9.2.jar (FCM Push < 15초)
- **N-13**: rabbitmq-server-3.12.8.tar.gz (Quorum Queue, Message persistence)
- **N-14**: sentinel.conf (Auto-failover < 5초)

### Modifiability (QAS-06, DD-03)
- **N-02~N-10**: application-*.yml (Externalized config)
- **N-10**: A-07 (MLOpsService.whl), A-22 (Hot Swap model)
- **N-11**: Database per Service (8개 독립 DB)
- **N-13**: queue-definitions.json, exchange-definitions.json (Event routing)

### Security (QAS-04)
- **N-01**: tls-certificates.pem (TLS 1.3)
- **N-02**: jwt-public-key.pem (JWT RS256 verification)
- **N-09**: fcm-service-account.json (FCM authentication)
- **N-15**: bucket-policy.json (IP Whitelist, Pre-signed URL)

---

## 전체 시스템 Artifact 배포 구성도

전체 시스템의 노드별 Artifact 배포 및 Execution Environment 관계를 PlantUML 다이어그램으로 표현합니다.

```plantuml
@include ArtifactDeploymentDiagram.puml
```

### 다이어그램 설명

본 다이어그램은 Smart Fitness Management System의 모든 노드(N-01 ~ N-18)에 배포되는 Artifact와 Execution Environment 간의 관계를 표현합니다.

#### 주요 관계 유형

1. **<<deploy>>**: Artifact가 Execution Environment에 배포됨
   - 예: `A-01: APIGateway.jar` → `Spring Cloud Gateway 4.1+` (N-02)
   - 예: `A-03: AccessService.jar` → `Spring Boot 3.2.x + gRPC 1.58+` (N-03)

2. **<<uses>>**: Artifact 간 의존성 관계
   - 예: `A-01: APIGateway.jar` → `spring-cloud-gateway-4.1.jar` (Library 사용)
   - 예: `A-03: AccessService.jar` → `facemodel-service.jar` (IPC 통신)

3. **<<depends on>>**: Artifact 간 전제 조건 관계
   - 예: `A-16: initial-data.sql` → `A-15: database-schema.sql` (스키마 선행 필요)
   - 예: `A-18: branch-index-mapping.json` → `A-20: nori-analyzer-plugin.zip` (플러그인 필요)

4. **<<gRPC Hot Swap>>**: MLOps에서 Access Service로 모델 배포
   - 예: `A-22: face-recognition-model.h5` → `facemodel-service.jar` (Hot Swap 배포)

#### 노드별 주요 Artifact

- **Kubernetes Cluster (N-01 ~ N-10)**: Java JAR 파일, Python Wheel, Configuration 파일
- **Database Zone (N-11 ~ N-15)**: SQL 스크립트, Binary 파일, Configuration 파일
- **Client Zone (N-16 ~ N-17)**: Mobile App, Web Dashboard
- **Branch Zone (N-18)**: Embedded Controller

#### Execution Environment 특징

- **Kubernetes Pod**: Spring Boot 3.2.x, Java 17, gRPC 1.58+
- **Database Cluster**: PostgreSQL 15.4, ElasticSearch 8.10+, RabbitMQ 3.12.8, Redis 7.2+
- **Cloud Storage**: AWS S3 / GCP Cloud Storage
- **Client Device**: iOS 14+ / Android 10+, HTML5 Browser
- **Branch Equipment**: Embedded Linux (Raspberry Pi OS, Yocto Linux 4.0+)

