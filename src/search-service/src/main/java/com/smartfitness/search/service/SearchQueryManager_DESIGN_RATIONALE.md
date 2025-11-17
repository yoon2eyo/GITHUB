# SearchQueryManager 컴포넌트 Design Rationale

## 5.2.4. Design Rationale

본 절에서는 `SearchQueryManager` 컴포넌트의 내부 설계가 시스템의 Quality Attributes (QA)를 달성하기 위해 어떻게 정당화되었는지 설명합니다. 각 QA에 대해 직접적으로 기여하는 design elements를 나열하고, 적용된 Pattern 및 Tactic을 구체적으로 제시하며, 고려된 다른 설계 후보와 비교하여 최적의 설계임을 정당화합니다.

---

## 1. QAS-03: 실시간성 (Real-Time Performance) 달성 정당화

### 1.1 QA 요구사항

**목표**: 자연어 검색 쿼리의 95%가 3초 이내 완료되어야 함
- 평균 응답 시간: ~530ms (목표의 17.7%)
- 99% 목표: 99%가 5초 이내 완료
- 피크 부하: 50 TPS (초당 50건) 처리 가능

### 1.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `SearchQueryManager` | 검색 쿼리 처리 조율 및 Hot/Cold Path 분리 | 핵심 |
| `SimpleKeywordTokenizer` | LLM 없이 빠른 키워드 토큰화 | 핵심 |
| `ISearchEngineClient` / `SearchEngineAdapter` | ElasticSearch 검색 추상화 | 핵심 |
| `IMessagePublisherService` / `RabbitMQAdapter` | 비동기 이벤트 발행 | 보조 |
| `SearchQueryEvent` | Hot/Cold Path 분리를 위한 이벤트 객체 | 보조 |

### 1.3 적용된 Pattern 및 Tactic

#### 1.3.1 Hot/Cold Path Separation Tactic (DD-09)

**설계 결정**: Hot Path에서 LLM 호출 완전 제거, Cold Path로 비동기 처리 분리

**구현 방식**:
- `SearchQueryManager`가 Hot Path와 Cold Path를 명확히 분리
- Hot Path: `SimpleKeywordTokenizer` → `SearchEngineAdapter` → 결과 반환
- Cold Path: `SearchQueryEvent` 발행 → `SearchQueryImprovementConsumer`가 비동기 처리
- `IMessagePublisherService`를 통한 논블로킹 이벤트 발행

**성능 효과**:
- LLM 호출 제거로 외부 네트워크 지연 제거: ~1000ms → 0ms
- Hot Path 총 처리 시간: ~530ms (목표 3초의 17.7%)
  - 토큰화: ~10ms
  - ElasticSearch 조회: ~500ms
  - 결과 포맷팅: ~10ms
  - 이벤트 발행: ~10ms (비동기, 논블로킹)

**정당화**:
- **대안 1: Hot Path에서 LLM 직접 호출**
  - LLM API 호출 시간: ~1000ms
  - 네트워크 지연: ~50-100ms
  - 총 처리 시간: ~1500ms 이상
  - SLA 위반 위험 (3초 초과 가능성)
  - **결론**: Hot Path에서 LLM 호출은 SLA 보장 불가능

- **대안 2: 동기식 LLM 호출 후 결과 반환**
  - 사용자는 LLM 분석 완료까지 대기
  - 응답 시간: ~1500ms 이상
  - 사용자 경험 저하
  - **결론**: 비동기 분리가 필수

- **대안 3: 모든 쿼리를 Cold Path로 처리**
  - 실시간 응답 불가능
  - 사용자 경험 저하
  - **결론**: Hot Path는 반드시 실시간 응답 필요

**적용된 Pattern**: **Observer Pattern**
- `SearchQueryManager` (Publisher)가 `SearchQueryEvent` 발행
- `SearchQueryImprovementConsumer` (Observer)가 비동기 구독
- Hot Path와 Cold Path의 완전한 분리

#### 1.3.2 Simple Tokenization Tactic

**설계 결정**: `SimpleKeywordTokenizer`를 통한 LLM 없는 빠른 토큰화

**구현 방식**:
- `IQueryTokenizer` 인터페이스를 통한 전략 패턴 적용
- `SimpleKeywordTokenizer`가 공백 분리 및 불용어 제거만 수행
- 인메모리 처리로 외부 의존성 없음

