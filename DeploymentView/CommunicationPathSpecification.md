# 4.3.4 Communication Path Specification

## Overview
본 문서는 Smart Fitness Management System의 Deployment Diagram에 표시된 모든 통신 경로의 상세 특성과 선택 근거를 기술합니다. 각 통신 경로는 Architectural Driver(QA 등)의 충족과 관련하여 정당화됩니다. 모든 통신 경로는 ExecutionEnvironmentSpecification.md에 정의된 노드 번호(N-01~N-18)를 사용하여 표현됩니다.

---

## 1. External Client to System Communication

### 1.1 Mobile Apps to Load Balancer

| Path | Description |
|:-----|:------------|
| **N-16 (Customer App) → N-01** | **Protocol**: HTTPS (TLS 1.3)<br>**Direction**: 단방향<br>**특성**: TLS 1.3 암호화, Certificate Pinning, HTTP/2, JSON/multipart 페이로드<br>**QA 기여**: 보안 강화 (QAS-04) - TLS 1.3으로 통신 암호화, Certificate Pinning으로 중간자 공격 방지. 성능 최적화 - HTTP/2로 여러 요청 병렬 처리, 헤더 압축으로 오버헤드 감소 |
| **N-16 (Helper App) → N-01** | **Protocol**: HTTPS (TLS 1.3)<br>**Direction**: 단방향<br>**특성**: TLS 1.3 암호화, Certificate Pinning, HTTP/2, JSON/multipart 페이로드, Gzip 압축<br>**QA 기여**: 보안 강화 (QAS-04) - TLS 1.3 암호화. 성능 최적화 - Gzip 압축으로 JSON 응답 크기 70% 감소, HTTP/2 멀티플렉싱으로 다중 파일 업로드 효율 향상 |
| **N-16 (Manager App) → N-01** | **Protocol**: HTTPS (TLS 1.3)<br>**Direction**: 단방향<br>**특성**: TLS 1.3 암호화, Certificate Pinning, HTTP/2, JSON 페이로드, FCM Push 알림 수신<br>**QA 기여**: 보안 강화 (QAS-04) - TLS 1.3 암호화, Role-Based Access Control (지점주 권한). 고장 감지 및 알림 (QAS-01) - FCM Push로 설비 고장 알림 실시간 수신 (< 15초) |
| **N-17 (Ops Dashboard) → N-01** | **Protocol**: HTTPS (TLS 1.3)<br>**Direction**: 단방향<br>**특성**: TLS 1.3 암호화, MFA (Multi-Factor Authentication), IP Whitelist, HTTP/2, JSON 페이로드, Server-Sent Events<br>**QA 기여**: 보안 강화 (QAS-04) - MFA + IP Whitelist로 관리자 계정 보호, 무차별 대입 공격 방지. 가용성 보장 - Server-Sent Events로 실시간 메트릭 스트리밍 (WebSocket 대신 HTTP 기반) |

---

## 2. Branch Equipment to System Communication

### 2.1 Branch Equipment to Access Service

| Path | Description |
|:-----|:------------|
| **N-18 → N-03** | **Protocol**: HTTPS<br>**Direction**: 단방향<br>**특성**: TLS 1.2 암호화, JSON 페이로드, 3회 재시도, 로컬 SQLite 버퍼링, OpenCV 이미지 전처리<br>**QA 기여**: 성능 최적화 (QAS-02) - OpenCV 이미지 전처리로 이미지 크기 70% 감소 (2MB → 600KB), 전송 시간 단축. 가용성 보장 (QAS-01) - 로컬 SQLite 버퍼링으로 일시적 네트워크 장애 시 데이터 손실 방지 |
| **N-03 → N-18** | **Protocol**: HTTPS<br>**Direction**: 단방향<br>**특성**: TLS 1.2 암호화, JSON 게이트 제어 명령<br>**QA 기여**: 성능 최적화 (QAS-02) - 게이트 제어 < 1초 목표 달성, 인증 후 500ms 이내 명령 전송. 가용성 보장 - 타임아웃으로 장애 설비 빠른 감지 |

### 2.2 Branch Equipment to Monitoring Service

