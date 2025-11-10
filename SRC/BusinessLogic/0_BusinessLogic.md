네, 알겠습니다. 저희 **하이브리드 MSA (4-Layer)** 구조에서 가장 상위 계층인 **Presentation Layer**와 연결되는 **Business Logic Layer**부터 시작하여, 설계된 컴포넌트들의 Java 코드를 **티어별/서비스별**로 나누어 제시하겠습니다.

이 코드는 **Database per Service 원칙**과 **Microsoft .NET Naming Guidelines**를 준수합니다.

-----

## 💻 1. Business Logic Layer 코드 목록

Business Logic Layer는 **API Gateway**를 통해 클라이언트 요청을 받고, **Auth, Helper, Search, Monitoring** 등 핵심 비즈니스 로직을 수행합니다.

### A. API Gateway (RequestRouter & InternalClientManager)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **RequestRouter** | `com.smartfitness.gateway.internal.logic.RequestRouter` |
| **InternalClientManager** | `com.smartfitness.gateway.internal.logic.InternalClientManager` |
| **RequestSignatureVerifier** | `com.smartfitness.gateway.security.RequestSignatureVerifier` |
| **NetworkZonePolicy** | `com.smartfitness.gateway.security.NetworkZonePolicy` |

```java
package com.smartfitness.gateway.internal.logic;

import com.smartfitness.gateway.ports.IApiGatewayEntry;
import com.smartfitness.gateway.model.ClientRequest;
import com.smartfitness.gateway.model.ServiceResponse;
import com.smartfitness.auth.internal.security.TokenValidatorService;
import com.smartfitness.gateway.ports.IAuthenticationClient; // Required Port
import com.smartfitness.system.discovery.ServiceDiscovery;
import com.smartfitness.system.exception.ServiceUnavailableException;
import com.smartfitness.system.client.HttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RequestRouter: API Gateway의 핵심 컴포넌트입니다. 모든 요청을 받아 인증을 검사하고 
 * 적절한 내부 마이크로서비스로 요청을 라우팅하는 책임을 가집니다.
 * Pattern: Front Controller, Broker
 */
public class RequestRouter implements IApiGatewayEntry {
    private final IAuthenticationClient authClient; // 수정됨: TokenValidatorService 대신 IAuthenticationClient 사용
    private final InternalClientManager internalClientManager;
    
    private static final Map<String, String> SERVICE_ROUTES = new ConcurrentHashMap<>();
    static {
        SERVICE_ROUTES.put("/auth", "AuthService");
        SERVICE_ROUTES.put("/access", "AccessService");
        SERVICE_ROUTES.put("/search", "SearchService");
        SERVICE_ROUTES.put("/helper", "HelperService");
    }

    public RequestRouter(IAuthenticationClient authClient, InternalClientManager internalClientManager) {
        this.authClient = authClient;
        this.internalClientManager = internalClientManager;
    }

    @Override
    public ServiceResponse routeRequest(ClientRequest request) {
        if (!processSecurityCheck(request)) {
            return ServiceResponse.FORBIDDEN("Invalid or missing token.");
        }

        String targetServicePath = resolveTargetServicePath(request.getPath());
        if (targetServicePath == null) {
            return ServiceResponse.NOT_FOUND("Endpoint not found.");
        }
        
        return internalClientManager.forwardRequest(targetServicePath, request);
    }

    /**
     * 인증/인가 처리를 Auth Service로 위임합니다.
     */
    private boolean processSecurityCheck(ClientRequest request) {
        String token = request.getAuthToken();
        
        if (isPublicEndpoint(request.getPath())) {
            return true;
        }

        // Auth Service의 Provided API 호출 (DD-09: Authenticate Actors)
        if (token == null || !authClient.validateToken(token)) {
            return false;
        }
        
        return true;
    }

    private String resolveTargetServicePath(String fullPath) {
        String segment = fullPath.split("/")[1];
        if (segment == null) return null;
        
        String pathPrefix = "/" + segment;
        return SERVICE_ROUTES.get(pathPrefix);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
    }
}

/**
 * InternalClientManager: 내부 마이크로서비스(Auth, Access, Search 등)와의 통신을 관리합니다.
 * Role: Broker (요청을 적절한 내부 서비스로 전달)
 * Tactic: Escalating Restart, Active Redundancy
 */
public class InternalClientManager {
    private final HttpClient httpClient;

    public InternalClientManager(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public ServiceResponse forwardRequest(String servicePath, ClientRequest request) {
        try {
            // 1. 서비스 디스커버리 및 로드 밸런싱을 통해 적절한 인스턴스 URL 확인 (Active Redundancy)
            String serviceUrl = ServiceDiscovery.resolveAndBalance(servicePath);
            
            // 2. 실제 구현: HTTP 클라이언트나 gRPC 클라이언트를 사용하여 내부 서비스 호출
            if (servicePath.equals("AccessService")) {
                return callGrpcService(serviceUrl, request); // DD-05 gRPC 호출
            }
            
            return httpClient.sendRequest(serviceUrl, request);
            
        } catch (ServiceUnavailableException e) {
            // Tactic: Escalating Restart (장애 발생 시 복구 오케스트레이터에 알림)
            return ServiceResponse.SERVICE_UNAVAILABLE();
        }
    }
    
    private ServiceResponse callGrpcService(String url, ClientRequest request) {
        // ... 실제 gRPC 호출 및 응답 처리 로직 ...
        return new ServiceResponse("200 OK", "Success via gRPC");
    }
}
```

