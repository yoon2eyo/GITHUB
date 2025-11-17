# SearchQueryManager 컴포넌트 Class Diagram Description

## 다이어그램 구조 설명

본 Class Diagram은 `SearchQueryManager` 컴포넌트의 정적 구조를 표현하며, **Facade Pattern**, **Strategy Pattern**, **Adapter Pattern**, **Observer Pattern**을 적용하여 설계되었습니다. 각 패턴에 참여하는 클래스는 색상으로 구분되어 있습니다.

---

## 1. Interface Layer

### BranchSearchController 클래스

`BranchSearchController` 클래스는 `IBranchSearchApi` 인터페이스를 구현하는 REST API 컨트롤러입니다. 

**IBranchSearchApi 기능**:
- `searchBranches(query, userLocation)`: 고객의 자연어 검색 쿼리를 받아 지점 검색 결과를 반환합니다. HTTP GET 요청을 처리하며, `ISearchQueryService`를 통해 비즈니스 로직을 호출합니다.

`BranchSearchController`는 HTTP 요청을 받아 `ISearchQueryService`의 `search()` 메서드를 호출하고, 검색 결과를 `ResponseEntity`로 래핑하여 반환합니다. 이 클래스는 Interface Layer의 책임만 담당하며, 비즈니스 로직은 Business Layer에 위임합니다.

---

## 2. Business Layer

### SearchQueryManager 클래스 (Facade Pattern - 분홍색)

`SearchQueryManager` 클래스는 **Facade Pattern**을 적용한 핵심 비즈니스 컴포넌트입니다. 복잡한 검색 쿼리 처리 프로세스를 단순한 인터페이스로 제공합니다.

**ISearchQueryService 기능**:
- `search(query, userLocation)`: 기본 검색 기능으로, 자연어 쿼리를 받아 지점 검색 결과를 반환합니다. Hot Path 처리만 수행합니다.
- `search(query, userLocation, customerId)`: 검색 기능에 추가로 Cold Path 이벤트 발행을 포함합니다. 고객 ID가 제공된 경우 `SearchQueryEvent`를 발행하여 비동기 인덱스 개선을 트리거합니다.

`SearchQueryManager`는 내부적으로 `IQueryTokenizer`를 통해 쿼리를 토큰화하고, `ISearchEngineClient`를 통해 ElasticSearch에서 검색을 수행합니다. 또한 `IMessagePublisherService`를 통해 `SearchQueryEvent`를 발행하여 Cold Path 처리를 트리거합니다. 이 클래스는 복잡한 검색 프로세스의 세부사항을 숨기고 클라이언트에게 간단한 API를 제공하는 Facade 역할을 합니다.

**Hot Path 처리 흐름**:
1. 쿼리 토큰화 (`IQueryTokenizer`)
2. ElasticSearch 조회 (`ISearchEngineClient`)
3. 결과 반환
4. Cold Path 이벤트 발행 (비동기)

### IQueryTokenizer 인터페이스 및 SimpleKeywordTokenizer 클래스 (Strategy Pattern - 초록색)

`IQueryTokenizer` 인터페이스와 `SimpleKeywordTokenizer` 클래스는 **Strategy Pattern**을 적용하여 구현되었습니다.

**IQueryTokenizer 기능**:
- `tokenize(query)`: 자연어 쿼리를 키워드 리스트로 변환합니다. 다양한 토큰화 전략을 교체 가능하도록 인터페이스로 추상화되었습니다.

`SimpleKeywordTokenizer` 클래스는 `IQueryTokenizer` 인터페이스를 구현하며, Hot Path에서 빠른 키워드 추출을 수행합니다. 이 클래스는 LLM 호출 없이 단순한 공백 분리와 불용어 제거만 수행하여 < 10ms 이내에 토큰화를 완료합니다. `STOPWORDS` 상수 필드를 통해 불용어 목록을 관리하며, `ISearchEngineClient`를 의존성으로 가집니다 (현재는 사용하지 않지만 향후 확장 가능).

**Strategy Pattern 적용 효과**:
- 다양한 토큰화 전략(예: LLM 기반 토큰화, 형태소 분석 기반 토큰화)을 런타임에 교체 가능
- Hot Path에서는 빠른 `SimpleKeywordTokenizer` 사용, Cold Path에서는 정교한 토큰화 전략 사용 가능

