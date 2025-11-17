# 고객앱 지점 검색 메시지 이동 경로

본 문서는 외부 고객앱에서 지점 검색을 수행할 때의 완전한 메시지 이동 경로를 클래스와 인터페이스 수준에서 상세히 기술합니다.

---

## 전체 메시지 플로우 개요

```
고객앱 → API Gateway → BranchSearchController → ISearchQueryService → SearchQueryManager → IQueryTokenizer → ISearchEngineClient → ISearchEngineRepository → ElasticSearch DB
                                                                                   ↓
                                                                    [Cold Path 이벤트 발행]
                                                                 SearchQueryManager → IMessagePublisherService → RabbitMQ
```

---

## 단계별 메시지 이동 경로

### Step 1: 고객앱 → API Gateway (또는 직접 서비스 호출)
**클래스/인터페이스**: `MobileApp` → `LoadBalancer/ReverseProxy` 또는 `ServiceDiscovery` → `SearchService`

**메시지**:
- `MobileApp`: HTTP GET `/api/search/branches?query={query}&userLocation={location}`

**API Gateway 라우팅 처리 방식**:
*현재 시스템에서는 별도의 API Gateway가 구현되어 있지 않으므로, 다음 중 하나의 방식으로 처리됨:*

**1. 직접 서비스 호출 (현재 구현 방식):**
```javascript
// Mobile App (JavaScript/TypeScript)
const response = await fetch('http://localhost:8085/search/branches?query=gym&userLocation=seoul');
// 또는 Kubernetes 환경에서는
const response = await fetch('http://search-service:8085/search/branches?query=gym&userLocation=seoul');
```

**2. 로드 밸런서/리버스 프록시 (추가 구성 필요):**
```nginx
# nginx.conf 예시
location /api/search/ {
    proxy_pass http://search-service-cluster;
    # JWT 검증 로직 추가 가능
}
```

**3. 서비스 디스커버리 기반 호출 (Eureka 사용):**
```java
// Eureka Client를 통한 동적 서비스 검색
@LoadBalanced
@RestTemplate restTemplate;

public String callSearchService(String query, String location) {
    return restTemplate.getForObject(
        "http://search-service/search/branches?query=" + query + "&userLocation=" + location,
        String.class
    );
}
```

**현재 시스템의 실제 동작:**
- 고객앱이 직접 `search-service:8085`로 HTTP 요청 전송
- 별도의 API Gateway 없이 각 서비스가 독립적으로 엔드포인트 노출
- `application.yml`의 `server.port: 8085`로 포트 설정됨

**일반적인 API Gateway 구현 예시 (Spring Cloud Gateway):**
```java
// ApiGatewayApplication.java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

// 라우팅 구성 (application.yml)
spring:
  cloud:
    gateway:
      routes:
      - id: search-service
        uri: http://search-service:8085  # ← Eureka에서 동적으로 결정
        predicates:
        - Path=/api/search/**            # ← 이 경로로 들어오는 요청
        filters:
        - RewritePath=/api/search/(?<path>.*), /$\{path}  # ← 경로 재작성
        - name: CircuitBreaker
          args:
            name: search-service-circuit-breaker
```

**API Gateway의 라우팅 처리 과정:**
1. **요청 수신**: `/api/search/branches?query=gym` 요청을 수신
2. **경로 매칭**: `predicates: Path=/api/search/**`와 매칭
3. **경로 재작성**: `/api/search/branches` → `/branches`로 변환
4. **서비스 검색**: Eureka에서 `search-service`의 실제 IP/포트 조회
5. **요청 포워딩**: `http://search-service:8085/branches?query=gym`으로 전달

**현재 시스템에서는 이 라우팅 로직이 없으므로:**
- 클라이언트가 직접 `http://localhost:8085/search/branches`로 호출
- Spring Boot의 내장 Tomcat이 직접 HTTP 요청 처리

### Step 2: API Gateway → 검색 서비스 진입 (상세 설명)
**클래스/인터페이스**: `ApiGateway` → `Spring MVC DispatcherServlet` → `HandlerMapping` → `HandlerAdapter` → `BranchSearchController` → `IBranchSearchApi`

**메시지 전달 과정 상세**:

**2.1 Spring Boot 내장 서버에서 HTTP 요청 수신**
```java
// SearchServiceApplication.java
@SpringBootApplication
@EnableDiscoveryClient
public class SearchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
```
- Spring Boot의 내장 Tomcat 서버(포트 8085)가 HTTP 요청을 수신
- `application.yml`에서 `server.port: 8085`로 설정됨
- Eureka에 `search-service`로 등록되어 서비스 디스커버리 가능

