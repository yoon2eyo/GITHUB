# 4.3.3 Execution Environment Specification

## Overview
본 문서는 Smart Fitness Management System의 Deployment Diagram에서 `<<execution environment>>`로 표시된 모든 실행 환경의 상세 사양과 선택 근거를 기술합니다. 각 실행 환경은 Architectural Driver(QA 등)의 만족을 위해 선택되었습니다.

---

## Kubernetes Cluster Execution Environments

| ID | Node | Name | Description |
|:---|:-----|:-----|:------------|
| **N-01** | **Load Balancer** | **Nginx Ingress Controller 1.9+** | QAS-05(가용성) 다수 API Gateway 인스턴스에 트래픽 균등 분산, 단일 장애점(SPOF) 제거, 인스턴스 장애 시 자동 페일오버. QAS-04(보안) TLS 종단 처리, 클라이언트와의 안전한 연결 보장 |
| **N-02** | **API Gateway** | **Spring Cloud Gateway 4.1+** | QAS-05(가용성) Circuit Breaker를 통한 장애 전파 차단, Retry 메커니즘과 타임아웃 설정으로 자동 복구. QAS-04(보안) JWT 토큰 검증(RS256), Rate Limiting, Eureka Service Discovery를 통한 동적 라우팅 |
| **N-03** | **Real-Time Access** | **Spring Boot 3.2.x + gRPC 1.58+** | QAS-02(성능) Access Service와 FaceModel Service가 동일 Pod에 공존하여 초저지연 출입 인증 수행, IPC/gRPC 통신으로 5-10ms 이내 응답 보장, Redis Face Vector Cache를 통해 DB 조회 90% 감소, 평균 응답 시간 ~230ms 달성 |
| **N-04** | **Auth Service** | **Spring Boot 3.2.x + Java 17** | QAS-04(보안) 사용자 인증/인가, 회원가입, JWT 발급 수행, JWT 기반 인증(RS256 서명), 비밀번호 암호화(BCrypt), 안면 벡터 저장 시 암호화 적용으로 개인정보 보호 |
| **N-05** | **Helper Service** | **Spring Boot 3.2.x + Java 17** | QAS-06(수정용이성) 헬퍼 작업 등록, AI 판독 요청, 보상 관리 수행, 이벤트 기반 아키텍처로 작업 사진 S3 업로드, TaskConfirmedEvent를 RabbitMQ에 발행하여 MLOps Service 재학습 트리거, 서비스 간 느슨한 결합 보장 |
| **N-06** | **Search Service** | **Spring Boot 3.2.x + Java 17** | QAS-03(실시간성) 자연어 지점 검색(Hot Path)과 콘텐츠 인덱싱(Cold Path) 수행, Hot Path는 Simple Tokenizer와 ElasticSearch만 사용하여 평균 70ms 이내 응답 보장, Cold Path는 비동기 LLM 분석으로 검색 품질 향상, 3초 이내 검색 응답 목표 달성 |
| **N-07** | **BranchOwner Service** | **Spring Boot 3.2.x + Java 17** | QAS-06(수정용이성) 지점 정보 관리, 작업 검수/컨펌 수행, 이벤트 기반 아키텍처로 작업 검수 시 AI 판독 결과 표시, 지점주 컨펌 후 TaskConfirmedEvent를 RabbitMQ에 발행하여 Helper Service(보상 갱신)와 MLOps Service(재학습) 트리거, 서비스 간 독립성 보장 |
| **N-08** | **Monitoring Service** | **Spring Boot 3.2.x + Java 17** | QAS-01(고장 감지 및 알림) 설비 상태 모니터링, 장애 감지(Heartbeat + Ping/Echo) 수행, Two-Level Fault Detection으로 Heartbeat는 설비가 10분마다 상태 보고하여 즉시 장애 감지, Quartz Scheduler가 10초마다 모든 설비의 최근 Heartbeat 확인하여 30초 초과 시 Ping 전송으로 장애 확정, P95 < 15초, P99 < 30초 알림 목표 달성 |
| **N-09** | **Notification Service** | **Spring Boot 3.2.x + Java 17** | QAS-01(고장 감지 및 알림) 이벤트 기반 푸시 알림 발송(FCM) 수행, RabbitMQ에서 EquipmentFaultEvent 구독하여 이벤트 수신, 지점주 디바이스 토큰 조회하여 FCM API 호출로 실시간 알림 전송, 15초 이내 알림 목표 달성 |
| **N-10** | **MLOps Service** | **Spring Boot 3.2.x + TensorFlow 2.13+ + CUDA 12.2** | QAS-06(수정용이성) AI 모델 재학습, 검증, 배포(Hot Swap) 수행, TaskConfirmedEvent 구독하여 수정 데이터 100건 이상 누적 시 자동 재학습 실행, gRPC로 FaceModel Service에 신규 모델을 Hot Swap 방식으로 배포하여 서비스 다운타임 0초와 API 실패율 < 0.1% 달성, GPU 가속으로 훈련 시간 단축, Spot Instances 활용으로 비용 70% 절감 |

