# AccessAuthorizationManager 컴포넌트 명세서

## 개요

**AccessAuthorizationManager**는 지점 설비에서 전송된 안면 사진을 기반으로 사용자를 식별하고 출입 권한을 검증하여 게이트 개방을 제어하는 핵심 비즈니스 로직 컴포넌트입니다. 안면인식 실패 시 대체 수단인 QR 코드 인증도 처리하며, 모든 출입 시도를 이벤트로 발행하여 감사 추적을 지원합니다.

---

## 컴포넌트 기능 요구사항

### Provided Interface: `IAccessAuthorizationService`

이 컴포넌트는 다음 세 가지 핵심 오퍼레이션을 제공합니다:

#### 1. `authorizeFaceAccess(String branchId, String equipmentId, MultipartFile facePhoto)`

**기능**: 안면인식 기반 출입 인증 처리 (UC-07)

**처리 흐름**:
1. **안면 사진 수신**: 지점 설비에서 전송된 안면 사진(`facePhoto`)을 바이트 배열로 변환
2. **벡터 캐시 조회**: `FaceVectorCache`를 통해 해당 지점의 활성 안면 벡터 데이터 조회 (DD-05: Data Pre-Fetching)
   - 캐시 히트율 >90% 목표
   - 캐시 미스 시 DB 조회 후 캐시 업데이트
3. **유사도 계산**: `IFaceModelServiceClient`를 통한 IPC/gRPC 호출로 안면 특징 벡터 비교 (DD-05: Same Physical Node)
   - Pipeline Optimization 적용으로 평균 ~205ms 처리 시간
   - 유사도 임계값: 0.85 이상
4. **출입 결정**: 유사도 점수 기반 허가/거부 판단
   - **허가(GRANTED)**: `IGateControlService`를 통해 게이트 개방 명령 전송
   - **거부(DENIED)**: 출입 거부 응답 반환
5. **이벤트 발행**: `IAccessEventPublisher`를 통해 `AccessGrantedEvent` 또는 `AccessDeniedEvent` 발행
   - RabbitMQ를 통한 비동기 이벤트 전달 (DD-02: Event-Based Architecture)
   - 감사 추적 및 후속 처리(MLOps 재학습 데이터 수집 등)를 위한 이벤트 기록

**반환값**: `Map<String, Object>`
- `result`: "GRANTED" 또는 "DENIED"
- `userId`: 인증된 사용자 ID (허가 시)
- `similarityScore`: 유사도 점수 (0.0 ~ 1.0)
- `processingTimeMs`: 처리 소요 시간 (밀리초)

#### 2. `authorizeQRAccess(String branchId, String equipmentId, String qrCode)`

**기능**: QR 코드 기반 수동 출입 인증 처리 (UC-08)

**처리 흐름**:
1. **QR 코드 검증**: QR 코드 형식 및 유효성 검증
2. **사용자 식별**: QR 코드에서 사용자 ID 추출
3. **게이트 개방**: `IGateControlService`를 통해 게이트 개방 명령 전송
4. **이벤트 발행**: `IAccessEventPublisher`를 통해 `AccessGrantedEvent` 발행 (인증 방법: "QR")

**반환값**: `Map<String, Object>`
- `result`: "GRANTED" 또는 "DENIED"
- `userId`: 인증된 사용자 ID
- `method`: "QR"

**사용 시나리오**: 안면인식 실패 시 고객이 앱의 QR 코드를 스캔하여 대체 인증 수행

#### 3. `controlGate(String equipmentId, String action)`

**기능**: 게이트 수동 제어 (관리 목적)

**처리 흐름**:
1. **액션 검증**: "OPEN" 또는 "CLOSE" 액션 확인
2. **게이트 제어**: `IGateControlService`를 통해 해당 액션 실행

**반환값**: `boolean` (성공 여부)

**사용 시나리오**: 관리자가 수동으로 게이트를 개방/폐쇄할 때 사용

---

## 컴포넌트 품질 요구사항

### 1. 성능 (Performance) - QAS-02

