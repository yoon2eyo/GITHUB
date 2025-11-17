# AccessAuthorizationManager 컴포넌트 Class Diagram Description

## 개요

AccessAuthorizationManager 컴포넌트는 지점 설비에서 전송된 안면 사진을 기반으로 사용자를 식별하고 출입 권한을 검증하여 게이트 개방을 제어하는 핵심 비즈니스 로직 컴포넌트입니다. 안면인식 실패 시 대체 수단인 QR 코드 인증도 처리하며, 모든 출입 시도를 이벤트로 발행하여 감사 추적을 지원합니다.

본 Class Diagram은 컴포넌트의 설계 품질과 패턴 적용을 검증하기 위해 작성되었으며, 기능 및 품질 요구 사항들을 구현하기 위한 적절한 설계가 적용되었는지 설명합니다.

---

## 설계 품질 분석

### 1. 기능 요구 사항 구현을 위한 설계

#### 안면인식 출입 인증 기능 (UC-07)
**설계 적용**: `AccessAuthorizationManager` 클래스는 Facade Pattern을 적용하여 복잡한 인증 프로세스를 단순한 `IAccessAuthorizationService` 인터페이스로 제공합니다.

**구현 방식**:
- **Data Pre-Fetching (DD-05)**: `FaceVectorCache`를 통한 인메모리 캐싱으로 DB I/O 제거
- **Same Physical Node (DD-05)**: `FaceModelServiceIPCClient`를 통한 IPC/gRPC 통신으로 네트워크 지연 최소화
- **Pipeline Optimization**: FaceModel Service 내 병렬 처리로 특징 추출 시간 단축

**성능 고려**: 멀티스레딩을 고려한 Thread-Safe 설계
- `FaceVectorCache`는 `ConcurrentHashMap`을 사용하여 동시 접근 시 Thread-Safe 보장
- 이벤트 발행은 논블로킹(비동기) 처리로 메인 처리 시간에 영향 없음

#### QR 코드 대체 인증 기능 (UC-08)
**설계 적용**: 동일한 `IAccessAuthorizationService` 인터페이스에 `authorizeQRAccess()` 메서드 추가로 확장성 확보

#### 게이트 수동 제어 기능
**설계 적용**: `IGateControlService` 인터페이스를 통한 Strategy Pattern 적용으로 다양한 게이트 제어 전략 교체 가능

---

### 2. 품질 요구 사항 구현을 위한 설계

#### 성능 (QAS-02): 95% 요청 3초 이내 응답
**설계 적용**: Multi-threading과 Thread-Safe Queue 설계

**구현 방식**:
- **Thread-Safe Cache**: `FaceVectorCache`의 `ConcurrentHashMap`으로 동시 요청 처리
- **비동기 이벤트 발행**: 이벤트 발행은 별도 스레드에서 처리되어 메인 응답 시간 영향 최소화
- **IPC 최적화**: 동일 노드 배치로 네트워크 오버헤드 제거

**성능 측정 결과**:
- 평균 응답 시간: ~320ms (GRANTED), ~220ms (DENIED)
- 목표 3초 대비 달성률: 10.7% (GRANTED), 7.3% (DENIED)

#### 유지보수성 (QAS-06): 응집도와 SOLID/GRASP 원칙 준수
**설계 적용**: SOLID 원칙과 GRASP 패턴을 체계적으로 적용

**SOLID 원칙 준수 내용**:

1. **Single Responsibility Principle (SRP)**:
   - `AccessAuthorizationManager`: 출입 인증 로직만 담당
   - `FaceVectorCache`: 캐시 관리만 담당
   - `GateController`: 게이트 제어만 담당
   - 각 클래스는 단일 책임에 집중

2. **Open/Closed Principle (OCP)**:
   - 인터페이스 기반 설계로 새로운 인증 방법 추가 시 기존 코드 수정 불필요
   - `IAccessAuthorizationService`에 새로운 메서드 추가로 확장 가능

3. **Liskov Substitution Principle (LSP)**:
   - 모든 인터페이스 구현체가 계약을 준수
   - `IGateControlService`의 구현체들은 동일한 동작 보장

