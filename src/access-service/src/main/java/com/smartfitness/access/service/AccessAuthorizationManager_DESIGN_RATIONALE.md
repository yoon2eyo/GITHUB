# AccessAuthorizationManager 컴포넌트 Design Rationale

## 5.1.4. Design Rationale

본 절에서는 `AccessAuthorizationManager` 컴포넌트의 내부 설계가 시스템의 Quality Attributes (QA)를 달성하기 위해 어떻게 정당화되었는지 설명합니다. 각 QA에 대해 직접적으로 기여하는 design elements를 나열하고, 적용된 Pattern 및 Tactic을 구체적으로 제시하며, 고려된 다른 설계 후보와 비교하여 최적의 설계임을 정당화합니다.

---

## 1. QAS-02: 성능 (Performance) 달성 정당화

### 1.1 QA 요구사항

**목표**: 안면인식 출입 인증의 95%가 3초 이내 완료되어야 함
- 평균 응답 시간: ~320ms (목표의 10.7%)
- 게이트 개방: 판독 성공 후 1초 이내
- 피크 부하: 20 TPS (초당 20건) 처리 가능

### 1.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `AccessAuthorizationManager` | 출입 인증 로직 조율 및 결정 | 핵심 |
| `FaceVectorCache` | 인메모리 캐시를 통한 벡터 데이터 조회 | 핵심 |
| `IFaceModelServiceClient` | FaceModel Service IPC 통신 추상화 | 핵심 |
| `IGateControlService` | 게이트 제어 명령 전송 추상화 | 보조 |
| `IAccessEventPublisher` | 비동기 이벤트 발행 | 보조 |

### 1.3 적용된 Pattern 및 Tactic

#### 1.3.1 Data Pre-Fetching Tactic (DD-05)

**설계 결정**: `FaceVectorCache`를 통한 인메모리 캐싱

**구현 방식**:
- 시스템 시작 시 상위 10,000개 활성 안면 벡터를 메모리에 사전 로드
- `@PostConstruct`를 통한 자동 캐시 워밍업
- `ConcurrentHashMap`을 사용한 Thread-Safe 캐시 관리
- LRU eviction 정책 및 24시간 TTL 적용

**성능 효과**:
- 캐시 히트율: >90%
- 데이터 조회 시간: 50ms (DB) → 5ms (캐시) = **90% 감소**
- Hot Path에서 DB I/O 제거

**정당화**:
- **대안 1: Write-Through Cache**
  - 데이터 변경 시 즉시 캐시 업데이트로 일관성 보장
  - 구현 복잡도 증가로 유지보수성 저하
  - **결론**: Cache-Aside Pattern이 더 적합

- **대안 2: Redis 외부 캐시**
  - 네트워크 지연 (~2-5ms)으로 성능 저하
  - 추가 인프라로 운영 복잡도 증가
  - **결론**: 인메모리 캐시가 더 빠름

**적용된 Pattern**: **Cache-Aside Pattern**
- 캐시 미스 시 DB 조회 후 캐시 업데이트
- 캐시와 DB 간 일관성 관리

#### 1.3.2 Same Physical Node Tactic (DD-05)

**설계 결정**: `IFaceModelServiceClient` 인터페이스를 통한 IPC/gRPC 통신

**구현 방식**:
- `AccessAuthorizationManager`와 `FaceModelService`를 동일 물리 노드에 배치
- `FaceModelServiceIPCClient`가 `IFaceModelServiceClient` 인터페이스 구현
- IPC/gRPC를 통한 로컬 통신 (localhost)

**성능 효과**:
- 네트워크 지연 제거: HTTP REST 대비 10-20ms 감소
- 직렬화/역직렬화 오버헤드 최소화
- 공유 메모리 활용 가능

**정당화**:
- **대안 1: HTTP REST 통신**
  - 네트워크 지연 (~10-20ms)으로 성능 저하
  - TCP 연결 오버헤드 및 직렬화 비용 증가
  - **결론**: IPC/gRPC가 더 빠름

- **대안 2: 메시지 큐 (비동기)**
  - 비동기 처리로 응답 시간 증가
  - 실시간 인증 요구사항에 부적합
  - **결론**: 동기 IPC가 적합

**적용된 Pattern**: **Strategy Pattern**
- `IFaceModelServiceClient` 인터페이스로 다양한 구현체 교체 가능
- 테스트 시 Mock 객체 주입 용이