#### 1.1 응답 시간 요구사항
- **목표**: 안면인식 출입 인증의 95%가 3초 이내 완료
- **99% 목표**: 99%가 5초 이내 완료
- **게이트 개방**: 판독 성공 후 1초 이내 게이트 개방 명령 전송

#### 1.2 성능 최적화 전술 (DD-05)

**Data Pre-Fetching (데이터 사전 로딩)**:
- `FaceVectorCache`를 통한 인메모리 캐싱
- 시스템 시작 시 상위 10,000개 활성 안면 벡터 사전 로드 (~500MB)
- 캐시 히트율 >90% 유지
- DB I/O를 Hot Path에서 제거하여 데이터 조회 시간 50ms → 5ms로 단축

**Same Physical Node (동일 물리 노드)**:
- `FaceModelService`와 동일 물리 노드에 배치
- IPC/gRPC 통신으로 네트워크 지연 제거 (HTTP 대비 10-20ms 감소)
- 평균 IPC 호출 시간: ~205ms (Pipeline Optimization 포함)

**Pipeline Optimization (파이프라인 최적화)**:
- `VectorComparisonEngine`에서 CompletableFuture 기반 병렬 처리
- 특징 추출 단계를 병렬화하여 순차 처리 대비 49% 지연 감소
- 순차: 405ms → 병렬: 205ms

#### 1.3 처리량 요구사항
- **피크 부하**: 순간 최대 20 TPS (100개 지점에서 5초 이내 동시 출입 발생 시)
- **동시성 처리**: 멀티스레드 기반 요청 병렬 처리
- **자원 관리**: Thread Pool을 통한 동시 요청 수 제한 및 부하 분산

#### 1.4 성능 측정 결과
- **평균 응답 시간**: ~320ms (GRANTED), ~220ms (DENIED)
- **목표 대비 달성률**: 목표 3초의 10.7% (GRANTED), 7.3% (DENIED)
- **성능 구성 요소**:
  - 캐시 조회: ~5ms (1.6%)
  - IPC 호출: ~205ms (64.1%)
  - 게이트 제어: ~100ms (31.2%)
  - 이벤트 발행: ~10ms (3.1%, 비동기)

### 2. 유지보수성 (Modifiability) - QAS-06

#### 2.1 인터페이스 기반 설계
- `IAccessAuthorizationService` 인터페이스 구현으로 의존성 역전 원칙(DIP) 적용
- 의존 컴포넌트들(`IFaceModelServiceClient`, `IGateControlService`, `IAccessEventPublisher` 등)을 인터페이스로 추상화
- 구현체 변경 시 컴포넌트 수정 최소화

#### 2.2 단일 책임 원칙 (SRP)
- 출입 인증 로직만 담당
- 게이트 제어, 이벤트 발행, 벡터 조회는 각각 전용 컴포넌트에 위임
- 컴포넌트 간 느슨한 결합 유지

#### 2.3 확장성
- 새로운 인증 방법 추가 시 인터페이스 확장으로 대응 가능
- 이벤트 기반 아키텍처로 후속 처리 로직 추가 시 컴포넌트 수정 불필요
- Strategy Pattern 적용으로 다양한 인증 전략 교체 가능

#### 2.4 이벤트 기반 아키텍처 (DD-02)
- `IAccessEventPublisher`를 통한 이벤트 발행
- RabbitMQ를 통한 비동기 이벤트 전달
- 후속 처리 로직 추가 시 컴포넌트 수정 불필요
- 느슨한 결합으로 시스템 확장성 확보

---

## 참고 문서

- **DD-05**: 안면인식 판독 후 게이트 개방 구조 설계 결정
- **DD-02**: 노드간 비동기 통신 구조 설계 결정
- **QAS-02**: 신속하고 정확한 안면인식 출입 인증
- **QAS-06**: 수정용이성
- **UC-07**: 안면인식 출입 인증
- **UC-08**: QR코드 수동 출입
- **Component Diagram**: `10_RealTimeAccessServiceComponent.puml`