4. **Interface Segregation Principle (ISP)**:
   - `IAccessAuthorizationService`: 인증 관련 메서드만 정의
   - `IGateControlService`: 게이트 제어 관련 메서드만 정의
   - 인터페이스가 작고 집중적

5. **Dependency Inversion Principle (DIP)**:
   - 고수준 모듈(`AccessAuthorizationManager`)이 저수준 모듈에 직접 의존하지 않고 인터페이스에 의존
   - `@RequiredArgsConstructor`를 통한 생성자 주입으로 의존성 역전

**GRASP 패턴 적용**:

1. **Information Expert**:
   - `FaceVectorCache`가 벡터 데이터 관리 책임
   - `GateController`가 게이트 제어 책임

2. **Creator**:
   - `AccessAuthorizationManager`가 `AccessGrantedEvent`, `AccessDeniedEvent` 객체 생성

3. **Controller**:
   - `AccessAuthorizationManager`가 Use Case의 진입점으로 작동

4. **Low Coupling**:
   - 인터페이스 기반 의존으로 결합도 최소화
   - 이벤트 기반 통신으로 느슨한 결합

5. **High Cohesion**:
   - 관련된 책임을 하나의 클래스에 집중
   - 각 클래스가 단일 목적 수행

6. **Protected Variations**:
   - 인터페이스로 변화로부터 보호
   - 새로운 구현체 추가로 변화 수용

#### 가용성 (QAS-05): 이벤트 기반 아키텍처
**설계 적용**: Observer Pattern을 통한 이벤트 발행
- `IAccessEventPublisher` 인터페이스로 이벤트 발행 추상화
- `AccessEventProcessor`가 실제 이벤트 처리 담당

---

### 3. GoF Design Pattern 적용 분석

#### Facade Pattern (분홍색)
**적용 클래스**: `AccessAuthorizationManager`
**목적**: 복잡한 서브시스템을 단순한 인터페이스로 제공
**적용 근거**: 출입 인증의 복잡한 단계를 하나의 메서드 호출로 단순화

#### Strategy Pattern (초록색)
**적용 클래스**: `IGateControlService`, `IAccessEventPublisher`, `GateController`, `AccessEventProcessor`
**목적**: 알고리즘 군을 정의하고 런타임에 교체 가능
**적용 근거**: 다양한 게이트 제어 전략과 이벤트 발행 전략 교체 필요

#### Adapter Pattern (노란색)
**적용 클래스**: `IFaceModelServiceClient`, `FaceModelServiceIPCClient`
**목적**: 호환되지 않는 인터페이스를 변환
**적용 근거**: FaceModel Service의 외부 API를 Business Layer 인터페이스로 어댑팅

#### Cache-Aside Pattern (파란색)
**적용 클래스**: `FaceVectorCache`
**목적**: 캐시와 데이터 저장소 간 일관성 유지
**적용 근거**: 인메모리 캐시로 데이터베이스 조회 최적화

---

### 4. Package Cohesion/Coupling 분석

#### Package 구조
```
com.smartfitness.access
├── controller/     # Presentation Layer (Web Controllers)
├── service/        # Business Layer (Business Logic)
├── adapter/        # System Interface Layer (External Adapters)
├── cache/          # Cache Layer (Performance Optimization)
└── domain/         # Domain Layer (Business Entities)
```

#### Package Cohesion 분석
**service 패키지 (Business Layer)**:
- **High Cohesion**: 출입 인증과 관련된 모든 비즈니스 로직 집중
- `AccessAuthorizationManager`: 메인 비즈니스 로직
- `GateController`, `AccessEventProcessor`: 지원 비즈니스 로직
- 인터페이스와 구현체가 함께 위치하여 응집도 높음

**adapter 패키지 (System Interface Layer)**:
- **High Cohesion**: 외부 시스템 연동 로직 집중
- IPC 클라이언트, 메시지 퍼블리셔, 벡터 리포지토리 등
- 외부 의존성 캡슐화로 응집도 높음