* **RequestSignatureVerifier:** API Gateway�� ������ HMAC ��ī�� �����Ͽ� ��û body/path/method ������ ��ȣȭ�ϰ� �ӷ��� ������ (DD-08: Verify Message Integrity).
* **NetworkZonePolicy:** ������ ��û IP�� ������/���� ������ �˻��Ͽ�, Private Network�� ��ġ�� ������ �����μ��񽺴� ������ �������� (DD-08: Limit Access).

-----

## B. Auth Service (TokenValidatorService)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **TokenValidatorService** | `com.smartfitness.auth.internal.security.TokenValidatorService` |

```java
package com.smartfitness.auth.internal.security;

import com.smartfitness.auth.ports.IAuthRepository; // IAuthRepository에 대한 의존성 제거됨 (상위 컴포넌트가 처리)
import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.UserAccount;
import java.util.Date;

/**
 * TokenValidatorService: 인증 토큰의 유효성 검증 및 암호 해독을 담당하는 핵심 보안 컴포넌트입니다.
 * Tactic: Verify Message Integrity (DD-09)
 * 역할: 순수하게 토큰의 기술적 유효성(서명, 만료)만 검사하며, DB 접근 책임은 상위 컴포넌트가 가집니다.
 */
public class TokenValidatorService {
    // IAuthRepository 의존성 제거됨

    public TokenValidatorService() {
        // 생성자 변경
    }

    /**
     * API Gateway로부터 요청받은 토큰의 유효성을 검증합니다.
     * (이 메서드는 토큰의 기술적 유효성만 확인하며, DB 접근은 상위 매니저가 담당합니다.)
     */
    public boolean isValid(String token) {
        // 1. 토큰 포맷/서명 검증 (Verify Message Integrity)
        if (!verifyTokenSignature(token)) {
            return false;
        }
        
        // 2. 토큰 만료 시간 확인
        if (isTokenExpired(token)) {
            return false;
        }

        return true;
    }
    
    private boolean verifyTokenSignature(String token) { /* ... */ return true; }
    private boolean isTokenExpired(String token) { /* ... */ return false; }
}
```

-----

## C. Helper Service (AIPanDokuConsumer & RewardUpdateConsumer)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **AIPanDokuConsumer** | `com.smartfitness.helper.internal.consumer.AIPanDokuConsumer` |
| **RewardUpdateConsumer** | `com.smartfitness.helper.internal.consumer.RewardUpdateConsumer` |


* **AIPanDokuConsumer:** `tasks.submitted` 토픽을 `IMessageSubscriptionService`로 구독하여 1차 AI 판독을 비동기로 처리합니다. 이벤트 페이로드(taskId, helperId, imageUrl)를 활용해 `IPanDokuModelService`를 호출하고, `IHelperRepository.updateTaskStatus()`로 결과를 반영합니다.
* **RewardUpdateConsumer:** `tasks.confirmed` 토픽을 구독하여 보상 승인 여부에 따라 `IHelperRepository.updateBalance()` 및 `updateTaskStatus()`를 업데이트합니다.

## 💻 Business Logic Layer 코드 목록 (Continuation)

### A. Search Service (SearchManager & PreferenceMatchConsumer)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **SearchManager** | `com.smartfitness.search.internal.logic.SearchManager` |
| **PreferenceMatchConsumer** | `com.smartfitness.search.internal.consumer.PreferenceMatchConsumer` |

