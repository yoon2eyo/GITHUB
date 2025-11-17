# SearchQueryManager 컴포넌트 시퀀스 다이어그램 설명

## 개요

본 문서는 `SearchQueryManager` 컴포넌트의 Behavior Diagram (Sequence Diagram)에 대한 상세 설명입니다. 컴포넌트를 구성하는 클래스의 인스턴스 수준에서 중요한 시나리오를 시퀀스 다이어그램으로 기술하며, 각 시나리오의 동작을 상세히 설명합니다.

---

## 시나리오 개요

본 시퀀스 다이어그램은 다음 주요 시나리오를 포함합니다:

**Hot Path: 자연어 지점 검색 (UC-09)**
- 고객의 자연어 검색 쿼리를 실시간으로 처리하여 지점 검색 결과를 반환
- 외부 의존성을 최소화하여 빠른 응답 시간 보장

---

## Hot Path: 자연어 지점 검색 (UC-09)

### 시나리오 시작점

**Provided Interface 호출**: `BranchSearchController`가 `ISearchQueryService.search()` 메서드를 호출합니다.

### [1] Provided Interface 호출

**호출**: `BranchSearchController` → `SearchQueryManager` : [1] `search(query, userLocation, customerId?)`

**설명**:
- 외부 Controller가 `ISearchQueryService` 인터페이스를 통해 검색 요청을 전달합니다.
- `SearchQueryManager`는 **Facade Pattern**을 적용하여 복잡한 검색 프로세스를 단순한 인터페이스로 제공합니다.
- 클라이언트는 내부의 복잡한 토큰화, 검색 과정을 알 필요 없이 간단한 API만 사용합니다.

**Design Pattern**: **Facade Pattern**
- 복잡한 서브시스템을 단순한 인터페이스로 제공
- 클라이언트와 내부 구현 간의 결합도 감소

### [2] 쿼리 토큰화

**호출**: `SearchQueryManager` → `SimpleKeywordTokenizer` : [2] `tokenize(query)`

**설명**:
- `SimpleKeywordTokenizer`를 사용하여 LLM 호출 없이 빠른 키워드 추출을 수행합니다.
- 처리 과정:
  1. 공백으로 쿼리 분리
  2. 불용어 제거 (the, a, an, in, on, at, to, for)
  3. 1글자 이하 토큰 제거
  4. 소문자 변환

**성능 최적화**:
- 처리 시간: < 10ms
- LLM 호출 제거로 외부 네트워크 지연 요소 완전 제거
- 인메모리 처리로 외부 의존성 없음

**QAS-03 (Real-Time Performance) 기여**:
- LLM 호출 제거로 응답 시간 보장
- 핵심 성능 최적화 요소

**Design Pattern**: **Strategy Pattern**
- `IQueryTokenizer` 인터페이스로 다양한 토큰화 전략 교체 가능
- Hot Path: `SimpleKeywordTokenizer` (빠름)

### [3] ElasticSearch 검색

**호출**: `SearchQueryManager` → `SearchEngineAdapter` : [3] `query(tokens, userLocation)`

**설명**:
- `SearchEngineAdapter`는 **Adapter Pattern**을 적용하여 ElasticSearch API를 Business Layer 인터페이스로 어댑팅합니다.
- `ElasticSearchRepository`가 실제 ElasticSearch 검색을 수행합니다.

**성능 최적화**:
- 평균 검색 시간: < 500ms
- 인덱싱된 데이터를 통한 빠른 검색
- 거리 기반 필터링 및 랭킹
- RDB LIKE 검색 대비 10-100배 빠름

**QAS-03 (Real-Time Performance) 기여**:
- 전문 검색 엔진 활용으로 빠른 검색 보장
- 핵심 성능 최적화 요소

**Design Pattern**: **Adapter Pattern**
- ElasticSearch API를 Business Layer 인터페이스로 변환

### [4] 결과 반환

**호출**: `SearchQueryManager` → `BranchSearchController` : [4] `List<Map<String, Object>> results`

**설명**:
- 검색 결과를 `List<Map<String, Object>>` 형태로 반환합니다.
- 각 Map은 지점 정보를 포함합니다:
  - `branchId`: 지점 ID
  - `name`: 지점 이름
  - `distance`: 사용자로부터의 거리 (km)
  - `score`: 검색 관련도 점수

**Hot Path 완료**:
- 총 처리 시간: ~530ms
  - 토큰화: ~10ms
  - ElasticSearch 조회: ~500ms
  - 결과 포맷팅: ~10ms
- 목표 3초의 17.7%
- **QAS-03: 95% < 3초 목표 달성**

---

## Design Pattern & Tactics 요약

### 1. 실시간성 최적화 (QAS-03)

#### Simple Tokenization
- **목적**: LLM 없이 빠른 키워드 추출
- **효과**: 처리 시간 < 10ms
- **적용**: `SimpleKeywordTokenizer` 사용

#### ElasticSearch Optimization
- **목적**: 전문 검색 엔진 활용
- **효과**: 평균 검색 시간 < 500ms
- **적용**: `ElasticSearchRepository` 사용

#### 외부 의존성 최소화
- **목적**: Hot Path에서 외부 호출 제거
- **효과**: 응답 시간 보장 (< 3초)
- **적용**: LLM 호출 완전 제거

**Design Pattern**: Facade, Strategy, Adapter

### 2. 유지보수성 (QAS-06)

#### Strategy Pattern
- **목적**: 다양한 토큰화/검색 전략 교체 가능
- **효과**: 새로운 전략 추가 시 기존 코드 수정 최소화
- **적용**: `IQueryTokenizer`, `ISearchEngineClient` 인터페이스

#### Adapter Pattern
- **목적**: 외부 시스템 변경 시 어댑터만 수정
- **효과**: Business Layer 코드 변경 불필요
- **적용**: `SearchEngineAdapter`

**Design Pattern**: Strategy, Adapter

### 3. 성능 (QAS-02)

#### Hot Path 최적화
- **목적**: 응답 시간 최소화
- **효과**: 총 처리 시간 ~530ms (목표 3초의 17.7%)
- **적용**: 외부 의존성 최소화, LLM 제거

**Design Pattern**: Facade, Strategy, Adapter

---

## Hot Path 처리 시간 요약

- 토큰화: ~10ms
- ElasticSearch 조회: ~500ms
- 결과 포맷팅: ~10ms
- **총계: ~530ms** (목표 3초의 17.7%)
- **QAS-03: 95% < 3초 목표 달성**

---

## 결론

`SearchQueryManager` 컴포넌트의 Hot Path 시퀀스 다이어그램은 외부 의존성을 최소화하여 실시간 검색 성능을 극대화하는 설계를 보여줍니다. LLM 호출 없이 빠른 토큰화와 ElasticSearch 최적화를 통해 QAS-03 (실시간성) 목표를 달성합니다. **Facade Pattern**, **Strategy Pattern**, **Adapter Pattern**을 체계적으로 적용하여 유지보수성과 성능을 동시에 확보하는 최적의 설계를 구성합니다.

