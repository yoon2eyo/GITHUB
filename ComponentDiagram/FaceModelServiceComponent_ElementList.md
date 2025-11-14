# FaceModel Service Component Element List

## 개요
본 문서는 FaceModel Service 컴포넌트 다이어그램(`12_FaceModelServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IFaceModelServiceApi** | 얼굴 모델 서비스 API 인터페이스를 정의<br>얼굴 벡터 비교 및 특징 추출 기능 제공<br>IPC/gRPC 기반 고성능 인터페이스 | UC-07 (안면인식 출입 인증)<br>QAS-02 (성능 - 3초 이내 응답) |
| **FaceModelIPCHandler** | IFaceModelServiceApi 인터페이스의 구현<br>IPC/gRPC 요청 수신 및 처리<br>벡터 비교 파이프라인 트리거 | UC-07<br>DD-05 (IPC 최적화 - 205ms) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IVectorComparisonService / VectorComparisonEngine** | 벡터 비교 서비스 인터페이스를 정의하고 구현<br>얼굴 벡터 유사도 계산 및 비교<br>병렬 벡터 비교 파이프라인 실행 및 Cosine Similarity 기반 유사도 계산 | UC-07<br>QAS-02 (실시간 성능, 205ms), DD-05 (Pipeline, 49% 성능 향상) |
| **IFeatureExtractionService / FeatureExtractor** | 특징 추출 서비스 인터페이스를 정의하고 구현<br>얼굴 이미지에서 특징 벡터 추출<br>ML 모델을 활용한 얼굴 특징 추출 및 입력 이미지에서 512차원 벡터 생성 | UC-07<br>DD-05 (병렬 처리, 병렬 특징 추출) |
| **ModelLifecycleManager** | 모델 생명주기 관리자<br>Hot Swap을 통한 모델 교체<br>AtomicReference 기반 무중단 업데이트 | QAS-06 (Modifiability - Hot Swap)<br>DD-05 (Runtime Binding) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IModelVersionRepository** | 모델 버전 저장소 인터페이스를 정의<br>모델 메타데이터 및 버전 정보 CRUD 연산<br>JPA 기반 모델 버전 관리 | QAS-06 (Modifiability - 버전 관리)<br>DD-05 (Hot Swap 지원) |
| **IMLInferenceEngine** | ML 추론 엔진 인터페이스를 정의<br>모델 배포, 추론, 롤백 기능 제공<br>TensorFlow/PyTorch 런타임 래핑 | QAS-06 (Modifiability - 모델 교체)<br>DD-05 (Pipeline 최적화) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>모델 업데이트 이벤트 발행<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>QAS-06 (Hot Swap 이벤트) |
| **ModelVersionJpaRepository** | IModelVersionRepository 구현<br>JPA/Hibernate 기반 모델 메타데이터 접근<br>모델 버전 및 배포 이력 저장 | DD-03 (Database per Service)<br>QAS-06 (모델 버전 추적) |
| **MLInferenceEngineAdapter** | IMLInferenceEngine 구현<br>ML 프레임워크 어댑터<br>모델 로드/언로드 및 추론 실행 | QAS-06 (Modifiability - 프레임워크 교체)<br>DD-05 (Hot Swap 지원) |
| **RabbitMQAdapter** | IMessagePublisherService 구현<br>RabbitMQ 클라이언트 통합<br>모델 배포 이벤트 발행 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 이벤트 내구성) |
| **ModelMetadataDB** | 모델 메타데이터 데이터베이스<br>모델 버전, 정확도, 배포 이력 저장<br>PostgreSQL 기반 메타데이터 저장 | DD-03 (Database per Service)<br>QAS-06 (모델 추적) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 IPC 요청 수신 및 처리<br>얼굴 모델 서비스 gRPC API 공개 인터페이스<br>Access Service와의 첫 번째 상호작용 지점 | UC-07<br>QAS-02 (성능), DD-05 (IPC 최적화) |
| **Business Layer** | 얼굴 인식 핵심 로직 구현<br>벡터 비교 파이프라인, 특징 추출, 모델 생명주기 관리<br>병렬 처리 및 Hot Swap 최적화 | DD-05 (Performance Optimization), QAS-06 (Modifiability)<br>QAS-02 (실시간 성능), UC-07 |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>모델 DB, ML 엔진, RabbitMQ 연동<br>프로토콜 변환 및 모델 관리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-03 (Database per Service), QAS-06 (Hot Swap) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 1 | 1 | 2 |
| **Business Layer** | 2 | 3 | 5 |
| **System Interface Layer** | 3 | 3 | 6 |
| **패키지** | - | - | 3 |
| **총계** | **6** | **7** | **16** |

---

## Architectural Drivers 적용 현황

### QAS-02 (성능 - 출입 인증 시스템 응답시간 3초 이내 95% 달성)
- **IPC 최적화**: FaceModelIPCHandler (동일 노드 gRPC 통신)
- **병렬 파이프라인**: VectorComparisonEngine (49% 지연시간 감소)
- **특징 추출**: FeatureExtractor (병렬 벡터 추출)

### QAS-06 (Modifiability - 새로운 AI 모델 및 분석 알고리즘의 신속한 적용)
- **Hot Swap**: ModelLifecycleManager (AtomicReference 기반 무중단 교체)
- **런타임 바인딩**: Runtime Binding으로 모델 교체
- **롤백 지원**: 이전 모델로 1ms 이내 복구

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: RabbitMQAdapter (모델 업데이트 이벤트 보장)
- **데이터 영속성**: ModelVersionJpaRepository (모델 메타데이터 보존)
- **Hot Swap 복원력**: ModelLifecycleManager (교체 실패 시 자동 롤백)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: ModelLifecycleManager → RabbitMQAdapter (모델 배포 이벤트)
- **비동기 처리**: 모델 업데이트를 동기 응답에서 분리

### DD-03 (Database per Service)
- **모델 메타데이터 DB**: ModelMetadataDB (모델 정보 독립 저장)
- **독점 접근**: ModelVersionJpaRepository (FaceModel Service만 접근)

### DD-05 (Performance Optimization - 동시성 도입, 데이터 사전 적재)
- **Introduce Concurrency**: VectorComparisonEngine (CompletableFuture 병렬화)
- **Pipeline Optimization**: 특징 추출 → 벡터 비교 파이프라인
- **IPC 최적화**: Access Service와 동일 노드 배치
- **Hot Swap**: Runtime Binding으로 모델 교체

### 관련 UC 목록
- **UC-07**: 안면인식 출입 인증 (VectorComparisonEngine, FeatureExtractor)

---

## 성능 최적화 아키텍처 특징

### 병렬 파이프라인 처리 (DD-05 적용)
**기존 순차 처리 (405ms):**
1. 요청 이미지 특징 추출 (200ms)
2. 저장 벡터 특징 추출 (200ms)
3. Cosine Similarity 계산 (5ms)

**병렬 파이프라인 처리 (205ms):**
1. 요청/저장 이미지 특징 추출 동시 실행 (max(200ms, 200ms) = 200ms)
2. Cosine Similarity 계산 (5ms)
3. **총 205ms (49% 성능 향상)**

### Hot Swap 메커니즘 (QAS-06, DD-05 적용)
- **AtomicReference**: 스레드 안전한 모델 참조
- **무중단 교체**: 기존 요청은 이전 모델로 계속 처리
- **즉시 적용**: 신규 요청은 새 모델로 즉시 적용
- **롤백 지원**: 1ms 이내 이전 버전으로 복구

### IPC 최적화 (DD-05 적용)
- **동일 물리 노드**: Access Service와 co-location
- **gRPC 통신**: HTTP/2 기반 저지연 프로토콜
- **205ms 목표**: calculateSimilarityScore() 응답시간
- **공유 메모리**: 가능 시 프로세스 간 메모리 공유

---

## 결론

FaceModel Service 컴포넌트 다이어그램의 모든 요소(16개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 얼굴 모델 IPC API (2개 요소)
- **Business Layer**: 벡터 비교 및 모델 관리 로직 (5개 요소)
- **System Interface Layer**: 모델/ML/RabbitMQ 연동 (6개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **병렬 파이프라인 최적화**와 **Hot Swap**을 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