---

## Database Zone Execution Environments

| ID | Node | Name | Description |
|:---|:-----|:-----|:------------|
| **N-11** | **RDS Cluster** | **PostgreSQL 15.4** | DD-03(수정용이성) 각 서비스별 독립 PostgreSQL 데이터베이스 클러스터로 Primary와 Read Replica 구성, Database per Service 패턴으로 각 서비스가 독립 DB 소유하여 스키마 변경이 다른 서비스에 영향 없음. QAS-05(가용성) Multi-AZ 배포로 99.95% SLA 보장, 자동 페일오버로 장애 시 Replica 승격. QAS-04(보안) Private subnet 배치, AES-256 암호화 적용 |
| **N-12** | **ElasticSearch Cluster** | **ElasticSearch 8.10+** | QAS-03(실시간성) 전문 검색 엔진으로 지점/리뷰 Full-text Search 제공, Inverted Index 활용하여 평균 50ms 이내 검색 응답 보장, Nori Analyzer를 통한 한글 형태소 분석, 3초 이내 검색 응답 목표 달성, Replica shard로 Data node 장애 시에도 검색 가능, Horizontal scaling으로 확장성 보장 |
| **N-13** | **RabbitMQ Cluster** | **RabbitMQ 3.12.8** | DD-02(가용성) 메시지 브로커로 이벤트 기반 비동기 통신 중개, Quorum Queue를 통한 3-node Raft Consensus로 메시지 손실 방지, 1개 노드 장애 시에도 정상 동작하여 메시지 전달 보장, Message persistence로 디스크 저장을 통한 노드 장애 시 메시지 손실 방지, 느슨한 결합으로 Publisher와 Subscriber의 독립성 보장 |
| **N-14** | **Redis Cache Cluster** | **Redis 7.2+** | QAS-02(성능) 인메모리 캐시로 Face Vector Cache 및 Session 저장, Sub-millisecond 응답 시간(~1ms) 보장, Face Vector Cache hit rate 90% 이상으로 DB 조회 98% 감소, 평균 응답 시간 ~230ms 달성, Primary 장애 시 Sentinel이 Replica 승격하여 가용성 보장 |
| **N-15** | **S3 Storage** | **AWS S3 / GCP Cloud Storage** | QAS-05(가용성) 객체 스토리지로 작업 사진 및 안면 이미지 저장, 99.99% 가용성과 11-nines 데이터 보존율 보장, 데이터 손실 방지. 확장성 무제한 확장 가능(Exabyte 규모), 트래픽 증가에 따른 자동 확장. QAS-04(보안) 저장 시 암호화(AES-256), 전송 시 암호화, IP 접근 제한, 임시 접근 URL 제공. 비용 효율성 자동 아카이빙 정책으로 오래된 데이터를 저렴한 저장소로 이동하여 비용 70% 절감 |

---

## Client Zone Execution Environments

| ID | Node | Name | Description |
|:---|:-----|:-----|:------------|
| **N-16** | **Customer/Helper/Manager Mobile Device** | **iOS 14+ / Android 10+** | QAS-04(보안) React Native/Flutter 기반 크로스플랫폼 모바일 앱으로 iOS와 Android 동시 지원, TLS 1.3 암호화와 Certificate Pinning을 통한 중간자 공격 방지 및 보안 강화. QAS-01(고장 감지 및 알림) FCM 푸시 알림을 통한 실시간 알림 수신 |
| **N-17** | **Operations Workstation** | **HTML5 Browser** | 수정용이성 달성, Chrome 90+ / Firefox 88+ / Safari 14+ / Edge 90+ 웹 브라우저로 React 18+ SPA 실행, 별도 설치 없이 웹 브라우저만으로 접근 가능하여 배포 및 업데이트 용이. QAS-04(보안) TLS 1.3과 Same-Origin Policy를 통한 보안 보장 |

---

## Branch Zone Execution Environments

| ID | Node | Name | Description |
|:---|:-----|:-----|:------------|
| **N-18** | **Branch Equipment** | **Embedded Linux** | QAS-01(가용성) Raspberry Pi OS (Debian 11) / Yocto Linux 4.0+ 임베디드 OS로 카메라 제어, 게이트 제어, 센서 관리 수행, Watchdog Timer와 Systemd 서비스를 통한 프로세스 장애 시 자동 재시작 보장. QAS-02(성능) OpenCV를 활용한 이미지 전처리로 전송 시간 70% 단축 |

---

## Summary Table