### ISearchEngineClient 인터페이스 및 SearchEngineAdapter 클래스

`ISearchEngineClient` 인터페이스와 `SearchEngineAdapter` 클래스는 **Strategy Pattern**과 **Adapter Pattern**을 동시에 적용하여 구현되었습니다.

**ISearchEngineClient 기능 (Strategy Pattern - 초록색)**:
- `query(keywords, location)`: 토큰화된 키워드와 사용자 위치를 기반으로 ElasticSearch에서 지점을 검색합니다.
- `index(documentId, document)`: 문서를 ElasticSearch 인덱스에 추가하거나 업데이트합니다. Cold Path에서 인덱스 개선 시 사용됩니다.

`SearchEngineAdapter` 클래스는 **Adapter Pattern (노란색)**을 적용하여 `ISearchEngineClient` 인터페이스를 구현합니다. 이 클래스는 Business Layer의 `ISearchEngineClient` 인터페이스를 System Interface Layer의 `ISearchEngineRepository` 인터페이스로 어댑팅합니다. 

**Adapter Pattern 적용 효과**:
- ElasticSearch의 복잡한 API를 Business Layer에 맞는 간단한 인터페이스로 변환
- 검색 엔진 변경 시(예: ElasticSearch → Solr) `SearchEngineAdapter`만 수정하면 됨
- Business Layer는 검색 엔진의 세부 구현을 알 필요 없음

`SearchEngineAdapter`는 `ISearchEngineRepository`를 의존성으로 가지며, `query()` 메서드는 Hot Path에서 실시간 검색에 사용되고, `index()` 메서드는 Cold Path에서 비동기 인덱싱에 사용됩니다.

### SearchQueryImprovementConsumer 클래스 (Observer Pattern - 하늘색)

`SearchQueryImprovementConsumer` 클래스는 **Observer Pattern**을 적용하여 Cold Path에서 검색 쿼리 개선을 처리합니다.

이 클래스는 `SearchQueryEvent`를 구독하여 이벤트를 수신하면, LLM 서비스를 호출하여 쿼리에서 키워드를 추출하고 ElasticSearch 인덱스를 개선합니다. 

**주요 기능**:
- `subscribeToSearchQueryEvent()`: `IMessageSubscriptionService`를 통해 `SearchQueryEvent`를 구독합니다.
- `handleSearchQueryEvent(event)`: 수신한 이벤트를 처리하여 LLM 분석을 수행하고 인덱스를 업데이트합니다.
- `shouldProcessQuery(event)`: 비용 효율성을 위해 10% 샘플링 정책을 적용합니다 (DD-09).
- `updateSearchIndex(query, keywords)`: LLM에서 추출한 키워드로 ElasticSearch 인덱스를 개선합니다.

**Observer Pattern 적용 효과**:
- `SearchQueryManager`(Publisher)와 `SearchQueryImprovementConsumer`(Observer) 간 느슨한 결합
- 여러 Observer가 동일한 이벤트를 구독하여 독립적으로 처리 가능
- Hot Path와 Cold Path의 완전한 분리

이 클래스는 `ILLMAnalysisServiceClient`, `ISearchEngineRepository`, `IMessageSubscriptionService`를 의존성으로 가지며, Cold Path 처리 실패가 Hot Path에 영향을 주지 않도록 예외 처리를 수행합니다.

---

## 3. System Interface Layer

### IMessagePublisherService 인터페이스 및 RabbitMQAdapter 클래스

`IMessagePublisherService` 인터페이스와 `RabbitMQAdapter` 클래스는 **Adapter Pattern (노란색)**을 적용하여 구현되었습니다.

**IMessagePublisherService 기능**:
- `publishEvent(event)`: 도메인 이벤트를 메시지 브로커에 발행합니다. 비동기 처리를 위해 사용됩니다.

`RabbitMQAdapter` 클래스는 `IMessagePublisherService`와 `IMessageSubscriptionService` 인터페이스를 모두 구현합니다. 이 클래스는 RabbitMQ 메시지 브로커를 Business Layer에 맞는 인터페이스로 어댑팅합니다.

