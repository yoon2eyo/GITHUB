# Real-Time Access Service Component Element List

## 개요
본 문서는 Real-Time Access Service 컴포넌트 다이어그램(`10_RealTimeAccessServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAccessControlApi** | 출입 제어 API 인터페이스를 정의<br>얼굴 인식 기반 출입 인증 기능 제공<br>실시간 출입 제어 요청 표준화 | UC-07 (안면인식 출입 인증)<br>QAS-02 (성능 - 3초 이내 출입) |
| **IQRAccessApi** | QR 코드 출입 API 인터페이스를 정의<br>QR 코드 기반 출입 인증 기능 제공<br>대안 출입 방식 지원 | UC-08 (QR 출입 인증)<br>QAS-02 (성능 - 빠른 QR 검증) |
| **AccessControlController** | IAccessControlApi 인터페이스의 구현<br>얼굴 인식 출입 요청 수신 및 처리<br>실시간 인증 파이프라인 트리거 | UC-07<br>QAS-02 (3초 SLA 준수) |
| **QRAccessController** | IQRAccessApi 인터페이스의 구현<br>QR 코드 출입 요청 수신 및 처리<br>빠른 QR 토큰 검증 | UC-08<br>QAS-02 (빠른 응답) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAccessAuthorizationService** | 출입 인증 서비스 인터페이스를 정의<br>얼굴 인식 및 QR 인증 통합 관리<br>출입 권한 검증 및 결정 | UC-07, UC-08<br>QAS-02 (실시간 성능), QAS-04 (Security) |
| **AccessAuthorizationManager** | IAccessAuthorizationService 구현<br>실시간 출입 인증 파이프라인 조율<br>얼굴 인식 → 벡터 매칭 → 권한 결정 | UC-07<br>DD-05 (Pipeline Optimization), QAS-02 (3초 SLA) |
| **IGateControlService** | 게이트 제어 서비스 인터페이스를 정의<br>설비 게이트 개폐 명령 전송<br>출입 성공/실패에 따른 게이트 제어 | UC-07, UC-08<br>QAS-02 (신속한 게이트 제어) |
| **GateController** | IGateControlService 구현<br>설비 게이트 제어 명령 실행<br>출입 성공 시 게이트 개방 | UC-07, UC-08<br>QAS-02 (즉시 게이트 제어) |
| **FaceVectorCache** | 얼굴 벡터 메모리 캐시<br>자주 사용되는 얼굴 벡터 메모리 저장<br>DB I/O 제거로 성능 최적화 | DD-05 (Data Pre-Fetching)<br>QAS-02 (캐시 히트율 90%) |
| **IAccessEventPublisher** | 출입 이벤트 발행 인터페이스를 정의<br>출입 성공/실패 이벤트 생성 및 발행<br>감사 및 모니터링을 위한 이벤트 | UC-07, UC-08<br>DD-02 (Event-Driven), QAS-04 (감사로그) |
| **AccessEventProcessor** | 출입 이벤트 처리자<br>출입 이벤트 생성 및 메시지 발행<br>실시간 감사 로그 생성 | DD-02 (Event-Driven)<br>QAS-04 (Security - 감사 추적) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAccessVectorRepository** | 출입 벡터 저장소 인터페이스를 정의<br>얼굴 벡터 및 출입 로그 데이터 CRUD 연산<br>JPA 기반 데이터 접근 추상화 | UC-07, UC-08<br>DD-03 (Database per Service) |
| **IFaceModelServiceClient** | 얼굴 모델 서비스 클라이언트 인터페이스를 정의<br>얼굴 특징 추출 및 벡터 비교 IPC 호출<br>gRPC 기반 고성능 통신 | UC-07<br>DD-05 (Same Physical Node - IPC/gRPC) |
| **IEquipmentGateway** | 설비 게이트웨이 인터페이스를 정의<br>설비 게이트 제어 명령 전송<br>HTTPS 기반 게이트 제어 | UC-07, UC-08<br>QAS-02 (신속한 게이트 응답) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>출입 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-07, UC-08 (출입 이벤트) |
| **VectorRepository** | IAccessVectorRepository 구현<br>JPA/Hibernate 기반 벡터 데이터 접근<br>얼굴 벡터 및 출입 로그 영속화 | DD-03 (Database per Service)<br>QAS-05 (Availability - 데이터 영속성) |
| **FaceModelServiceIPCClient** | IFaceModelServiceClient 구현<br>gRPC 클라이언트로 FaceModel Service 호출<br>최소 지연시간 얼굴 벡터 비교 | DD-05 (IPC/gRPC - 205ms)<br>QAS-02 (성능 최적화) |
| **EquipmentGatewayAdapter** | IEquipmentGateway 구현<br>설비 게이트웨이 HTTPS 클라이언트<br>게이트 개폐 명령 전송 | QAS-02 (신속한 게이트 제어)<br>UC-07, UC-08 |
| **RabbitMQAdapter** | IMessagePublisherService 구현<br>RabbitMQ 클라이언트 통합<br>출입 이벤트 발행 및 라우팅 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 이벤트 내구성) |
| **VectorDatabase** | 벡터 데이터베이스<br>얼굴 벡터, 출입 로그 저장<br>벡터 검색 최적화된 저장소 | DD-03 (Database per Service)<br>QAS-02 (빠른 벡터 조회) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 출입 요청 수신 및 처리<br>얼굴/QR 출입 인증 API 공개 인터페이스<br>설비와의 첫 번째 상호작용 지점 | UC-07, UC-08<br>QAS-02 (3초 SLA), QAS-04 (Security) |
| **Business Layer** | 실시간 출입 제어 핵심 로직 구현<br>얼굴 인식 파이프라인, 게이트 제어, 이벤트 처리<br>병렬 처리 및 캐시 최적화 | DD-05 (Performance Optimization), DD-02 (Event-Driven)<br>QAS-02 (실시간 성능), QAS-04 (Security) |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>벡터 DB, FaceModel IPC, 게이트웨이, RabbitMQ 연동<br>프로토콜 변환 및 고성능 최적화 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), DD-05 (IPC 최적화) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 3 | 4 | 7 |
| **System Interface Layer** | 4 | 4 | 9 |
| **패키지** | - | - | 3 |
| **총계** | **9** | **10** | **23** |

---

## Architectural Drivers 적용 현황

### QAS-02 (성능 - 출입 인증 시스템 응답시간 3초 이내 95% 달성)
- **파이프라인 최적화**: AccessAuthorizationManager (병렬 처리 단계)
- **데이터 사전 적재**: FaceVectorCache (메모리 캐시, 90% 히트율)
- **IPC 최적화**: FaceModelServiceIPCClient (205ms 벡터 비교)
- **캐시 메모리**: FaceVectorCache (~500MB, 10K 벡터)

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **출입 권한**: AccessAuthorizationManager, IAccessAuthorizationService
- **이벤트 감사**: AccessEventProcessor, IAccessEventPublisher (모든 출입 이벤트)
- **데이터 암호화**: VectorDatabase (얼굴 벡터 암호화 저장)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **데이터 영속성**: VectorRepository, IAccessVectorRepository
- **메시지 내구성**: RabbitMQAdapter (출입 이벤트 발행 보장)
- **IPC 복원력**: FaceModelServiceIPCClient (동일 노드 내 통신)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: AccessEventProcessor → RabbitMQAdapter (AccessGrantedEvent, AccessDeniedEvent)
- **비동기 처리**: 출입 이벤트를 동기 응답에서 분리
- **실시간 모니터링**: 이벤트 기반 출입 상태 추적

### DD-03 (Database per Service)
- **벡터 전용 DB**: VectorDatabase (얼굴 벡터 독립 저장)
- **벡터 최적화**: 벡터 검색에 특화된 스토리지

### DD-05 (Performance Optimization - 동시성 도입, 데이터 사전 적재)
- **Introduce Concurrency**: AccessAuthorizationManager (병렬 파이프라인)
- **Data Pre-Fetching**: FaceVectorCache (시작 시 10K 벡터 적재)
- **Encapsulate**: FaceModelServiceIPCClient (IPC 캡슐화)
- **Same Physical Node**: FaceModel Service와 동일 노드 배치

### 관련 UC 목록
- **UC-07**: 안면인식 출입 인증 (AccessAuthorizationManager)
- **UC-08**: QR 출입 인증 (QRAccessController)

---

## 실시간 출입 제어 아키텍처 특징

### 5단계 파이프라인 최적화 (DD-05)
1. **요청 수신**: AccessControlController (HTTP 수신)
2. **특징 추출**: FaceModelServiceIPCClient (병렬 처리)
3. **IPC 호출**: FaceModel Service로 벡터 비교
4. **권한 검증**: AccessAuthorizationManager (병렬 처리)
5. **게이트 제어**: GateController (즉시 실행)

### 데이터 사전 적재 전략 (DD-05)
- **시작 시 적재**: 상위 10K 얼굴 벡터 메모리 로드
- **런타임 관리**: LRU 알고리즘, 24시간 TTL
- **메모리 사용**: ~500MB (10K × 512차원 벡터)
- **DB I/O 제거**: 핫 패스에서 데이터베이스 접근 배제

### IPC 최적화 (DD-05)
- **동일 물리 노드**: Access ↔ FaceModel Service co-location
- **gRPC 통신**: HTTP/2 기반 고성능 IPC
- **공유 메모리**: 가능 시 공유 메모리 활용
- **지연시간**: calculateSimilarityScore() ≈ 205ms

---

## 결론

Real-Time Access Service 컴포넌트 다이어그램의 모든 요소(23개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 출입 제어 API 인터페이스 (4개 요소)
- **Business Layer**: 실시간 인증 파이프라인 로직 (7개 요소)
- **System Interface Layer**: 벡터 DB/IPC/게이트웨이/RabbitMQ 연동 (9개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **실시간 성능 최적화**(파이프라인, 캐시, IPC)를 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
