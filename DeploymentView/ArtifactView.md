## 4.3.6.1. N-01: Load Balancer Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N01_Load_Balancer

title N-01: Load Balancer Node Artifacts

node "N-01: Load Balancer Node" {
  artifact "nginx-ingress-controller.yaml" as A01_Config {
    component NginxIngressController
    component LoadBalancerConfiguration
    component HealthCheckConfiguration
  }
  
  artifact "ingress-config.yaml" as A01_Ingress {
    component RoutingRules
    component SSLTermination
    component RateLimitRules
  }
  
  artifact "tls-certificates.pem" as A01_Cert {
    component TLSCertificate
    component PrivateKey
    component CertificateChain
  }
}

note right of A01_Config
  **QAS-05**: L7 Load Balancing
  **QAS-04**: TLS 1.3
end note

@enduml
```

## nginx-ingress-controller.yaml

**Artifact Type**: Configuration File  
**Description**: Nginx Ingress Controller 설정 파일로 L7 로드 밸런싱을 수행한다.

**<<manifest>> Components**:

- NginxIngressController
    
- LoadBalancerConfiguration
    
- HealthCheckConfiguration
    

**Dependencies**: None

**관련 QAS**: QAS-05 (L7 Load Balancing), QAS-04 (TLS 1.3)

---

## ingress-config.yaml

**Artifact Type**: Configuration File  
**Description**: Ingress 라우팅 규칙, SSL 종료, Rate Limiting 설정을 포함한다.

**<<manifest>> Components**:

- RoutingRules
    
- SSLTermination
    
- RateLimitRules
    

**Dependencies**: None

---

## tls-certificates.pem

**Artifact Type**: Certificate File  
**Description**: TLS 1.3 인증서 파일로 HTTPS 통신을 보장한다.

**<<manifest>> Components**:

- TLSCertificate
    
- PrivateKey
    
- CertificateChain
    

**Dependencies**: None

**관련 QAS**: QAS-04 (TLS 1.3)

---

## 4.3.6.2. N-02: API Gateway Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N04_API_Gateway

title N-04: API Gateway Node Artifacts

node "N-04: API Gateway Node" {
  artifact "A-01: APIGateway.jar" as A01 {
    component GatewayRouterController
    component AuthenticationFilter
    component AuthorizationFilter
    component JWTTokenValidator
    component RateLimitingFilter
    component CircuitBreakerFilter
    component RequestLoggingInterceptor
  }
  
  artifact "A-02: gateway-config.yml" as A02 {
    component RoutingConfiguration
    component RateLimitConfiguration
    component CircuitBreakerConfiguration
    component SecurityConfiguration
  }
  
  package "Dependencies" {
    [spring-cloud-gateway-core.jar]
    [spring-security-jwt.jar]
    [redis-client.jar]
  }
}

A01 --> [spring-cloud-gateway-core.jar] : <<uses>>
A01 --> [spring-security-jwt.jar] : <<uses>>
A01 --> [redis-client.jar] : <<uses>>
A01 --> A02 : <<configures>>

note right of A01
  **QAS-04**: 개인 정보 보안
  **QAS-05**: 자동 복구
end note

@enduml
```

## A-01: APIGateway.jar

**Artifact Type**: Java Executable JAR  
**Description**: Spring Cloud Gateway 기반 API Gateway로 모든 외부 요청의 진입점 역할을 수행한다.

**<<manifest>> Components**:

- GatewayRouterController
    
- AuthenticationFilter
    
- AuthorizationFilter
    
- JWTTokenValidator
    
- RateLimitingFilter
    
- CircuitBreakerFilter
    
- RequestLoggingInterceptor
    

**Dependencies**:

- spring-cloud-gateway-core.jar
    
- spring-security-jwt.jar
    
- redis-client.jar (세션 관리)
    

**관련 QAS**: QAS-04 (개인 정보 보안), QAS-05 (자동 복구)

---

## A-02: gateway-config.yml

**Artifact Type**: Configuration File  
**Description**: API Gateway의 라우팅 규칙, Rate Limiting, Circuit Breaker 설정을 포함한다.

**<<manifest>> Components**:

- RoutingConfiguration
    
- RateLimitConfiguration
    
- CircuitBreakerConfiguration
    
- SecurityConfiguration
    

**Dependencies**: None

---

## 4.3.6.3. N-03: Real-Time Access Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N03_Real_Time_Access

title N-03: Real-Time Access Node Artifacts

node "N-03: Real-Time Access Node" {
  artifact "A-03: AccessService.jar" as A03 {
    component AccessController
    component FaceRecognitionService
    component CacheManager
    component VectorDatabaseClient
    component GateControlService
    component AccessLogRepository
    component HeartbeatHandler
  }
  
  artifact "A-04: access-config.yml" as A04 {
    component DataSourceConfiguration
    component CacheConfiguration
    component KafkaProducerConfiguration
    component FaceModelClientConfiguration
  }
  
  artifact "facemodel-service.jar" as A03_Face {
    component FaceModelIPCHandler
    component VectorComparisonEngine
    component ModelLifecycleManager
    component FeatureExtractor
  }
  
  artifact "application-facemodel.yml" as A03_FaceConfig {
    component FaceModelConfiguration
    component ModelPathConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [jedis.jar]
    [postgresql-jdbc.jar]
    [kafka-client.jar]
    [grpc-java-1.58.0.jar]
    [lettuce-6.2.jar]
  }
}