**2.2 Spring MVC 프레임워크 진입 - DispatcherServlet**
```java
// Spring MVC 아키텍처 (자동 구성)
@Configuration
public class WebMvcAutoConfiguration {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }
}
```
- `DispatcherServlet.doDispatch(HttpServletRequest, HttpServletResponse)`: 모든 HTTP 요청의 단일 진입점
- Spring Boot가 자동으로 `DispatcherServlet`을 `/` 경로에 매핑
- 요청 URL `/search/branches?query=gym&userLocation=seoul`을 분석

**2.3 HandlerMapping을 통한 컨트롤러 매핑**
```java
// BranchSearchController.java
@RestController
@RequestMapping("/search/branches")  // ← HandlerMapping이 이 경로를 찾음
@RequiredArgsConstructor
public class BranchSearchController implements IBranchSearchApi {
    // ...
}
```
- `RequestMappingHandlerMapping.getHandler(request)`: URL 패턴과 HTTP 메서드 매칭
- `@RequestMapping("/search/branches")` 클래스 레벨 어노테이션 발견
- `@GetMapping` 메서드 레벨 어노테이션과 결합하여 `BranchSearchController.searchBranches()` 메서드 매핑
- `HandlerMethod` 객체 생성 (컨트롤러 인스턴스 + 메서드 정보)

**2.4 HandlerAdapter를 통한 메서드 호출 준비**
```java
// BranchSearchController.java
@Override
@GetMapping  // ← HTTP GET 요청 처리
public ResponseEntity<List<Map<String, Object>>> searchBranches(
        @RequestParam String query,                    // ← 쿼리 파라미터 매핑
        @RequestParam(required = false) String userLocation) {  // ← 위치 파라미터 매핑
```
- `RequestMappingHandlerAdapter.handle(request, response, handler)`: 컨트롤러 메서드 호출 준비
- HTTP 요청 파라미터를 Java 메서드 파라미터로 자동 변환:
  - `?query=gym` → `String query = "gym"`
  - `?userLocation=seoul` → `String userLocation = "seoul"`
- `@RequestParam` 어노테이션 기반 파라미터 바인딩 수행

**2.5 컨트롤러 인터페이스 호출**
```java
// BranchSearchController.java
private final ISearchQueryService searchQueryService;  // ← @Autowired 생략 (Lombok)

@Override
public ResponseEntity<List<Map<String, Object>>> searchBranches(String query, String userLocation) {
    List<Map<String, Object>> results = searchQueryService.search(query, userLocation);
    return ResponseEntity.ok(results);
}
```
- `BranchSearchController.searchBranches()`: 컨트롤러 메서드 실제 실행
- 내부적으로 `ISearchQueryService.search()` 인터페이스 호출
- 인터페이스 기반 설계로 `SearchQueryManager` 구현체와 느슨한 결합

### Step 3: 검색 컨트롤러 → 비즈니스 로직 (상세 설명)
**클래스/인터페이스**: `BranchSearchController` → `Spring IoC Container` → `ISearchQueryService` → `SearchQueryManager`

**메시지 전달 과정 상세**:

**3.1 컨트롤러 클래스 실행**
```java
// BranchSearchController.java
@Override
public ResponseEntity<List<Map<String, Object>>> searchBranches(String query, String userLocation) {
    List<Map<String, Object>> results = searchQueryService.search(query, userLocation);
    return ResponseEntity.ok(results);
}
```
- `BranchSearchController.searchBranches()` 메서드 본문 실행 시작
- HTTP 파라미터가 이미 Java 객체로 변환된 상태 (`String query`, `String userLocation`)

**3.2 Spring 의존성 주입을 통한 서비스 객체 획득**
```java
// BranchSearchController.java
@RequiredArgsConstructor  // ← Lombok이 @Autowired 자동 생성
public class BranchSearchController implements IBranchSearchApi {
    private final ISearchQueryService searchQueryService;  // ← 인터페이스 타입으로 선언
}
```
- Spring IoC Container가 애플리케이션 시작 시 `@Service` 어노테이션이 있는 `SearchQueryManager` 인스턴스 생성
- `@RequiredArgsConstructor`가 `ISearchQueryService` 타입의 프록시 객체를 생성자 주입
- 런타임에 인터페이스 타입으로 실제 구현체에 접근