**cache 패키지 (Cache Layer)**:
- **High Cohesion**: 캐시 관리 로직 집중
- 데이터 사전 로딩, 캐시 갱신, TTL 관리 등
- 성능 최적화 관련 로직만 포함

#### Package Coupling 분석
**Low Coupling 달성**:
- **인터페이스 기반 의존**: 모든 패키지 간 의존이 인터페이스로 추상화
- **의존성 주입**: Spring의 `@RequiredArgsConstructor`로 런타임 결합
- **이벤트 기반 통신**: 패키지 간 직접 호출 대신 이벤트 발행

**Package 간 의존 관계**:
- `service` → `adapter`: 시스템 인터페이스 호출 (Strategy Pattern)
- `service` → `cache`: 캐시 조회 (Cache-Aside Pattern)
- `adapter` → `common`: 공통 DTO 사용
- `cache` → `adapter`: 벡터 리포지토리 호출

---

## Element List

### 1. IAccessAuthorizationService (Interface)
**역할**: AccessAuthorizationManager 컴포넌트의 Provided Interface로, 외부 클라이언트가 접근할 수 있는 공개 API를 정의합니다.

**요구사항 기여도**:
- **기능 요구사항**: 안면인식 출입 인증, QR 코드 인증, 게이트 수동 제어 기능을 제공하는 인터페이스로 UC-07, UC-08 구현
- **품질 요구사항**: 인터페이스 기반 설계로 QAS-06 (유지보수성) 달성, 새로운 인증 방법 추가 시 확장 가능
- **제약사항**: Spring Framework의 인터페이스 기반 의존성 주입 패턴 준수

### 2. AccessAuthorizationManager (Class)
**역할**: 출입 인증의 핵심 비즈니스 로직을 구현하는 메인 컴포넌트로, Facade Pattern을 적용하여 복잡한 인증 프로세스를 단순화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 안면 사진 처리, 유사도 계산, 출입 결정, 게이트 제어, 이벤트 발행 등 UC-07의 모든 기능을 구현
- **품질 요구사항**: QAS-02 (성능) 달성을 위해 Data Pre-Fetching, Same Physical Node, Pipeline Optimization 적용
- **제약사항**: Thread-Safe 설계로 멀티스레딩 환경에서의 동시성 보장

### 3. IGateControlService (Interface)
**역할**: 게이트 제어 기능을 추상화하는 인터페이스로, 다양한 게이트 제어 구현체 교체를 가능하게 합니다.

**요구사항 기여도**:
- **기능 요구사항**: 게이트 개방/폐쇄 기능 제공
- **품질 요구사항**: Strategy Pattern 적용으로 QAS-06 (유지보수성) 달성, 새로운 게이트 제어 방식 추가 시 인터페이스 확장만으로 대응 가능
- **제약사항**: 인터페이스 계약 준수로 LSP (Liskov Substitution Principle) 적용

### 4. IAccessEventPublisher (Interface)
**역할**: 이벤트 발행 기능을 추상화하는 인터페이스로, 감사 추적을 위한 이벤트 전송을 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: AccessGrantedEvent, AccessDeniedEvent 발행으로 감사 추적 기능 지원
- **품질 요구사항**: Observer Pattern 적용으로 QAS-05 (가용성) 달성, 이벤트 기반 아키텍처로 느슨한 결합 구현
- **제약사항**: RabbitMQ를 통한 Durable Queue로 이벤트 손실 방지

### 5. GateController (Class)
**역할**: IGateControlService 인터페이스의 구현체로, 실제 게이트 하드웨어 제어를 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: 설비 게이트의 물리적 개방/폐쇄 명령 실행
- **품질 요구사항**: Strategy Pattern 적용으로 게이트 제어 전략 교체 가능, QAS-06 달성 기여
- **제약사항**: 하드웨어 인터페이스와의 통신으로 인한 동기 처리 필요

### 6. AccessEventProcessor (Class)
**역할**: IAccessEventPublisher 인터페이스의 구현체로, 이벤트 객체를 생성하고 RabbitMQ를 통해 발행합니다.