A03 --> [spring-boot-starter-web.jar] : <<uses>>
A03 --> [jedis.jar] : <<uses>>
A03 --> [postgresql-jdbc.jar] : <<uses>>
A03 --> [kafka-client.jar] : <<uses>>
A03 --> [grpc-java-1.58.0.jar] : <<uses>>
A03 --> [lettuce-6.2.jar] : <<uses>>
A03 --> A03_Face : <<IPC call>>
A03 --> A04 : <<configures>>
A03_Face --> [grpc-java-1.58.0.jar] : <<uses>>
A03_Face --> A03_FaceConfig : <<configures>>

note right of A03
  **QAS-02**: 안면인식 출입 인증
  **QAS-05**: 자동 복구
end note

note right of A03_Face
  **QAS-02**: IPC/gRPC < 10ms
  Co-located with Access Service
end note

@enduml
```

## A-03: AccessService.jar

**Artifact Type**: Java Executable JAR  
**Description**: 안면 인식 기반 출입 인증을 처리하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- AccessController
    
- FaceRecognitionService
    
- CacheManager
    
- VectorDatabaseClient
    
- GateControlService
    
- AccessLogRepository
    
- HeartbeatHandler
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- jedis.jar (Redis Client)
    
- postgresql-jdbc.jar
    
- kafka-client.jar
    
- face-model-client.jar
    

**관련 QAS**: QAS-02 (안면인식 출입 인증), QAS-05 (자동 복구)

---

## A-04: access-config.yml

**Artifact Type**: Configuration File  
**Description**: Access Service의 Redis, PostgreSQL, Kafka 연결 설정을 포함한다.

**<<manifest>> Components**:

- DataSourceConfiguration
    
- CacheConfiguration
    
- KafkaProducerConfiguration
    
- FaceModelClientConfiguration
    

**Dependencies**: None

---

## facemodel-service.jar

**Artifact Type**: Java Executable JAR  
**Description**: 안면 인식 모델 추론을 수행하는 서비스로 Access Service와 동일 Pod에 Co-located되어 IPC/gRPC로 통신한다.

**<<manifest>> Components**:

- FaceModelIPCHandler
    
- VectorComparisonEngine
    
- ModelLifecycleManager
    
- FeatureExtractor
    

**Dependencies**:

- grpc-java-1.58.0.jar (IPC/gRPC 통신)
    

**관련 QAS**: QAS-02 (IPC/gRPC < 10ms)

---

## application-facemodel.yml

**Artifact Type**: Configuration File  
**Description**: FaceModel Service의 모델 경로 및 설정을 포함한다.

**<<manifest>> Components**:

- FaceModelConfiguration
    
- ModelPathConfiguration
    

**Dependencies**: None

---

## 4.3.6.4. N-04: Auth Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N04_Auth_Service

title N-04: Auth Service Node Artifacts

node "N-04: Auth Service Node" {
  artifact "auth-service.jar" as A04_Auth {
    component AuthServiceController
    component UserManagementController
    component AuthenticationManager
    component AuthorizationManager
    component UserRegistrationManager
    component AuthEventConsumer
  }
  
  artifact "application-auth.yml" as A04_Config {
    component DataSourceConfiguration
    component SecurityConfiguration
    component JWTConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [spring-security-6.1.jar]
    [jjwt-0.12.3.jar]
    [postgresql-jdbc.jar]
  }
}

A04_Auth --> [spring-boot-starter-web.jar] : <<uses>>
A04_Auth --> [spring-security-6.1.jar] : <<uses>>
A04_Auth --> [jjwt-0.12.3.jar] : <<uses>>
A04_Auth --> [postgresql-jdbc.jar] : <<uses>>
A04_Auth --> A04_Config : <<configures>>

note right of A04_Auth
  **QAS-04**: JWT 기반 인증
  **QAS-05**: 자동 복구
end note

@enduml
```

## auth-service.jar

**Artifact Type**: Java Executable JAR  
**Description**: 사용자 인증/인가, 회원가입, JWT 발급을 수행하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- AuthServiceController
    
- UserManagementController
    
- AuthenticationManager
    
- AuthorizationManager
    
- UserRegistrationManager
    
- AuthEventConsumer
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- spring-security-6.1.jar
    
- jjwt-0.12.3.jar
    
- postgresql-jdbc.jar
    

**관련 QAS**: QAS-04 (JWT 기반 인증), QAS-05 (자동 복구)

---

## application-auth.yml

**Artifact Type**: Configuration File  
**Description**: Auth Service의 데이터베이스, 보안, JWT 설정을 포함한다.

**<<manifest>> Components**:

- DataSourceConfiguration
    
- SecurityConfiguration
    
- JWTConfiguration
    

**Dependencies**: None

---

## 4.3.6.5. N-05: Helper Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N05_Helper_Service

title N-05: Helper Service Node Artifacts

node "N-05: Helper Service Node" {
  artifact "helper-service.jar" as A05_Helper {
    component TaskController
    component RewardController
    component TaskSubmissionManager
    component DailyLimitValidator
    component AITaskAnalysisConsumer
    component TaskAnalysisEngine
    component RewardConfirmationManager
  }
  
  artifact "application-helper.yml" as A05_Config {
    component DataSourceConfiguration
    component S3Configuration
    component RabbitMQConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [aws-s3-sdk-2.20.jar]
    [postgresql-jdbc.jar]
  }
}

A05_Helper --> [spring-boot-starter-web.jar] : <<uses>>
A05_Helper --> [aws-s3-sdk-2.20.jar] : <<uses>>
A05_Helper --> [postgresql-jdbc.jar] : <<uses>>
A05_Helper --> A05_Config : <<configures>>