#### 1.3.3 Pipeline Optimization Tactic (DD-05)

**설계 결정**: `IFaceModelServiceClient`를 통한 병렬 특징점 추출 요청

**구현 방식**:
- `VectorComparisonEngine` (FaceModel Service)에서 `CompletableFuture` 사용
- 요청된 이미지와 저장된 벡터의 특징점 추출을 병렬 처리
- 두 특징 벡터의 코사인 유사도 계산

**성능 효과**:
- 순차 처리: 405ms → 병렬 처리: 205ms
- **49% 지연 감소**
- 전체 응답 시간의 64% 차지하는 병목 구간 최적화

**정당화**:
- **대안: 멀티스레드 풀 직접 관리**
  - `CompletableFuture` 대비 구현 복잡도 증가
  - 스레드 풀 관리 부담으로 운영 복잡도 상승
  - **결론**: `CompletableFuture`가 더 간단하고 효율적

**적용된 Pattern**: **Pipeline Pattern**
- 독립적인 처리 단계를 파이프라인으로 구성
- 병렬화를 통한 처리 시간 단축

#### 1.3.4 비동기 이벤트 발행

**설계 결정**: `IAccessEventPublisher`를 통한 비동기 이벤트 발행

**구현 방식**:
- `AccessEventProcessor`가 `IAccessEventPublisher` 인터페이스 구현
- RabbitMQ를 통한 비동기 메시지 발행
- 핫 패스에서 논블로킹 처리

**성능 효과**:
- 이벤트 발행 시간: ~10ms (논블로킹)
- 핫 패스 영향 최소화
- 전체 응답 시간의 3.1%만 차지

**정당화**:
- **대안: 동기 이벤트 발행**
  - RabbitMQ 응답 대기 (~50-100ms)로 핫 패스 지연 증가
  - 전체 응답 시간 증가로 성능 목표 위반
  - **결론**: 비동기 처리가 필수적

**적용된 Pattern**: **Observer Pattern**
- 이벤트 기반 Pub/Sub 구조
- 느슨한 결합 유지

### 1.4 종합 정당화

**총 응답 시간 분석**:
- 캐시 조회: ~5ms (1.6%)
- IPC 호출: ~205ms (64.1%)
- 게이트 제어: ~100ms (31.2%)
- 이벤트 발행: ~10ms (3.1%)
- **총계: ~320ms << 3초 (목표의 10.7%)**

**최적 설계 근거**:
1. **Data Pre-Fetching**: DB I/O를 Hot Path에서 완전히 제거
2. **Same Physical Node**: 네트워크 지연 제거
3. **Pipeline Optimization**: 가장 큰 병목 구간 최적화
4. **비동기 이벤트**: 핫 패스 영향 최소화

**대안 설계와 비교**:
- **단일 프로세스 설계**: 성능은 우수하나 마이크로서비스 원칙 위배
- **동기식 설계**: 구현은 간단하나 성능 목표 달성 불가
- **캐시 없는 설계**: DB 병목으로 3초 목표 달성 불가

**결론**: 현재 설계는 QAS-02 목표를 **10.7%의 시간**으로 달성하여 여유있게 목표를 초과 달성하며, 마이크로서비스 아키텍처 원칙도 준수합니다.

---

## 2. QAS-04: 보안 (Security) 달성 정당화

### 2.1 QA 요구사항

**목표**: 모든 출입 시도에 대한 감사 추적 (Audit Trail) 유지
- 모든 출입 시도(허가/거부) 기록
- 사용자 ID, 지점 ID, 설비 ID, 유사도 점수, 타임스탬프 포함
- 보안 사고 발생 시 추적 가능

### 2.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `AccessAuthorizationManager` | 출입 시도 감지 및 이벤트 트리거 | 핵심 |
| `IAccessEventPublisher` | 이벤트 발행 인터페이스 | 핵심 |
| `AccessGrantedEvent` | 출입 허가 이벤트 도메인 모델 | 핵심 |
| `AccessDeniedEvent` | 출입 거부 이벤트 도메인 모델 | 핵심 |

### 2.3 적용된 Pattern 및 Tactic

#### 2.3.1 Event-Based Architecture (DD-02)

**설계 결정**: `IAccessEventPublisher`를 통한 이벤트 기반 감사 추적