| Path | Description |
|:-----|:------------|
| **N-18 → N-08 (Heartbeat)** | **Protocol**: TCP<br>**Direction**: 단방향<br>**특성**: TCP Socket 영속 연결, JSON 상태 페이로드, TCP keep-alive, 10분 주기 상태 보고<br>**QA 기여**: 고장 감지 및 알림 (QAS-01) - 10분마다 상태 보고하여 30초(3 heartbeats) 무응답 시 장애 감지, Ping/Echo 트리거하여 P95 < 15초, P99 < 30초 알림 목표 달성 |
| **N-08 → N-18 (Ping/Echo)** | **Protocol**: HTTPS<br>**Direction**: 단방향<br>**특성**: TLS 1.2 암호화, HTTP GET 상태 확인, 10초 주기 능동 점검<br>**QA 기여**: 고장 감지 및 알림 (QAS-01) - Ping/Echo로 능동 점검하여 Heartbeat 장애 시 빠른 감지 (10초마다), P95 < 15초, P99 < 30초 알림 목표 달성 |

---

## 3. Internal System Communication (Kubernetes Cluster)

### 3.1 Load Balancer to API Gateway

| Path | Description |
|:-----|:------------|
| **N-01 → N-02** | **Protocol**: HTTP<br>**Direction**: 단방향<br>**특성**: HTTP/1.1, Weighted Round Robin 로드 밸런싱, Health Check 자동 제외, Connection Pooling<br>**QA 기여**: 장애 전파 차단 및 자동 복구 (QAS-05) - Health Check로 장애 API Gateway 자동 제외, 정상 인스턴스만 트래픽 수신. 성능 최적화 - Connection Pooling으로 TCP 핸드셰이크 오버헤드 제거 |

### 3.2 API Gateway to Microservices

| Path | Description |
|:-----|:------------|
| **N-02 → N-03, N-04, N-05, N-06, N-07, N-08, N-09** | **Protocol**: HTTP<br>**Direction**: 단방향<br>**특성**: HTTP/1.1 REST API, Eureka Service Discovery, Circuit Breaker, 최대 3회 Retry, Connection Pool<br>**QA 기여**: 장애 전파 차단 및 자동 복구 (QAS-05) - Circuit Breaker로 장애 전파 차단, 한 서비스 장애가 전체 시스템 영향 최소화, Retry로 일시적 장애 복구. 성능 최적화 - Service Discovery로 동적 라우팅, Connection Pool로 TCP 오버헤드 제거 |

### 3.3 Access Service to FaceModel Service (Co-located IPC)

| Path | Description |
|:-----|:------------|
| **N-03 ↔ FaceModel (Co-located)** | **Protocol**: IPC/gRPC<br>**Direction**: 양방향<br>**특성**: gRPC HTTP/2, Protobuf 직렬화, Local Socket, 영속 연결, 네트워크 스택 생략<br>**QA 기여**: 성능 최적화 (QAS-02) - IPC/gRPC 통신 < 10ms (HTTP REST 30-50ms 대비 3-5배 빠름), Protobuf 직렬화로 JSON 대비 5-10배 빠름, Local Socket으로 네트워크 스택 생략하여 평균 IPC 시간 ~5-10ms, 전체 응답 시간 ~230ms << 3초 목표 달성 |

### 3.4 MLOps Service to FaceModel Service (Model Hot Swap)

| Path | Description |
|:-----|:------------|
| **N-10 → N-03** | **Protocol**: gRPC<br>**Direction**: 단방향<br>**특성**: gRPC HTTP/2, Protobuf 모델 바이너리 전송, gRPC Streaming, AtomicReference 런타임 교체<br>**QA 기여**: 수정 용이성 (QAS-06) - gRPC로 신규 모델 전송하여 FaceModel Service의 AtomicReference로 런타임 교체 (< 1ms, 무중단), 서비스 재시작 불필요하여 서비스 다운타임 0초, API 실패율 < 0.1% 달성 |

---

## 4. Service to Database Communication

### 4.1 Services to PostgreSQL