note right of A05_Helper
  **QAS-06**: 이벤트 기반 재학습 트리거
  **QAS-05**: 자동 복구
end note

@enduml
```

## helper-service.jar

**Artifact Type**: Java Executable JAR  
**Description**: 헬퍼 작업 등록, AI 판독 요청, 보상 관리를 수행하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- TaskController
    
- RewardController
    
- TaskSubmissionManager
    
- DailyLimitValidator
    
- AITaskAnalysisConsumer
    
- TaskAnalysisEngine
    
- RewardConfirmationManager
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- aws-s3-sdk-2.20.jar (S3 업로드)
    
- postgresql-jdbc.jar
    

**관련 QAS**: QAS-06 (이벤트 기반 재학습 트리거), QAS-05 (자동 복구)

---

## application-helper.yml

**Artifact Type**: Configuration File  
**Description**: Helper Service의 데이터베이스, S3, RabbitMQ 연결 설정을 포함한다.

**<<manifest>> Components**:

- DataSourceConfiguration
    
- S3Configuration
    
- RabbitMQConfiguration
    

**Dependencies**: None

---

## 4.3.6.6. N-06: Search Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N06_Search_Service

title N-06: Search Service Node Artifacts

node "N-06: Search Service Node" {
  artifact "A-05: SearchService.jar" as A05 {
    component SearchController
    component ElasticSearchClient
    component NoriTokenizer
    component HotPathSearchHandler
    component ColdPathEventPublisher
    component LLMServiceClient
    component SearchResultCache
  }
  
  artifact "A-06: search-config.yml" as A06 {
    component ElasticSearchConfiguration
    component KafkaProducerConfiguration
    component LLMAPIConfiguration
    component NoriAnalyzerConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [elasticsearch-rest-client.jar]
    [kafka-client.jar]
    [openai-java-client.jar]
  }
}

A05 --> [spring-boot-starter-web.jar] : <<uses>>
A05 --> [elasticsearch-rest-client.jar] : <<uses>>
A05 --> [kafka-client.jar] : <<uses>>
A05 --> [openai-java-client.jar] : <<uses>>
A05 --> A06 : <<configures>>

note right of A05
  **QAS-03**: 자연어 검색 실시간성
  **QAS-05**: 자동 복구
end note

@enduml
```

## A-05: SearchService.jar

**Artifact Type**: Java Executable JAR  
**Description**: 자연어 검색을 처리하는 Spring Boot 애플리케이션이다. Hot Path 및 Cold Path 처리를 수행한다.

**<<manifest>> Components**:

- SearchController
    
- ElasticSearchClient
    
- NoriTokenizer
    
- HotPathSearchHandler
    
- ColdPathEventPublisher
    
- LLMServiceClient
    
- SearchResultCache
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- elasticsearch-rest-client.jar
    
- kafka-client.jar
    
- openai-java-client.jar
    

**관련 QAS**: QAS-03 (자연어 검색 실시간성), QAS-05 (자동 복구)

---

## A-06: search-config.yml

**Artifact Type**: Configuration File  
**Description**: Search Service의 ElasticSearch, Kafka, LLM API 연결 설정을 포함한다.

**<<manifest>> Components**:

- ElasticSearchConfiguration
    
- KafkaProducerConfiguration
    
- LLMAPIConfiguration
    
- NoriAnalyzerConfiguration
    

**Dependencies**: None

---

## 4.3.6.7. N-07: BranchOwner Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N07_BranchOwner_Service

title N-07: BranchOwner Service Node Artifacts

node "N-07: BranchOwner Service Node" {
  artifact "branchowner-service.jar" as A07_Branch {
    component BranchOwnerController
    component BranchQueryController
    component BranchOwnerManager
    component BranchInfoValidator
    component BranchEventProcessor
  }
  
  artifact "application-branchowner.yml" as A07_Config {
    component DataSourceConfiguration
    component RabbitMQConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [postgresql-jdbc.jar]
  }
}

A07_Branch --> [spring-boot-starter-web.jar] : <<uses>>
A07_Branch --> [postgresql-jdbc.jar] : <<uses>>
A07_Branch --> A07_Config : <<configures>>

note right of A07_Branch
  **QAS-06**: TaskConfirmedEvent 발행
  **QAS-05**: 자동 복구
end note

@enduml
```

## branchowner-service.jar

**Artifact Type**: Java Executable JAR  
**Description**: 지점 정보 관리, 작업 검수/컨펌을 수행하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- BranchOwnerController
    
- BranchQueryController
    
- BranchOwnerManager
    
- BranchInfoValidator
    
- BranchEventProcessor
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- postgresql-jdbc.jar
    

**관련 QAS**: QAS-06 (TaskConfirmedEvent 발행), QAS-05 (자동 복구)

---

## application-branchowner.yml

**Artifact Type**: Configuration File  
**Description**: BranchOwner Service의 데이터베이스, RabbitMQ 연결 설정을 포함한다.

**<<manifest>> Components**:

- DataSourceConfiguration
    
- RabbitMQConfiguration
    

**Dependencies**: None

---

## 4.3.6.8. N-08: Monitoring Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N08_Monitoring_Service

title N-08: Monitoring Service Node Artifacts

node "N-08: Monitoring Service Node" {
  artifact "A-10: MonitoringService.jar" as A10 {
    component MonitoringController
    component HeartbeatChecker
    component PingEchoExecutor
    component FaultDetector
    component AlertPublisher
    component ScheduledMonitoringTask
    component DeviceStatusRepository
  }
  
  artifact "A-11: monitoring-config.yml" as A11 {
    component ScheduledTaskConfiguration
    component RedisConfiguration
    component KafkaProducerConfiguration
    component PingEchoConfiguration
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [jedis.jar]
    [kafka-client.jar]
    [spring-boot-starter-scheduling.jar]
  }
}

A10 --> [spring-boot-starter-web.jar] : <<uses>>
A10 --> [jedis.jar] : <<uses>>
A10 --> [kafka-client.jar] : <<uses>>
A10 --> [spring-boot-starter-scheduling.jar] : <<uses>>
A10 --> A11 : <<configures>>

note right of A10
  **QAS-01**: 설비 고장 감지 및 알림
  **QAS-05**: 자동 복구
end note

@enduml
```