**구현 방식**:
- 모든 출입 시도에 대해 `AccessGrantedEvent` 또는 `AccessDeniedEvent` 발행
- 이벤트에 다음 정보 포함:
  - `userId`: 사용자 ID
  - `branchId`: 지점 ID
  - `equipmentId`: 설비 ID
  - `similarityScore`: 유사도 점수
  - `timestamp`: 처리 시간
  - `result`: GRANTED/DENIED
  - `reason`: 거부 사유 (DENIED 시)

**보안 효과**:
- 모든 출입 시도에 대한 완전한 감사 추적
- 비정상 패턴 분석 가능
- 보안 사고 발생 시 추적 가능

**정당화**:
- **대안 1: 동기식 DB 직접 저장**
  - 핫 패스 지연 증가 (~50-100ms)로 성능 저하
  - DB 장애 시 감사 추적 실패로 보안 위험
  - **결론**: 비동기 이벤트가 더 적합

- **대안 2: 로그 파일 기록**
  - 파일 I/O 오버헤드로 실시간 분석 어려움
  - 구조화되지 않아 보안 패턴 분석 불가
  - **결론**: 구조화된 이벤트가 더 적합

**적용된 Pattern**: **Observer Pattern**
- 이벤트 발행자와 구독자 간 느슨한 결합
- 여러 구독자(보안 모니터링, MLOps 등)가 동시에 이벤트 처리 가능

#### 2.3.2 Durable Queue (DD-02)

**설계 결정**: RabbitMQ Durable Queue 사용

**구현 방식**:
- `AccessEventProcessor`가 RabbitMQ Durable Queue에 이벤트 발행
- 서비스 재시작 시에도 이벤트 손실 방지
- Passive Redundancy 적용

**보안 효과**:
- 이벤트 손실 방지
- 감사 추적의 완전성 보장

**정당화**:
- **대안: 메모리 큐**
  - 서비스 재시작 시 이벤트 손실로 감사 추적 불완전
  - 휘발성 저장소로 데이터 영구성 보장 불가
  - **결론**: Durable Queue가 필수

**적용된 Tactic**: **Passive Redundancy** (DD-02)
- 이벤트를 영구 저장소에 보관하여 가용성 보장

### 2.4 종합 정당화

**최적 설계 근거**:
1. **이벤트 기반 아키텍처**: 느슨한 결합으로 확장성 확보
2. **비동기 처리**: 성능 영향 최소화
3. **Durable Queue**: 이벤트 손실 방지
4. **구조화된 이벤트**: 분석 및 추적 용이

**대안 설계와 비교**:
- **동기식 DB 저장**: 성능 저하로 QAS-02 위배
- **로그 파일**: 구조화되지 않아 분석 어려움
- **메모리 큐**: 이벤트 손실 위험

**결론**: 현재 설계는 QAS-04 요구사항을 완전히 만족하며, QAS-02 성능 목표와도 충돌하지 않습니다.

---

## 3. QAS-05: 가용성 (Availability) 달성 정당화

### 3.1 QA 요구사항

**목표**: 주요 서비스 자동 복구 시간 보장
- `FaceModelService` 장애 시에도 QR 코드 인증은 정상 동작
- 캐시 미스 시 DB 조회로 폴백 처리
- 게이트 제어 실패 시 재시도 로직 적용

### 3.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `AccessAuthorizationManager` | 장애 상황 처리 및 폴백 로직 | 핵심 |
| `FaceVectorCache` | 캐시 미스 시 DB 폴백 | 보조 |
| `IAccessEventPublisher` | Durable Queue를 통한 이벤트 보존 | 보조 |

### 3.3 적용된 Pattern 및 Tactic

#### 3.3.1 장애 격리 (Fault Isolation)

**설계 결정**: 인터페이스 기반 의존성으로 장애 격리

**구현 방식**:
- `IFaceModelServiceClient` 인터페이스를 통한 의존성
- `FaceModelService` 장애 시 예외 처리 및 빠른 실패
- QR 코드 인증은 독립적으로 동작

**가용성 효과**:
- `FaceModelService` 장애가 전체 시스템에 전파되지 않음
- QR 코드 인증으로 대체 수단 제공

**정당화**:
- **대안: 직접 의존성**
  - `FaceModelService`에 직접 의존으로 장애 전파
  - 인터페이스 없이 강한 결합으로 격리 불가
  - **결론**: 인터페이스 기반 설계가 필수

**적용된 Pattern**: **Circuit Breaker Pattern** (구현체에서)
- 장애 감지 시 빠른 실패 반환