| Path | Description |
|:-----|:------------|
| **N-03, N-04, N-05, N-06, N-07, N-08, N-09, N-10 → N-11** | **Protocol**: JDBC<br>**Direction**: 단방향<br>**특성**: PostgreSQL JDBC Driver, Connection Pool, TLS 1.2 암호화, Read Committed 트랜잭션 격리, Read Replica 자동 라우팅, Prepared Statement<br>**QA 기여**: 수정 용이성 (DD-03) - Database per Service 패턴으로 각 서비스가 독립 DB 소유, 스키마 변경이 다른 서비스에 영향 없음. 성능 최적화 - Connection Pool로 DB 연결 재사용, Read Replica로 읽기 부하 분산. 보안 강화 (QAS-04) - TLS로 DB 통신 암호화, Prepared Statement로 SQL Injection 방지 |

### 4.2 Access Service to Vector DB

| Path | Description |
|:-----|:------------|
| **N-03 → N-11 (Vector DB)** | **Protocol**: JDBC<br>**Direction**: 단방향<br>**특성**: PostgreSQL JDBC Driver, Connection Pool, pgvector Extension, Cosine Similarity 쿼리, IVFFlat Index<br>**QA 기여**: 성능 최적화 (QAS-02) - pgvector로 벡터 유사도 검색 ~50ms (캐시 미스 시), IVFFlat Index로 검색 속도 10배 향상. 가용성 보장 - Multi-AZ로 Primary 장애 시 Replica 승격 (< 60초) |

### 4.3 MLOps Service to Helper/Auth DB (Read-Only)

| Path | Description |
|:-----|:------------|
| **N-10 → N-11 (Helper DB / Auth DB)** | **Protocol**: JDBC (Read-Only)<br>**Direction**: 단방향<br>**특성**: PostgreSQL JDBC Driver, Connection Pool, Read-Only 트랜잭션, Read Replica 자동 라우팅<br>**QA 기여**: 수정 용이성 (DD-03) - Database per Service 원칙 유지, MLOps는 다른 서비스 DB 쓰기 금지하여 데이터 무결성 보장. 성능 최적화 - Read Replica로 쿼리 성능 향상, Primary 부하 감소 |

---

## 5. Service to Cache/Message Broker Communication

### 5.1 Services to Redis

| Path | Description |
|:-----|:------------|
| **N-02, N-03 → N-14** | **Protocol**: Redis Protocol (RESP)<br>**Direction**: 단방향<br>**특성**: Lettuce Reactive Redis Client, Connection Pooling, GET/SET/DEL 명령, JSON 직렬화, TTL 24시간 (Face Vectors), 30분 (Session)<br>**QA 기여**: 성능 최적화 (QAS-02) - Face Vector Cache hit ~1ms (Sub-millisecond), DB 조회 50ms → 98% 감소, 90%+ 히트율로 대부분 요청이 DB 조회 생략, 평균 응답 시간 ~230ms 달성. 가용성 보장 - Sentinel로 Primary 장애 시 Replica 승격 (< 5초) |

### 5.2 Services to RabbitMQ

| Path | Description |
|:-----|:------------|
| **N-03, N-04, N-05, N-06, N-07, N-08, N-09, N-10 ↔ N-13** | **Protocol**: AMQP 0-9-1<br>**Direction**: 양방향<br>**특성**: Spring AMQP Client, CachingConnectionFactory, Topic Exchange 이벤트 라우팅, Quorum Queue 3-node Raft 합의, at-least-once semantics, Publisher Confirm, Spring Retry 최대 3회, Dead Letter Queue<br>**QA 기여**: 가용성 보장 (DD-02) - Guaranteed delivery (at-least-once semantics)로 메시지 손실 방지, Quorum Queue (3-node 합의)로 1개 노드 장애에도 메시지 전달 보장, Publisher Confirm으로 메시지 전달 확인. 수정 용이성 - 느슨한 결합으로 Publisher와 Consumer 독립, 새 Consumer 추가 용이 |

### 5.3 Search Service to ElasticSearch

| Path | Description |
|:-----|:------------|
| **N-06 → N-12** | **Protocol**: HTTP (ES REST API)<br>**Direction**: 단방향<br>**특성**: ElasticSearch Java Client, Apache HttpClient Connection Pooling, REST API, JSON Query DSL, 1초 Refresh Interval<br>**QA 기여**: 성능 최적화 (QAS-03) - Inverted Index로 Full-text search ~50ms (RDBMS LIKE 대비 10-100배 빠름), 3초 이내 검색 응답 목표 달성. 확장성 - Horizontal scaling으로 Data node 추가 시 샤드 재분배 |

---

## 6. Service to Object Storage Communication