## A-10: MonitoringService.jar

**Artifact Type**: Java Executable JAR  
**Description**: 설비 상태 모니터링 및 고장 감지를 수행하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- MonitoringController
    
- HeartbeatChecker
    
- PingEchoExecutor
    
- FaultDetector
    
- AlertPublisher
    
- ScheduledMonitoringTask
    
- DeviceStatusRepository
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- jedis.jar (Redis Client)
    
- kafka-client.jar
    
- spring-boot-starter-scheduling.jar
    

**관련 QAS**: QAS-01 (설비 고장 감지 및 알림), QAS-05 (자동 복구)

---

## A-11: monitoring-config.yml

**Artifact Type**: Configuration File  
**Description**: Monitoring Service의 Scheduled Task, Redis, Kafka 연결 설정을 포함한다.

**<<manifest>> Components**:

- ScheduledTaskConfiguration
    
- RedisConfiguration
    
- KafkaProducerConfiguration
    
- PingEchoConfiguration
    

**Dependencies**: None

---

## 4.3.6.9. N-09: Notification Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N09_Notification_Service

title N-09: Notification Service Node Artifacts

node "N-09: Notification Service Node" {
  artifact "notification-service.jar" as A09_Notif {
    component NotificationController
    component NotificationDispatcherManager
    component NotificationDispatcherConsumer
  }
  
  artifact "application-notification.yml" as A09_Config {
    component FCMConfiguration
    component RabbitMQConfiguration
  }
  
  artifact "fcm-service-account.json" as A09_Cred {
    component FCMServiceAccount
    component PrivateKey
  }
  
  package "Dependencies" {
    [spring-boot-starter-web.jar]
    [firebase-admin-sdk-9.2.jar]
  }
}

A09_Notif --> [spring-boot-starter-web.jar] : <<uses>>
A09_Notif --> [firebase-admin-sdk-9.2.jar] : <<uses>>
A09_Notif --> A09_Config : <<configures>>
A09_Notif --> A09_Cred : <<uses>>

note right of A09_Notif
  **QAS-01**: FCM Push < 15초
  **QAS-05**: 자동 복구
end note

@enduml
```

## notification-service.jar

**Artifact Type**: Java Executable JAR  
**Description**: 이벤트 기반 푸시 알림 발송(FCM)을 수행하는 Spring Boot 애플리케이션이다.

**<<manifest>> Components**:

- NotificationController
    
- NotificationDispatcherManager
    
- NotificationDispatcherConsumer
    

**Dependencies**:

- spring-boot-starter-web.jar
    
- firebase-admin-sdk-9.2.jar (FCM Push)
    

**관련 QAS**: QAS-01 (FCM Push < 15초), QAS-05 (자동 복구)

---

## application-notification.yml

**Artifact Type**: Configuration File  
**Description**: Notification Service의 FCM, RabbitMQ 연결 설정을 포함한다.

**<<manifest>> Components**:

- FCMConfiguration
    
- RabbitMQConfiguration
    

**Dependencies**: None

---

## fcm-service-account.json

**Artifact Type**: Credential File  
**Description**: Firebase Cloud Messaging 인증을 위한 서비스 계정 키 파일이다.

**<<manifest>> Components**:

- FCMServiceAccount
    
- PrivateKey
    

**Dependencies**: None

**관련 QAS**: QAS-04 (보안 인증)

---

## 4.3.6.10. N-10: MLOps Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N10_MLOps_Service

title N-10: MLOps Service Node Artifacts

node "N-10: MLOps Service Node" {
  artifact "A-07: MLOpsService.whl" as A07 {
    component TrainingManager
    component ModelValidator
    component ModelDeployer
    component DataCollector
    component KafkaConsumer
    component S3ModelUploader
    component ModelSwapClient
  }
  
  artifact "A-08: training_scripts/" as A08 {
    component data_preprocessor.py
    component model_trainer.py
    component model_evaluator.py
    component hyperparameter_tuner.py
  }
  
  artifact "A-09: mlops-config.yml" as A09 {
    component KafkaConsumerConfiguration
    component S3BucketConfiguration
    component TrainingConfiguration
    component ModelValidationConfiguration
  }
  
  artifact "model-weights-v1.0.pb" as A10_Model {
    component ModelWeights
    component ModelMetadata
  }
  
  package "Dependencies" {
    [tensorflow-2.13.jar]
    [pytorch-2.1.jar]
    [kafka-python.whl]
    [boto3.whl]
    [numpy.whl]
    [pandas.whl]
    [scikit-learn]
  }
}

A07 --> [tensorflow-2.13.jar] : <<uses>>
A07 --> [pytorch-2.1.jar] : <<uses>>
A07 --> [kafka-python.whl] : <<uses>>
A07 --> [boto3.whl] : <<uses>>
A07 --> [numpy.whl] : <<uses>>
A07 --> [pandas.whl] : <<uses>>
A07 --> A08 : <<uses>>
A07 --> A09 : <<configures>>
A07 --> A10_Model : <<uses>>
A08 --> [tensorflow] : <<uses>>
A08 --> [scikit-learn] : <<uses>>

note right of A07
  **QAS-06**: AI 모델 재학습 및 배포
  Hot Swap 방식
end note

@enduml
```