**성능 효과**:
- 토큰화 처리 시간: < 10ms
- LLM 호출 대비 100배 이상 빠름 (~1000ms → ~10ms)
- 외부 네트워크 지연 제거

**정당화**:
- **대안 1: LLM 기반 토큰화 (Hot Path)**
  - 처리 시간: ~1000ms
  - SLA 위반 위험
  - **결론**: Hot Path에 부적합

- **대안 2: 형태소 분석기 (예: KoNLPy)**
  - 처리 시간: ~50-100ms
  - 추가 라이브러리 의존성
  - 초기 로딩 시간 증가
  - **결론**: Simple Tokenization이 더 빠르고 단순

- **대안 3: ElasticSearch 내장 분석기만 사용**
  - 토큰화 로직을 ElasticSearch에 위임
  - 검색 엔진과의 결합도 증가
  - 토큰화 전략 변경 어려움
  - **결론**: Strategy Pattern 적용으로 유연성 확보

**적용된 Pattern**: **Strategy Pattern**
- `IQueryTokenizer` 인터페이스로 다양한 토큰화 전략 교체 가능
- Hot Path: `SimpleKeywordTokenizer` (빠름)
- Cold Path: 향후 LLM 기반 토큰화 전략 추가 가능 (정확도)

#### 1.3.3 ElasticSearch Optimization Tactic

**설계 결정**: `SearchEngineAdapter`를 통한 ElasticSearch 최적화된 검색

**구현 방식**:
- `ISearchEngineClient` 인터페이스를 통한 검색 엔진 추상화
- `SearchEngineAdapter`가 `ISearchEngineRepository`로 어댑팅
- ElasticSearch의 인덱싱된 데이터 활용

**성능 효과**:
- 평균 검색 시간: < 500ms
- RDB LIKE 검색 대비 10-100배 빠름
- 전문 검색 기능 (퍼지 매칭, 하이라이팅 등) 제공

**정당화**:
- **대안 1: RDB LIKE 검색**
  - 처리 시간: ~2000-5000ms
  - 전체 테이블 스캔 필요
  - 인덱스 활용 제한적
  - **결론**: 전문 검색 엔진이 필수

- **대안 2: 직접 ElasticSearch API 호출**
  - Business Layer와 ElasticSearch 강한 결합
  - 검색 엔진 변경 시 전체 수정 필요
  - **결론**: Adapter Pattern으로 결합도 감소

- **대안 3: 검색 결과 캐싱**
  - 동일 쿼리에 대해서는 효과적
  - 다양한 쿼리 패턴에서는 캐시 히트율 낮음
  - **결론**: ElasticSearch 자체 최적화가 더 효과적

**적용된 Pattern**: **Adapter Pattern**, **Strategy Pattern**
- `SearchEngineAdapter`: ElasticSearch API를 Business Layer 인터페이스로 어댑팅
- `ISearchEngineClient`: 다양한 검색 엔진 전략 교체 가능

---

## 2. QAS-02: 성능 (Performance) 달성 정당화

### 2.1 QA 요구사항

**목표**: 검색 쿼리 처리 성능 최적화
- 평균 응답 시간: < 1초
- 처리량: 50 TPS 지원
- 리소스 효율적 운영

### 2.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `SearchQueryManager` | Facade Pattern으로 복잡한 프로세스 단순화 | 핵심 |
| `SimpleKeywordTokenizer` | 빠른 인메모리 토큰화 | 핵심 |
| `SearchEngineAdapter` | ElasticSearch 최적화된 접근 | 핵심 |
| `RabbitMQAdapter` | 비동기 이벤트 발행 (논블로킹) | 보조 |

### 2.3 적용된 Pattern 및 Tactic

#### 2.3.1 Facade Pattern

**설계 결정**: `SearchQueryManager`가 복잡한 검색 프로세스를 단순한 인터페이스로 제공

**구현 방식**:
- `ISearchQueryService` 인터페이스를 통한 단일 진입점 제공
- 내부적으로 토큰화, 검색, 이벤트 발행을 조율
- 클라이언트는 복잡한 내부 구조를 알 필요 없음