**3.3 인터페이스를 통한 비즈니스 로직 호출**
```java
// BranchSearchController.java
List<Map<String, Object>> results = searchQueryService.search(query, userLocation);
//                                                ↑ 인터페이스 메서드 호출
```
- `ISearchQueryService.search()` 인터페이스 메서드 호출
- Spring AOP가 JDK Dynamic Proxy를 통해 실제 `SearchQueryManager.search()` 구현체로 위임
- 인터페이스 기반 호출로 구현체 변경 시 컨트롤러 코드 수정 불필요

**3.4 SearchQueryManager의 Provided Interface 구현**
```java
// SearchQueryManager.java
@Service
@RequiredArgsConstructor
public class SearchQueryManager implements ISearchQueryService {  // ← 인터페이스 구현
    
    @Override
    public List<Map<String, Object>> search(String query, String userLocation, String customerId) {
        // 1. Tokenize query (IQueryTokenizer)
        List<String> tokens = queryTokenizer.tokenize(query);
        
        // 2. Query SearchEngine (ISearchEngineClient)
        List<Map<String, Object>> results = /* ElasticSearch query */;
        
        // 3. Publish SearchQueryEvent for Cold Path (if customerId exists)
        if (customerId != null) {
            messagePublisherService.publishEvent(new SearchQueryEvent(query, customerId, results.size()));
        }
        
        return results;
    }
}
```
- `SearchQueryManager.search()`: Facade Pattern으로 복잡한 검색 로직을 단순 API로 제공
- 내부적으로 `IQueryTokenizer`, `ISearchEngineClient`, `IMessagePublisherService` 등 다수의 인터페이스 조율

**메시지**:
- `BranchSearchController` → `ISearchQueryService.search(query, userLocation, customerId?)`: 검색 요청 위임
- `SearchQueryManager.search(query, userLocation, customerId?)`: Provided Interface 구현 및 내부 로직 실행 시작

### Step 4: 쿼리 토큰화 (Hot Path)
**클래스/인터페이스**: `SearchQueryManager` → `IQueryTokenizer` → `SimpleKeywordTokenizer`

**메시지**:
- `SearchQueryManager` → `IQueryTokenizer.tokenize(query)`: 쿼리 토큰화 요청
- `SimpleKeywordTokenizer.tokenize(query)`: 불용어 제거 및 공백 분리로 토큰 추출
- `SimpleKeywordTokenizer` → `SearchQueryManager`: `List<String> tokens` 반환

### Step 5: ElasticSearch 검색 수행
**클래스/인터페이스**: `SearchQueryManager` → `ISearchEngineClient` → `SearchEngineAdapter` → `ISearchEngineRepository` → `ElasticSearchRepository`

**메시지**:
- `SearchQueryManager` → `ISearchEngineClient.query(tokens, userLocation)`: 검색 요청
- `SearchEngineAdapter.query(tokens, userLocation)`: 검색 엔진 어댑팅
- `SearchEngineAdapter` → `ISearchEngineRepository.search(tokens, userLocation)`: 리포지토리 호출
- `ElasticSearchRepository.search(tokens, userLocation)`: 실제 ElasticSearch 쿼리 실행
- `ElasticSearchRepository` → `SearchEngineAdapter`: `List<Map<String, Object>> results` 반환
- `SearchEngineAdapter` → `SearchQueryManager`: `List<Map<String, Object>> results` 반환

### Step 6: Cold Path 이벤트 발행 (조건부)
**클래스/인터페이스**: `SearchQueryManager` → `IMessagePublisherService` → `RabbitMQAdapter` → `RabbitMQ`

**메시지**:
- `SearchQueryManager` → `IMessagePublisherService.publishEvent(SearchQueryEvent)`: 이벤트 발행 요청
- `RabbitMQAdapter.publishEvent(SearchQueryEvent)`: RabbitMQ에 이벤트 발행
- `RabbitMQAdapter` → `RabbitMQ`: AMQP 프로토콜로 메시지 전송

### Step 7: 검색 결과 반환
**클래스/인터페이스**: `SearchQueryManager` → `BranchSearchController` → `ApiGateway` → `MobileApp`

**메시지**:
- `SearchQueryManager` → `BranchSearchController`: `List<Map<String, Object>> results` 반환
- `BranchSearchController` → `ApiGateway`: HTTP 200 OK with JSON response
- `ApiGateway` → `MobileApp`: 검색 결과 JSON 응답

---

## Cold Path 메시지 플로우 (비동기)

### Step C1: 이벤트 수신 및 처리
**클래스/인터페이스**: `RabbitMQ` → `SearchQueryImprovementConsumer` → `ILLMAnalysisServiceClient`

