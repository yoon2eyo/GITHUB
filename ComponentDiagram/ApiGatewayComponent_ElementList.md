# API Gateway Component Element List

## 개요
본 문서는 API Gateway 컴포넌트 다이어그램(`07_ApiGatewayComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IApiGatewayEntry** | 모든 클라이언트 요청의 단일 진입점 인터페이스를 정의<br>요청 라우팅, 기본 검증, 응답 포맷팅 기능을 제공<br>외부 클라이언트(모바일 앱, 웹)와의 통신 표준화 | UC-01, UC-02, UC-03, UC-04, UC-05, UC-06, UC-07, UC-08, UC-09, UC-10, UC-11, UC-12, UC-13, UC-14, UC-15, UC-18<br>QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리) |
| **IApiGatewayManagement** | API Gateway 관리 인터페이스를 정의<br>서비스 등록/해제, 헬스체크, 메트릭 조회 기능을 제공<br>운영팀의 게이트웨이 모니터링 및 제어를 지원 | UC-05 (관리자 로그인), UC-06 (서비스 상태 조회)<br>QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장) |
| **ApiGatewayController** | IApiGatewayEntry 인터페이스의 구현<br>클라이언트 요청 수신 및 기본 처리 수행<br>요청 헤더/바디 검증, CORS 처리, 기본 로깅 담당 | UC-01~UC-15 (모든 사용자 UC)<br>QAS-04 (Security), QAS-05 (Availability) |
| **ApiGatewayManagementController** | IApiGatewayManagement 인터페이스의 구현<br>관리 API 요청 처리 및 서비스 디스커버리 관리<br>운영팀의 게이트웨이 제어 인터페이스 제공 | UC-05, UC-06<br>QAS-05 (Availability), DD-01 (MSA Architecture) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IRequestRoutingService** | 요청 라우팅 전략 인터페이스를 정의<br>요청 URL 패턴 분석 및 적절한 백엔드 서비스로 라우팅<br>Path 기반, Header 기반 라우팅 로직 제공 | UC-01~UC-15 (모든 서비스 라우팅)<br>DD-01 (4-Layer MSA), DD-02 (Event-Driven) |
| **RequestRouter** | IRequestRoutingService 구현<br>요청 URL 분석 및 백엔드 서비스 라우팅 실행<br>보안 검증, 서비스 디스커버리, 로드밸런싱 통합 | UC-01~UC-15<br>QAS-05 (Availability), DD-01 (MSA) |
| **ISecurityService** | 보안 검증 서비스 인터페이스를 정의<br>인증, 인가, 요청 서명 검증 통합 관리<br>다중 보안 메커니즘(토큰, API 키, 서명) 지원 | UC-01~UC-15 (모든 요청 보안)<br>QAS-04 (Security), QAS-01 (설비 고장 감지 - API 보호) |
| **SecurityManager** | ISecurityService 구현<br>인증, 인가, 서명 검증 통합 관리<br>다중 보안 프로토콜(JWT, OAuth2, API Key) 지원 | QAS-04 (Security)<br>UC-01~UC-15 |
| **IServiceDiscoveryService** | 서비스 디스커버리 인터페이스를 정의<br>동적 서비스 등록/해제, 헬스체크, 로드밸런싱 지원<br>Eureka/ZooKeeper 등 레지스트리 연동 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-05 (Availability - 서비스 자동 복구) |
| **ServiceDiscoveryManager** | IServiceDiscoveryService 구현<br>Eureka 클라이언트로 서비스 등록/조회<br>서비스 헬스체크 및 자동 제거 수행 | DD-01 (MSA), QAS-05 (Availability)<br>DD-02 (Event-Driven) |
| **ILoadBalancingService** | 로드 밸런싱 인터페이스를 정의<br>다중 인스턴스 간 트래픽 분산 알고리즘 구현<br>Round Robin, Weighted, Least Connection 전략 지원 | QAS-05 (Availability - 트래픽 분산)<br>DD-01 (MSA 확장성) |
| **LoadBalancer** | ILoadBalancingService 구현<br>서킷 브레이커, Rate Limiter 통합<br>장애 인스턴스 자동 제외 및 복구 | QAS-05 (Availability)<br>DD-01 (MSA 확장성) |
| **IAuthenticationService** | 인증 서비스 인터페이스를 정의<br>JWT 토큰 검증, API 키 인증, OAuth2 지원<br>사용자 신원 확인 및 세션 관리 | UC-01~UC-15 (모든 인증)<br>QAS-04 (Security - 인증) |
| **AuthenticationManager** | IAuthenticationService 구현<br>외부 인증 서비스(Auth Service)와 gRPC 통신<br>JWT 토큰 검증 및 사용자 정보 조회 | QAS-04 (Security), UC-01~UC-15<br>DD-01 (MSA) |
| **IAuthorizationService** | 인가 서비스 인터페이스를 정의<br>역할 기반 접근 제어(RBAC), 권한 검증<br>리소스별 접근 정책 적용 | UC-01~UC-15 (모든 인가)<br>QAS-04 (Security - 인가) |
| **AuthorizationManager** | IAuthorizationService 구현<br>외부 인가 서비스(Auth Service)와 gRPC 통신<br>사용자 권한 및 역할 검증 | QAS-04 (Security), UC-01~UC-15<br>DD-01 (MSA) |
| **IRequestSignatureVerifier** | 요청 서명 검증 인터페이스를 정의<br>HMAC, RSA 서명 검증으로 요청 무결성 보장<br>중간자 공격 방지 및 데이터 변조 감지 | QAS-04 (Security - 요청 무결성)<br>UC-01~UC-15 (모든 외부 요청) |
| **RequestSignatureVerifier** | IRequestSignatureVerifier 구현<br>HMAC-SHA256 서명 검증 알고리즘<br>타임스탬프 기반 리플레이 공격 방지 | QAS-04 (Security - 무결성)<br>UC-01~UC-15 |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IServiceRegistry** | 서비스 레지스트리 인터페이스를 정의<br>Eureka/ZooKeeper API 래핑<br>서비스 등록, 조회, 헬스체크 기능 제공 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-05 (Availability) |
| **EurekaServiceRegistry** | IServiceRegistry 구현<br>Netflix Eureka 클라이언트<br>서비스 등록/해제, 헬스체크, 조회 기능 | DD-01 (MSA), QAS-05 (Availability)<br>DD-02 (Event-Driven) |
| **IAuthenticationClient** | 인증 클라이언트 인터페이스를 정의<br>Auth Service와의 gRPC 통신 래핑<br>토큰 검증 및 사용자 정보 조회 API 제공 | QAS-04 (Security), DD-01 (MSA)<br>UC-01~UC-15 |
| **AuthenticationClientAdapter** | IAuthenticationClient 구현<br>gRPC 클라이언트로 Auth Service 호출<br>JWT 검증 및 사용자 정보 조회 | QAS-04 (Security), DD-01 (MSA)<br>UC-01~UC-15 |
| **IAuthorizationClient** | 인가 클라이언트 인터페이스를 정의<br>Auth Service와의 gRPC 통신 래핑<br>권한 및 역할 검증 API 제공 | QAS-04 (Security), DD-01 (MSA)<br>UC-01~UC-15 |
| **AuthorizationClientAdapter** | IAuthorizationClient 구현<br>gRPC 클라이언트로 Auth Service 호출<br>RBAC 기반 권한 검증 | QAS-04 (Security), DD-01 (MSA)<br>UC-01~UC-15 |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>RabbitMQ API 래핑<br>이벤트 발행 및 라우팅 기능 제공 | DD-02 (Event-Driven)<br>UC-01~UC-15 (감사 로그 이벤트) |
| **RabbitMQAdapter** | IMessagePublisherService 구현<br>RabbitMQ 클라이언트<br>이벤트 발행 및 Topic Exchange 라우팅 | DD-02 (Event-Driven)<br>UC-01~UC-15 (보안 이벤트) |
| **ICircuitBreaker** | 서킷 브레이커 인터페이스를 정의<br>Resilience4j API 래핑<br>장애 전파 방지 및 자동 복구 기능 제공 | QAS-05 (Availability), DD-01 (MSA)<br>UC-01~UC-15 |
| **ResilientCircuitBreaker** | ICircuitBreaker 구현<br>Resilience4j Circuit Breaker<br>실패율 임계치 기반 자동 개방/폐쇄 | QAS-05 (Availability)<br>DD-01 (MSA) |
| **IRateLimiter** | Rate Limiter 인터페이스를 정의<br>Redis 기반 속도 제한 래핑<br>과도한 요청 방지 및 DDoS 보호 | QAS-05 (Availability), QAS-04 (Security)<br>UC-01~UC-15 |
| **ResilientRateLimiter** | IRateLimiter 구현<br>Redis 기반 Rate Limiting<br>사용자별/IP별 요청 제한 | QAS-04 (Security), QAS-05 (Availability)<br>UC-01~UC-15 |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 요청 수신 및 기본 응답 처리<br>API Gateway의 공개 인터페이스 정의<br>클라이언트 요청의 첫 번째 접점 | UC-01~UC-18 (모든 클라이언트 요청)<br>QAS-04 (Security), QAS-05 (Availability) |
| **Business Layer** | API Gateway의 핵심 비즈니스 로직 구현<br>라우팅, 보안, 서비스 디스커버리, 로드밸런싱<br>외부 서비스와의 통합 로직 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-04 (Security), QAS-05 (Availability) |
| **System Interface Layer** | 외부 시스템과의 연동 인터페이스<br>Eureka, Auth Service, RabbitMQ, Redis 등과의 통신<br>프로토콜 변환 및 오류 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-04 (Security), QAS-05 (Availability) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 7 | 7 | 14 |
| **System Interface Layer** | 6 | 6 | 12 |
| **패키지** | - | - | 3 |
| **총계** | **15** | **15** | **33** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **인증**: AuthenticationManager, IAuthenticationService, IAuthenticationClient
- **인가**: AuthorizationManager, IAuthorizationService, IAuthorizationClient
- **무결성**: RequestSignatureVerifier, IRequestSignatureVerifier
- **접근제어**: ResilientRateLimiter, IRateLimiter
- **감사**: RabbitMQAdapter, IMessagePublisherService (보안 이벤트 발행)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **서비스 디스커버리**: ServiceDiscoveryManager, IServiceDiscoveryService, EurekaServiceRegistry
- **로드밸런싱**: LoadBalancer, ILoadBalancingService, ResilientCircuitBreaker
- **장애 내성**: ResilientCircuitBreaker, ICircuitBreaker, ResilientRateLimiter

### DD-01 (4-Layer Hybrid MSA)
- **라우팅**: RequestRouter, IRequestRoutingService
- **보안**: SecurityManager, ISecurityService
- **디스커버리**: ServiceDiscoveryManager, IServiceDiscoveryService

### DD-02 (Event-Driven Architecture)
- **메시징**: RabbitMQAdapter, IMessagePublisherService
- **이벤트 발행**: 감사 로그, 보안 이벤트, 서비스 상태 변경 이벤트

### 관련 UC 목록
- **UC-01~UC-15**: 모든 사용자 유스케이스 (API Gateway 경유)
- **UC-05**: 관리자 로그인 (IApiGatewayManagement 활용)
- **UC-06**: 서비스 상태 조회 (IServiceDiscoveryService 활용)

---

## 결론

API Gateway 컴포넌트 다이어그램의 모든 요소(33개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 외부 요청 처리 (4개 요소)
- **Business Layer**: 비즈니스 로직 (14개 요소)
- **System Interface Layer**: 외부 연동 (12개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
