# SearchQueryManager 컴포넌트 Cold Path 시퀀스 다이어그램 설명

## 개요

본 문서는 `SearchQueryManager` 컴포넌트의 Cold Path 시나리오에 대한 상세 설명입니다. Cold Path는 Hot Path와 완전히 분리되어 비동기로 동작하며, 검색 쿼리를 LLM으로 분석하여 ElasticSearch 인덱스를 개선하는 역할을 수행합니다.

---

## 시나리오: Cold Path - 검색 쿼리 개선 (DD-09)

### 시나리오 시작점

**이벤트 수신**: `SearchQueryImprovementConsumer`가 Message Broker (RabbitMQ)로부터 `SearchQueryEvent`를 비동기로 수신합니다.

**시작 조건**:
- Hot Path에서 `SearchQueryManager`가 `SearchQueryEvent`를 발행한 후
- Message Broker (RabbitMQ)를 통해 이벤트가 전달됨
- `SearchQueryImprovementConsumer`가 이벤트를 구독하여 수신

**특징**:
- Hot Path와 완전히 분리된 비동기 처리
- Hot Path 응답 시간에 영향 없음
- 이벤트 기반 통신으로 느슨한 결합

---

## [1.1] 이벤트 수신

**호출**: `RabbitMQ` → `SearchQueryImprovementConsumer` : [1.1] `handleSearchQueryEvent(SearchQueryEvent)`

**설명**:
- `SearchQueryImprovementConsumer`는 **Observer Pattern**을 적용하여 `SearchQueryEvent`를 구독합니다.
- Message Broker (RabbitMQ)로부터 비동기로 이벤트를 수신합니다.
- Hot Path와 완전히 분리되어 처리되므로 Hot Path 응답 시간에 영향이 없습니다.

**이벤트 데이터**:
- `query`: 검색 쿼리 텍스트
- `customerId`: 검색을 수행한 고객 ID
- `resultCount`: 검색 결과 개수
- `eventId`: 이벤트 고유 ID
- `occurredAt`: 이벤트 발생 시각

**QAS-05 (Availability) 기여**:
- Hot Path와 독립적으로 동작
- Cold Path 장애가 Hot Path에 영향 없음
- 부분 장애 시에도 서비스 지속 가능

**Design Pattern**: **Observer Pattern**
- Publisher (`SearchQueryManager`)와 Observer (`SearchQueryImprovementConsumer`) 간 느슨한 결합
- 비동기 이벤트 처리

---

## [1.2] 샘플링 체크

**호출**: `SearchQueryImprovementConsumer` → `SearchQueryImprovementConsumer` : [1.2] `shouldProcessQuery(event)`

**설명**:
- **Sampling Tactic (DD-09)**을 적용하여 10% 샘플링 정책을 수행합니다.
- 해시 기반 샘플링으로 동일 쿼리는 항상 처리되거나 스킵되도록 일관성을 보장합니다.
- 구현: `event.getQuery().hashCode() % 10 == 0`

**샘플링 로직**:
1. 쿼리 텍스트의 해시 코드 계산
2. 해시 코드를 10으로 나눈 나머지가 0인 경우에만 처리
3. 동일 쿼리는 항상 동일한 결과 (일관성 보장)

**Sampling Tactic (DD-09) 적용**:
- 10% 샘플링으로 LLM 호출 비용 절감
- 대표적인 쿼리 패턴 수집으로 효과 유지

**처리 시간**: ~1ms (샘플링 체크만)

---

## [1.3] LLM 키워드 추출 요청 (10% 샘플링 통과 시)

**호출**: `SearchQueryImprovementConsumer` → `ILLMAnalysisServiceClient` : [1.3] `extractKeywords(query)`

---

## [1.4] LLM 서비스 호출

**호출**: `ILLMAnalysisServiceClient` → `LLMServiceClient` : [1.4] `extractKeywords(query)`

**설명**:
- `ILLMAnalysisServiceClient` 인터페이스를 통해 **Strategy Pattern**이 적용되어 다양한 LLM 서비스 전략을 교체할 수 있습니다.
- `LLMServiceClient`가 실제 LLM API (예: OpenAI API)를 호출하여 키워드를 추출합니다.
- Cold Path에서만 사용되므로 Hot Path 응답 시간에 영향이 없습니다.

**처리 내용**:
1. **키워드 추출**: 자연어 쿼리에서 핵심 키워드 추출
2. **감정 분석**: 쿼리의 감정 분석 (positive, negative, neutral)
3. **카테고리 분류**: 쿼리의 카테고리 분류 (fitness, gym, equipment 등)

---

## [1.4] LLM 분석 결과 응답

**호출**: `LLMServiceClient` → `ILLMAnalysisServiceClient` : [1.4] `Map<String, Object> analysis{keywords, sentiment, category}`

---

## [1.3] LLM 분석 결과 반환

**호출**: `ILLMAnalysisServiceClient` → `SearchQueryImprovementConsumer` : [1.3] `Map<String, Object> analysis`

**반환 데이터**:
```json
{
  "keywords": ["fitness", "gym", "equipment", "training"],
  "sentiment": "positive",
  "category": "fitness"
}
```

**성능**:
- 처리 시간: ~1000ms
- 비동기 처리로 Hot Path에 영향 없음

**Sampling Tactic (DD-09) 적용**:
- 10% 샘플링으로 LLM 호출 비용 절감
- 비동기 처리로 성능 영향 없음

**QAS-05 (Availability) 기여**:
- LLM 서비스 장애 시에도 Hot Path는 정상 동작
- Cold Path 장애 격리