| Path | Description |
|:-----|:------------|
| **N-04, N-05, N-10 → N-15** | **Protocol**: HTTPS (S3 API)<br>**Direction**: 단방향<br>**특성**: AWS S3 SDK / GCP Cloud Storage SDK, PutObject/GetObject/DeleteObject API, Transfer Acceleration, Multipart Upload, Pre-signed URL, SDK 자동 재시도<br>**QA 기여**: 가용성 보장 - 99.99% SLA, 11-nines durability로 데이터 손실 확률 거의 0%. 확장성 - 무제한 확장 (Exabyte 규모), 초당 5,500+ GET req per prefix. 보안 강화 (QAS-04) - Encryption at rest (AES-256), Encryption in transit (TLS 1.2), Pre-signed URL로 안전한 임시 접근 |

---

## 7. Service to External System Communication

### 7.1 Auth Service to Credit Card Service

| Path | Description |
|:-----|:------------|
| **N-04 → External (Credit Card Service)** | **Protocol**: HTTPS<br>**Direction**: 단방향<br>**특성**: REST API, TLS 1.2/1.3, AES-256 암호화 페이로드, 5초 타임아웃, 1회 재시도, Resilience4j Circuit Breaker<br>**QA 기여**: 보안 강화 (QAS-04) - PCI-DSS Level 1 준수, 카드 정보 AES-256 암호화, TLS 1.2 암호화 통신. 가용성 보장 - Circuit Breaker로 외부 장애 시 빠른 실패, 전체 시스템 영향 최소화, Timeout으로 무한 대기 방지 |

### 7.2 Search Service to LLM Service

| Path | Description |
|:-----|:------------|
| **N-06 → External (LLM Service - Cold Path)** | **Protocol**: HTTPS<br>**Direction**: 단방향<br>**특성**: REST API, TLS 1.3, 비동기 Cold Path 처리, 30초 타임아웃, 3회 재시도 Exponential Backoff, Rate Limiting<br>**QA 기여**: 성능 최적화 (QAS-03) - Hot/Cold Path 분리 (DD-09)로 Hot Path에서 LLM 제거하여 Simple Tokenizer + ElasticSearch만 사용, 평균 응답 시간 ~70ms << 3초, Cold Path는 비동기 LLM 분석으로 검색 품질 향상 (실시간 성능 영향 없음). 비용 효율성 - Cold Path로 제한하여 LLM API 호출 비용 90% 절감 |

### 7.3 Notification Service to FCM Server

| Path | Description |
|:-----|:------------|
| **N-09 → External (FCM Server)** | **Protocol**: HTTPS (FCM API)<br>**Direction**: 단방향<br>**특성**: REST API, TLS 1.3, Firebase Admin SDK, Batch Send 최대 500개, FCM SDK 자동 재시도, 10초 타임아웃<br>**QA 기여**: 고장 감지 및 알림 (QAS-01) - FCM Push로 설비 고장 알림 실시간 전달 (< 15초). 확장성 - Batch Send로 다수 디바이스 동시 알림 (최대 500개), 99.95% SLA, FCM SDK 자동 재시도로 일시적 장애 복구 |

### 7.4 FCM Server to Manager App

| Path | Description |
|:-----|:------------|
| **External (FCM Server) → N-16** | **Protocol**: FCM Push (HTTPS, HTTP/2)<br>**Direction**: 단방향<br>**특성**: HTTP/2 Server Push, JSON 페이로드, Best-effort 배달, High Priority 즉시 전달<br>**QA 기여**: 고장 감지 및 알림 (QAS-01) - 설비 고장 알림 실시간 수신 (P95 < 15초, P99 < 30초), FCM은 99.95% SLA, 디바이스 오프라인 시 4주간 보관 후 전송 |

---

## 8. Communication Path Summary Table