```java
package com.smartfitness.search.internal.logic;

import com.smartfitness.search.ports.ISearchServiceApi;
import com.smartfitness.search.ports.ISearchRepository;
import com.smartfitness.search.ports.ILLMAnalysisService;
import com.smartfitness.search.model.SearchQuery;
import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.event.BranchPreferenceCreatedEvent;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import com.smartfitness.search.model.ContentType;
import com.smartfitness.search.model.CustomerPreference;
import java.util.List;

/**
 * SearchManager: 고객의 자연어 쿼리 처리, LLM 연동, 성향 데이터 생성 및 검색을 총괄합니다.
 */
public class SearchManager implements ISearchServiceApi {
    private final ISearchRepository repository;
    private final ILLMAnalysisService llmClient;
    private final IMessagePublisherService messagePublisher;
    
    public SearchManager(ISearchRepository repository, ILLMAnalysisService llmClient, IMessagePublisherService messagePublisher) {
        this.repository = repository;
        this.llmClient = llmClient;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public List<BranchRecommendation> searchBranches(SearchQuery query, Long customerId) {
        // 1. LLM을 통해 고객의 자연어 쿼리 분석 (UC-09)
        List<String> customerKeywords = llmClient.analyzeTextForPreferences(query.getText());

        // 2. 성향 데이터 생성 및 저장 (SF-06)
        repository.saveCustomerPreference(customerId, customerKeywords);

        // 3. 전문 검색 엔진(DS-07)에서 고속 매칭 쿼리 실행 (DD-06)
        return repository.executeMatchQuery(customerKeywords);
    }

    @Override
    public void registerContent(String content, Long sourceId, ContentType type) {
        // 1. LLM을 통해 콘텐츠 분석 (성향 추출)
        List<String> preferenceKeywords = llmClient.analyzeTextForPreferences(content);
        
        // 2. DB에 성향 데이터 저장 (UC-10, UC-18)
        repository.saveBranchPreference(sourceId, preferenceKeywords);

        // 3. 알림 매칭을 위한 이벤트 발행 (DD-07의 실시간 트리거)
        messagePublisher.publish(new BranchPreferenceCreatedEvent(sourceId, preferenceKeywords));
    }
}

* **PreferenceMatchConsumer:** `preferences` 토픽을 `IMessageSubscriptionService`로 구독하여 `BranchPreferenceCreatedEvent`를 처리합니다. 이벤트를 수신하면 `ISearchRepository.executeMatchQuery()`를 호출해 비동기 추천을 사전 계산합니다. (코드: `search/internal/consumer/PreferenceMatchConsumer.java`)
```

-----

### B. Monitoring Service (StatusReceiverManager & HeartbeatChecker)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **StatusReceiverManager** | `com.smartfitness.monitor.internal.logic.StatusReceiverManager` |
| **HeartbeatChecker** | `com.smartfitness.monitor.internal.logic.HeartbeatChecker` |

```java
package com.smartfitness.monitor.internal.logic;

import com.smartfitness.monitor.ports.IEquipmentStatusService;
import com.smartfitness.monitor.ports.IMonitoringTriggerService;
import com.smartfitness.monitor.ports.IMonitorRepository;
import com.smartfitness.monitor.ports.IMessagePublisherService;
import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.event.EquipmentFaultDetectedEvent;
import java.util.Date;
import java.util.List;

/**
 * StatusReceiverManager: 설비 보고를 수신하고 DB에 기록하며, 즉각적인 고장 보고를 처리합니다.
 */
public class StatusReceiverManager implements IEquipmentStatusService {
    private final IMonitorRepository repository;
    private final IMessagePublisherService publisher;

    public StatusReceiverManager(IMonitorRepository repository, IMessagePublisherService publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public void receiveStatusReport(EquipmentStatusReport report) {
        repository.saveStatus(report); // UC-20: 상태 기록

        if (report.isFault()) {
            // 설비 자체에서 '고장' 상태를 보고한 경우 (경로 A: 즉각 고장 감지)
            publisher.publish(new EquipmentFaultDetectedEvent(report.getEquipmentId(), "Direct Fault Report"));
        }
    }
}


/**
 * HeartbeatChecker: 타이머에 의해 트리거되어 모든 설비의 상태를 주기적으로 점검합니다.
 * Tactic: Process Control / Ping/echo (DD-04)
 */
public class HeartbeatChecker implements IMonitoringTriggerService {
    private final IMonitorRepository repository;
    private final IMessagePublisherService publisher;
    private static final long TIMEOUT_THRESHOLD_MS = 30000; // 30초

    private final List<String> allEquipmentIds = List.of("GATE-01", "CAM-01", "GATE-02"); 

    @Override
    public void triggerMonitorCheck() {
        for (String equipmentId : allEquipmentIds) {
            Date lastReportTime = repository.findLastReportTime(equipmentId);
            
            if (lastReportTime == null || (System.currentTimeMillis() - lastReportTime.getTime() > TIMEOUT_THRESHOLD_MS)) {
                // 30초 이상 보고 누락 감지 (UC-21)
                
                // 고장으로 확정하고 이벤트 발행 (경로 B)
                publisher.publish(new EquipmentFaultDetectedEvent(equipmentId, "Heartbeat Timeout"));
            }
        }
    }
}
```

-----

### C. Auth Service (AuthorizationManager)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **AuthorizationManager** | `com.smartfitness.auth.internal.logic.AuthorizationManager` |

