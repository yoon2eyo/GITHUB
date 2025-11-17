# AccessAuthorizationManager 컴포넌트 시퀀스 다이어그램 설명

## 개요

본 문서는 `AccessAuthorizationManager` 컴포넌트의 인스턴스 수준에서 중요한 시나리오들을 시퀀스 다이어그램으로 기술하고, 각 시나리오의 동작을 상세히 설명합니다.

**범위**: `AccessAuthorizationManager` 컴포넌트가 **Required Interface를 호출하는 범위**로 제한됩니다.

**시작점**: `IAccessAuthorizationService` 인터페이스(Provided Interface)가 외부 Controller로부터 호출되는 방식으로 시작됩니다.

**종료점**: Required Interface 호출 및 응답 수신까지를 다룹니다. Required Interface의 구현체 내부 동작이나 외부 시스템과의 상호작용은 포함하지 않습니다.

---

## 시나리오 1: 안면인식 출입 인증 (성공 시나리오)

### 시나리오 개요
지점 설비에서 촬영한 안면 사진을 기반으로 사용자를 식별하고 출입을 허가하는 메인 성공 시나리오입니다. (UC-07)

### 시작점
- **호출자**: `AccessControlController` (Interface Layer)
- **호출 메서드**: `IAccessAuthorizationService.authorizeFaceAccess(branchId, equipmentId, facePhoto)`
- **트리거**: 지점 설비가 POST `/access/face` 요청 전송

### 상세 동작 흐름

#### Step 1: Provided Interface 호출
```
AccessControlController → AccessAuthorizationManager: authorizeFaceAccess(...)
```
- 외부 Controller가 `IAccessAuthorizationService` 인터페이스를 통해 컴포넌트 호출
- **설계 원칙**: Dependency Inversion Principle (DIP) - 인터페이스에 의존

#### Step 2: 안면 사진 바이트 변환
```
AccessAuthorizationManager → AccessAuthorizationManager: facePhoto.getBytes()
```
- `MultipartFile`을 `byte[]`로 변환
- **내부 연산**: 컴포넌트 내부에서 처리되는 private 메서드 호출

#### Step 3: Required Interface 호출 - 캐시에서 벡터 조회
```
AccessAuthorizationManager → FaceVectorCache: getActiveVector(branchId)
FaceVectorCache → AccessAuthorizationManager: FaceVectorDto
```
- **Required Interface**: `FaceVectorCache`
- **Operation**: `getActiveVector(branchId)`
- **전술**: Data Pre-Fetching (DD-05)
- **캐시 히트율**: >90%
- **응답 시간**: ~5ms (캐시 히트 시)
- **효과**: DB I/O를 Hot Path에서 제거하여 성능 향상

**참고**: `FaceVectorCache`의 내부 구현(DB 조회, 캐시 업데이트 등)은 본 다이어그램 범위를 벗어나므로 표시하지 않습니다.

#### Step 4: Required Interface 호출 - FaceModel Service IPC
```
AccessAuthorizationManager → IFaceModelServiceClient: calculateSimilarity(photoBytes, cachedVector)
IFaceModelServiceClient → AccessAuthorizationManager: SimilarityResultDto
```
- **Required Interface**: `IFaceModelServiceClient`
- **Operation**: `calculateSimilarity(byte[] requestedPhoto, FaceVectorDto storedVector)`
- **전술**: Same Physical Node (DD-05)
- **통신 방식**: IPC/gRPC (동일 물리 노드)
- **응답 시간**: ~205ms (Pipeline Optimization 포함)
- **효과**: HTTP REST 대비 10-20ms 지연 감소

**참고**: `IFaceModelServiceClient`의 구현체(`FaceModelServiceIPCClient`) 내부 동작이나 FaceModel Service와의 통신 세부사항은 본 다이어그램 범위를 벗어나므로 표시하지 않습니다.

#### Step 5: 출입 결정 (Internal Logic)
```
AccessAuthorizationManager → AccessAuthorizationManager: checkSimilarity(similarity, threshold)
```
- **내부 연산**: 유사도 점수와 임계값(0.85) 비교
- **판단 기준**: `isMatch == true && similarityScore >= 0.85`
- **결과**: GRANTED 또는 DENIED