## A-07: MLOpsService.whl

**Artifact Type**: Python Wheel Package  
**Description**: AI 모델 재학습 및 배포를 수행하는 Python 애플리케이션이다.

**<<manifest>> Components**:

- TrainingManager
    
- ModelValidator
    
- ModelDeployer
    
- DataCollector
    
- KafkaConsumer
    
- S3ModelUploader
    
- ModelSwapClient
    

**Dependencies**:

- tensorflow-2.13.jar
    
- pytorch-2.1.jar
    
- kafka-python.whl
    
- boto3.whl (AWS SDK)
    
- numpy.whl
    
- pandas.whl
    

**관련 QAS**: QAS-06 (AI 모델 재학습 및 배포)

---

## A-08: training_scripts/

**Artifact Type**: Python Scripts Directory  
**Description**: AI 모델 학습을 위한 Python 스크립트 모음이다.

**<<manifest>> Components**:

- data_preprocessor.py
    
- model_trainer.py
    
- model_evaluator.py
    
- hyperparameter_tuner.py
    

**Dependencies**:

- tensorflow
    
- scikit-learn
    

---

## A-09: mlops-config.yml

**Artifact Type**: Configuration File  
**Description**: MLOps Service의 Kafka, S3, 학습 파라미터 설정을 포함한다.

**<<manifest>> Components**:

- KafkaConsumerConfiguration
    
- S3BucketConfiguration
    
- TrainingConfiguration
    
- ModelValidationConfiguration
    

**Dependencies**: None

---

## model-weights-v1.0.pb

**Artifact Type**: Model File  
**Description**: TensorFlow 모델 가중치 파일로 Hot Swap 방식으로 배포된다.

**<<manifest>> Components**:

- ModelWeights
    
- ModelMetadata
    

**Dependencies**: None

**관련 QAS**: QAS-06 (Hot Swap 모델 배포)

---

## 4.3.6.11. N-11: RDS Cluster Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N11_RDS_Cluster

title N-11: RDS Cluster Artifacts

node "N-11: RDS Cluster Node" {
  artifact "A-15: database-schema.sql" as A15 {
    component user_table
    component branch_table
    component reservation_table
    component payment_table
    component access_log_table
    component device_status_table
  }
  
  artifact "A-16: initial-data.sql" as A16 {
    component branch_initial_data
    component admin_user_data
  }
  
  artifact "ivfflat-index.sql" as A15_Index {
    component IVFFlatIndex
    component VectorIndexConfiguration
  }
}

A16 --> A15 : <<depends on>>
A15_Index --> A15 : <<depends on>>

note right of A15
  **QAS-04**: 데이터 암호화
  **QAS-05**: Multi-AZ 고가용성
end note

note right of A15_Index
  **QAS-02**: Vector similarity 10x faster
end note

@enduml
```

## A-15: database-schema.sql

**Artifact Type**: SQL DDL Script  
**Description**: 회원 정보, 지점 정보, 예약 정보, 결제 정보 테이블의 DDL 스크립트이다.

**<<manifest>> Components**:

- user_table (DDL)
    
- branch_table (DDL)
    
- reservation_table (DDL)
    
- payment_table (DDL)
    
- access_log_table (DDL)
    
- device_status_table (DDL)
    

**Dependencies**: None

**관련 QAS**: QAS-04 (데이터 암호화), QAS-05 (Multi-AZ 고가용성)

---

## A-16: initial-data.sql

**Artifact Type**: SQL DML Script  
**Description**: 초기 데이터 로드를 위한 DML 스크립트이다.

**<<manifest>> Components**:

- branch_initial_data (INSERT)
    
- admin_user_data (INSERT)
    

**Dependencies**: A-15 (database-schema.sql)

---

## ivfflat-index.sql

**Artifact Type**: SQL DDL Script  
**Description**: PostgreSQL pgvector 확장을 사용한 IVFFlat 인덱스 생성 스크립트로 벡터 유사도 검색 성능을 10배 향상시킨다.

**<<manifest>> Components**:

- IVFFlatIndex
    
- VectorIndexConfiguration
    

**Dependencies**: A-15 (database-schema.sql)

**관련 QAS**: QAS-02 (Vector similarity 10x faster)

---

## 4.3.6.12. N-12: ElasticSearch Cluster Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N12_ElasticSearch

title N-12: ElasticSearch Cluster Artifacts

node "N-12: ElasticSearch Cluster Node" {
  artifact "A-18: branch-index-mapping.json" as A18 {
    component branch_id
    component branch_name
    component address
    component facilities
    component keywords
    component location
  }
  
  artifact "A-19: review-index-mapping.json" as A19 {
    component review_id
    component branch_id
    component rating
    component review_text
    component keywords
    component created_at
  }
  
  artifact "A-20: nori-analyzer-plugin.zip" as A20 {
    component NoriTokenizer
    component NoriPartOfSpeechFilter
    component NoriReadingFormFilter
    component KoreanStopFilter
  }
  
  artifact "A-21: elasticsearch.yml" as A21 {
    component ClusterConfiguration
    component NetworkConfiguration
    component DiscoveryConfiguration
    component SecurityConfiguration
  }
  
  artifact "elasticsearch-8.10.tar.gz" as A21_Binary {
    component ElasticSearchNode
    component SearchEngine
  }
}

A18 --> A20 : <<depends on>>
A19 --> A20 : <<depends on>>
A21_Binary --> A21 : <<configures>>

note right of A18
  **QAS-03**: Hot Path 검색
end note

note right of A21_Binary
  **QAS-03**: Full-text search ~50ms
end note

@enduml
```