**메시지**:
- `RabbitMQ` → `SearchQueryImprovementConsumer.handleSearchQueryEvent(SearchQueryEvent)`: 이벤트 수신
- `SearchQueryImprovementConsumer` → `ILLMAnalysisServiceClient.extractKeywords(query)`: LLM 키워드 추출 요청

### Step C2: 인덱스 개선
**클래스/인터페이스**: `SearchQueryImprovementConsumer` → `ISearchEngineRepository` → `ElasticSearchRepository`

**메시지**:
- `SearchQueryImprovementConsumer` → `ISearchEngineRepository.index(documentId, document)`: 인덱스 업데이트 요청
- `ElasticSearchRepository.index(documentId, document)`: ElasticSearch 인덱스 개선 실행

---

## 인터페이스 계약 상세

### IBranchSearchApi
```java
interface IBranchSearchApi {
    ResponseEntity<List<BranchInfo>> searchBranches(
        @RequestParam String query,
        @RequestParam Location userLocation,
        @RequestParam(required = false) String customerId
    );
}
```

### ISearchQueryService
```java
interface ISearchQueryService {
    List<Map<String, Object>> search(String query, Location userLocation);
    List<Map<String, Object>> search(String query, Location userLocation, String customerId);
}
```

### IQueryTokenizer
```java
interface IQueryTokenizer {
    List<String> tokenize(String query);
}
```

### ISearchEngineClient
```java
interface ISearchEngineClient {
    List<Map<String, Object>> query(List<String> tokens, Location userLocation);
}
```

### IMessagePublisherService
```java
interface IMessagePublisherService {
    void publishEvent(Object event);
}
```

---

## 성능 및 품질 속성 고려사항

### Hot Path (실시간 검색)
- **QAS-03 달성**: 토큰화 <10ms + ElasticSearch <500ms = 총 <530ms
- **QAS-02 달성**: 외부 의존성 최소화로 리소스 효율성 확보

### Cold Path (비동기 개선)
- **QAS-05 달성**: Hot Path 장애와 완전 격리로 가용성 보장
- **QAS-06 달성**: 지속적인 검색 정확도 개선으로 유지보수성 향상

---

## 지점 성향 맞춤 알림 기능 구현 방식 비교

### 제시된 Design Approach #3 vs 실제 구현 방식

**Design Approach #3 (데이터베이스 이벤트 리스너):**
- 데이터베이스 내부 트리거/리스너가 지점 성향 저장 감지
- 동일 트랜잭션 내에서 매칭 로직 실행
- 데이터베이스에 강하게 결합된 방식

**실제 구현 방식 (이벤트 기반 분산 처리):**
```java
// PreferenceMatchConsumer.java - 이벤트 기반 처리
@Component
public class PreferenceMatchConsumer {
    public void subscribeToBranchPreferenceCreated() {
        messageSubscriptionService.subscribe("BranchPreferenceCreatedEvent", this);
    }

    public void handleBranchPreferenceCreatedEvent(String branchId, String preferenceDetails) {
        if (isPeakTime()) {
            // 피크 타임에는 처리 미룸 (DD-07: Scheduling Policy)
            return;
        }
        // 오프피크 타임에 매칭 수행
        List<Map<String, Object>> matches = searchEngineRepository.search(keywords, null);
    }
}
```

### 실제 구현 방식의 장점

1. **유연한 스케줄링**: DD-07 Scheduling Policy 적용으로 피크 타임 부하 방지
2. **확장성**: RabbitMQ 기반 이벤트 큐잉으로 다중 인스턴스 확장 가능
3. **결합도 감소**: 데이터베이스와 비즈니스 로직의 분리로 유지보수성 향상
4. **모니터링 용이**: 이벤트 기반으로 처리 상태 추적 가능

### 구현된 컴포넌트 구조

```
지점 성향 저장 → BranchPreferenceCreatedEvent 발행 → RabbitMQ → PreferenceMatchConsumer → 매칭 수행 → 알림 발송
```

**결론:** 실제 구현은 Design Approach #3의 개념을 이벤트 기반 아키텍처로 확장한 형태로, 더 유연하고 확장 가능한 방식으로 구현되었습니다.

---

## 결론

고객앱에서 지점 검색 시 메시지는 7단계의 Hot Path와 선택적인 Cold Path를 통해 이동합니다. 각 단계에서 인터페이스 기반 설계로 느슨한 결합을 유지하며, Strategy Pattern과 Observer Pattern을 통해 확장성과 유지보수성을 확보합니다. 특히 Hot/Cold Path 분리로 실시간성과 정확성을 동시에 달성하는 설계를 보여줍니다.