| Path | Protocol | Direction | Key QA Contribution |
|:-----|:---------|:----------|:---------------------|
| **N-16 → N-01** | HTTPS (TLS 1.3) | 단방향 | Security (QAS-04) |
| **N-17 → N-01** | HTTPS (TLS 1.3) | 단방향 | Security (QAS-04) |
| **N-18 → N-03** | HTTPS | 단방향 | Performance (QAS-02) |
| **N-03 → N-18** | HTTPS | 단방향 | Performance (QAS-02) |
| **N-18 → N-08 (Heartbeat)** | TCP | 단방향 | Availability (QAS-01) |
| **N-08 → N-18 (Ping)** | HTTPS | 단방향 | Availability (QAS-01) |
| **N-01 → N-02** | HTTP | 단방향 | Availability (QAS-05) |
| **N-02 → N-03, N-04, N-05, N-06, N-07, N-08, N-09** | HTTP | 단방향 | Availability (QAS-05) |
| **N-03 ↔ FaceModel (Co-located)** | IPC/gRPC | 양방향 | **Performance (QAS-02)** |
| **N-10 → N-03** | gRPC | 단방향 | **Modifiability (QAS-06)** |
| **N-03, N-04, N-05, N-06, N-07, N-08, N-09, N-10 → N-11** | JDBC | 단방향 | Modifiability (DD-03) |
| **N-02, N-03 → N-14** | RESP | 단방향 | **Performance (QAS-02)** |
| **N-03, N-04, N-05, N-06, N-07, N-08, N-09, N-10 ↔ N-13** | AMQP | 양방향 | **Availability (DD-02)** |
| **N-06 → N-12** | HTTP (ES API) | 단방향 | **Performance (QAS-03)** |
| **N-04, N-05, N-10 → N-15** | HTTPS (S3 API) | 단방향 | Availability, Scalability |
| **N-04 → External (CC Service)** | HTTPS | 단방향 | Security (QAS-04) |
| **N-06 → External (LLM - Cold)** | HTTPS | 단방향 | Performance (QAS-03) |
| **N-09 → External (FCM)** | HTTPS (FCM API) | 단방향 | Availability (QAS-01) |
| **External (FCM) → N-16** | FCM Push (HTTPS) | 단방향 | Availability (QAS-01) |

---

## 9. Key Communication Path Decisions

### 9.1 IPC/gRPC for N-03 Access ↔ FaceModel (QAS-02)
- **근거**: 안면 인식 출입은 < 3초 응답 필수 → HTTP REST (30-50ms) 대신 IPC/gRPC (< 10ms) 선택
- **결과**: 평균 응답 시간 ~230ms (목표 대비 13배 빠름)

### 9.2 Hot/Cold Path Separation for N-06 Search (QAS-03)
- **근거**: 자연어 검색은 < 3초 응답 필수 → Hot Path에서 LLM 제거 (10초+ 지연)
- **결과**: Hot Path 응답 시간 ~70ms (목표 대비 43배 빠름)

### 9.3 Quorum Queue for N-13 RabbitMQ (QAS-05)
- **근거**: 이벤트 기반 통신은 메시지 손실 방지 필수 → Quorum Queue (Raft consensus) 선택
- **결과**: 1개 노드 장애에도 메시지 전달 보장

### 9.4 Redis Cache for N-14 Face Vectors (QAS-02)
- **근거**: 안면 벡터 조회는 빈번하고 지연 민감 → Redis Cache (Sub-millisecond) 선택
- **결과**: Cache hit ~1ms (DB 조회 98% 감소)

### 9.5 TLS 1.3 for External Communication (QAS-04)
- **근거**: 모바일 앱 ↔ 서버 통신은 보안 필수 → TLS 1.3 (최신 암호화) 선택
- **결과**: 중간자 공격 방지, 핸드셰이크 시간 30% 단축

---

## 10. Conclusion

본 Communication Path Specification은 Smart Fitness Management System의 모든 통신 경로가 **Architectural Driver(QA)의 만족을 위해 최적화**되었음을 보여줍니다. 특히 다음 핵심 QA 달성에 기여합니다:

- **Performance (QAS-02, QAS-03)**: IPC/gRPC (< 10ms), Redis (< 5ms), ElasticSearch (< 50ms)
- **Availability (QAS-01, QAS-05)**: Quorum Queue (메시지 손실 방지), Circuit Breaker (장애 전파 차단), Heartbeat + Ping/Echo (능동 점검)
- **Security (QAS-04)**: TLS 1.3 (외부 통신), Encryption at rest/in transit (DB, S3), Certificate Pinning (중간자 공격 방지)
- **Modifiability (QAS-06)**: gRPC Hot Swap (무중단 모델 교체), AMQP (느슨한 결합), Database per Service (독립 배포)