**성능 효과**:
- 불필요한 오버헤드 제거
- 명확한 책임 분리로 최적화 포인트 식별 용이
- 코드 재사용성 향상

**정당화**:
- **대안 1: 클라이언트가 직접 각 컴포넌트 호출**
  - 클라이언트 코드 복잡도 증가
  - 비즈니스 로직이 클라이언트로 분산
  - 유지보수 어려움
  - **결론**: Facade Pattern이 적합

- **대안 2: 모든 로직을 하나의 클래스에 집중**
  - 단일 책임 원칙 위배
  - 테스트 어려움
  - 확장성 저하
  - **결론**: 책임 분리가 필요

**적용된 Pattern**: **Facade Pattern**
- `SearchQueryManager`가 복잡한 서브시스템을 단순한 인터페이스로 제공

#### 2.3.2 Asynchronous Event Publishing Tactic

**설계 결정**: Cold Path 이벤트 발행을 비동기 논블로킹으로 처리

**구현 방식**:
- `IMessagePublisherService`를 통한 이벤트 발행
- `RabbitMQAdapter`가 RabbitMQ에 비동기 발행
- Hot Path 응답은 이벤트 발행 완료를 기다리지 않음

**성능 효과**:
- 이벤트 발행 시간: ~10ms (비동기)
- Hot Path 응답 시간에 영향 없음
- 논블로킹 처리로 처리량 향상

**정당화**:
- **대안 1: 동기식 이벤트 발행**
  - 이벤트 발행 완료까지 대기
  - RabbitMQ 지연 시 Hot Path 지연
  - **결론**: 비동기가 필수

- **대안 2: 이벤트 발행 생략**
  - Cold Path 인덱스 개선 불가능
  - 검색 정확도 향상 불가
  - **결론**: 이벤트 발행 필요하나 비동기로 처리

**적용된 Pattern**: **Observer Pattern**
- Publisher와 Observer 간 느슨한 결합
- 비동기 이벤트 처리

---

## 3. QAS-06: 유지보수성 (Modifiability) 달성 정당화

### 3.1 QA 요구사항

**목표**: 컴포넌트 수정 용이성 및 확장성
- 새로운 토큰화 전략 추가 용이
- 검색 엔진 변경 시 최소 수정
- Hot/Cold Path 독립적 수정 가능

### 4.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `ISearchQueryService` | Provided Interface로 의존성 역전 | 핵심 |
| `IQueryTokenizer` | 토큰화 전략 인터페이스 | 핵심 |
| `ISearchEngineClient` | 검색 엔진 전략 인터페이스 | 핵심 |
| `SearchEngineAdapter` | 검색 엔진 어댑터 | 핵심 |
| `IMessagePublisherService` | 메시지 발행 인터페이스 | 보조 |

### 4.3 적용된 Pattern 및 Tactic

#### 3.3.1 Strategy Pattern

**설계 결정**: `IQueryTokenizer`, `ISearchEngineClient` 인터페이스를 통한 전략 패턴 적용

**구현 방식**:
- `IQueryTokenizer` 인터페이스로 다양한 토큰화 전략 교체 가능
- `ISearchEngineClient` 인터페이스로 다양한 검색 엔진 전략 교체 가능
- 런타임에 전략 교체 가능 (의존성 주입)

**유지보수 효과**:
- 새로운 토큰화 전략 추가 시 `IQueryTokenizer` 구현체만 추가
- 검색 엔진 변경 시 `ISearchEngineClient` 구현체만 변경
- 기존 코드 수정 최소화

**정당화**:
- **대안 1: 구체 클래스에 직접 의존**
  - 전략 변경 시 `SearchQueryManager` 수정 필요
  - 테스트 시 Mock 객체 주입 어려움
  - **결론**: 인터페이스 기반 설계가 필수

- **대안 2: if-else 분기로 전략 선택**
  - 코드 복잡도 증가
  - 새로운 전략 추가 시 기존 코드 수정 필요
  - Open/Closed Principle 위배
  - **결론**: Strategy Pattern이 적합

**적용된 Pattern**: **Strategy Pattern**
- `IQueryTokenizer`: `SimpleKeywordTokenizer` 외에 LLM 기반 토큰화 전략 추가 가능
- `ISearchEngineClient`: ElasticSearch 외에 Solr, Algolia 등 추가 가능