## A-18: branch-index-mapping.json

**Artifact Type**: ElasticSearch Index Mapping  
**Description**: 지점 정보 인덱스의 필드 매핑 정의이다.

**<<manifest>> Components**:

- branch_id (keyword)
    
- branch_name (text with nori analyzer)
    
- address (text)
    
- facilities (text array)
    
- keywords (text array)
    
- location (geo_point)
    

**Dependencies**: A-20 (nori-analyzer-plugin)

**관련 QAS**: QAS-03 (Hot Path 검색)

---

## A-19: review-index-mapping.json

**Artifact Type**: ElasticSearch Index Mapping  
**Description**: 리뷰 인덱스의 필드 매핑 정의이다.

**<<manifest>> Components**:

- review_id (keyword)
    
- branch_id (keyword)
    
- rating (integer)
    
- review_text (text with nori analyzer)
    
- keywords (text array)
    
- created_at (date)
    

**Dependencies**: A-20 (nori-analyzer-plugin)

---

## A-20: nori-analyzer-plugin.zip

**Artifact Type**: ElasticSearch Plugin  
**Description**: 한국어 형태소 분석기 플러그인이다.

**<<manifest>> Components**:

- NoriTokenizer
    
- NoriPartOfSpeechFilter
    
- NoriReadingFormFilter
    
- KoreanStopFilter
    

**Dependencies**: None

---

## A-21: elasticsearch.yml

**Artifact Type**: Configuration File  
**Description**: ElasticSearch 클러스터 설정 파일이다.

**<<manifest>> Components**:

- ClusterConfiguration (3 data nodes + 3 master nodes)
    
- NetworkConfiguration
    
- DiscoveryConfiguration
    
- SecurityConfiguration
    

**Dependencies**: None

---

## elasticsearch-8.10.tar.gz

**Artifact Type**: Binary Archive  
**Description**: ElasticSearch 8.10+ 바이너리 파일로 전문 검색 엔진을 제공한다.

**<<manifest>> Components**:

- ElasticSearchNode
    
- SearchEngine
    

**Dependencies**: A-21 (elasticsearch.yml)

**관련 QAS**: QAS-03 (Full-text search ~50ms)

---

## 4.3.6.13. N-13: RabbitMQ Cluster Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N13_RabbitMQ_Cluster

title N-13: RabbitMQ Cluster Artifacts

node "N-13: RabbitMQ Cluster Node" {
  artifact "rabbitmq-server-3.12.8.tar.gz" as A13_Binary {
    component RabbitMQBroker
    component QuorumQueueManager
    component ExchangeManager
    component MessagePersistenceManager
  }
  
  artifact "rabbitmq.conf" as A13_Config {
    component BrokerConfiguration
    component QuorumQueueConfiguration
    component RaftConsensusConfiguration
  }
  
  artifact "queue-definitions.json" as A13_Queue {
    component QueueSchema
    component QueueRouting
  }
  
  artifact "exchange-definitions.json" as A13_Exchange {
    component ExchangeSchema
    component EventRouting
  }
  
  artifact "erlang-26.tar.gz" as A13_Erlang {
    component ErlangVM
  }
}

A13_Binary --> A13_Config : <<configures>>
A13_Binary --> A13_Queue : <<uses>>
A13_Binary --> A13_Exchange : <<uses>>
A13_Binary --> A13_Erlang : <<uses>>

note right of A13_Binary
  **DD-02**: Message-Based Communication
  **QAS-05**: Quorum Queue, Message persistence
end note

@enduml
```

## rabbitmq-server-3.12.8.tar.gz

**Artifact Type**: Binary Archive  
**Description**: RabbitMQ Broker 바이너리로 메시지 브로커, Quorum Queue, Raft 합의를 제공한다.

**<<manifest>> Components**:

- RabbitMQBroker
    
- QuorumQueueManager
    
- ExchangeManager
    
- MessagePersistenceManager
    

**Dependencies**: erlang-26.tar.gz

**관련 QAS**: DD-02 (Message-Based Communication), QAS-05 (Quorum Queue, Message persistence)

---

## rabbitmq.conf

**Artifact Type**: Configuration File  
**Description**: RabbitMQ Broker 설정 파일로 Quorum Queue, Raft 합의 설정을 포함한다.

**<<manifest>> Components**:

- BrokerConfiguration
    
- QuorumQueueConfiguration
    
- RaftConsensusConfiguration
    

**Dependencies**: None

---

## queue-definitions.json

**Artifact Type**: Queue Definition File  
**Description**: RabbitMQ 큐 스키마 정의 파일로 이벤트 라우팅을 설정한다.

**<<manifest>> Components**:

- QueueSchema
    
- QueueRouting
    

**Dependencies**: None

**관련 QAS**: QAS-06 (Modifiability: Event routing)

---

## exchange-definitions.json

**Artifact Type**: Exchange Definition File  
**Description**: RabbitMQ Exchange 스키마 정의 파일로 이벤트 라우팅을 설정한다.

**<<manifest>> Components**:

- ExchangeSchema
    
- EventRouting
    

**Dependencies**: None

**관련 QAS**: QAS-06 (Modifiability: Event routing)

---

## erlang-26.tar.gz

**Artifact Type**: Runtime Archive  
**Description**: Erlang VM 런타임으로 RabbitMQ 실행에 필요하다.

**<<manifest>> Components**:

- ErlangVM
    

**Dependencies**: None

---

## 4.3.6.14. N-14: Redis Cache Cluster Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N14_Redis_Cache

title N-14: Redis Cache Cluster Artifacts

node "N-14: Redis Cache Cluster Node" {
  artifact "A-17: redis.conf" as A17 {
    component ClusterConfiguration
    component MemoryPolicyConfiguration
    component PersistenceConfiguration
    component AuthenticationConfiguration
  }
  
  artifact "redis-server-7.2.tar.gz" as A17_Binary {
    component RedisServer
    component CacheEngine
  }
  
  artifact "sentinel.conf" as A17_Sentinel {
    component SentinelConfiguration
    component FailoverConfiguration
  }
}

A17_Binary --> A17 : <<configures>>
A17_Binary --> A17_Sentinel : <<uses>>

note right of A17
  **QAS-02**: Data Cache Tactic
  **QAS-05**: Redis Cluster 고가용성
end note

note right of A17_Sentinel
  **QAS-05**: Auto-failover < 5초
end note

@enduml
```

