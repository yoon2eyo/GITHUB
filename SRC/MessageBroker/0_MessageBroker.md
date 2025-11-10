네, 알겠습니다. **AI Pipeline Layer (MLOps Tier)** 다음은 **Persistence Layer**입니다.

Persistence Layer는 \*\*Application Layers (Business Logic, Real-Time Access, AI Pipeline)\*\*의 요청을 받아 데이터를 저장하고 영속성을 책임지는 **수동적 인프라** 영역입니다. 이 Layer는 **Database per Service** 원칙을 준수하며, **Message Broker** 및 모든 DB 인스턴스를 포함합니다.

Persistence Layer에 속하는 컴포넌트들은 **Application Layer**에서 정의된 **Repository 인터페이스**를 구현하거나, **메시징 계약**을 이행하는 역할을 합니다.

-----

## 💻 Persistence Layer 코드 목록

Persistence Layer는 **실제 DB와 Message Broker 자체**이므로, 여기서는 **Application Layer**에서 정의된 포트를 구현하는 **DB 접근 구현체**와 **메시징 인프라**의 핵심 구조를 Java 인터페이스로 정의하여 제시합니다.

### A. MessageBroker (핵심 인프라 컴포넌트)

Message Broker는 비동기 통신을 담당하며, Application Layer의 `IMessagePublisherService`와 `IMessageSubscriptionService` 계약을 이행합니다.

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **MessageBrokerComponent** | `com.smartfitness.messaging.core.MessageBrokerComponent` |

```java
package com.smartfitness.messaging.core;

import com.smartfitness.event.DomainEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.messaging.IMessageSubscriptionService;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MessageBrokerComponent: 메시지 큐와 Pub/Sub 로직을 구현하는 핵심 인프라 컴포넌트입니다.
 * Tactic: Passive Redundancy (메시지 보존), Message Based (비동기 전송)
 * Role: Implements the message passing contract for the Application Layers.
 */
public class MessageBrokerComponent implements IMessagePublisherService, IMessageSubscriptionService {
    // 실제 구현에서는 이 맵 대신 Kafka, RabbitMQ 등의 영속적 스토리지 및 엔진이 사용됩니다.
    private final Map<String, List<Consumer<DomainEvent>>> topicSubscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * IMessagePublisherService 구현: 이벤트를 수신하여 구독자들에게 비동기적으로 전달합니다.
     */
    @Override
    public void publishEvent(String topic, DomainEvent event) {
        // 1. 메시지 큐에 이벤트 영속적으로 저장 (Passive Redundancy 지원)
        // 2. 구독자들에게 이벤트 전달
        List<Consumer<DomainEvent>> handlers = topicSubscribers.get(topic);
        if (handlers != null) {
            for (Consumer<DomainEvent> handler : handlers) {
                // 각 핸들러를 별도의 스레드에서 비동기 실행 (Introduce Concurrency)
                executor.submit(() -> handler.accept(event));
            }
        }
    }

    /**
     * IMessageSubscriptionService 구현: Consumer의 핸들러를 등록합니다.
     */
    @Override
    public void subscribeToTopic(String topic, Consumer<DomainEvent> eventHandler) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(eventHandler);
    }
    
    // 이외에 startListening, Dead Letter Queue (DLQ) 관리 등의 운영 로직을 가집니다.
}
```

-----

### B. Repository 구현체 (DAL Layer Implementation)

이 컴포넌트들은 **Database per Service** 원칙에 따라 Application Layer의 Repository 인터페이스 계약을 이행하며, 물리적 DB에 직접 연결합니다.

| 인터페이스 | 구현체 (가정) | 소유 서비스 |
| :--- | :--- | :--- |
| **IHelperRepository** | `HelperRepositoryImpl` | Helper Service |
| **IAuthRepository** | `AuthRepositoryImpl` | Auth Service |

```java
package com.smartfitness.persistence.dal.impl;

import com.smartfitness.app.repository.IHelperRepository;
import com.smartfitness.app.repository.IAuthRepository;
// ... (나머지 IMonitorRepository, ISearchRepository 등의 인터페이스 임포트) ...

// **주의**: 이 구현체들은 실제로는 Persistence Layer의 전용 DB 서버 내 모듈에 위치합니다.

/**
 * HelperRepositoryImpl: IHelperRepository 계약을 이행하며 Helper DB와 통신합니다.
 * Role: Implements the data access contract for the Helper Service.
 */
public class HelperRepositoryImpl implements IHelperRepository {
    // JDBC/JPA/Hibernate 등의 DB 연결 클라이언트 객체 주입
    
    @Override
    public void updateBalance(Long helperId, double amount) {
        // 실제 SQL 트랜잭션 실행: UPDATE helper_balances SET amount = amount + ? WHERE helper_id = ?
        System.out.println("Persistence: Executing updateBalance transaction on Helper DB for ID: " + helperId);
    }
    
    // ... (나머지 findTasksByHelperId, save 등의 메서드 구현)
}

/**
 * AuthRepositoryImpl: IAuthRepository 계약을 이행하며 Auth DB와 통신합니다.
 * Role: Implements the data access contract for the Auth Service.
 */
public class AuthRepositoryImpl implements IAuthRepository {
    
    @Override
    public String loadPasswordHash(String userId) {
        // 실제 SQL 쿼리 실행: SELECT password_hash FROM user_accounts WHERE user_id = ?
        System.out.println("Persistence: Loading encrypted hash from Auth DB for user: " + userId);
        return "encrypted_hash"; 
    }
    
    // ... (나머지 saveUser, findByUsername 등의 메서드 구현)
}
```
네, 알겠습니다. **Persistence Layer**의 나머지 컴포넌트들 중, **Application Layer**에서 정의된 포트들을 구현하는 **Repository 구현체**들과 **특수 DB에 대한 구현체** 코드를 이어서 출력하겠습니다.