#### 3.3.2 Adapter Pattern

**설계 결정**: `SearchEngineAdapter`, `RabbitMQAdapter`를 통한 외부 시스템 어댑팅

**구현 방식**:
- `SearchEngineAdapter`가 ElasticSearch API를 `ISearchEngineClient` 인터페이스로 변환
- `RabbitMQAdapter`가 RabbitMQ API를 `IMessagePublisherService` 인터페이스로 변환
- Business Layer는 외부 시스템의 세부 구현을 알 필요 없음

**유지보수 효과**:
- ElasticSearch → Solr 변경 시 `SearchEngineAdapter`만 수정
- RabbitMQ → Kafka 변경 시 `RabbitMQAdapter`만 수정
- Business Layer 코드 변경 불필요

**정당화**:
- **대안 1: Business Layer에서 직접 외부 API 호출**
  - 외부 시스템 변경 시 Business Layer 수정 필요
  - 테스트 시 외부 시스템 Mock 어려움
  - **결론**: Adapter Pattern이 필수

- **대안 2: 외부 시스템을 Business Layer 인터페이스에 맞게 수정**
  - 외부 시스템은 제어 불가능
  - 비현실적
  - **결론**: Adapter로 외부 시스템을 어댑팅

**적용된 Pattern**: **Adapter Pattern**
- `SearchEngineAdapter`: ElasticSearch → Business Layer
- `RabbitMQAdapter`: RabbitMQ → Business Layer

#### 3.3.3 Hot/Cold Path Separation

**설계 결정**: Hot Path와 Cold Path의 완전한 분리

**구현 방식**:
- Hot Path: `SearchQueryManager` → `SimpleKeywordTokenizer` → `SearchEngineAdapter`
- Cold Path: `SearchQueryEvent` → `SearchQueryImprovementConsumer` → `LLMServiceClient`
- 이벤트 기반 비동기 통신으로 완전 분리

**유지보수 효과**:
- Cold Path 로직 변경이 Hot Path에 영향 없음
- Hot Path 성능 최적화가 Cold Path에 영향 없음
- 독립적 배포 및 스케일링 가능

**정당화**:
- **대안 1: Hot Path와 Cold Path 통합**
  - LLM 호출이 Hot Path에 포함
  - SLA 위반 위험
  - **결론**: 분리가 필수

- **대안 2: 동기식 통합 처리**
  - 사용자가 모든 처리 완료까지 대기
  - 응답 시간 증가
  - **결론**: 비동기 분리가 적합

**적용된 Pattern**: **Observer Pattern**
- 이벤트 기반 비동기 통신으로 완전 분리

---

## 4. QAS-05: 가용성 (Availability) 달성 정당화

### 4.1 QA 요구사항

**목표**: 서비스 가용성 보장
- ElasticSearch 장애 시에도 서비스 정상 동작
- Cold Path 장애가 Hot Path에 영향 없음
- 이벤트 손실 방지

### 4.2 QA 달성에 기여하는 Design Elements

| Design Element | 역할 | QA 기여도 |
|---------------|------|----------|
| `SearchQueryManager` | 장애 격리 및 폴백 처리 | 핵심 |
| `RabbitMQAdapter` | Durable Queue를 통한 이벤트 보존 | 핵심 |
| `SearchQueryImprovementConsumer` | Cold Path 장애 격리 | 보조 |

### 4.3 적용된 Pattern 및 Tactic

#### 4.3.1 Fault Isolation Tactic

**설계 결정**: Hot Path와 Cold Path의 장애 격리

**구현 방식**:
- Hot Path 장애: ElasticSearch 조회 실패 시 빈 결과 반환
- Cold Path 장애: LLM 서비스 장애 시 로깅만 수행, Hot Path는 정상 동작
- 이벤트 발행 실패 시에도 Hot Path는 정상 응답

**가용성 효과**:
- Cold Path 장애가 Hot Path에 영향 없음
- LLM 서비스 장애 시에도 검색 서비스 정상 동작
- 부분 장애 시에도 서비스 지속 가능

**정당화**:
- **대안 1: Hot Path와 Cold Path 통합**
  - Cold Path 장애 시 Hot Path도 영향
  - 전체 서비스 중단 위험
  - **결론**: 분리가 필수