#### Step 6: Required Interface 호출 - 게이트 개방
```
AccessAuthorizationManager → IGateControlService: openGate(equipmentId)
IGateControlService → AccessAuthorizationManager: true
```
- **Required Interface**: `IGateControlService`
- **Operation**: `openGate(String equipmentId)`
- **UC-22**: 게이트 개방 실행
- **QAS-02 요구사항**: 판독 성공 후 1초 이내 게이트 개방

**참고**: `IGateControlService`의 구현체(`GateController`) 내부 동작이나 Equipment Gateway와의 통신 세부사항은 본 다이어그램 범위를 벗어나므로 표시하지 않습니다.

#### Step 7: Required Interface 호출 - 이벤트 발행
```
AccessAuthorizationManager → IAccessEventPublisher: publishAccessGranted(AccessGrantedEvent)
IAccessEventPublisher → AccessAuthorizationManager: void
```
- **Required Interface**: `IAccessEventPublisher`
- **Operation**: `publishAccessGranted(AccessGrantedEvent event)`
- **전술**: Event-Based Architecture (DD-02)
- **이벤트 타입**: `AccessGrantedEvent`
- **이벤트 내용**: userId, branchId, equipmentId, similarityScore, timestamp
- **반환값**: `void` (비동기 처리)
- **목적**: 
  - 감사 추적 (QAS-04: Security)
  - MLOps 재학습 데이터 수집
  - 통계 및 모니터링

**참고**: `IAccessEventPublisher`의 구현체(`AccessEventProcessor`) 내부 동작이나 RabbitMQ와의 통신 세부사항은 본 다이어그램 범위를 벗어나므로 표시하지 않습니다.

#### Step 8: 응답 반환
```
AccessAuthorizationManager → AccessControlController: Map{result: "GRANTED", userId, similarityScore, processingTimeMs}
```
- **응답 내용**:
  - `result`: "GRANTED"
  - `userId`: 인증된 사용자 ID
  - `similarityScore`: 유사도 점수 (0.0 ~ 1.0)
  - `processingTimeMs`: 처리 소요 시간

**성능 검증 (QAS-02)**:
- 총 처리 시간: ~320ms
- 목표: 95% 요청이 3초 이내
- **✓ 목표 달성** (320ms << 3000ms)

---

## 시나리오 2: 안면인식 출입 인증 (실패 시나리오)

### 시나리오 개요
안면인식 결과 유사도 점수가 임계값 미만이거나 매칭되지 않은 경우 출입을 거부하는 시나리오입니다.

### 시작점
- **호출자**: `AccessControlController`
- **호출 메서드**: `IAccessAuthorizationService.authorizeFaceAccess(...)`

### 상세 동작 흐름

#### Step 1-4: 안면인식 처리 (시나리오 1과 동일)
- 안면 사진 수신
- 캐시에서 벡터 조회
- FaceModel Service IPC 호출
- 유사도 계산

#### Step 5: 출입 거부 결정
```
AccessAuthorizationManager → AccessAuthorizationManager: checkSimilarity(similarity, threshold)
```
- **판단 결과**: DENIED
- **이유**: 
  - `similarityScore < 0.85` (임계값 미만)
  - 또는 `isMatch == false`

#### Step 6: 게이트 개방 없음
- **중요**: 출입 거부 시 게이트는 개방하지 않음
- 게이트는 닫힌 상태 유지

#### Step 7: 거부 이벤트 발행
```
AccessAuthorizationManager → IAccessEventPublisher: publishAccessDenied(AccessDeniedEvent)
IAccessEventPublisher → AccessEventProcessor: publishAccessDenied(event)
AccessEventProcessor → RabbitMQ Broker: <<AMQP>> AccessDeniedEvent
```
- **이벤트 타입**: `AccessDeniedEvent`
- **이벤트 내용**: branchId, equipmentId, denialReason, similarityScore, timestamp
- **목적**: 
  - **QAS-04 (Security)**: 감사 추적을 위한 모든 출입 시도 기록
  - 보안 모니터링 및 이상 패턴 분석

