네, 알겠습니다. **세탁물 판독 책임 소재 변경**에 따라 `ILLMPanDokuClient`를 \*\*`IPanDokuModelClient`\*\*로 수정한 내용과, 외부 시스템 및 인프라와의 모든 계약을 정의하는 **External Services 및 Interface Definitions**의 코드를 하나의 블록으로 묶어 다시 출력하겠습니다.

이 코드는 **하이브리드 MSA 구조**의 **느슨한 결합**을 위한 최종적인 계약서 역할을 수행합니다.

-----

## 💻 External Services 및 Interface Definitions (최종 코드)

```java
package com.smartfitness.auth.ports;

/**
 * ICreditCardVerificationService: 외부 신용카드 검증 시스템과의 통신 계약입니다. (어댑터 패턴)
 */
public interface ICreditCardVerificationService {
    boolean verifyIdentity(String cardDetails, String userId);
}


// ----------------------------------------------------


package com.smartfitness.search.ports;

import java.util.List;
import com.smartfitness.search.model.SearchQuery;

/**
 * ILLMAnalysisService: 상용 LLM 서비스와의 통신 계약입니다. (자연어 키워드 추출용으로만 사용)
 */
public interface ILLMAnalysisService {
    List<String> analyzeTextForPreferences(String text);
}


// ----------------------------------------------------


package com.smartfitness.ai.ports; 

/**
 * IPanDokuModelService: MLOps Tier의 LAUNDRY MODEL Service가 제공하는 
 * 1차 판독 연산에 대한 계약입니다.
 * (세탁물 판독 책임이 MLOps Tier 내부 모델에 있음을 명확히 함)
 */
public interface IPanDokuModelService {
    String requestPanDoku(String imageUrl);
}


// ----------------------------------------------------


package com.smartfitness.notify.ports;

/**
 * IPushNotificationGateway: NotificationDispatcher가 외부 푸시 알림 서비스에 
 * 의존하는 계약입니다.
 */
public interface IPushNotificationGateway {
    void sendPushNotification(String recipientId, String title, String body);
}


// ----------------------------------------------------


package com.smartfitness.mlo.ports;

/**
 * IFaceModelClient: DeploymentService가 Real-Time Tier의 FACE MODEL Service에
 * Hot Swap 배포 및 롤백을 요청하는 클라이언트 계약입니다.
 * Tactic: Hot Swap, Rollback
 */
public interface IFaceModelClient {
    void callHotSwapApi(String modelId, String newVersion, byte[] modelBinary); 
    void callRollbackApi(String modelId, String previousVersion); 
}


// ----------------------------------------------------


package com.smartfitness.system.discovery;

import com.smartfitness.system.exception.ServiceUnavailableException;
import java.util.List;

/**
 * ServiceDiscovery: 내부 마이크로서비스의 위치(URL)를 찾고 로드 밸런싱을 수행하는 계약입니다.
 * Tactic: Active Redundancy, Broker (내부 통신 중개)
 */
public interface ServiceDiscovery {
    String resolveAndBalance(String servicePath) throws ServiceUnavailableException;
    List<String> getServiceInstances(String servicePath);
}


// ----------------------------------------------------


package com.smartfitness.messaging.ports;

import com.smartfitness.event.DomainEvent;
import java.util.function.Consumer;

/**
 * IMessagePublisherService: Message Broker에 이벤트를 발행하는 포트입니다.
 */
public interface IMessagePublisherService {
    void publish(DomainEvent event);
}

/**
 * IMessageSubscriptionService: Consumer Service가 Message Broker로부터 이벤트를 구독하기 위한 포트입니다.
 */
public interface IMessageSubscriptionService {
    void subscribeToTopic(String topic, Consumer<DomainEvent> eventHandler);
    void startListening();
}
```