**Adapter Pattern 적용 효과**:
- RabbitMQ의 복잡한 AMQP 프로토콜을 간단한 인터페이스로 추상화
- 메시지 브로커 변경 시(예: RabbitMQ → Kafka) `RabbitMQAdapter`만 수정하면 됨
- Business Layer는 메시지 브로커의 세부 구현을 알 필요 없음

`RabbitMQAdapter`는 `publishEvent()` 메서드를 통해 `SearchQueryEvent`를 RabbitMQ에 발행하며, `subscribe()` 및 `unsubscribe()` 메서드를 통해 이벤트 구독을 관리합니다.

### ISearchEngineRepository 인터페이스 및 ElasticSearchRepository 클래스

`ISearchEngineRepository` 인터페이스와 `ElasticSearchRepository` 클래스는 ElasticSearch 데이터베이스 접근을 담당합니다.

**ISearchEngineRepository 기능**:
- `search(keywords, location)`: ElasticSearch에서 키워드와 위치 기반 검색을 수행합니다.
- `index(documentId, document)`: 문서를 ElasticSearch 인덱스에 저장합니다.

`ElasticSearchRepository` 클래스는 `ISearchEngineRepository` 인터페이스를 구현하며, ElasticSearch의 REST API를 호출하여 검색 및 인덱싱을 수행합니다. 이 클래스는 System Interface Layer의 책임을 담당하며, ElasticSearch의 세부 구현을 Business Layer로부터 숨깁니다.

### ILLMAnalysisServiceClient 인터페이스 및 LLMServiceClient 클래스

`ILLMAnalysisServiceClient` 인터페이스와 `LLMServiceClient` 클래스는 외부 LLM 서비스와의 통신을 담당합니다.

**ILLMAnalysisServiceClient 기능**:
- `extractKeywords(content)`: 자연어 텍스트에서 키워드를 추출합니다. Cold Path에서만 사용됩니다.

`LLMServiceClient` 클래스는 `ILLMAnalysisServiceClient` 인터페이스를 구현하며, 외부 LLM 서비스(예: OpenAI API)를 호출하여 키워드 추출을 수행합니다. 이 클래스는 Cold Path에서만 사용되며, Hot Path에서는 호출되지 않습니다.

---

## 4. Domain Event

### DomainEvent 인터페이스 및 SearchQueryEvent 클래스 (Observer Pattern - 하늘색)

`DomainEvent` 인터페이스와 `SearchQueryEvent` 클래스는 **Observer Pattern**의 이벤트 객체로 사용됩니다.

**DomainEvent 기능**:
- `getEventId()`: 이벤트의 고유 ID를 반환합니다.
- `getEventType()`: 이벤트 타입을 반환합니다.
- `getOccurredAt()`: 이벤트 발생 시각을 반환합니다.
- `getAggregateId()`: 이벤트와 관련된 집계 ID를 반환합니다.

`SearchQueryEvent` 클래스는 `DomainEvent` 인터페이스를 구현하며, 검색 쿼리 정보를 담는 도메인 이벤트입니다. 이 클래스는 다음 필드를 제공합니다:

- `eventId`: 이벤트 고유 ID (UUID)
- `eventType`: "SearchQueryEvent" 문자열
- `occurredAt`: 이벤트 발생 시각 (Instant)
- `query`: 검색 쿼리 텍스트
- `customerId`: 검색을 수행한 고객 ID
- `resultCount`: 검색 결과 개수

`SearchQueryEvent`는 `SearchQueryManager`에 의해 생성되어 `IMessagePublisherService`를 통해 발행되며, `SearchQueryImprovementConsumer`가 구독하여 처리합니다.

**Observer Pattern 적용 효과**:
- Publisher(`SearchQueryManager`)와 Observer(`SearchQueryImprovementConsumer`) 간 완전한 분리
- 이벤트 기반 비동기 처리로 Hot Path 성능 보장
- 여러 Observer가 동일한 이벤트를 구독하여 독립적으로 처리 가능

---

## 5. 설계 패턴 요약

