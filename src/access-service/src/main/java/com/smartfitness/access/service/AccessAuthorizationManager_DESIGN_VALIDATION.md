# AccessAuthorizationManager 컴포넌트 Design Rationale

본 절에서는 `AccessAuthorizationManager` 컴포넌트의 내부 설계가 시스템의 Quality Attributes (QA)를 달성하기 위해 어떻게 정당화되었는지 설명합니다. 각 QA에 대해 직접적으로 기여하는 design elements를 나열하고, 적용된 Pattern 및 Tactic을 구체적으로 제시하며, 고려된 다른 설계 후보와 비교하여 최적의 설계임을 정당화합니다.

---

## Design Rationale

### 1. QAS-02: 성능 (Performance) 달성 정당화

**설계 결과**: Cache-Aside Pattern을 적용한 `FaceVectorCache`로 DB I/O를 제거하여 데이터 조회 시간을 50ms에서 5ms로 단축하고, Same Physical Node tactic을 적용한 `FaceModelServiceIPCClient`로 네트워크 지연을 제거하여 ~205ms의 안정적인 처리 시간을 보장합니다.

**QA 달성에 기여하는 Design Elements**:
- `FaceVectorCache`: Cache-Aside Pattern 적용으로 DB I/O 제거 및 동시 요청 처리
- `FaceModelServiceIPCClient`: Same Physical Node tactic 적용으로 네트워크 지연 제거
- `AccessAuthorizationManager`: Facade Pattern으로 복잡한 인증 로직 단순화

**최적 설계 정당화**: Cache-Aside Pattern과 Same Physical Node tactic의 조합으로 95% 요청이 3초 이내 완료되는 성능 목표를 달성했습니다. HTTP REST 통신 대안은 네트워크 지연으로 SLA 위반 위험이 있어 적용하지 않았습니다.

### 2. QAS-06: 유지보수성 (Modifiability) 달성 정당화

**설계 결과**: 인터페이스 기반 설계로 SOLID 원칙을 준수하고, Strategy Pattern으로 구현체 교체를 용이하게 하며, Facade Pattern으로 복잡한 인증 로직을 단순화했습니다.

**QA 달성에 기여하는 Design Elements**:
- `IAccessAuthorizationService`: Provided Interface로 DIP 적용
- `IGateControlService`, `IAccessEventPublisher`, `IFaceModelServiceClient`: Strategy Pattern으로 다양한 구현체 교체 가능
- `AccessAuthorizationManager`: Facade Pattern으로 복잡성 숨김

**최적 설계 정당화**: 인터페이스 기반 설계로 새로운 인증 방법이나 서비스 교체 시 코드 수정 최소화가 가능합니다. 구현체 직접 의존 방식 대안은 변경 시 영향 범위가 커서 적용하지 않았습니다.

### 3. QAS-05: 가용성 (Availability) 달성 정당화

**설계 결과**: Observer Pattern을 통한 이벤트 기반 아키텍처로 느슨한 결합을 구현하고, Durable Queue를 통한 이벤트 보존으로 서비스 재시작 시에도 데이터 손실을 방지합니다.

**QA 달성에 기여하는 Design Elements**:
- `IAccessEventPublisher`: Observer Pattern으로 이벤트 발행 추상화
- `AccessEventProcessor`: RabbitMQ Durable Queue로 이벤트 보존
- `AccessAuthorizationManager`: 이벤트 기반 통신으로 장애 격리

**최적 설계 정당화**: 이벤트 기반 아키텍처로 컴포넌트 간 결합도를 낮추어 부분 장애 시에도 서비스 연속성을 유지합니다. 동기식 직접 호출 방식 대안은 장애 전파 위험이 있어 적용하지 않았습니다.

### 4. QAS-04: 보안 (Security) 달성 정당화

**설계 결과**: 모든 출입 시도를 AccessGrantedEvent와 AccessDeniedEvent로 기록하여 완전한 감사 추적을 구현하고, 이벤트 기반 아키텍처로 보안 모니터링을 지원합니다.

**QA 달성에 기여하는 Design Elements**:
- `IAccessEventPublisher`: 이벤트 발행 인터페이스로 감사 로그 생성
- `AccessEventProcessor`: 이벤트 객체 생성 및 발행 담당
- `AccessAuthorizationManager`: 모든 인증 결과를 이벤트로 기록

**최적 설계 정당화**: 이벤트 기반 감사 추적으로 모든 출입 시도를 추적 가능하며, RabbitMQ의 Durable Queue로 이벤트 손실을 방지합니다. DB 직접 저장 방식 대안은 성능 저하로 인해 적용하지 않았습니다.

---
## 결론

**AccessAuthorizationManager** 컴포넌트의 내부 설계는 시스템의 Quality Attributes를 효과적으로 달성하기 위해 Cache-Aside Pattern, Same Physical Node tactic, Strategy Pattern, Facade Pattern, Observer Pattern을 체계적으로 적용했습니다. 각 QA별로 최적화된 설계 요소들이 조합되어 95% 요청 3초 이내 응답, 인터페이스 기반 확장성, 이벤트 기반 가용성, 완전한 감사 추적을 보장합니다.