**Design Pattern**: **Strategy Pattern**
- `ILLMAnalysisServiceClient` 인터페이스로 다양한 LLM 서비스 전략 교체 가능
- 향후 다른 LLM 서비스 (예: Claude, Gemini) 추가 가능

---

## [1.5] 인덱스 개선 처리

**호출**: `SearchQueryImprovementConsumer` → `SearchQueryImprovementConsumer` : [1.5] `updateSearchIndex(query, keywords)`

---

## [1.6] 검색 엔진 인덱스 요청

**호출**: `SearchQueryImprovementConsumer` → `ISearchEngineRepository` : [1.6] `index(documentId, document)`

---

## [1.7] ElasticSearch 인덱스 업데이트

**호출**: `ISearchEngineRepository` → `ElasticSearchRepository` : [1.7] `index(documentId, document)`

**설명**:
- LLM에서 추출한 키워드로 ElasticSearch 인덱스를 개선합니다.
- 동의어 사전 업데이트, 쿼리 확장 인덱스 업데이트 등을 수행합니다.
- 향후 유사한 쿼리에 대한 검색 정확도가 향상됩니다.

**인덱스 개선 내용**:
1. **동의어 사전 업데이트**: 유사한 의미의 키워드들을 동의어로 등록
2. **쿼리 확장 인덱스**: 쿼리와 관련된 키워드 매핑 저장
3. **검색 가중치 조정**: 자주 검색되는 키워드의 가중치 조정

**효과**:
- 향후 검색 정확도 향상
- 비동기 처리로 Hot Path 영향 없음
- 지속적인 인덱스 개선

**QAS-06 (Modifiability) 기여**:
- 인덱스 개선 로직 변경이 Hot Path에 영향 없음
- 독립적 수정 가능

**처리 시간**: ~100ms (인덱스 업데이트)

---

## [1.7] ElasticSearch 업데이트 완료 응답

**호출**: `ElasticSearchRepository` → `ISearchEngineRepository` : [1.7] `void`

---

## [1.6] 검색 엔진 인덱스 완료 응답

**호출**: `ISearchEngineRepository` → `SearchQueryImprovementConsumer` : [1.6] `void`

---

## [1.8] Cold Path 완료

**호출**: `SearchQueryImprovementConsumer` → `SearchQueryImprovementConsumer` : [1.8] `(처리 완료)`

**처리 완료**:
- 인덱스 개선 완료
- 향후 검색 정확도 향상
- 비동기 처리로 Hot Path 영향 없음

**총 처리 시간**: ~1101ms (비동기)
- 샘플링 체크: ~1ms
- LLM 호출: ~1000ms (10%만 수행)
- 인덱스 업데이트: ~100ms

**90% 샘플링 스킵 시**:
- 처리 시간: ~1ms (샘플링 체크만)
- LLM 호출 생략으로 비용 절감

---

## Design Pattern & Tactics 요약

### 1. 가용성 (QAS-05)

#### Fault Isolation
- **목적**: Cold Path 장애가 Hot Path에 영향 없음
- **효과**: 부분 장애 시에도 서비스 지속 가능
- **적용**: Hot Path와 Cold Path의 완전한 분리

#### LLM 서비스 장애 격리
- **목적**: LLM 서비스 장애 시에도 Hot Path 정상 동작
- **효과**: 서비스 가용성 향상
- **적용**: 비동기 처리 및 예외 처리

**Design Pattern**: **Observer Pattern**

### 2. 유지보수성 (QAS-06)

#### Strategy Pattern
- **목적**: 다양한 LLM 서비스 전략 교체 가능
- **효과**: 새로운 LLM 서비스 추가 시 기존 코드 수정 최소화
- **적용**: `ILLMAnalysisServiceClient` 인터페이스

#### Hot/Cold Path Separation
- **목적**: 독립적 수정 가능
- **효과**: Cold Path 로직 변경이 Hot Path에 영향 없음
- **적용**: 이벤트 기반 비동기 통신

**Design Pattern**: **Strategy Pattern**, **Observer Pattern**

### 4. 인덱스 개선 효과

#### 지속적 개선
- **목적**: 검색 정확도 지속적 향상
- **효과**: 시간이 지날수록 검색 품질 향상
- **적용**: Cold Path에서 수집된 데이터로 인덱스 지속 개선

#### 검색 정확도 향상
- **목적**: 사용자 만족도 증가
- **효과**: 향후 검색 정확도 향상으로 사용자 만족도 증가
- **적용**: LLM 분석 결과를 인덱스에 반영

---

## Cold Path vs Hot Path 비교

| 항목 | Hot Path | Cold Path |
|------|----------|-----------|
| **목적** | 실시간 검색 응답 | 인덱스 개선 |
| **처리 시간** | ~530ms | ~1101ms (비동기) |
| **LLM 호출** | 없음 | 있음 (10% 샘플링) |
| **응답 시간 영향** | 직접 영향 | 영향 없음 (비동기) |
| **비용** | 낮음 | 중간 (10% 샘플링) |
| **SLA** | 95% < 3초 | SLA 없음 |
| **장애 영향** | 서비스 중단 | Hot Path 영향 없음 |

---

## 결론

`SearchQueryManager` 컴포넌트의 Cold Path는 Hot Path와 완전히 분리되어 비동기로 동작합니다. **Sampling Tactic (DD-09)**을 통해 10% 샘플링으로 LLM 호출 비용을 절감하면서도, 대표적인 쿼리 패턴을 수집하여 인덱스를 지속적으로 개선합니다. **Observer Pattern**을 적용하여 Hot Path와 느슨하게 결합되어 있으며, Cold Path 장애가 Hot Path에 영향을 주지 않도록 설계되었습니다. 이를 통해 QAS-05 (가용성), QAS-06 (유지보수성)을 동시에 달성합니다.