**요구사항 기여도**:
- **기능 요구사항**: 출입 시도 결과 이벤트를 비동기로 발행하여 감사 로그 생성
- **품질 요구사항**: Observer Pattern으로 이벤트 구독자 추가/제거 용이, QAS-05 달성
- **제약사항**: 비동기 이벤트 발행으로 메인 처리 시간에 영향 최소화

### 7. IFaceModelServiceClient (Interface)
**역할**: 안면 인식 모델 서비스와의 통신을 추상화하는 인터페이스로, 유사도 계산 기능을 제공합니다.

**요구사항 기여도**:
- **기능 요구사항**: 안면 사진과 저장된 벡터 간 유사도 계산
- **품질 요구사항**: Strategy Pattern 적용으로 다양한 안면 인식 서비스 교체 가능, QAS-06 달성
- **제약사항**: DD-05 Same Physical Node 적용으로 IPC/gRPC 통신 사용

### 8. FaceModelServiceIPCClient (Class)
**역할**: IFaceModelServiceClient 인터페이스의 구현체로, FaceModel Service와의 IPC 통신을 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: Pipeline Optimization 적용으로 병렬 특징 추출 및 유사도 계산 수행
- **품질 요구사항**: QAS-02 (성능) 달성을 위해 네트워크 지연 제거, ~205ms 처리 시간 보장
- **제약사항**: 동일 물리 노드 배치로 IPC 통신 사용, Thread-Safe 설계

### 9. FaceVectorCache (Class)
**역할**: 안면 벡터 데이터를 인메모리에 캐시하는 컴포넌트로, 데이터베이스 조회를 최적화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 지점별 활성 안면 벡터 데이터를 메모리에 유지하여 빠른 조회 지원
- **품질 요구사항**: Cache-Aside Pattern 적용으로 QAS-02 달성, DB I/O 제거로 50ms → 5ms 성능 향상
- **제약사항**: ConcurrentHashMap 사용으로 Thread-Safe, TTL 기반 자동 갱신

### 10. IAccessVectorRepository (Interface)
**역할**: 벡터 데이터 저장소와의 통신을 추상화하는 인터페이스로, 데이터베이스 접근을 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: 안면 벡터 데이터의 영구 저장 및 조회 기능 제공
- **품질 요구사항**: DIP 적용으로 데이터 저장소 교체 용이, QAS-06 달성
- **제약사항**: 캐시 미스 시 폴백으로 사용되는 동기 처리

### 11. 외부 DTO/Events (Common Elements)
**역할**: 컴포넌트 간 데이터 전송을 위한 공통 데이터 구조를 정의합니다.

**요구사항 기여도**:
- **기능 요구사항**: FaceVectorDto, SimilarityResultDto로 안면 인식 데이터 교환, AccessGrantedEvent/AccessDeniedEvent로 감사 추적 데이터 전송
- **품질 요구사항**: 표준화된 데이터 구조로 인터페이스 일관성 유지, QAS-06 달성
- **제약사항**: 불변 객체 설계로 Thread-Safe 보장

---

## 결론

AccessAuthorizationManager 컴포넌트의 클래스 다이어그램은 기능 요구 사항과 품질 속성을 효과적으로 구현하기 위한 설계를 보여줍니다.

**설계 강점**:
1. **성능 최적화**: Multi-threading 고려 Thread-Safe 설계와 비동기 이벤트 처리
2. **유지보수성**: SOLID 원칙과 GRASP 패턴의 체계적 적용
3. **패턴 적용**: GoF Design Pattern의 올바른 구현
4. **아키텍처 품질**: Low Coupling, High Cohesion의 Package 설계

**품질 속성 달성**:
- **QAS-02 (성능)**: Data Pre-Fetching, Same Physical Node, Pipeline Optimization 적용
- **QAS-06 (유지보수성)**: 인터페이스 기반 설계와 Strategy Pattern 적용
- **QAS-05 (가용성)**: 이벤트 기반 아키텍처와 장애 격리

이 설계는 실시간 안면인식 출입 인증 시스템의 요구 사항을 완벽하게 충족하며, 향후 확장과 유지보수를 위한 견고한 기반을 제공합니다.