#### 3.3.2 폴백 처리 (Fallback)

**설계 결정**: 캐시 미스 시 DB 조회로 폴백

**구현 방식**:
- `FaceVectorCache.getActiveVector()`에서 캐시 미스 시 `VectorRepository`를 통한 DB 조회
- 조회한 벡터를 캐시에 업데이트

**가용성 효과**:
- 캐시 장애 시에도 서비스 정상 동작
- 데이터 가용성 보장

**정당화**:
- **대안: 캐시만 사용**
  - 캐시 장애 시 서비스 완전 중단으로 가용성 저하
  - 데이터 접근성 보장 불가
  - **결론**: 폴백 처리가 필수

**적용된 Pattern**: **Cache-Aside Pattern**
- 캐시 미스 시 DB 조회 후 캐시 업데이트

### 3.4 종합 정당화

**최적 설계 근거**:
1. **인터페이스 기반 설계**: 장애 격리 및 독립 배포 가능
2. **폴백 처리**: 단일 장애점 제거
3. **Durable Queue**: 이벤트 손실 방지

**대안 설계와 비교**:
- **강한 결합**: 장애 전파 위험
- **단일 장애점**: 가용성 저하

**결론**: 현재 설계는 QAS-05 요구사항을 만족하며, 장애 격리와 폴백 처리를 통해 가용성을 보장합니다.

---

## 4. QAS-06: 유지보수성 (Modifiability) 달성 정당화

### 4.1 QA 요구사항

**목표**: 새로운 인증 방법 추가 및 구현체 변경 시 컴포넌트 수정 최소화
- 인터페이스 기반 설계로 구현체 교체 가능
- 새로운 인증 방법 추가 시 인터페이스 확장으로 대응
- 단일 책임 원칙 준수

### 4.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `IAccessAuthorizationService` | Provided Interface 정의 | 핵심 |
| `IFaceModelServiceClient` | Required Interface 추상화 | 핵심 |
| `IGateControlService` | Required Interface 추상화 | 핵심 |
| `IAccessEventPublisher` | Required Interface 추상화 | 핵심 |
| `AccessAuthorizationManager` | 단일 책임 원칙 준수 | 핵심 |

### 4.3 적용된 Pattern 및 Tactic

#### 4.3.1 Dependency Inversion Principle (DIP)

**설계 결정**: 모든 의존성을 인터페이스를 통해 주입

**구현 방식**:
```java
private final IFaceModelServiceClient faceModelServiceClient;
private final IGateControlService gateControlService;
private final IAccessEventPublisher accessEventPublisher;
```

**유지보수성 효과**:
- 구현체 변경 시 `AccessAuthorizationManager` 수정 불필요
- 테스트 시 Mock 객체 주입 용이
- 다양한 구현체 교체 가능

**정당화**:
- **대안: 구현체에 직접 의존**
  ```java
  private final FaceModelServiceIPCClient faceModelServiceClient;
  ```
  - 구현체 변경 시 컴포넌트 수정 필요로 유지보수 비용 증가
  - 테스트 시 Mock 객체 주입 불가로 단위 테스트 어려움
  - **결론**: 인터페이스 의존이 필수

**적용된 Pattern**: **Strategy Pattern**
- 알고리즘(구현체)을 런타임에 교체 가능
- `IFaceModelServiceClient`, `IGateControlService` 등

#### 4.3.2 Single Responsibility Principle (SRP)

**설계 결정**: `AccessAuthorizationManager`는 출입 인증 결정만 담당

**구현 방식**:
- 게이트 제어는 `IGateControlService`에 위임
- 이벤트 발행은 `IAccessEventPublisher`에 위임
- 벡터 조회는 `FaceVectorCache`에 위임
- 유사도 계산은 `IFaceModelServiceClient`에 위임

**유지보수성 효과**:
- 각 컴포넌트의 책임이 명확
- 변경 영향 범위 최소화
- 테스트 용이성 향상

**정당화**:
- **대안: 모든 로직을 한 클래스에 구현**
  - 클래스 크기 증가로 가독성 저하
  - 변경 영향 범위 확대 및 테스트 어려움
  - 단일 책임 원칙 위배로 유지보수성 저하
  - **결론**: 단일 책임 원칙이 필수

**적용된 Pattern**: **Facade Pattern**
- 복잡한 출입 인증 프로세스를 단순한 인터페이스로 제공
- 내부 복잡성을 숨기고 클라이언트는 간단한 API만 사용