## redis-server-7.2.tar.gz

**Artifact Type**: Binary Archive  
**Description**: Redis 7.2+ 서버 바이너리로 인메모리 캐시를 제공한다.

**<<manifest>> Components**:

- RedisServer
    
- CacheEngine
    

**Dependencies**: A-17 (redis.conf)

**관련 QAS**: QAS-02 (Sub-millisecond cache)

---

## sentinel.conf

**Artifact Type**: Configuration File  
**Description**: Redis Sentinel 설정 파일로 자동 페일오버를 구성한다.

**<<manifest>> Components**:

- SentinelConfiguration
    
- FailoverConfiguration
    

**Dependencies**: None

**관련 QAS**: QAS-05 (Auto-failover < 5초)

---

## 4.3.6.10. N-10: MLOps Service Node Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_N10_MLOps_Service

title N-10: MLOps Service Node Artifacts

node "N-10: MLOps Service Node" {
  artifact "A-07: MLOpsService.whl" as A07 {
    component TrainingManager
    component ModelValidator
    component ModelDeployer
    component DataCollector
    component KafkaConsumer
    component S3ModelUploader
    component ModelSwapClient
  }
  
  artifact "A-08: training_scripts/" as A08 {
    component data_preprocessor.py
    component model_trainer.py
    component model_evaluator.py
    component hyperparameter_tuner.py
  }
  
  artifact "A-09: mlops-config.yml" as A09 {
    component KafkaConsumerConfiguration
    component S3BucketConfiguration
    component TrainingConfiguration
    component ModelValidationConfiguration
  }
  
  artifact "model-weights-v1.0.pb" as A10_Model {
    component ModelWeights
    component ModelMetadata
  }
  
  package "Dependencies" {
    [tensorflow-2.13.jar]
    [pytorch-2.1.jar]
    [kafka-python.whl]
    [boto3.whl]
    [numpy.whl]
    [pandas.whl]
    [scikit-learn]
  }
}

A07 --> [tensorflow-2.13.jar] : <<uses>>
A07 --> [pytorch-2.1.jar] : <<uses>>
A07 --> [kafka-python.whl] : <<uses>>
A07 --> [boto3.whl] : <<uses>>
A07 --> [numpy.whl] : <<uses>>
A07 --> [pandas.whl] : <<uses>>
A07 --> A08 : <<uses>>
A07 --> A09 : <<configures>>
A07 --> A10_Model : <<uses>>
A08 --> [tensorflow] : <<uses>>
A08 --> [scikit-learn] : <<uses>>

note right of A07
  **QAS-06**: AI 모델 재학습 및 배포
  Hot Swap 방식
end note

@enduml
```

## A-07: MLOpsService.whl

**Artifact Type**: Python Wheel Package  
**Description**: AI 모델 재학습 및 배포를 수행하는 Python 애플리케이션이다.

**<<manifest>> Components**:

- TrainingManager
    
- ModelValidator
    
- ModelDeployer
    
- DataCollector
    
- KafkaConsumer
    
- S3ModelUploader
    
- ModelSwapClient
    

**Dependencies**:

- tensorflow-2.13.jar
    
- pytorch-2.1.jar
    
- kafka-python.whl
    
- boto3.whl (AWS SDK)
    
- numpy.whl
    
- pandas.whl
    

**관련 QAS**: QAS-06 (AI 모델 재학습 및 배포)

---

## A-08: training_scripts/

**Artifact Type**: Python Scripts Directory  
**Description**: AI 모델 학습을 위한 Python 스크립트 모음이다.

**<<manifest>> Components**:

- data_preprocessor.py
    
- model_trainer.py
    
- model_evaluator.py
    
- hyperparameter_tuner.py
    

**Dependencies**:

- tensorflow
    
- scikit-learn
    

---

## A-09: mlops-config.yml

**Artifact Type**: Configuration File  
**Description**: MLOps Service의 Kafka, S3, 학습 파라미터 설정을 포함한다.

**<<manifest>> Components**:

- KafkaConsumerConfiguration
    
- S3BucketConfiguration
    
- TrainingConfiguration
    
- ModelValidationConfiguration
    

**Dependencies**: None

---

## model-weights-v1.0.pb

**Artifact Type**: Model File  
**Description**: TensorFlow 모델 가중치 파일로 Hot Swap 방식으로 배포된다.

**<<manifest>> Components**:

- ModelWeights
    
- ModelMetadata
    

**Dependencies**: None

**관련 QAS**: QAS-06 (Hot Swap 모델 배포)

---

## 4.3.6.11. N-11: RDS Cluster Artifacts

### 💻 PlantUML Artifact Definition Diagram

```plantuml
@startuml ArtifactView_S3_Storage