#### Step 8: 거부 응답 반환
```
AccessAuthorizationManager → AccessControlController: Map{result: "DENIED", reason: "NO_MATCH", similarityScore: 0.70}
```

---

## 시나리오 3: QR 코드 출입 인증

### 시나리오 개요
안면인식 실패 시 대체 수단으로 QR 코드를 통한 수동 출입 인증을 처리하는 시나리오입니다. (UC-08)

### 시작점
- **호출자**: `QRAccessController` (Interface Layer)
- **호출 메서드**: `IAccessAuthorizationService.authorizeQRAccess(branchId, equipmentId, qrCode)`
- **트리거**: 고객이 앱의 QR 코드를 스캔하여 POST `/access/qr` 요청 전송

### 상세 동작 흐름

#### Step 1: QR 코드 검증
```
AccessAuthorizationManager → AccessAuthorizationManager: validateQRCode(qrCode)
```
- **내부 연산**: QR 코드 형식 검증
- **검증 기준**: `qrCode != null && qrCode.startsWith("QR-")`

#### Step 2: 사용자 ID 추출 (Valid QR Code)
```
AccessAuthorizationManager → AccessAuthorizationManager: extractUserIdFromQR(qrCode)
```
- **내부 연산**: QR 코드에서 사용자 ID 추출
- **예시**: "QR-USER-123" → "USER-123"

#### Step 3: 게이트 개방
```
AccessAuthorizationManager → IGateControlService: openGate(equipmentId)
```
- QR 코드 인증 성공 시 즉시 게이트 개방
- 안면인식 단계 없이 빠른 처리

#### Step 4: 이벤트 발행
```
AccessAuthorizationManager → IAccessEventPublisher: publishAccessGranted(AccessGrantedEvent{method: "QR"})
```
- **이벤트 내용**: 인증 방법이 "QR"로 표시됨
- 감사 추적을 위해 모든 출입 방법 기록

#### Invalid QR Code 처리
- QR 코드 형식이 잘못된 경우
- 게이트 개방 없음
- `AccessDeniedEvent` 발행 (reason: "INVALID_QR")

---

## 시나리오 4: 게이트 수동 제어

### 시나리오 개요
관리자가 수동으로 게이트를 개방/폐쇄하는 시나리오입니다. (관리 목적)

### 시작점
- **호출자**: `AccessControlController`
- **호출 메서드**: `IAccessAuthorizationService.controlGate(equipmentId, action)`
- **트리거**: 관리자가 POST `/access/gate/{equipmentId}/control?action=OPEN` 요청 전송

### 상세 동작 흐름

#### Step 1: 액션 검증
```
AccessAuthorizationManager → AccessAuthorizationManager: validateAction(action)
```
- **내부 연산**: 액션 유효성 검증
- **유효한 액션**: "OPEN", "CLOSE"
- **무효한 액션**: 그 외 모든 값 → `false` 반환

#### Step 2: 게이트 제어 실행
```
AccessAuthorizationManager → IGateControlService: openGate(equipmentId) 또는 closeGate(equipmentId)
IGateControlService → GateController: openGate(...) 또는 closeGate(...)
GateController → Equipment Gateway: <<HTTPS>> POST /equipment/gate/open 또는 /close
Equipment Gateway → GateController: {status: "success"}
GateController → IGateControlService: true
IGateControlService → AccessAuthorizationManager: true
```

**액션별 처리**:
- **"OPEN"**: 게이트 개방 명령 전송
- **"CLOSE"**: 게이트 폐쇄 명령 전송
- **Invalid Action**: `false` 반환

---

## Required Interface 요약

### AccessAuthorizationManager의 Required Interface

