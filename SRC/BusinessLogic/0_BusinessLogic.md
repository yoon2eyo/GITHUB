?? ?Œê² ?µë‹ˆ?? ?€??**?˜ì´ë¸Œë¦¬??MSA (4-Layer)** êµ¬ì¡°?ì„œ ê°€???ìœ„ ê³„ì¸µ??**Presentation Layer**?€ ?°ê²°?˜ëŠ” **Business Logic Layer**ë¶€???œì‘?˜ì—¬, ?¤ê³„??ì»´í¬?ŒíŠ¸?¤ì˜ Java ì½”ë“œë¥?**?°ì–´ë³??œë¹„?¤ë³„**ë¡??˜ëˆ„???œì‹œ?˜ê² ?µë‹ˆ??

??ì½”ë“œ??**Database per Service ?ì¹™**ê³?**Microsoft .NET Naming Guidelines**ë¥?ì¤€?˜í•©?ˆë‹¤.

-----

## ?’» 1. Business Logic Layer ì½”ë“œ ëª©ë¡

Business Logic Layer??**API Gateway**ë¥??µí•´ ?´ë¼?´ì–¸???”ì²­??ë°›ê³ , **Auth, Helper, Search, Monitoring** ???µì‹¬ ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§???˜í–‰?©ë‹ˆ??

### A. API Gateway (RequestRouter & InternalClientManager)

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
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
 * RequestRouter: API Gateway???µì‹¬ ì»´í¬?ŒíŠ¸?…ë‹ˆ?? ëª¨ë“  ?”ì²­??ë°›ì•„ ?¸ì¦??ê²€?¬í•˜ê³?
 * ?ì ˆ???´ë? ë§ˆì´?¬ë¡œ?œë¹„?¤ë¡œ ?”ì²­???¼ìš°?…í•˜??ì±…ì„??ê°€ì§‘ë‹ˆ??
 * Pattern: Front Controller, Broker
 */