```java
package com.smartfitness.auth.internal.logic;

import com.smartfitness.auth.ports.IAuthServiceApi;
import com.smartfitness.auth.ports.IAuthRepository;
import com.smartfitness.auth.ports.ICreditCardVerificationService;
import com.smartfitness.auth.internal.security.TokenService; // TokenService 사용
import com.smartfitness.auth.internal.security.TokenValidatorService; // TokenValidatorService 사용
import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.UserCredentials;
import com.smartfitness.auth.model.RegistrationDetails;
import com.smartfitness.event.UserRegisteredEvent;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import java.util.List;

/**
 * AuthorizationManager: 로그인, 토큰 검증, 계정 등록 등의 핵심 인증 흐름을 관리합니다.
 * Tactic: Authenticate Actors, Authorize Actors
 */
public class AuthorizationManager implements IAuthServiceApi {
    private final IAuthRepository repository;
    private final ICreditCardVerificationService verificationClient;
    private final IMessagePublisherService messagePublisher;
    private final TokenValidatorService tokenValidator;
    private final TokenService tokenGenerator; // TokenService를 생성용으로 사용 가정

    // 생성자에 필요한 의존성 주입
    public AuthorizationManager(IAuthRepository repository, 
                                ICreditCardVerificationService verificationClient, 
                                IMessagePublisherService messagePublisher,
                                TokenValidatorService tokenValidator,
                                TokenService tokenGenerator) {
        this.repository = repository;
        this.verificationClient = verificationClient;
        this.messagePublisher = messagePublisher;
        this.tokenValidator = tokenValidator;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public AuthToken login(UserCredentials credentials) {
        // 1. DB에서 사용자 정보 및 해시된 비밀번호 로드 (IAuthRepository 사용)
        String storedHash = repository.loadPasswordHash(credentials.getUserId());
        
        // 2. 비밀번호 해시 비교 (Verify Message Integrity)
        if (!tokenGenerator.verifyPassword(credentials.getPassword(), storedHash)) {
             throw new SecurityException("Invalid credentials.");
        }
        
        // 3. 성공 시, 토큰 생성 및 발급
        return tokenGenerator.generateAuthToken(credentials.getUserId(), List.of("CUSTOMER"));
    }

    @Override
    public boolean validateToken(String token) {
        // 1. 기술적 유효성 검증 (TokenValidatorService 사용)
        if (!tokenValidator.isValid(token)) {
            return false;
        }
        
        // 2. DB 기반 검증 (예: 토큰 블랙리스트 확인, IAuthRepository 사용)
        // 이 로직은 TokenValidatorService에서 분리되어 AuthorizationManager가 DB 접근 책임을 가짐.
        // if (repository.isTokenBlacklisted(token)) return false; 

        return true;
    }

    @Override
    public void registerUser(RegistrationDetails details) {
        // 1. 신용카드 본인 인증 수행 (ICreditCardVerificationService 사용)
        if (!verificationClient.verifyIdentity(details.getCardDetails(), details.getUserId())) {
            throw new SecurityException("Identity verification failed.");
        }
        
        // 2. 비밀번호 해싱 (TokenService 사용) 및 DB 저장 
        details.setPasswordHash(tokenGenerator.hashPassword(details.getPassword()));
        repository.saveUser(details.toUserAccount());
        
        // 3. 회원가입 완료 이벤트 발행
        messagePublisher.publish(new UserRegisteredEvent(details.getUserId()));
    }
}
```

-----

### C. Notification Dispatcher (NotificationDispatcherConsumer)

| 구성요소 | 클래스 경로 |
| :--- | :--- |
| **NotificationDispatcherConsumer** | `com.smartfitness.notify.internal.consumer.NotificationDispatcherConsumer` |

```java
package com.smartfitness.notify.internal.consumer;

import com.smartfitness.messaging.ports.IMessageSubscriptionService;
import com.smartfitness.notify.ports.IPushNotificationGateway;
import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.EquipmentFaultDetectedEvent;

/**
 * NotificationDispatcherConsumer: "faults" 토픽을 구독하여 관리자 PUSH 알림을 전송합니다.
 * Tactic: Use an Intermediary, Message Based.
 */
public class NotificationDispatcherConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private final IPushNotificationGateway pushGateway;

    public NotificationDispatcherConsumer(IMessageSubscriptionService subscriptionService,
                                          IPushNotificationGateway pushGateway) {
        this.subscriptionService = subscriptionService;
        this.pushGateway = pushGateway;
    }

    public void register() {
        subscriptionService.subscribeToTopic("faults", this::handleFaultEvent);
    }

    private void handleFaultEvent(DomainEvent event) {
        if (!(event instanceof EquipmentFaultDetectedEvent faultEvent)) return;
        pushGateway.sendPushNotification("ADMIN", "[FAULT] " + faultEvent.getEquipmentId(),
                                         "Reason: " + faultEvent.getReason());
    }
}
```
