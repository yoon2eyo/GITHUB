# Search Service Component Element List

## 개요
본 문서는 Search Service 컴포넌트 다이어그램(`03_BranchContentServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IBranchSearchApi** | 지점 검색 API 인터페이스를 정의<br>자연어 쿼리 기반 지점 검색 기능 제공<br>실시간 검색 결과 반환 표준화 | UC-09 (자연어 지점 검색)<br>QAS-03 (성능 - 3초 응답시간 보장) |
| **IBranchReviewApi** | 리뷰 등록 API 인터페이스를 정의<br>고객 리뷰 작성 및 등록 기능 제공<br>리뷰 기반 선호도 분석 트리거 | UC-10 (고객 리뷰 등록), UC-18 (지점 정보 등록)<br>QAS-04 (Security - 리뷰 내용 검증) |
| **BranchSearchController** | IBranchSearchApi 인터페이스의 구현<br>검색 요청 수신 및 응답 처리<br>Hot Path 검색 쿼리 라우팅 | UC-09<br>QAS-03 (성능 - Hot Path SLA) |
| **ReviewController** | IBranchReviewApi 인터페이스의 구현<br>리뷰 등록 요청 수신 및 처리<br>Cold Path 콘텐츠 등록 트리거 | UC-10, UC-18<br>QAS-04 (Security - 입력 검증) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ISearchQueryService** | 검색 쿼리 처리 인터페이스를 정의<br>자연어 쿼리 분석 및 토큰화 조율<br>Hot Path 검색 워크플로우 관리 | UC-09<br>DD-09 (Hot/Cold Path Separation) |
| **SearchQueryManager** | ISearchQueryService 구현<br>Hot Path 검색 요청 조율 및 실행<br>토큰화 → 검색 엔진 호출 → 결과 반환 | UC-09<br>QAS-03 (3초 SLA), DD-09 (Hot Path) |
| **IQueryTokenizer** | 쿼리 토큰화 인터페이스를 정의<br>자연어 텍스트를 검색 키워드로 변환<br>간단한 키워드 추출 및 정규화 | UC-09<br>QAS-03 (성능 - 빠른 토큰화) |
| **SimpleKeywordTokenizer** | IQueryTokenizer 구현<br>단순 키워드 기반 텍스트 토큰화<br>LLM 없이 빠른 키워드 추출 | UC-09<br>QAS-03 (성능 우선) |
| **ISearchEngineClient** | 검색 엔진 클라이언트 인터페이스를 정의<br>ElasticSearch 쿼리 실행 및 결과 처리<br>검색 결과 포맷팅 및 랭킹 | UC-09, UC-10<br>DD-06 (ElasticSearch 활용) |
| **SearchEngineAdapter** | ISearchEngineClient 구현<br>ElasticSearch 쿼리 실행 및 결과 매핑<br>검색 결과 JSON 변환 | UC-09, UC-10<br>DD-06 (ElasticSearch) |
| **IContentRegistrationService** | 콘텐츠 등록 인터페이스를 정의<br>리뷰 및 지점 정보 등록 처리<br>등록 완료 후 인덱싱 및 이벤트 발행 | UC-10, UC-18<br>DD-06 (인덱싱) |
| **ContentRegistrationManager** | IContentRegistrationService 구현<br>리뷰/지점 정보 등록 및 인덱싱<br>등록 완료 이벤트 발행 | UC-10, UC-18<br>DD-02 (Event-Driven), DD-06 (인덱싱) |
| **IPreferenceAnalysisService** | 선호도 분석 인터페이스를 정의<br>LLM을 활용한 키워드 및 감정 분석<br>브랜드 선호도 데이터 생성 | UC-10<br>DD-09 (Cold Path - LLM 분석) |
| **PreferenceAnalyzer** | IPreferenceAnalysisService 구현<br>LLM 서비스 호출로 선호도 분석<br>키워드/감정 추출 및 데이터베이스 저장 | UC-10<br>DD-09 (Cold Path), QAS-04 (Security) |
| **PreferenceMatchConsumer** | 선호도 매칭 이벤트 소비자<br>BranchPreferenceCreatedEvent 수신<br>스케줄링 정책에 따라 매칭 처리 | UC-11 (맞춤형 알림)<br>DD-07 (Scheduling Policy) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **ISearchEngineRepository** | 검색 엔진 저장소 인터페이스를 정의<br>ElasticSearch 문서 CRUD 연산<br>인덱스 생성, 검색, 업데이트 추상화 | UC-09, UC-10, UC-18<br>DD-06 (ElasticSearch) |
| **ElasticSearchRepository** | ISearchEngineRepository 구현<br>ElasticSearch REST API 클라이언트<br>문서 인덱싱 및 검색 쿼리 실행 | DD-06 (ElasticSearch)<br>QAS-03 (성능 - 검색 속도) |
| **SearchEngineDB** | 검색 엔진 데이터베이스<br>지점 정보, 리뷰, 인덱스 데이터 저장<br>ElasticSearch 클러스터 | DD-06 (ElasticSearch)<br>QAS-03 (성능 - 검색 성능) |
| **ILLMAnalysisServiceClient** | LLM 분석 서비스 클라이언트 인터페이스를 정의<br>외부 LLM 서비스 HTTPS 호출 래핑<br>텍스트 분석 결과 파싱 | UC-10<br>DD-09 (Cold Path), QAS-04 (Security) |
| **LLMServiceClient** | ILLMAnalysisServiceClient 구현<br>외부 LLM 서비스 HTTPS 호출<br>JSON 요청/응답 처리 및 재시도 로직 | DD-09 (Cold Path)<br>QAS-05 (Availability - 외부 서비스 복원력) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>검색 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-10, UC-11 (선호도 이벤트) |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>외부 이벤트 수신 및 처리<br>PreferenceMatchConsumer 연동 | DD-02 (Event-Driven)<br>UC-11 (선호도 매칭 이벤트) |
| **RabbitMQAdapter** | IMessagePublisherService 및 IMessageSubscriptionService 구현<br>RabbitMQ 클라이언트 통합<br>이벤트 발행 및 구독 관리 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 내구성) |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 검색 요청 수신 및 처리<br>검색 및 리뷰 등록 API 공개 인터페이스<br>Hot/Cold Path 요청 분리 | UC-09, UC-10, UC-18<br>QAS-03 (Hot Path 성능), QAS-04 (Security) |
| **Business Layer** | 검색 및 콘텐츠 등록 핵심 로직 구현<br>Hot Path (실시간 검색) / Cold Path (비동기 인덱싱) 분리<br>LLM 분석, 이벤트 처리, 스케줄링 | DD-09 (Hot/Cold Path), DD-06 (ElasticSearch)<br>QAS-03 (성능), DD-02 (Event-Driven) |
| **System Interface Layer** | 외부 시스템 연동 인터페이스<br>ElasticSearch, LLM 서비스, RabbitMQ 연동<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>DD-06 (ElasticSearch), DD-09 (Cold Path) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 5 | 6 | 11 |
| **System Interface Layer** | 4 | 3 | 8 |
| **패키지** | - | - | 3 |
| **총계** | **11** | **11** | **26** |

---

## Architectural Drivers 적용 현황

### QAS-03 (성능 - 검색 서비스 응답시간 3초 이내 95% 달성)
- **Hot Path 최적화**: SearchQueryManager, SimpleKeywordTokenizer, SearchEngineAdapter
- **LLM 배제**: Hot Path에서 LLM 호출 완전 제외 (Cold Path로 이관)
- **로컬 처리**: 토큰화 및 검색 모두 로컬 컴포넌트로 처리

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **입력 검증**: ReviewController, IBranchReviewApi (리뷰 내용 검증)
- **접근 제어**: BranchSearchController, IBranchSearchApi (검색 권한)
- **데이터 암호화**: ElasticSearchRepository (인덱스 데이터 암호화)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **메시지 내구성**: RabbitMQAdapter (이벤트 발행 보장)
- **외부 서비스 복원력**: LLMServiceClient (재시도 및 서킷 브레이커)
- **검색 엔진 HA**: ElasticSearchRepository (클러스터 지원)

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: ContentRegistrationManager → RabbitMQAdapter (BranchPreferenceCreatedEvent)
- **이벤트 구독**: PreferenceMatchConsumer ← RabbitMQAdapter
- **비동기 처리**: 선호도 분석 및 매칭을 동기 응답에서 분리

### DD-06 (ElasticSearch 기반 검색 및 인덱싱)
- **검색 인터페이스**: ISearchEngineRepository, ElasticSearchRepository
- **인덱싱**: ContentRegistrationManager → ElasticSearchRepository
- **쿼리 실행**: SearchEngineAdapter → ElasticSearchRepository

### DD-07 (Scheduling Policy - 피크타임 부하 분산)
- **스케줄링 적용**: PreferenceMatchConsumer
- **부하 분산**: 선호도 매칭을 피크타임 외 시간대로 지연 처리
- **Hot Path 보호**: 실시간 검색 성능 저하 방지

### DD-09 (Hot/Cold Path Separation)
- **Hot Path (실시간)**: SearchQueryManager → SimpleKeywordTokenizer → SearchEngineAdapter
- **Cold Path (비동기)**: ContentRegistrationManager → PreferenceAnalyzer → LLMServiceClient
- **성능 보장**: Hot Path에서 LLM 호출 완전 배제

### 관련 UC 목록
- **UC-09**: 자연어 지점 검색 (Hot Path)
- **UC-10**: 고객 리뷰 등록 (Cold Path 인덱싱)
- **UC-11**: 맞춤형 알림 발송 (PreferenceMatchConsumer)
- **UC-18**: 지점 정보 등록 (Cold Path 인덱싱)

---

## Hot/Cold Path 분리 설계 원칙

### Hot Path (실시간 검색 - QAS-03 준수)
- **목표**: 3초 이내 95% 응답
- **구성요소**: SearchQueryManager, SimpleKeywordTokenizer, SearchEngineAdapter
- **특징**: 로컬 처리만, 외부 의존성 없음, LLM 배제

### Cold Path (비동기 처리 - 확장성 우선)
- **목표**: 정확도 및 풍부한 분석
- **구성요소**: ContentRegistrationManager, PreferenceAnalyzer, LLMServiceClient
- **특징**: LLM 활용, 이벤트 기반, 스케줄링 적용

---

## 결론

Search Service 컴포넌트 다이어그램의 모든 요소(26개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 검색/리뷰 API 인터페이스 (4개 요소)
- **Business Layer**: Hot/Cold Path 비즈니스 로직 (11개 요소)
- **System Interface Layer**: ElasticSearch/LLM/RabbitMQ 연동 (8개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 특히 **Hot/Cold Path 분리** 원칙을 중심으로 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
