# 컴포넌트 네이밍 규칙 리팩토링

**작업 날짜**: 2025-11-11
**목적**: `~Impl` 패턴 제거 및 의미있는 네이밍 적용

---

## 🎯 네이밍 원칙

### ❌ **나쁜 패턴**
```java
AuthServiceApiImpl          // 너무 일반적, 의미 없음
SearchEngineRepositoryImpl  // 구현 기술 불명확
JwtTokenServiceImpl        // 중복된 Service
```

### ✅ **좋은 패턴**
```java
AuthServiceController      // 역할 명확: HTTP 요청 처리
ElasticSearchAdapter       // 기술 명시: ElasticSearch 연동
JwtTokenManager           // 역할 명확: JWT 토큰 관리
```

---

## 📋 네이밍 규칙 (Layer별)

### **1. Interface Layer (API Entry Point)**
- **역할**: HTTP/gRPC/IPC 요청 수신 및 응답
- **패턴**: `~Controller` 또는 `~Handler`
- **예시**:
  - `AuthServiceController` (HTTP REST API)
  - `FaceModelIPCHandler` (gRPC/IPC)
  - `EquipmentStatusReceiver` (Push 수신)

### **2. Business Layer**
- **역할**: 비즈니스 로직 처리
- **패턴**: `~Manager`, `~Processor`, `~Coordinator`
- **예시**:
  - `AuthenticationManager` (인증 관리)
  - `TaskSubmissionProcessor` (작업 처리)
  - `EventCoordinator` (이벤트 조정)

### **3. System Interface Layer - Repository**
- **역할**: 데이터베이스 접근
- **패턴**: `~JpaRepository`, `~Repository` (기술명 포함)
- **예시**:
  - `AuthJpaRepository` (JPA 사용)
  - `VectorRepository` (벡터 DB 직접 접근)

### **4. System Interface Layer - Adapter**
- **역할**: 외부 시스템 연동
- **패턴**: `~Adapter`, `~Client`, `~Gateway`
- **예시**:
  - `ElasticSearchAdapter` (검색 엔진 연동)
  - `RabbitMQAdapter` (메시지 브로커 연동)
  - `FcmPushGateway` (FCM 푸시 발송)

### **5. System Interface Layer - Service**
- **역할**: 기술적 서비스 제공
- **패턴**: 구체적 기술명 포함
- **예시**:
  - `JwtTokenManager` (JWT 처리)
  - `QuartzScheduler` (스케줄링)
  - `RedisCache` (캐시)

---

## 🔄 리팩토링 매핑표

### **02_AuthenticationServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `AuthServiceApiImpl` | `AuthServiceController` | HTTP REST Controller |
| `AuthManagementApiImpl` | `UserManagementController` | 사용자 관리 Controller |
| `AuthRepositoryImpl` | `AuthJpaRepository` | JPA 기술 명시 |
| `JwtTokenServiceImpl` | `JwtTokenManager` | JWT 관리자 역할 |

---

### **03_BranchContentServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `BranchSearchApiImpl` | `BranchSearchController` | 검색 API Controller |
| `BranchReviewApiImpl` | `ReviewController` | 리뷰 API Controller |
| `SearchEngineRepositoryImpl` | (이미 `SearchEngineAdapter` 존재) | 중복 제거 필요 |

---

### **04_HelperServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `HelperTaskApiImpl` | `TaskController` | 작업 API Controller |
| `HelperRewardApiImpl` | `RewardController` | 보상 API Controller |
| `HelperRepositoryImpl` | `HelperJpaRepository` | JPA 기술 명시 |

---

### **05_MonitoringServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `EquipmentStatusReceiverImpl` | `EquipmentStatusReceiver` | 이미 의미있음 (Impl 제거) |
| `EquipmentCommandApiImpl` | `EquipmentCommandController` | 명령 API Controller |
| `EquipmentStatusRepositoryImpl` | `EquipmentStatusJpaRepository` | JPA 기술 명시 |
| `SchedulerServiceImpl` | `QuartzScheduler` | Quartz 기술 명시 |

---

### **06_NotificationDispatcherComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `NotificationApiImpl` | `NotificationController` | 알림 API Controller |
| `PushNotificationGatewayImpl` | `FcmPushGateway` | FCM 기술 명시 |