#### 4.3.3 Open/Closed Principle (OCP)

**설계 결정**: 인터페이스 확장으로 새로운 기능 추가

**구현 방식**:
- 새로운 인증 방법 추가 시 `IAccessAuthorizationService` 인터페이스에 메서드 추가
- 기존 코드 수정 없이 새로운 구현 추가 가능

**유지보수성 효과**:
- 기존 코드 수정 최소화
- 확장성 확보

**정당화**:
- **대안: 기존 메서드 수정**
  - `IAccessAuthorizationService`에 새로운 메서드 추가 대신 기존 메서드 변경
  - 기존 코드 영향으로 회귀 테스트 필요 및 리스크 증가
  - 클라이언트 코드 수정 필요로 유지보수성 저하
  - **결론**: 인터페이스 확장이 더 안전

**적용된 Pattern**: **Strategy Pattern**
- 새로운 인증 전략을 추가로 구현 가능

### 4.4 종합 정당화

**최적 설계 근거**:
1. **인터페이스 기반 설계**: 구현체 교체 용이
2. **단일 책임 원칙**: 변경 영향 범위 최소화
3. **개방/폐쇄 원칙**: 확장성 확보

**대안 설계와 비교**:
- **구현체 의존**: 변경 시 컴포넌트 수정 필요
- **과도한 책임**: 변경 영향 범위 확대

**결론**: 현재 설계는 QAS-06 요구사항을 완전히 만족하며, SOLID 원칙을 준수하여 유지보수성을 극대화합니다.

---

## 5. 종합 Design Rationale

### 5.1 설계 결정의 일관성

모든 QA 달성을 위한 설계 결정들이 서로 일관되게 작동합니다:

1. **인터페이스 기반 설계**: 
   - QAS-06 (유지보수성) 달성
   - QAS-05 (가용성) 장애 격리 지원
   - QAS-02 (성능) Strategy Pattern으로 최적 구현체 선택 가능

2. **이벤트 기반 아키텍처**:
   - QAS-04 (보안) 감사 추적 달성
   - QAS-02 (성능) 비동기 처리로 성능 영향 최소화
   - QAS-06 (유지보수성) 느슨한 결합으로 확장성 확보

3. **캐시 전략**:
   - QAS-02 (성능) 핵심 병목 구간 최적화
   - QAS-05 (가용성) 폴백 처리로 가용성 보장

### 5.2 Trade-off 분석

| 설계 결정 | 장점 | 단점 | 선택 근거 |
|----------|------|------|----------|
| **인메모리 캐시** | 빠른 조회 (~5ms) | 메모리 사용 (~500MB) | 성능 목표 달성 필수 |
| **IPC/gRPC** | 낮은 지연 (~10ms) | 배치 제약 (동일 노드) | 성능 목표 달성 필수 |
| **비동기 이벤트** | 성능 영향 최소화 | 이벤트 순서 보장 어려움 | 감사 추적은 순서보다 완전성이 중요 |
| **인터페이스 의존** | 유지보수성 향상 | 약간의 추상화 오버헤드 | 유지보수성 > 미세한 성능 |

### 5.3 최종 정당화

**AccessAuthorizationManager** 컴포넌트의 설계는 다음과 같은 이유로 최적입니다:

1. **QAS-02 (성능)**: 목표 3초 대비 10.7% 시간으로 초과 달성
   - Data Pre-Fetching, Same Physical Node, Pipeline Optimization 전술 적용
   - 비동기 이벤트로 핫 패스 영향 최소화

2. **QAS-04 (보안)**: 완전한 감사 추적 달성
   - 이벤트 기반 아키텍처로 모든 출입 시도 기록
   - Durable Queue로 이벤트 손실 방지

3. **QAS-05 (가용성)**: 장애 격리 및 폴백 처리
   - 인터페이스 기반 설계로 장애 격리
   - 캐시 미스 시 DB 폴백

4. **QAS-06 (유지보수성)**: SOLID 원칙 준수
   - Dependency Inversion, Single Responsibility, Open/Closed 원칙 적용
   - Strategy, Facade, Observer Pattern 적용

**결론**: 현재 설계는 모든 QA 요구사항을 동시에 만족하며, 각 QA 간의 Trade-off를 최적화한 설계입니다. 고려된 대안 설계들과 비교하여 성능, 보안, 가용성, 유지보수성 모든 측면에서 우수한 설계임을 정당화합니다.