public class RequestRouter implements IApiGatewayEntry {
    private final IAuthenticationClient authClient; // ?˜ì •?? TokenValidatorService ?€??IAuthenticationClient ?¬ìš©
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
     * ?¸ì¦/?¸ê? ì²˜ë¦¬ë¥?Auth Serviceë¡??„ì„?©ë‹ˆ??
     */
    private boolean processSecurityCheck(ClientRequest request) {
        String token = request.getAuthToken();
        
        if (isPublicEndpoint(request.getPath())) {
            return true;
        }

        // Auth Service??Provided API ?¸ì¶œ (DD-09: Authenticate Actors)
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
 * InternalClientManager: ?´ë? ë§ˆì´?¬ë¡œ?œë¹„??Auth, Access, Search ???€???µì‹ ??ê´€ë¦¬í•©?ˆë‹¤.
 * Role: Broker (?”ì²­???ì ˆ???´ë? ?œë¹„?¤ë¡œ ?„ë‹¬)
 * Tactic: Escalating Restart, Active Redundancy
 */
public class InternalClientManager {
    private final HttpClient httpClient;

    public InternalClientManager(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public ServiceResponse forwardRequest(String servicePath, ClientRequest request) {
        try {
            // 1. ?œë¹„???”ìŠ¤ì»¤ë²„ë¦?ë°?ë¡œë“œ ë°¸ëŸ°?±ì„ ?µí•´ ?ì ˆ???¸ìŠ¤?´ìŠ¤ URL ?•ì¸ (Active Redundancy)
            String serviceUrl = ServiceDiscovery.resolveAndBalance(servicePath);
            
            // 2. ?¤ì œ êµ¬í˜„: HTTP ?´ë¼?´ì–¸?¸ë‚˜ gRPC ?´ë¼?´ì–¸?¸ë? ?¬ìš©?˜ì—¬ ?´ë? ?œë¹„???¸ì¶œ
            if (servicePath.equals("AccessService")) {
                return callGrpcService(serviceUrl, request); // DD-05 gRPC ?¸ì¶œ
            }
            
            return httpClient.sendRequest(serviceUrl, request);
            
        } catch (ServiceUnavailableException e) {
            // Tactic: Escalating Restart (?¥ì•  ë°œìƒ ??ë³µêµ¬ ?¤ì??¤íŠ¸?ˆì´?°ì— ?Œë¦¼)
            return ServiceResponse.SERVICE_UNAVAILABLE();
        }
    }
    
    private ServiceResponse callGrpcService(String url, ClientRequest request) {
        // ... ?¤ì œ gRPC ?¸ì¶œ ë°??‘ë‹µ ì²˜ë¦¬ ë¡œì§ ...
        return new ServiceResponse("200 OK", "Success via gRPC");
    }
}
```

* **RequestSignatureVerifier:** API Gatewayï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ HMAC ï¿½ï¿½Ä«ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½Ï¿ï¿½ ï¿½ï¿½Ã» body/path/method ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½È£È­ï¿½Ï°ï¿½ ï¿½Ó·ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ (DD-08: Verify Message Integrity).
* **NetworkZonePolicy:** ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½Ã» IPï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½/ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½Ë»ï¿½ï¿½Ï¿ï¿½, Private Networkï¿½ï¿½ ï¿½ï¿½Ä¡ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½Î¼ï¿½ï¿½ñ½º´ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ (DD-08: Limit Access).

-----

## B. Auth Service (TokenValidatorService)

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
| :--- | :--- |
| **TokenValidatorService** | `com.smartfitness.auth.internal.security.TokenValidatorService` |

```java
package com.smartfitness.auth.internal.security;

import com.smartfitness.auth.ports.IAuthRepository; // IAuthRepository???€???˜ì¡´???œê±°??(?ìœ„ ì»´í¬?ŒíŠ¸ê°€ ì²˜ë¦¬)
import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.UserAccount;
import java.util.Date;

/**
 * TokenValidatorService: ?¸ì¦ ? í°??? íš¨??ê²€ì¦?ë°??”í˜¸ ?´ë…???´ë‹¹?˜ëŠ” ?µì‹¬ ë³´ì•ˆ ì»´í¬?ŒíŠ¸?…ë‹ˆ??
 * Tactic: Verify Message Integrity (DD-09)
 * ??• : ?œìˆ˜?˜ê²Œ ? í°??ê¸°ìˆ ??? íš¨???œëª…, ë§Œë£Œ)ë§?ê²€?¬í•˜ë©? DB ?‘ê·¼ ì±…ì„?€ ?ìœ„ ì»´í¬?ŒíŠ¸ê°€ ê°€ì§‘ë‹ˆ??
 */
public class TokenValidatorService {
    // IAuthRepository ?˜ì¡´???œê±°??

    public TokenValidatorService() {
        // ?ì„±??ë³€ê²?
    }

    /**
     * API Gatewayë¡œë????”ì²­ë°›ì? ? í°??? íš¨?±ì„ ê²€ì¦í•©?ˆë‹¤.
     * (??ë©”ì„œ?œëŠ” ? í°??ê¸°ìˆ ??? íš¨?±ë§Œ ?•ì¸?˜ë©°, DB ?‘ê·¼?€ ?ìœ„ ë§¤ë‹ˆ?€ê°€ ?´ë‹¹?©ë‹ˆ??)
     */
    public boolean isValid(String token) {
        // 1. ? í° ?¬ë§·/?œëª… ê²€ì¦?(Verify Message Integrity)
        if (!verifyTokenSignature(token)) {
            return false;
        }
        
        // 2. ? í° ë§Œë£Œ ?œê°„ ?•ì¸
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

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
| :--- | :--- |
| **AIPanDokuConsumer** | `com.smartfitness.helper.internal.consumer.AIPanDokuConsumer` |
| **RewardUpdateConsumer** | `com.smartfitness.helper.internal.consumer.RewardUpdateConsumer` |


* **AIPanDokuConsumer:** `tasks.submitted` ? í”½??`IMessageSubscriptionService`ë¡?êµ¬ë…?˜ì—¬ 1ì°?AI ?ë…??ë¹„ë™ê¸°ë¡œ ì²˜ë¦¬?©ë‹ˆ?? ?´ë²¤???˜ì´ë¡œë“œ(taskId, helperId, imageUrl)ë¥??œìš©??`IPanDokuModelService`ë¥??¸ì¶œ?˜ê³ , `IHelperRepository.updateTaskStatus()`ë¡?ê²°ê³¼ë¥?ë°˜ì˜?©ë‹ˆ??
* **RewardUpdateConsumer:** `tasks.confirmed` ? í”½??êµ¬ë…?˜ì—¬ ë³´ìƒ ?¹ì¸ ?¬ë????°ë¼ `IHelperRepository.updateBalance()` ë°?`updateTaskStatus()`ë¥??…ë°?´íŠ¸?©ë‹ˆ??

## ?’» Business Logic Layer ì½”ë“œ ëª©ë¡ (Continuation)

### A. Search Service (BranchContentService & PreferenceMatchConsumer)

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
| :--- | :--- |
| **BranchContentService** | `com.smartfitness.search.internal.logic.BranchContentService` |
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
 * BranchContentService: ê³ ê°???ì—°??ì¿¼ë¦¬ ì²˜ë¦¬, LLM ?°ë™, ?±í–¥ ?°ì´???ì„± ë°?ê²€?‰ì„ ì´ê´„?©ë‹ˆ??
 */
public class BranchContentService implements ISearchServiceApi {
    private final ISearchRepository repository;
    private final ILLMAnalysisService llmClient;
    private final IMessagePublisherService messagePublisher;
    
    public BranchContentService(ISearchRepository repository, ILLMAnalysisService llmClient, IMessagePublisherService messagePublisher) {
        this.repository = repository;
        this.llmClient = llmClient;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public List<BranchRecommendation> searchBranches(SearchQuery query, Long customerId) {
        // 1. LLM???µí•´ ê³ ê°???ì—°??ì¿¼ë¦¬ ë¶„ì„ (UC-09)
        List<String> customerKeywords = llmClient.analyzeTextForPreferences(query.getText());

        // 2. ?±í–¥ ?°ì´???ì„± ë°??€??(SF-06)
        repository.saveCustomerPreference(customerId, customerKeywords);

        // 3. ?„ë¬¸ ê²€???”ì§„(DS-07)?ì„œ ê³ ì† ë§¤ì¹­ ì¿¼ë¦¬ ?¤í–‰ (DD-06)
        return repository.executeMatchQuery(customerKeywords);
    }

    @Override
    public void registerContent(String content, Long sourceId, ContentType type) {
        // 1. LLM???µí•´ ì½˜í…ì¸?ë¶„ì„ (?±í–¥ ì¶”ì¶œ)
        List<String> preferenceKeywords = llmClient.analyzeTextForPreferences(content);
        
        // 2. DB???±í–¥ ?°ì´???€??(UC-10, UC-18)
        repository.saveBranchPreference(sourceId, preferenceKeywords);

        // 3. ?Œë¦¼ ë§¤ì¹­???„í•œ ?´ë²¤??ë°œí–‰ (DD-07???¤ì‹œê°??¸ë¦¬ê±?
        messagePublisher.publish(new BranchPreferenceCreatedEvent(sourceId, preferenceKeywords));
    }
}

* **PreferenceMatchConsumer:** `preferences` ? í”½??`IMessageSubscriptionService`ë¡?êµ¬ë…?˜ì—¬ `BranchPreferenceCreatedEvent`ë¥?ì²˜ë¦¬?©ë‹ˆ?? ?´ë²¤?¸ë? ?˜ì‹ ?˜ë©´ `ISearchRepository.executeMatchQuery()`ë¥??¸ì¶œ??ë¹„ë™ê¸?ì¶”ì²œ???¬ì „ ê³„ì‚°?©ë‹ˆ?? (ì½”ë“œ: `search/internal/consumer/PreferenceMatchConsumer.java`)
```

-----

### B. Monitoring Service (StatusReceiverManager & HeartbeatChecker)

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
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
 * StatusReceiverManager: ?¤ë¹„ ë³´ê³ ë¥??˜ì‹ ?˜ê³  DB??ê¸°ë¡?˜ë©°, ì¦‰ê°?ì¸ ê³ ì¥ ë³´ê³ ë¥?ì²˜ë¦¬?©ë‹ˆ??
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
        repository.saveStatus(report); // UC-20: ?íƒœ ê¸°ë¡

        if (report.isFault()) {
            // ?¤ë¹„ ?ì²´?ì„œ 'ê³ ì¥' ?íƒœë¥?ë³´ê³ ??ê²½ìš° (ê²½ë¡œ A: ì¦‰ê° ê³ ì¥ ê°ì?)
            publisher.publish(new EquipmentFaultDetectedEvent(report.getEquipmentId(), "Direct Fault Report"));
        }
    }
}


/**
 * HeartbeatChecker: ?€?´ë¨¸???˜í•´ ?¸ë¦¬ê±°ë˜??ëª¨ë“  ?¤ë¹„???íƒœë¥?ì£¼ê¸°?ìœ¼ë¡??ê??©ë‹ˆ??
 * Tactic: Process Control / Ping/echo (DD-04)
 */
public class HeartbeatChecker implements IMonitoringTriggerService {
    private final IMonitorRepository repository;
    private final IMessagePublisherService publisher;
    private static final long TIMEOUT_THRESHOLD_MS = 30000; // 30ì´?

    private final List<String> allEquipmentIds = List.of("GATE-01", "CAM-01", "GATE-02"); 

    @Override
    public void triggerMonitorCheck() {
        for (String equipmentId : allEquipmentIds) {
            Date lastReportTime = repository.findLastReportTime(equipmentId);
            
            if (lastReportTime == null || (System.currentTimeMillis() - lastReportTime.getTime() > TIMEOUT_THRESHOLD_MS)) {
                // 30ì´??´ìƒ ë³´ê³  ?„ë½ ê°ì? (UC-21)
                
                // ê³ ì¥?¼ë¡œ ?•ì •?˜ê³  ?´ë²¤??ë°œí–‰ (ê²½ë¡œ B)
                publisher.publish(new EquipmentFaultDetectedEvent(equipmentId, "Heartbeat Timeout"));
            }
        }
    }
}
```

-----

### C. Auth Service (AuthorizationManager)

| ì»´í¬?ŒíŠ¸ | ?Œì¼ ê²½ë¡œ |
| :--- | :--- |
| **AuthorizationManager** | `com.smartfitness.auth.internal.logic.AuthorizationManager` |

```java
package com.smartfitness.auth.internal.logic;

import com.smartfitness.auth.ports.IAuthServiceApi;
import com.smartfitness.auth.ports.IAuthRepository;
import com.smartfitness.auth.ports.ICreditCardVerificationService;
import com.smartfitness.auth.internal.security.TokenService; // TokenService ?¬ìš©
import com.smartfitness.auth.internal.security.TokenValidatorService; // TokenValidatorService ?¬ìš©
import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.UserCredentials;
import com.smartfitness.auth.model.RegistrationDetails;
import com.smartfitness.event.UserRegisteredEvent;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import java.util.List;

/**
 * AuthorizationManager: ë¡œê·¸?? ? í° ê²€ì¦? ê³„ì • ?±ë¡ ?±ì˜ ?µì‹¬ ?¸ì¦ ?ë¦„??ê´€ë¦¬í•©?ˆë‹¤.
 * Tactic: Authenticate Actors, Authorize Actors
 */
public class AuthorizationManager implements IAuthServiceApi {
    private final IAuthRepository repository;
    private final ICreditCardVerificationService verificationClient;
    private final IMessagePublisherService messagePublisher;
    private final TokenValidatorService tokenValidator;
    private final TokenService tokenGenerator; // TokenServiceë¥??ì„±?©ìœ¼ë¡??¬ìš© ê°€??

    // ?ì„±?ì— ?„ìš”???˜ì¡´??ì£¼ì…
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
        // 1. DB?ì„œ ?¬ìš©???•ë³´ ë°??´ì‹œ??ë¹„ë?ë²ˆí˜¸ ë¡œë“œ (IAuthRepository ?¬ìš©)
        String storedHash = repository.loadPasswordHash(credentials.getUserId());
        
        // 2. ë¹„ë?ë²ˆí˜¸ ?´ì‹œ ë¹„êµ (Verify Message Integrity)
        if (!tokenGenerator.verifyPassword(credentials.getPassword(), storedHash)) {
             throw new SecurityException("Invalid credentials.");
        }
        
        // 3. ?±ê³µ ?? ? í° ?ì„± ë°?ë°œê¸‰
        return tokenGenerator.generateAuthToken(credentials.getUserId(), List.of("CUSTOMER"));
    }

    @Override
    public boolean validateToken(String token) {
        // 1. ê¸°ìˆ ??? íš¨??ê²€ì¦?(TokenValidatorService ?¬ìš©)
        if (!tokenValidator.isValid(token)) {
            return false;
        }
        
        // 2. DB ê¸°ë°˜ ê²€ì¦?(?? ? í° ë¸”ë™ë¦¬ìŠ¤???•ì¸, IAuthRepository ?¬ìš©)
        // ??ë¡œì§?€ TokenValidatorService?ì„œ ë¶„ë¦¬?˜ì–´ AuthorizationManagerê°€ DB ?‘ê·¼ ì±…ì„??ê°€ì§?
        // if (repository.isTokenBlacklisted(token)) return false; 

        return true;
    }

    @Override
    public void registerUser(RegistrationDetails details) {
        // 1. ? ìš©ì¹´ë“œ ë³¸ì¸ ?¸ì¦ ?˜í–‰ (ICreditCardVerificationService ?¬ìš©)
        if (!verificationClient.verifyIdentity(details.getCardDetails(), details.getUserId())) {
            throw new SecurityException("Identity verification failed.");
        }
        
        // 2. ë¹„ë?ë²ˆí˜¸ ?´ì‹± (TokenService ?¬ìš©) ë°?DB ?€??
        details.setPasswordHash(tokenGenerator.hashPassword(details.getPassword()));
        repository.saveUser(details.toUserAccount());
        
        // 3. ?Œì›ê°€???„ë£Œ ?´ë²¤??ë°œí–‰
        messagePublisher.publish(new UserRegisteredEvent(details.getUserId()));
    }
}
```

-----

### C. Notification Dispatcher (NotificationDispatcherConsumer)

| êµ¬ì„±?”ì†Œ | ?´ë˜??ê²½ë¡œ |
| :--- | :--- |
| **NotificationDispatcherConsumer** | `com.smartfitness.notify.internal.consumer.NotificationDispatcherConsumer` |

```java
package com.smartfitness.notify.internal.consumer;

import com.smartfitness.messaging.ports.IMessageSubscriptionService;
import com.smartfitness.notify.ports.IPushNotificationGateway;
import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.EquipmentFaultDetectedEvent;

/**
 * NotificationDispatcherConsumer: "faults" ? í”½??êµ¬ë…?˜ì—¬ ê´€ë¦¬ì PUSH ?Œë¦¼???„ì†¡?©ë‹ˆ??
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