| Execution Environment | Version | Key Characteristics | Primary QA Contribution |
|:---------------------|:-------|:-------------------|:------------------------|
| **Nginx Ingress Controller** | 1.9+ | L7 Load Balancing, TLS 1.3 Termination | Availability (QAS-05), Security (QAS-04) |
| **Spring Cloud Gateway** | 4.1+ | Circuit Breaker, JWT 검증, Rate Limiting | Availability (QAS-05), Security (QAS-04) |
| **Spring Boot + gRPC** | 3.2.x + 1.58+ | IPC 통신, Protobuf 직렬화 | Performance (QAS-02) |
| **Spring Boot + Java** | 3.2.x + 17 | Reactive, JPA, Service Discovery | Modifiability, Availability (QAS-05) |
| **PostgreSQL** | 15.4 | Multi-AZ, pgvector, Read Replica | Availability (QAS-05), Performance (QAS-02) |
| **ElasticSearch** | 8.10+ | Inverted Index, Nori Analyzer | Performance (QAS-03) |
| **RabbitMQ** | 3.12.8 | Quorum Queue, Raft Consensus | Availability (DD-02), Modifiability |
| **Redis** | 7.2+ | In-memory, Sentinel, LRU | Performance (QAS-02), Availability |
| **AWS S3 / GCP Storage** | S3 API / GCS API | Lifecycle Policy, Versioning | Availability, Scalability, Cost |
| **iOS/Android** | iOS 14+, Android 10+ | React Native, FCM Push | Security (QAS-04), Usability |
| **HTML5 Browser** | Chrome 90+, Firefox 88+ | React SPA, Zero Installation | Modifiability, Security (QAS-04) |
| **Embedded Linux** | Raspberry Pi OS, Yocto 4.0+ | OpenCV, GPIO, Systemd | Availability (QAS-01), Cost |

---

## Key Technology Decisions

### 1. Nginx Ingress Controller (Load Balancer)
- **근거**: L7 로드 밸런싱, TLS 1.3 Termination, Health check 자동 페일오버
- **QA**: Availability (QAS-05) - Active-Active 구성으로 SPOF 제거, Security (QAS-04) - TLS 1.3 암호화

### 2. Spring Cloud Gateway (API Gateway)
- **근거**: Circuit Breaker, JWT 검증, Rate Limiting, Service Discovery
- **QA**: Availability (QAS-05) - 장애 전파 차단, Security (QAS-04) - JWT RS256 검증

### 3. gRPC + Protobuf (Access ↔ FaceModel IPC)
- **근거**: JSON 대비 5-10배 빠른 직렬화, HTTP/2 멀티플렉싱, Local Socket으로 < 10ms 통신
- **QA**: **Performance (QAS-02)** → 평균 응답 시간 ~230ms 달성 (목표 3초 대비 13배 빠름)

### 4. PostgreSQL + pgvector (Vector DB)
- **근거**: 오픈소스 무료, pgvector extension으로 벡터 유사도 검색 (Cosine Similarity) 지원
- **QA**: Performance (QAS-02) → 벡터 검색 ~50ms, Availability (Multi-AZ)

### 5. ElasticSearch (Search Engine)
- **근거**: Inverted Index로 Full-text search ~50ms (RDBMS LIKE 대비 10-100배 빠름)
- **QA**: **Performance (QAS-03)** → 평균 검색 응답 시간 ~70ms (목표 3초 대비 43배 빠름)

### 6. RabbitMQ Quorum Queue (Message Broker)
- **근거**: Raft consensus로 메시지 손실 방지, at-least-once semantics
- **QA**: **Availability (DD-02)** → 1개 노드 장애에도 메시지 전달 보장

### 7. Redis + Sentinel (Cache)
- **근거**: In-memory 저장으로 Sub-millisecond 응답 시간, Sentinel로 자동 페일오버
- **QA**: **Performance (QAS-02)** → Face Vector Cache hit ~1ms (DB 조회 98% 감소)

### 8. TensorFlow + CUDA (AI Pipeline)
- **근거**: GPU 가속으로 훈련 시간 단축 (CPU 대비 10-20배), SavedModel 형식으로 언어 중립적
- **QA**: **Modifiability (QAS-06)** → Hot Swap (무중단 모델 교체), Performance (GPU 가속)

### 9. Kubernetes + containerd (Container Orchestration)
- **근거**: Auto-scaling (HPA), Auto-healing (Liveness Probe), Rolling Update (무중단 배포)
- **QA**: Availability (QAS-05) → 자동 재시작, Scalability (자동 확장), Modifiability (무중단 배포)

---

## Conclusion

본 Execution Environment Specification은 Smart Fitness Management System의 모든 실행 환경이 **Architectural Driver(QA)의 만족을 위해 신중히 선택**되었음을 보여줍니다. 특히 다음 핵심 QA 달성에 기여합니다:

- **Performance (QAS-02, QAS-03)**: gRPC IPC (<10ms), Redis Cache (~1ms), ElasticSearch (~50ms)
- **Availability (QAS-01, QAS-05)**: Multi-AZ (RDS), Quorum Queue (RabbitMQ), Sentinel (Redis), Auto-healing (Kubernetes)
- **Security (QAS-04)**: TLS 1.3, Encryption at rest/in transit, Private subnet
- **Modifiability (QAS-06)**: Hot Swap (TensorFlow), Database per Service (Spring Boot), Rolling Update (Kubernetes)