title AWS S3 Storage Artifacts

cloud "AWS S3 Storage" {
  artifact "A-22: face-recognition-model.h5" as A22 {
    component FaceRecognitionModel
    component ModelMetadata
  }
  
  artifact "lifecycle-policy.json" as A22_Lifecycle {
    component LifecyclePolicy
    component StorageClassTransition
  }
  
  artifact "bucket-policy.json" as A22_BucketPolicy {
    component BucketPolicy
    component IPWhitelist
    component PreSignedURLPolicy
  }
}

note right of A22
  **QAS-06**: AI 모델 재학습 및 배포
  **QAS-02**: 안면인식 출입 인증
  Hot Swap 방식으로 배포
end note

note right of A22_Lifecycle
  **Cost**: Standard → IA → Glacier (70% 절감)
end note

note right of A22_BucketPolicy
  **QAS-04**: IP Whitelist, Pre-signed URL
end note

@enduml
```

## A-22: face-recognition-model.h5

**Artifact Type**: TensorFlow/Keras Model Binary  
**Description**: 안면 인식 AI 모델 바이너리 파일로 S3에 저장되고 Hot Swap 방식으로 배포된다.

**<<manifest>> Components**:

- FaceRecognitionModel (Neural Network Weights)
    
- ModelMetadata (version, accuracy, timestamp)
    

**Dependencies**: None

**관련 QAS**: QAS-06 (AI 모델 재학습 및 배포), QAS-02 (안면인식 출입 인증)

---

## lifecycle-policy.json

**Artifact Type**: Lifecycle Policy File  
**Description**: S3 버킷의 자동 아카이빙 정책으로 오래된 데이터를 저렴한 저장소로 이동시킨다.

**<<manifest>> Components**:

- LifecyclePolicy
    
- StorageClassTransition (Standard → IA → Glacier)
    

**Dependencies**: None

**관련 QAS**: Cost Optimization (70% 절감)

---

## bucket-policy.json

**Artifact Type**: Bucket Policy File  
**Description**: S3 버킷 접근 정책으로 IP 화이트리스트 및 Pre-signed URL 정책을 포함한다.

**<<manifest>> Components**:

- BucketPolicy
    
- IPWhitelist
    
- PreSignedURLPolicy
    

**Dependencies**: None

**관련 QAS**: QAS-04 (IP Whitelist, Pre-signed URL)

---

## 4.3.6.16. Artifact Dependency Matrix

|Artifact|Depends On|Used By Nodes|
|---|---|---|
|nginx-ingress-controller.yaml|-|N-01|
|ingress-config.yaml|-|N-01|
|tls-certificates.pem|-|N-01|
|A-01: APIGateway.jar|spring-cloud-gateway, spring-security-jwt, redis-client|N-02|
|A-02: gateway-config.yml|-|N-02|
|A-03: AccessService.jar|spring-boot, jedis, postgresql-jdbc, kafka-client, grpc-java, lettuce|N-03|
|A-04: access-config.yml|-|N-03|
|facemodel-service.jar|grpc-java|N-03|
|application-facemodel.yml|-|N-03|
|auth-service.jar|spring-boot, spring-security, jjwt, postgresql-jdbc|N-04|
|application-auth.yml|-|N-04|
|helper-service.jar|spring-boot, aws-s3-sdk, postgresql-jdbc|N-05|
|application-helper.yml|-|N-05|
|A-05: SearchService.jar|spring-boot, elasticsearch-client, kafka-client, openai-client|N-06|
|A-06: search-config.yml|-|N-06|
|branchowner-service.jar|spring-boot, postgresql-jdbc|N-07|
|application-branchowner.yml|-|N-07|
|A-10: MonitoringService.jar|spring-boot, jedis, kafka-client, scheduling|N-08|
|A-11: monitoring-config.yml|-|N-08|
|notification-service.jar|spring-boot, firebase-admin-sdk|N-09|
|application-notification.yml|-|N-09|
|fcm-service-account.json|-|N-09|
|A-07: MLOpsService.whl|tensorflow, pytorch, kafka-python, boto3, numpy, pandas|N-10|
|A-08: training_scripts/|tensorflow, scikit-learn|N-10|
|A-09: mlops-config.yml|-|N-10|
|model-weights-v1.0.pb|-|N-10|
|A-15: database-schema.sql|-|N-11|
|A-16: initial-data.sql|A-15|N-11|
|ivfflat-index.sql|A-15|N-11|
|A-18: branch-index-mapping.json|A-20|N-12|
|A-19: review-index-mapping.json|A-20|N-12|
|A-20: nori-analyzer-plugin.zip|-|N-12|
|A-21: elasticsearch.yml|-|N-12|
|elasticsearch-8.10.tar.gz|A-21|N-12|
|rabbitmq-server-3.12.8.tar.gz|erlang-26|N-13|
|rabbitmq.conf|-|N-13|
|queue-definitions.json|-|N-13|
|exchange-definitions.json|-|N-13|
|erlang-26.tar.gz|-|N-13|
|A-17: redis.conf|-|N-14|
|redis-server-7.2.tar.gz|A-17|N-14|
|sentinel.conf|-|N-14|
|A-22: face-recognition-model.h5|-|N-15 → N-10 → N-03|
|lifecycle-policy.json|-|N-15|
|bucket-policy.json|-|N-15|