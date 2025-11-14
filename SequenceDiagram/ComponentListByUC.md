# 시퀀스 다이어그램별 컴포넌트 리스트

## 개요
UC-07, UC-09, UC-21, UC-24의 시퀀스 다이어그램에 나타나는 컴포넌트들을 Layer별로 정리한 표입니다.

---

## UC-07: 안면인식 출입 인증 (Face Recognition Access Control)

### Access Service Node (실시간 접근 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| AccessControlController | Interface | 출입 인증 요청 수신 및 응답 |
| FaceModelServiceIPCClient | System Interface | FaceModel Service와 IPC 통신 |
| EquipmentGatewayAdapter | System Interface | 설비 게이트 제어 |
| VectorRepository | System Interface | 안면 벡터 데이터베이스 접근 |
| RabbitMQAdapter | System Interface | 이벤트 발행 |
| AccessAuthorizationManager | Business | 출입 권한 검증 및 처리 |
| FaceVectorCache | Business | 안면 벡터 메모리 캐싱 |
| GateController | Business | 게이트 개폐 제어 |
| AccessEventProcessor | Business | 출입 이벤트 처리 및 로깅 |

### FaceModel Service Node (실시간 접근 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| FaceModelIPCHandler | Interface | IPC 요청 수신 및 처리 |
| VectorComparisonEngine | Business | 벡터 유사도 계산 및 비교 |
| FeatureExtractor | Business | 이미지 특징점 추출 |
| MLInferenceEngineAdapter | System Interface | ML 추론 엔진 호출 |

### 외부 시스템
| Component Name | Type | Description |
|---------------|------|-------------|
| VectorDatabase | Database | 안면 벡터 및 로그 저장 |
| RabbitMQ Broker | Message Broker | 이벤트 라우팅 |

---

## UC-09: 자연어 지점 검색 (Natural Language Branch Search)

### API Gateway
| Component Name | Layer | Description |
|---------------|-------|-------------|
| ApiGateway | API Gateway | 요청 라우팅, 인증, Rate Limiting |

### Search Service Node (비즈니스 로직 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| BranchSearchController | Interface | 검색 요청 수신 및 응답 |
| SearchQueryManager | Business | 검색 쿼리 처리 및 조정 |
| SimpleKeywordTokenizer | Business | 자연어 텍스트 토큰화 |
| SearchEngineAdapter | Business | ElasticSearch 쿼리 실행 |
| ElasticSearchRepository | System Interface | ElasticSearch 데이터 접근 |

### 외부 시스템
| Component Name | Type | Description |
|---------------|------|-------------|
| ElasticSearch DB | Search Engine | 지점 및 리뷰 검색 인덱스 |

---

## UC-21: 설비 상태 모니터링 (Equipment Status Monitoring)

### Timer
| Component Name | Type | Description |
|---------------|------|-------------|
| Quartz Scheduler | Timer | 주기적 모니터링 트리거 |

### Monitoring Service Node (비즈니스 로직 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| EquipmentHealthChecker | Business | 설비 상태 검사 및 판정 |
| PingEchoExecutor | Business | 설비 Ping/Echo 테스트 실행 |
| FaultDetector | Business | 고장 감지 및 이벤트 생성 |
| AuditLogger | Business | 감사 로그 기록 |
| EquipmentStatusJpaRepository | System Interface | 설비 상태 데이터베이스 접근 |
| EquipmentGatewayClient | System Interface | 설비 게이트웨이 통신 |
| RabbitMQAdapter | System Interface | 이벤트 발행 |

### Notification Service Node (비즈니스 로직 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| NotificationDispatcherConsumer | Business | 이벤트 수신 및 알림 발송 |
| NotificationDispatcherManager | Business | 알림 처리 및 관리 |
| FcmPushGateway | System Interface | FCM 푸시 알림 전송 |

### 외부 시스템
| Component Name | Type | Description |
|---------------|------|-------------|
| Branch Equipment | Device | 지점 설비 (카메라, 게이트, 센서) |
| MonitorDatabase | Database | 설비 상태 및 로그 저장 |
| RabbitMQ Broker | Message Broker | 이벤트 라우팅 |

---

## UC-24: 세탁물 모델 재학습 (Laundry Model Retraining)

### Helper Service Node (인터페이스 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| TaskController | Interface | 작업 컨펌 요청 수신 |

### Helper Service Node (비즈니스 로직 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| RewardConfirmationManager | Business | 작업 컨펌 및 보상 처리 |
| RabbitMQAdapter | System Interface | 이벤트 발행/수신 |

### MLOps Service Node (AI 파이프라인 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| TrainingManager | Business | 재학습 워크플로우 관리 |
| TrainingPipelineOrchestrator | Business | 훈련 파이프라인 조정 |
| ModelVerificationService | Business | 모델 검증 (정확도, 성능) |
| DeploymentService | Business | 모델 배포 및 Hot Swap |
| MLInferenceEngineAdapter | System Interface | ML 추론 엔진 호출 |
| RabbitMQAdapter | System Interface | 이벤트 발행/수신 |

### FaceModel Service Node (실시간 접근 계층)
| Component Name | Layer | Description |
|---------------|-------|-------------|
| ModelLifecycleManager | Business | 모델 생명주기 관리 및 Hot Swap |

### 외부 시스템
| Component Name | Type | Description |
|---------------|------|-------------|
| Helper Service DB | Database | 작업 데이터 및 분석 결과 저장 |
| Training Data Store | Database | 훈련 데이터 저장 |
| Model Database | Database | 모델 메타데이터 저장 |
| RabbitMQ Broker | Message Broker | 이벤트 라우팅 |

---

## 요약 표

| UC | 총 컴포넌트 수 | Interface Layer | Business Layer | System Interface Layer | 외부 시스템 |
|----|--------------|----------------|---------------|----------------------|------------|
| UC-07 | 18 | 2 | 7 | 6 | 3 (DB, MQ, Equipment) |
| UC-09 | 6 | 1 | 3 | 1 | 1 (Search Engine) |
| UC-21 | 13 | 0 | 6 | 4 | 3 (Device, DB, MQ) |
| UC-24 | 10 | 1 | 5 | 3 | 4 (DB×3, MQ) |

### Layer 분포 분석
- **Interface Layer**: 사용자/API 요청 처리 (Controller, Handler)
- **Business Layer**: 비즈니스 로직 및 워크플로우 (Manager, Engine, Consumer)
- **System Interface Layer**: 외부 시스템 연동 (Repository, Adapter, Gateway)
- **외부 시스템**: 데이터베이스, 메시지 브로커, 검색 엔진, IoT 장비

### 디자인 패턴 적용
- **Event-Driven**: UC-07, UC-21, UC-24 (RabbitMQ 이벤트)
- **Repository Pattern**: 모든 UC (JpaRepository, ElasticSearchRepository)
- **Adapter Pattern**: UC-07, UC-09, UC-21, UC-24 (ML, Gateway, Storage Adapter)
- **Command Pattern**: UC-21 (HealthChecker, PingExecutor)
- **Strategy Pattern**: UC-09 (다양한 검색 전략)
- **Observer Pattern**: UC-24 (이벤트 기반 재학습 트리거)
