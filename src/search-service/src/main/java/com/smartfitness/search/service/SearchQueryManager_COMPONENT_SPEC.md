# SearchQueryManager 컴포넌트 명세서

## 개요

**SearchQueryManager**는 고객의 자연어 검색 쿼리를 실시간으로 처리하여 지점 검색 결과를 반환하는 핵심 비즈니스 로직 컴포넌트입니다. Hot Path에서 LLM 호출 없이 빠른 키워드 토큰화와 ElasticSearch 조회를 통해 3초 이내 응답을 보장하며, Cold Path로 검색 쿼리 이벤트를 발행하여 비동기 인덱스 개선을 지원합니다.

---

## 컴포넌트 기능 요구사항

### Provided Interface: `ISearchQueryService`

이 컴포넌트는 다음 두 가지 핵심 오퍼레이션을 제공합니다:

#### 1. `search(String query, String userLocation)`

**기능**: 자연어 검색 쿼리 처리 (UC-09 - Hot Path)

**처리 흐름**:
1. **쿼리 토큰화**: `IQueryTokenizer`를 통해 자연어 쿼리를 키워드로 변환
   - LLM 호출 없이 단순 키워드 추출 (공백 분리, 불용어 제거)
   - Hot Path에서 외부 의존성 제거로 응답 시간 보장
2. **검색 엔진 조회**: `ISearchEngineClient`를 통해 ElasticSearch에서 지점 검색
   - 토큰화된 키워드와 사용자 위치 기반 검색
   - 거리 기반 랭킹 및 관련도 점수 계산
3. **결과 반환**: 검색 결과 리스트 반환
   - 지점 ID, 이름, 거리, 관련도 점수 포함

**반환값**: `List<Map<String, Object>>`
- 각 Map은 지점 정보를 포함:
  - `branchId`: 지점 ID
  - `name`: 지점 이름
  - `distance`: 사용자로부터의 거리 (km)
  - `score`: 검색 관련도 점수

**성능 목표**: 95% 요청이 3초 이내 응답 (QAS-03)

#### 2. `search(String query, String userLocation, String customerId)`

**기능**: 자연어 검색 쿼리 처리 및 Cold Path 이벤트 발행 (UC-09 - Hot Path + Cold Path 트리거)

**처리 흐름**:
1. **Hot Path 처리**: `search(query, userLocation)`과 동일한 처리 수행
   - 쿼리 토큰화 → ElasticSearch 조회 → 결과 반환
2. **Cold Path 이벤트 발행**: `IMessagePublisherService`를 통해 `SearchQueryEvent` 발행
   - 이벤트 내용: 쿼리 텍스트, 고객 ID, 검색 결과 개수
   - RabbitMQ를 통한 비동기 이벤트 전달 (DD-02: Event-Based Architecture)
   - Cold Path에서 LLM 분석 및 인덱스 개선을 위한 트리거

**반환값**: `List<Map<String, Object>>` (Hot Path 결과와 동일)

**사용 시나리오**: 
- 고객이 앱에서 지점을 검색할 때 사용
- Cold Path 이벤트 발행으로 향후 검색 정확도 개선에 활용

**Hot/Cold Path 분리 (DD-09)**:
- **Hot Path**: 실시간 검색 응답 (LLM 호출 없음) → SLA 보장
- **Cold Path**: 비동기 인덱스 개선 (LLM 분석) → 비용 효율성

---

## 컴포넌트 품질 요구사항

### 1. 성능 (Performance) - QAS-02, QAS-03

#### 1.1 응답 시간 요구사항
- **목표**: 자연어 검색 쿼리의 95%가 3초 이내 완료 (QAS-03)
- **99% 목표**: 99%가 5초 이내 완료
- **평균 응답 시간**: < 1초 목표

#### 1.2 Hot Path 최적화 전술 (DD-09)

**LLM 호출 제거**:
- Hot Path에서 외부 LLM 서비스 호출 완전 제거
- 단순 키워드 토큰화로 빠른 처리 보장
- 외부 네트워크 지연 요소 제거