### Facade Pattern (분홍색)
- **참여 클래스**: `SearchQueryManager`
- **목적**: 복잡한 검색 쿼리 처리 프로세스를 단순한 인터페이스로 제공
- **효과**: 클라이언트는 내부 복잡성을 모르고 간단한 API만 사용

### Strategy Pattern (초록색)
- **참여 클래스**: `IQueryTokenizer`, `ISearchEngineClient`, `SimpleKeywordTokenizer`
- **목적**: 다양한 토큰화 전략과 검색 엔진 전략을 런타임에 교체 가능
- **효과**: Hot Path와 Cold Path에서 다른 전략 사용 가능, 확장성 향상

### Adapter Pattern (노란색)
- **참여 클래스**: `SearchEngineAdapter`, `RabbitMQAdapter`
- **목적**: 외부 시스템(ElasticSearch, RabbitMQ)의 인터페이스를 Business Layer에 맞게 변환
- **효과**: 외부 시스템 변경 시 어댑터만 수정하면 됨, 느슨한 결합

### Observer Pattern (하늘색)
- **참여 클래스**: `SearchQueryEvent`, `SearchQueryImprovementConsumer`
- **목적**: Hot Path와 Cold Path의 완전한 분리, 이벤트 기반 비동기 처리
- **효과**: Publisher와 Observer 간 느슨한 결합, Hot Path 성능 보장

---

## 6. Hot/Cold Path 분리 (DD-09)

### Hot Path
**경로**: `BranchSearchController` → `SearchQueryManager` → `SimpleKeywordTokenizer` → `SearchEngineAdapter` → `ElasticSearchRepository`

- **목적**: 실시간 검색 응답 (QAS-03: < 3초)
- **특징**: LLM 호출 없음, 외부 의존성 최소화
- **성능**: 평균 ~530ms (목표 3초의 17.7%)

### Cold Path
**경로**: `SearchQueryEvent` → `RabbitMQAdapter` → `SearchQueryImprovementConsumer` → `LLMServiceClient` → `ElasticSearchRepository`

- **목적**: 비동기 인덱스 개선
- **특징**: LLM 분석, 10% 샘플링, 비동기 처리
- **효과**: 향후 검색 정확도 향상, 비용 효율성

---

## 7. SOLID 원칙 준수

### Single Responsibility Principle (SRP)
- 각 클래스가 단일 책임을 가짐
- `SearchQueryManager`: 검색 쿼리 처리 조율
- `SimpleKeywordTokenizer`: 키워드 토큰화
- `SearchEngineAdapter`: 검색 엔진 어댑팅

### Open/Closed Principle (OCP)
- 인터페이스를 통한 확장 가능
- 새로운 토큰화 전략 추가 시 `IQueryTokenizer` 구현체만 추가

### Liskov Substitution Principle (LSP)
- 모든 인터페이스 구현체가 계약을 준수
- `SimpleKeywordTokenizer`는 `IQueryTokenizer`를 완전히 대체 가능

### Interface Segregation Principle (ISP)
- 클라이언트별로 특화된 인터페이스 분리
- `IQueryTokenizer`, `ISearchEngineClient` 등으로 분리

### Dependency Inversion Principle (DIP)
- 고수준 모듈이 저수준 모듈에 직접 의존하지 않음
- 모든 의존성이 인터페이스를 통해 주입됨

---

## 8. Quality Attributes 달성

### QAS-03: 실시간성
- Hot Path에서 LLM 호출 제거로 < 3초 응답 보장
- 평균 응답 시간 ~530ms (목표의 17.7%)

### QAS-02: 성능
- 외부 의존성 최소화
- ElasticSearch 최적화된 인덱스 활용

### Sampling Tactic (DD-09)
- Cold Path 10% 샘플링으로 LLM 호출 비용 절감
- 비동기 처리로 리소스 효율적 운영

### QAS-06: 유지보수성
- 인터페이스 기반 설계로 확장성 확보
- Hot/Cold Path 분리로 독립적 수정 가능

---

## Element List

### Interface Layer 패키지

#### BranchSearchController 클래스
**역할**: REST API 컨트롤러로 HTTP 요청을 받아 비즈니스 로직을 호출하고 결과를 반환합니다.