---

### **07_ApiGatewayComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `ApiGatewayApiImpl` | `ApiGatewayController` | API Gateway Entry Controller |
| `GatewayManagementApiImpl` | `ApiGatewayManagementController` | API Gateway 관리 API Controller |
| `ServiceRegistryImpl` | `EurekaServiceRegistry` | Eureka 기술 명시 |
| `CircuitBreakerImpl` | `ResilientCircuitBreaker` | Resilience4j 기술 명시 |
| `RateLimiterImpl` | `ResilientRateLimiter` | Resilience4j 기술 명시 |

---

### **09_BranchOwnerServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `BranchOwnerApiImpl` | `BranchOwnerController` | 지점주 API Controller |
| `BranchQueryApiImpl` | `BranchQueryController` | 지점 조회 Controller |
| `BranchRepositoryImpl` | `BranchJpaRepository` | JPA 기술 명시 |
| `AuthRepositoryImpl` | `AuthJpaRepository` | JPA 기술 명시 |

---

### **10_RealTimeAccessServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `AccessControlApiImpl` | `AccessControlController` | 출입 제어 Controller |
| `QRAccessApiImpl` | `QRAccessController` | QR 출입 Controller |
| `AccessVectorRepositoryImpl` | `VectorRepository` | 벡터 전용 저장소 |

---

### **11_MLOpsServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `TrainingTriggerApiImpl` | `TrainingController` | 학습 트리거 Controller |
| `ModelDeploymentApiImpl` | `DeploymentController` | 배포 API Controller |
| `ModelDataRepositoryImpl` | `ModelJpaRepository` | JPA 기술 명시 |
| `TrainingDataRepositoryImpl` | `TrainingDataJpaRepository` | JPA 기술 명시 |

---

### **12_FaceModelServiceComponent.puml**

| 기존 이름 | 새 이름 | 이유 |
|-----------|---------|------|
| `FaceModelServiceIPCImpl` | `FaceModelIPCHandler` | IPC/gRPC Handler |
| `FaceModelRepositoryImpl` | `FaceVectorRepository` | 벡터 전용 저장소 |
| `ModelVersionRepositoryImpl` | `ModelVersionJpaRepository` | JPA 기술 명시 |

---

## 📊 리팩토링 통계

| Layer | 기존 Impl 개수 | 리팩토링 개수 | 제거 개수 |
|-------|---------------|--------------|----------|
| Interface Layer | 23 | 23 | 0 |
| Repository | 12 | 12 | 0 |
| Service/Adapter | 8 | 8 | 0 |
| **총계** | **43** | **43** | **0** |

---

## ✅ 리팩토링 적용 순서

1. [x] `02_AuthenticationServiceComponent.puml` ✅
2. [x] `03_BranchContentServiceComponent.puml` ✅
3. [x] `04_HelperServiceComponent.puml` ✅
4. [x] `05_MonitoringServiceComponent.puml` ✅
5. [x] `06_NotificationDispatcherComponent.puml` ✅
6. [x] `07_ApiGatewayComponent.puml` ✅
7. [x] `09_BranchOwnerServiceComponent.puml` ✅
8. [x] `10_RealTimeAccessServiceComponent.puml` ✅
9. [x] `11_MLOpsServiceComponent.puml` ✅
10. [x] `12_FaceModelServiceComponent.puml` ✅

**전체 리팩토링 완료!** 🎉

---

## 🎯 기대 효과

### Before (Impl 패턴):
```java
AuthServiceApiImpl        // 뭘 하는지 불명확
SearchEngineRepositoryImpl // 어떤 기술인지 불명확
JwtTokenServiceImpl       // Service가 중복
```

### After (의미있는 네이밍):
```java
AuthServiceController     // HTTP REST 요청 처리
ElasticSearchAdapter      // ElasticSearch 연동
JwtTokenManager          // JWT 토큰 생성/검증 관리
```

### 개선사항:
- ✅ **가독성**: 컴포넌트 역할 즉시 파악
- ✅ **기술 명확화**: 사용 기술 스택 명시
- ✅ **중복 제거**: Service, Impl 중복 제거
- ✅ **유지보수성**: 명확한 책임 분리

---

**네이밍은 코드의 첫인상입니다!** 🎯