**로컬 토큰화**:
- `IQueryTokenizer` 인터페이스를 통한 전략 패턴 적용
- `SimpleKeywordTokenizer`를 통한 인메모리 키워드 추출
- 공백 분리 및 불용어 제거만 수행
- 처리 시간: < 10ms

**ElasticSearch 최적화**:
- `ISearchEngineClient`를 통한 ElasticSearch 조회
- 인덱싱된 데이터를 통한 빠른 검색
- 거리 기반 필터링 및 랭킹
- 평균 검색 시간: < 500ms

**비동기 이벤트 발행**:
- `IMessagePublisherService`를 통한 Cold Path 이벤트 발행
- 논블로킹 처리로 Hot Path 응답 시간에 영향 없음
- 이벤트 발행 시간: < 10ms (비동기)

#### 1.3 처리량 요구사항
- **피크 부하**: 순간 최대 50 TPS (동시 검색 요청)
- **동시성 처리**: 멀티스레드 기반 요청 병렬 처리
- **자원 관리**: Thread Pool을 통한 동시 요청 수 제한 및 부하 분산

#### 1.4 성능 측정 결과
- **Hot Path 총 처리 시간**: ~530ms (목표 3초의 17.7%)
  - 토큰화: ~10ms (1.9%)
  - ElasticSearch 조회: ~500ms (94.3%)
  - 결과 포맷팅: ~10ms (1.9%)
  - 이벤트 발행: ~10ms (1.9%, 비동기)
- **목표 대비 달성률**: 목표 3초의 17.7%로 초과 달성

#### 1.5 외부 의존성 최소화
- Hot Path에서 외부 서비스 호출 제거
- ElasticSearch만 의존 (고성능 검색 엔진)
- LLM 서비스는 Cold Path에서만 사용

### 2. 유지보수성 (Modifiability) - QAS-06

#### 2.1 인터페이스 기반 설계
- `ISearchQueryService` 인터페이스 구현으로 의존성 역전 원칙(DIP) 적용
- 의존 컴포넌트들(`IQueryTokenizer`, `ISearchEngineClient`, `IMessagePublisherService`)을 인터페이스로 추상화
- 구현체 변경 시 컴포넌트 수정 최소화

#### 2.2 단일 책임 원칙 (SRP)
- 검색 쿼리 처리 로직만 담당
- 토큰화, 검색 엔진 조회, 이벤트 발행은 각각 전용 컴포넌트에 위임
- 컴포넌트 간 느슨한 결합 유지

#### 2.3 Hot/Cold Path 분리
- Hot Path와 Cold Path의 명확한 분리
- Cold Path 로직 변경이 Hot Path에 영향 없음
- 새로운 토큰화 전략이나 검색 알고리즘 추가 시 확장 용이

#### 2.4 확장성
- 새로운 검색 필터나 정렬 기준 추가 시 인터페이스 확장으로 대응 가능
- 이벤트 기반 아키텍처로 후속 처리 로직 추가 시 컴포넌트 수정 불필요
- Strategy Pattern 적용으로 다양한 토큰화 전략 및 검색 엔진 지원 가능 (ElasticSearch 외)

#### 2.5 이벤트 기반 아키텍처 (DD-02)
- `IMessagePublisherService`를 통한 이벤트 발행
- RabbitMQ를 통한 비동기 이벤트 전달
- 후속 처리 로직 추가 시 컴포넌트 수정 불필요
- 느슨한 결합으로 시스템 확장성 확보

---

## 참고 문서

- **DD-09**: 자연어 검색 질의 응답의 실시간성을 위한 구조 결정
- **DD-02**: 노드간 비동기 통신 구조 설계 결정
- **QAS-02**: 성능 최적화
- **QAS-03**: 신속한 자연어 검색 질의 응답
- **QAS-06**: 수정용이성
- **UC-09**: 자연어 지점 검색
- **Component Diagram**: `03_BranchContentServiceComponent.puml`