**요구사항 기여도**:
- **기능 요구사항**: HTTP 요청 처리 및 응답 반환으로 검색 API 기능 제공
- **품질 요구사항**: 표준화된 REST API로 인터페이스 일관성 유지, QAS-06 달성
- **제약사항**: Spring MVC 프레임워크 기반으로 웹 요청 처리 표준 준수

#### IBranchSearchApi 인터페이스
**역할**: REST API 계약을 정의하는 인터페이스로 클라이언트와의 통신 규약을 명시합니다.

**요구사항 기여도**:
- **기능 요구사항**: 검색 API 계약 정의로 일관된 인터페이스 제공
- **품질 요구사항**: 인터페이스 표준화로 클라이언트 개발 용이성 향상

### Business Layer 패키지

#### SearchQueryManager 클래스 (Facade Pattern)
**역할**: 검색 쿼리 처리의 핵심 컴포넌트로 복잡한 검색 로직을 단순한 인터페이스로 제공합니다.

**요구사항 기여도**:
- **기능 요구사항**: Hot Path 검색 처리 및 Cold Path 이벤트 발행으로 완전한 검색 기능 제공
- **품질 요구사항**: Facade Pattern으로 복잡성 숨김, QAS-03 실시간성 및 QAS-06 유지보수성 달성
- **제약사항**: Hot/Cold Path 분리로 성능 최적화 및 확장성 보장

#### IQueryTokenizer 인터페이스
**역할**: 쿼리 토큰화 전략을 정의하는 인터페이스로 다양한 토큰화 알고리즘 교체를 가능하게 합니다.

**요구사항 기여도**:
- **기능 요구사항**: 쿼리 토큰화 기능 제공으로 검색 정확도 향상
- **품질 요구사항**: Strategy Pattern으로 확장성 제공, QAS-06 유지보수성 달성
- **제약사항**: Hot Path에서 < 10ms 성능 요구사항 준수

#### SimpleKeywordTokenizer 클래스 (Strategy Pattern)
**역할**: 기본적인 키워드 토큰화 전략을 구현하는 클래스로 빠른 쿼리 전처리를 수행합니다.

**요구사항 기여도**:
- **기능 요구사항**: 공백 분리 및 불용어 제거로 쿼리 토큰화 기능 제공
- **품질 요구사항**: Strategy Pattern 구현으로 교체 가능성 제공, QAS-03 실시간성 달성
- **제약사항**: LLM 호출 없이 빠른 처리로 Hot Path 성능 보장

#### ISearchEngineClient 인터페이스
**역할**: 검색 엔진과의 통신을 정의하는 인터페이스로 검색 및 인덱싱 기능을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 검색 및 인덱싱 기능 제공으로 핵심 검색 로직 지원
- **품질 요구사항**: Strategy Pattern으로 검색 엔진 교체 용이, QAS-06 유지보수성 달성
- **제약사항**: ElasticSearch 최적화로 QAS-03 실시간성 목표 준수

#### SearchEngineAdapter 클래스 (Adapter Pattern)
**역할**: 검색 엔진 인터페이스를 비즈니스 로직에 맞게 변환하는 어댑터 클래스입니다.

**요구사항 기여도**:
- **기능 요구사항**: ElasticSearch API를 간단한 인터페이스로 변환하여 검색 기능 제공
- **품질 요구사항**: Adapter Pattern으로 외부 시스템 변경 시 영향 최소화, QAS-06 유지보수성 달성
- **제약사항**: Hot Path/Cold Path 모두에서 안정적인 검색 성능 보장

#### SearchQueryImprovementConsumer 클래스 (Observer Pattern)
**역할**: 검색 쿼리 개선을 위한 이벤트 소비자로 Cold Path에서 LLM 분석을 수행합니다.

**요구사항 기여도**:
- **기능 요구사항**: 검색 인덱스 개선으로 장기적인 검색 정확도 향상
- **품질 요구사항**: Observer Pattern으로 Hot/Cold Path 완전 분리, QAS-03 실시간성 달성
- **제약사항**: 10% 샘플링으로 비용 효율성 및 성능 균형 유지

### System Interface Layer 패키지