| Required Interface | Operation | 목적 | 반환 타입 |
|-------------------|-----------|------|----------|
| `FaceVectorCache` | `getActiveVector(branchId)` | 안면 벡터 데이터 캐시 조회 (DD-05: Data Pre-Fetching) | `FaceVectorDto` |
| `IFaceModelServiceClient` | `calculateSimilarity(photoBytes, cachedVector)` | 안면 유사도 계산 (DD-05: IPC/gRPC) | `SimilarityResultDto` |
| `IGateControlService` | `openGate(equipmentId)`<br>`closeGate(equipmentId)` | 게이트 제어 명령 전송 | `boolean` |
| `IAccessEventPublisher` | `publishAccessGranted(event)`<br>`publishAccessDenied(event)` | 출입 이벤트 발행 (DD-02: Event-Driven) | `void` |

### 인터페이스 기반 설계
- 모든 의존성이 Required Interface를 통해 주입됨
- **SOLID 원칙**: Dependency Inversion Principle (DIP) 준수
- 구현체 변경 시 `AccessAuthorizationManager` 컴포넌트 수정 불필요
- 컴포넌트의 책임 범위를 Required Interface 호출로 명확히 제한

---

## 성능 특성 (QAS-02)

### 시나리오 1 (안면인식 성공) 성능 분석

| 단계 | 소요 시간 | 비고 |
|------|----------|------|
| 캐시 조회 | ~5ms | 90%+ 히트율 |
| IPC 호출 | ~205ms | Pipeline Optimization 포함 |
| 게이트 제어 | ~100ms | HTTPS 통신 |
| 이벤트 발행 | ~10ms | 비동기 처리 |
| **총계** | **~320ms** | **목표 3초의 10.7%** |

### 성능 최적화 전술 (DD-05)

1. **Data Pre-Fetching**
   - 캐시 히트율 >90%로 DB I/O 제거
   - 응답 시간: 50ms → 5ms (90% 감소)

2. **Pipeline Optimization**
   - 병렬 특징점 추출
   - 응답 시간: 405ms → 205ms (49% 감소)

3. **Same Physical Node**
   - IPC/gRPC 통신으로 네트워크 지연 제거
   - HTTP REST 대비 10-20ms 감소

---

## 설계 원칙 적용

### SOLID 원칙
- **Single Responsibility**: 출입 인증 결정만 담당
- **Dependency Inversion**: 인터페이스에 의존
- **Open/Closed**: 인터페이스 확장으로 새로운 인증 방법 추가 가능

### GRASP 원칙
- **Information Expert**: 출입 인증 결정에 필요한 정보를 가진 전문가
- **Low Coupling**: 인터페이스를 통한 느슨한 결합
- **High Cohesion**: 관련된 메서드들이 한 클래스에 모여있음

### Design Pattern
- **Strategy Pattern**: `IFaceModelServiceClient`, `IGateControlService` 등
- **Adapter Pattern**: `FaceModelServiceIPCClient`, `GateController` 등
- **Observer Pattern**: 이벤트 기반 Pub/Sub 구조

---

## 다이어그램 범위 제한 사항

본 시퀀스 다이어그램은 `AccessAuthorizationManager` 컴포넌트가 Required Interface를 호출하는 범위로 제한됩니다.

### 포함되지 않는 내용
- Required Interface의 구현체 내부 동작
  - `FaceModelServiceIPCClient`의 IPC/gRPC 통신 세부사항
  - `GateController`의 Equipment Gateway 통신 세부사항
  - `AccessEventProcessor`의 RabbitMQ 통신 세부사항
- 외부 시스템과의 상호작용
  - FaceModel Service 내부 처리
  - Equipment Gateway 응답 처리
  - RabbitMQ 메시지 브로커 동작

### 포함되는 내용
- `AccessAuthorizationManager` 컴포넌트의 내부 연산
- Required Interface 호출 및 응답 수신
- Provided Interface를 통한 외부 호출 수신 및 응답 반환

이러한 범위 제한을 통해 컴포넌트의 책임과 의존성을 명확히 표현할 수 있습니다.

## 참고 문서

- **Component Diagram**: `10_RealTimeAccessServiceComponent.puml`
- **UC-07**: 안면인식 출입 인증
- **UC-08**: QR코드 수동 출입
- **DD-05**: 안면인식 판독 후 게이트 개방 구조 설계 결정
- **QAS-02**: 신속하고 정확한 안면인식 출입 인증

