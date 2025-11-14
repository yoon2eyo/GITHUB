# Authentication Service Component Element List

## 개요
본 문서는 Authentication Service 컴포넌트 다이어그램(`02_AuthenticationServiceComponent.puml`)에 나타나는 모든 정적 구조 요소들을 나열하고, 각 요소의 역할(responsibility)과 관련 Architectural Drivers(ADs)를 기술합니다.

요소들은 Layer별로 분류하여 Interface Layer → Business Layer → System Interface Layer 순으로 나열합니다.

---

## Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAuthServiceApi** | 인증/인가 서비스의 공개 API 인터페이스를 정의<br>JWT 토큰 발급, 검증, 사용자 권한 조회 기능 제공<br>외부 클라이언트와의 인증 통신 표준화 | UC-01 (사용자 로그인), UC-02 (사용자 인증), UC-03 (권한 검증)<br>QAS-04 (Security - 인증/인가) |
| **IAuthManagementApi** | 사용자 관리 API 인터페이스를 정의<br>회원가입, 사용자 정보 관리, 계정 상태 변경 기능 제공<br>관리자용 사용자 관리 인터페이스 | UC-04 (사용자 등록), UC-05 (관리자 로그인)<br>QAS-04 (Security - 접근권한 분리) |
| **AuthServiceController** | IAuthServiceApi 인터페이스의 구현<br>인증/인가 요청 수신 및 응답 처리<br>JWT 토큰 관리 및 사용자 세션 처리 | UC-01, UC-02, UC-03<br>QAS-04 (Security), QAS-05 (Availability) |
| **UserManagementController** | IAuthManagementApi 인터페이스의 구현<br>사용자 등록 및 관리 요청 처리<br>관리자 권한 검증 및 감사 로그 기록 | UC-04, UC-05<br>QAS-04 (Security - 감사로그) |

---

## Business Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAuthenticationService** | 사용자 인증 로직 인터페이스를 정의<br>로그인 자격 증명 검증, 비밀번호 해싱, 계정 상태 확인<br>다중 인증 방식(JWT, OAuth2, API Key) 지원 | UC-01, UC-02 (모든 로그인)<br>QAS-04 (Security - 인증) |
| **AuthenticationManager** | IAuthenticationService 구현<br>로그인 요청 처리, 비밀번호 검증, JWT 토큰 생성<br>실패한 인증 시도 로깅 및 잠금 처리 | UC-01, UC-02<br>QAS-04 (Security), DD-02 (Event-Driven) |
| **IAuthorizationService** | 사용자 인가 로직 인터페이스를 정의<br>역할 기반 접근 제어(RBAC), 리소스 권한 검증<br>사용자 권한 정책 적용 및 캐싱 | UC-03 (권한 검증), UC-05 (관리자 권한)<br>QAS-04 (Security - 인가) |
| **AuthorizationManager** | IAuthorizationService 구현<br>사용자 권한 조회 및 검증<br>권한 캐싱으로 성능 최적화 | UC-03, UC-05<br>QAS-04 (Security) |
| **IUserRegistrationService** | 사용자 등록 로직 인터페이스를 정의<br>회원가입 처리, 신용카드 본인 인증, 초기 권한 설정<br>안면 사진 등록 및 벡터 생성 트리거 | UC-04 (사용자 등록)<br>QAS-04 (Security - 사용자 검증) |
| **UserRegistrationManager** | IUserRegistrationService 구현<br>회원가입 프로세스 관리, 신용카드 검증 연동<br>등록 완료 이벤트 발행 | UC-04<br>QAS-04 (Security), DD-02 (Event-Driven) |
| **AuthEventConsumer** | 인증 관련 이벤트 구독자<br>다른 서비스의 인증 상태 변경 이벤트 처리<br>토큰 블랙리스트 관리 및 사용자 세션 정리 | DD-02 (Event-Driven)<br>QAS-04 (Security - 세션 관리) |

---

## System Interface Layer

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **IAuthRepository** | 인증 데이터 저장소 인터페이스를 정의<br>사용자 정보, 토큰, 권한 데이터 CRUD 연산<br>JPA 기반 데이터 접근 추상화 | UC-01~UC-05 (모든 인증 데이터)<br>DD-03 (Database per Service) |
| **AuthJpaRepository** | IAuthRepository 구현<br>JPA/Hibernate 기반 데이터 접근<br>사용자 정보, 권한, 토큰 영속화 | DD-03 (Database per Service)<br>QAS-05 (Availability - 데이터 영속성) |
| **AuthDatabase** | 인증 서비스 전용 데이터베이스<br>사용자 정보, 권한, 토큰 저장<br>PostgreSQL 기반 암호화 저장 | DD-03 (Database per Service)<br>QAS-04 (Security - 데이터 암호화) |
| **IMessagePublisherService** | 메시지 발행 인터페이스를 정의<br>인증 이벤트 발행 및 라우팅<br>RabbitMQ Topic Exchange 활용 | DD-02 (Event-Driven)<br>UC-01~UC-05 (인증 이벤트) |
| **IMessageSubscriptionService** | 메시지 구독 인터페이스를 정의<br>외부 인증 이벤트 수신 및 처리<br>AuthEventConsumer와의 연동 | DD-02 (Event-Driven)<br>UC-01~UC-05 (상호 인증 이벤트) |
| **RabbitMQAdapter** | IMessagePublisherService 및 IMessageSubscriptionService 구현<br>RabbitMQ 클라이언트 통합<br>인증 이벤트 발행 및 구독 | DD-02 (Event-Driven)<br>QAS-05 (Availability - 메시지 내구성) |
| **ITokenService** | 토큰 관리 인터페이스를 정의<br>JWT 토큰 생성, 검증, 만료 처리<br>토큰 서명 및 암호화 | QAS-04 (Security - 토큰 보안)<br>UC-01~UC-05 (토큰 기반 인증) |
| **JwtTokenManager** | ITokenService 구현<br>JWT 토큰 생성 및 검증<br>RS256 서명 알고리즘, 토큰 만료 관리 | QAS-04 (Security - 토큰 무결성)<br>UC-01~UC-05 |
| **ICreditCardVerificationService** | 신용카드 검증 인터페이스를 정의<br>외부 PG사와의 통신 래핑<br>본인 인증 및 결제 정보 검증 | UC-04 (회원가입)<br>QAS-04 (Security - 결제 정보 보호) |
| **CreditCardVerificationClient** | ICreditCardVerificationService 구현<br>외부 PG사 HTTPS API 호출<br>결제 정보 암호화 및 전송 | QAS-04 (Security - 결제 보안)<br>UC-04 |