#### IMessagePublisherService 인터페이스
**역할**: 메시지 발행 기능을 정의하는 인터페이스로 이벤트 기반 통신을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 이벤트 발행 기능 제공으로 Hot/Cold Path 연결
- **품질 요구사항**: Adapter Pattern으로 메시지 브로커 교체 용이, QAS-06 유지보수성 달성
- **제약사항**: RabbitMQ 기반으로 안정적인 메시지 전송 보장

#### RabbitMQAdapter 클래스 (Adapter Pattern)
**역할**: RabbitMQ 메시지 브로커를 비즈니스 로직에 맞는 인터페이스로 변환하는 어댑터입니다.

**요구사항 기여도**:
- **기능 요구사항**: 이벤트 발행 및 구독 기능 제공으로 Hot/Cold Path 통합
- **품질 요구사항**: Adapter Pattern으로 메시지 브로커 변경 시 영향 최소화
- **제약사항**: AMQP 프로토콜 추상화로 안정적인 비동기 통신 보장

#### ISearchEngineRepository 인터페이스
**역할**: 검색 엔진 데이터 접근을 정의하는 인터페이스로 검색 및 인덱싱을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: ElasticSearch 데이터 접근 기능 제공
- **품질 요구사항**: 인터페이스 분리로 검색 엔진 독립성 보장
- **제약사항**: REST API 기반으로 외부 시스템 안정적 연동

#### ElasticSearchRepository 클래스
**역할**: ElasticSearch 데이터베이스에 대한 구체적인 접근을 구현하는 클래스입니다.

**요구사항 기여도**:
- **기능 요구사항**: 검색 쿼리 실행 및 인덱스 업데이트 기능 제공
- **품질 요구사항**: System Interface Layer 책임 분리로 비즈니스 로직 격리
- **제약사항**: ElasticSearch 최적화로 검색 성능 보장

#### ILLMAnalysisServiceClient 인터페이스
**역할**: LLM 서비스와의 통신을 정의하는 인터페이스로 키워드 추출 기능을 추상화합니다.

**요구사항 기여도**:
- **기능 요구사항**: 키워드 추출 기능 제공으로 Cold Path 검색 개선
- **품질 요구사항**: 인터페이스 분리로 LLM 서비스 교체 용이
- **제약사항**: Cold Path에서만 사용으로 Hot Path 성능 영향 없음

#### LLMServiceClient 클래스
**역할**: 외부 LLM 서비스와의 구체적인 통신을 구현하는 클래스입니다.

**요구사항 기여도**:
- **기능 요구사항**: LLM 기반 키워드 추출로 검색 인덱스 개선
- **품질 요구사항**: System Interface Layer 책임으로 외부 의존성 관리
- **제약사항**: 샘플링 적용으로 비용 효율성 및 성능 균형 유지

### Domain Event 패키지

#### DomainEvent 인터페이스
**역할**: 도메인 이벤트의 공통 구조를 정의하는 인터페이스로 이벤트 표준화를 제공합니다.

**요구사항 기여도**:
- **기능 요구사항**: 이벤트 기반 통신의 표준 구조 제공
- **품질 요구사항**: Observer Pattern 지원으로 느슨한 결합 구현
- **제약사항**: 이벤트 ID, 타입, 타임스탬프 등 필수 속성 정의

#### SearchQueryEvent 클래스 (Observer Pattern)
**역할**: 검색 쿼리 정보를 담는 구체적인 도메인 이벤트로 Hot/Cold Path 연결을 담당합니다.

**요구사항 기여도**:
- **기능 요구사항**: 검색 쿼리 메타데이터 전송으로 Cold Path 트리거
- **품질 요구사항**: Observer Pattern 구현으로 Hot Path와 Cold Path 완전 분리
- **제약사항**: 쿼리 텍스트, 고객 ID, 결과 개수 등 분석에 필요한 정보 포함

---

## 결론

`SearchQueryManager` 컴포넌트의 Class Diagram은 **Facade Pattern**, **Strategy Pattern**, **Adapter Pattern**, **Observer Pattern**을 체계적으로 적용하여 설계되었습니다. 각 패턴은 색상으로 구분되어 있으며, Hot/Cold Path 분리를 통해 실시간성을 달성합니다. 인터페이스 기반 설계로 SOLID 원칙을 준수하며, 유지보수성과 확장성을 보장합니다.