-----

## 💻 Persistence Layer 코드 목록 (Continuation)

### C. Remaining Repository Implementations

이 컴포넌트들은 **Database per Service** 원칙에 따라 Application Layer의 Repository 인터페이스 계약을 이행하며, 물리적 DB에 직접 연결합니다.

| 인터페이스 | 구현체 (가정) | 소유 서비스 |
| :--- | :--- | :--- |
| **ISearchRepository** | `SearchRepositoryImpl` | Search Service |
| **IMonitorRepository** | `MonitorRepositoryImpl` | Monitoring Service |
| **IAccessVectorRepository**| `AccessVectorRepositoryImpl` | Access Service |
| **IModelDataRepository** | `ModelDataRepositoryImpl` | MLOps Service |

```java
package com.smartfitness.persistence.dal.impl;

import com.smartfitness.app.repository.ISearchRepository;
import com.smartfitness.app.repository.IMonitorRepository;
import com.smartfitness.app.repository.IAccessVectorRepository;
import com.smartfitness.mlo.internal.storage.IModelDataRepository;
import com.smartfitness.search.model.BranchRecommendation;
import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.common.model.FaceVector;
import java.util.List;
import java.util.Date;
import java.util.Optional;

// ----------------------------------------------------
// 1. SearchRepositoryImpl (ISearchRepository 구현)
// ----------------------------------------------------

/**
 * SearchRepositoryImpl: ISearchRepository 계약을 이행하며 Search DB 및 전문 검색 엔진과 통신합니다.
 * Role: Implements high-speed querying for the Search Service.
 */
public class SearchRepositoryImpl implements ISearchRepository {
    // DS-07 (전문 검색 엔진) 클라이언트 객체 주입 가정

    @Override
    public List<BranchRecommendation> executeMatchQuery(List<String> keywords) {
        // 실제 전문 검색 엔진(DS-07) 쿼리 실행 로직
        System.out.println("Persistence: Executing high-speed matching query on Search Engine.");
        return List.of(); 
    }

    // ... (나머지 saveReview, saveCustomerPreference 등의 메서드 구현)
}

// ----------------------------------------------------
// 2. MonitorRepositoryImpl (IMonitorRepository 구현)
// ----------------------------------------------------

/**
 * MonitorRepositoryImpl: IMonitorRepository 계약을 이행하며 Monitor DB와 통신합니다.
 * Role: Implements Heartbeat recording and log saving for the Monitoring Service.
 */
public class MonitorRepositoryImpl implements IMonitorRepository {
    
    @Override
    public void saveStatus(EquipmentStatusReport report) {
        // 실제 DB 트랜잭션 실행: INSERT INTO equipment_status ...
        System.out.println("Persistence: Saving Heartbeat status to Monitor DB for ID: " + report.getEquipmentId());
    }

    @Override
    public Date findLastReportTime(String equipmentId) {
        // 실제 DB 쿼리 실행: SELECT last_report_time FROM equipment_status ...
        return new Date(); 
    }
    
    // ... (나머지 saveNotificationLog 등의 메서드 구현)
}

// ----------------------------------------------------
// 3. AccessVectorRepositoryImpl (IAccessVectorRepository 구현)
// ----------------------------------------------------

/**
 * AccessVectorRepositoryImpl: IAccessVectorRepository 계약을 이행하며 안면 벡터 DB(DS-02)와 통신합니다.
 * Role: Implements ultra-low latency query for the Access Service.
 */
public class AccessVectorRepositoryImpl implements IAccessVectorRepository {

    @Override
    public Optional<FaceVector> findVectorById(String faceId) {
        // 실제 Vector DB 또는 초고속 Key-Value Store 쿼리 실행 로직
        System.out.println("Persistence: Ultra-low latency vector lookup on DB_VECTOR.");
        return Optional.empty();
    }
    
    // ... (나머지 saveVector 등의 메서드 구현)
}

// ----------------------------------------------------
// 4. ModelDataRepositoryImpl (IModelDataRepository 구현)
// ----------------------------------------------------

/**
 * ModelDataRepositoryImpl: IModelDataRepository 계약을 이행하며 MLOps Data DB(DS-05)와 통신합니다.
 * Role: Implements storage for raw training data and model binaries.
 */
public class ModelDataRepositoryImpl implements IModelDataRepository {

    @Override
    public void saveRawTrainingData(String dataType, byte[] data) {
        // 실제 DB 저장 로직: 대용량의 raw training data를 DS-05에 저장
        System.out.println("Persistence: Saving raw training data to MLOps DB.");
    }
    
    // ... (나머지 loadAllTrainingData, saveModelBinary 등의 메서드 구현)
}
```