- **대안 2: 동기식 처리**
  - Cold Path 실패 시 Hot Path도 실패
  - 가용성 저하
  - **결론**: 비동기 분리가 적합

**적용된 Pattern**: **Observer Pattern**
- Publisher와 Observer 간 느슨한 결합으로 장애 격리

#### 4.3.2 Event Preservation Tactic (DD-02)

**설계 결정**: RabbitMQ Durable Queue를 통한 이벤트 보존

**구현 방식**:
- `RabbitMQAdapter`가 Durable Queue에 이벤트 발행
- 서비스 재시작 시에도 이벤트 손실 방지
- Cold Path 처리가 지연되어도 이벤트 보존

**가용성 효과**:
- 이벤트 손실 방지
- 서비스 재시작 후에도 Cold Path 처리 가능
- 데이터 일관성 보장

**정당화**:
- **대안 1: 메모리 큐 사용**
  - 서비스 재시작 시 이벤트 손실
  - **결론**: Durable Queue 필요

- **대안 2: 이벤트 발행 생략**
  - Cold Path 인덱스 개선 불가능
  - **결론**: 이벤트 발행 필요

**적용된 Pattern**: **Observer Pattern** (Passive Redundancy)
- Durable Queue를 통한 이벤트 보존

---

## 6. 종합 정당화

### 6.1 설계 요소들의 상호 보완성

본 컴포넌트의 설계 요소들은 서로 상호 보완적으로 작동하여 여러 QA를 동시에 달성합니다:

1. **Hot/Cold Path Separation** (Observer Pattern)
   - QAS-03 (실시간성): Hot Path에서 LLM 제거
   - QAS-05 (가용성): 장애 격리

2. **Strategy Pattern**
   - QAS-06 (유지보수성): 전략 교체 용이
   - QAS-03 (실시간성): Hot Path에 빠른 전략 사용

3. **Adapter Pattern**
   - QAS-06 (유지보수성): 외부 시스템 변경 시 최소 수정
   - QAS-02 (성능): 최적화된 접근 경로 제공

4. **Facade Pattern**
   - QAS-06 (유지보수성): 복잡한 프로세스 단순화
   - QAS-02 (성능): 불필요한 오버헤드 제거

### 6.2 대안 설계와의 비교

#### 대안 1: 단일 경로 설계 (Hot Path에 LLM 포함)
- **장점**: 구현 단순
- **단점**: 
  - QAS-03 위반 (응답 시간 > 3초)
  - QAS-05 위반 (장애 전파)
- **결론**: 현재 설계가 모든 QA를 만족

#### 대안 2: 동기식 통합 처리
- **장점**: 구현 단순
- **단점**:
  - QAS-03 위반 (응답 시간 증가)
  - QAS-05 위반 (장애 전파)
- **결론**: 비동기 분리가 필수

#### 대안 3: 구체 클래스 직접 의존
- **장점**: 구현 단순
- **단점**:
  - QAS-06 위반 (유지보수성 저하)
  - 테스트 어려움
- **결론**: 인터페이스 기반 설계가 필수

### 6.3 최적 설계 정당화

현재 설계는 다음과 같은 이유로 최적입니다:

1. **다중 QA 동시 달성**: 실시간성, 성능, 비용 효율성, 유지보수성, 가용성을 모두 만족
2. **확장성**: 새로운 전략 추가 시 기존 코드 수정 최소화
3. **장애 격리**: Hot Path와 Cold Path의 완전한 분리
4. **비용 효율성**: 10% 샘플링으로 비용 90% 절감
5. **성능 최적화**: 평균 응답 시간 530ms (목표의 17.7%)

---

## 결론

`SearchQueryManager` 컴포넌트는 **Hot/Cold Path Separation**, **Strategy Pattern**, **Adapter Pattern**, **Observer Pattern**, **Facade Pattern**을 체계적으로 적용하여 QAS-03 (실시간성), QAS-02 (성능), QAS-06 (유지보수성), QAS-05 (가용성)을 모두 달성합니다. 각 설계 요소는 특정 QA 달성에 직접적으로 기여하며, 상호 보완적으로 작동하여 최적의 설계를 구성합니다. 고려된 대안 설계들과 비교하여 현재 설계가 모든 QA를 만족하는 최적의 설계임을 정당화합니다.