---

## 패키지 구조

| Name | Responsibility | Relevant ADs |
|------|---------------|--------------|
| **Interface Layer** | 외부 요청 수신 및 기본 처리<br>인증/인가 API 공개 인터페이스 정의<br>클라이언트와의 첫 번째 상호작용 지점 | UC-01~UC-05 (모든 인증 요청)<br>QAS-04 (Security), QAS-05 (Availability) |
| **Business Layer** | 인증/인가 핵심 비즈니스 로직 구현<br>토큰 관리, 사용자 등록, 권한 검증<br>이벤트 기반 상태 동기화 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-04 (Security), QAS-05 (Availability) |
| **System Interface Layer** | 외부 시스템과의 연동 인터페이스<br>데이터베이스, 메시지 브로커, 외부 서비스 연동<br>프로토콜 변환 및 보안 처리 | DD-01 (MSA), DD-02 (Event-Driven)<br>QAS-04 (Security), DD-03 (Database per Service) |

---

## 요소 수량 요약

| Layer | 인터페이스 수 | 컴포넌트 수 | 총 요소 수 |
|-------|--------------|------------|-----------|
| **Interface Layer** | 2 | 2 | 4 |
| **Business Layer** | 3 | 4 | 7 |
| **System Interface Layer** | 5 | 4 | 10 |
| **패키지** | - | - | 3 |
| **총계** | **10** | **10** | **24** |

---

## Architectural Drivers 적용 현황

### QAS-04 (Security - 민감 정보 접근 감사로그 및 접근권한 분리)
- **인증**: AuthenticationManager, IAuthenticationService, JwtTokenManager
- **인가**: AuthorizationManager, IAuthorizationService
- **접근권한 분리**: UserManagementController, IAuthManagementApi (관리자 전용)
- **민감정보 보호**: CreditCardVerificationClient, ICreditCardVerificationService (결제 정보 암호화)
- **감사로그**: 모든 인증 이벤트 RabbitMQ로 발행 (RabbitMQAdapter)

### QAS-05 (Availability - 주요 서비스 자동 복구 시간 보장)
- **데이터 영속성**: AuthJpaRepository, IAuthRepository, AuthDatabase
- **메시지 내구성**: RabbitMQAdapter, IMessagePublisherService, IMessageSubscriptionService
- **토큰 관리**: JwtTokenManager, ITokenService (토큰 만료 자동 처리)

### DD-01 (4-Layer Hybrid MSA)
- **인터페이스**: IAuthServiceApi, IAuthManagementApi
- **비즈니스 로직**: AuthenticationManager, AuthorizationManager, UserRegistrationManager
- **시스템 연동**: AuthJpaRepository, RabbitMQAdapter, JwtTokenManager

### DD-02 (Event-Driven Architecture)
- **이벤트 발행**: AuthenticationManager, AuthorizationManager, UserRegistrationManager → RabbitMQAdapter
- **이벤트 구독**: AuthEventConsumer ← RabbitMQAdapter
- **이벤트 유형**: 로그인 이벤트, 권한 변경 이벤트, 사용자 등록 이벤트

### DD-03 (Database per Service)
- **독립 데이터베이스**: AuthDatabase (다른 서비스와 완전 격리)
- **독점 접근**: AuthJpaRepository, IAuthRepository (Auth Service만 접근)

### 관련 UC 목록
- **UC-01**: 사용자 로그인 (IAuthServiceApi 활용)
- **UC-02**: 사용자 인증 (토큰 검증)
- **UC-03**: 권한 검증 (RBAC)
- **UC-04**: 사용자 등록 (신용카드 검증 포함)
- **UC-05**: 관리자 로그인 (IAuthManagementApi 활용)

---

## 결론

Authentication Service 컴포넌트 다이어그램의 모든 요소(24개)를 Layer별로 분류하여 역할과 관련 Architectural Drivers를 명확히 기술하였습니다.

- **Interface Layer**: 외부 API 인터페이스 (4개 요소)
- **Business Layer**: 인증/인가 비즈니스 로직 (7개 요소)
- **System Interface Layer**: 외부 시스템 연동 (10개 요소)

각 요소의 이름은 제공하는 역할을 명확히 나타내며, 기능, QA, Constraint 등 Architectural Driver 관점에서 기술되었습니다.
