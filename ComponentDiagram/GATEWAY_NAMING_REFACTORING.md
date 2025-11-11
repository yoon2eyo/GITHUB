# Gateway 용어 리팩토링 보고서

## 📋 리팩토링 배경

**문제점:**
- `Gateway` / `GatewayController` (HTTP API 라우팅) ↔ `GateController` (물리적 출입문 제어)
- 두 가지 완전히 다른 개념이 유사한 이름으로 혼동 발생

**해결 방안:**
- API 요청 라우팅 컴포넌트에 명확한 `Api` 접두사 추가
- 물리적 출입문 및 외부 시스템 어댑터는 기존 명칭 유지

---

## 🔄 변경 사항

### 1. 파일명 변경

| Before | After | 비고 |
|--------|-------|------|
| `07_GatewayComponent.puml` | `07_ApiGatewayComponent.puml` | ✅ 완료 |

### 2. 컴포넌트명 변경

#### 07_ApiGatewayComponent.puml

| Before | After | 역할 |
|--------|-------|------|
| `GatewayController` | `ApiGatewayController` | 외부 클라이언트 API 요청 진입점 |
| `GatewayManagementController` | `ApiGatewayManagementController` | API Gateway 관리 인터페이스 (Operations Center) |

#### 00_Overall_Architecture.puml

| Before | After | 역할 |
|--------|-------|------|
| `RequestRouter (API Gateway)` | `RequestRouter (ApiGateway)` | 전체 아키텍처 컴포넌트명 |
| `Gateway` (alias) | `ApiGateway` (alias) | PlantUML 다이어그램 alias |

### 3. 주석 및 설명 변경

```diff
- ' Client → Gateway
+ ' Client → ApiGateway

- ' Gateway → Services (Synchronous)
+ ' ApiGateway → Services (Synchronous)

- Customer --> Gateway : HTTPS /auth,/search
+ Customer --> ApiGateway : HTTPS /auth,/search
```

---

## 📚 명확한 용어 정의

### 용어 구분 규칙

| 용어 | 의미 | 사용 예시 | 프로토콜/기술 |
|------|------|-----------|--------------|
| **ApiGateway** | HTTP/HTTPS 요청 라우팅 시스템 | `ApiGatewayController`<br>`ApiGatewayManagementController` | HTTP/HTTPS<br>Service Discovery (Eureka) |
| **Gate** | 물리적 출입문 제어 | `GateController`<br>(Access Service 내부) | HTTPS → Equipment |
| **~Gateway** (suffix) | 외부 시스템 어댑터 패턴 | `EquipmentGateway`<br>`IPushNotificationGateway`<br>`FcmPushGateway` | HTTP/TCP<br>(Adapter Pattern) |

### 용어 사용 컨텍스트

#### 1️⃣ **ApiGateway** - HTTP 요청 라우팅
```
외부 클라이언트 (Customer/Helper/Manager App)
    ↓ HTTPS
ApiGatewayController ← 모든 비즈니스 API 요청
    ↓
RequestRouter → SecurityManager → LoadBalancer
    ↓
백엔드 마이크로서비스 (Auth, Search, Helper, etc.)
```

**관련 컴포넌트:**
- `ApiGatewayController`: 비즈니스 API 진입점
- `ApiGatewayManagementController`: 운영/관리 API 진입점
- `RequestRouter`: 요청 라우팅 로직
- `EurekaServiceRegistry`: 서비스 디스커버리

**관련 UC:** 모든 UC (UC-01 ~ UC-21)

---

#### 2️⃣ **Gate** - 물리적 출입문 제어
```
Branch Equipment (Camera)
    ↓ HTTPS (얼굴 사진)
AccessAuthorizationManager
    ↓
GateController ← 출입문 개폐 제어
    ↓ HTTPS (open/close command)
Branch Equipment (출입문)
```

**관련 컴포넌트:**
- `GateController` (Access Service): 출입문 제어 로직
- `EquipmentGateway` / `EquipmentGatewayAdapter`: 장비 통신 어댑터

**관련 UC:** UC-08 (얼굴 인식 출입)

---

#### 3️⃣ **~Gateway** (suffix) - 외부 시스템 어댑터
```
내부 서비스
    ↓
EquipmentGateway ← Adapter Pattern
    ↓ TCP/HTTPS
외부 장비 (카메라, 출입문, 센서)
```

**관련 컴포넌트:**
- `EquipmentGateway` / `EquipmentGatewayAdapter`: 지점 장비 통신
- `IPushNotificationGateway`: 푸시 알림 외부 서비스
- `FcmPushGateway`: Firebase Cloud Messaging 어댑터

**패턴:** Adapter Pattern, External System Integration

---

## 🎯 리팩토링 효과

### Before (혼동 발생)
```
❌ Gateway (API 라우팅? 출입문 제어?)
   - GatewayController
   - GateController
   ⚠️ 어떤 Gateway를 말하는가?
```

### After (명확한 구분)
```
✅ ApiGateway (HTTP 요청 라우팅) - 시스템 레벨
   - ApiGatewayController
   - ApiGatewayManagementController

✅ Gate (물리적 출입문) - 도메인 레벨
   - GateController

✅ ~Gateway (외부 어댑터) - 통합 레벨
   - EquipmentGateway
   - FcmPushGateway
```

---

## 📊 영향 범위 분석

### 수정된 파일 목록

| 파일 | 변경 내용 | 상태 |
|------|-----------|------|
| `07_GatewayComponent.puml` → `07_ApiGatewayComponent.puml` | 파일명 변경 | ✅ |
| `07_ApiGatewayComponent.puml` | 컴포넌트명 리팩토링 | ✅ |
| `00_Overall_Architecture.puml` | Gateway → ApiGateway | ✅ |
| `NAMING_REFACTORING_COMPLETE.md` | 문서 업데이트 | ✅ |
| `NAMING_CONVENTION_REFACTORING.md` | 문서 업데이트 | ✅ |
| `COMPONENT_REVIEW_SUMMARY.md` | 문서 업데이트 | ✅ |

### 영향 없는 컴포넌트 (확인 완료)

| 컴포넌트 | 이유 |
|----------|------|
| `10_RealTimeAccessServiceComponent.puml` | `GateController` 유지 (물리적 출입문) |
| `05_MonitoringServiceComponent.puml` | `EquipmentGateway` 유지 (어댑터 패턴) |
| `06_NotificationDispatcherComponent.puml` | `FcmPushGateway` 유지 (어댑터 패턴) |

---

## ✅ 체크리스트

- [x] 파일명 변경: `07_GatewayComponent.puml` → `07_ApiGatewayComponent.puml`
- [x] PlantUML 헤더 변경: `@startuml ApiGatewayComponent`
- [x] 컴포넌트명 변경: `GatewayController` → `ApiGatewayController`
- [x] 컴포넌트명 변경: `GatewayManagementController` → `ApiGatewayManagementController`
- [x] Overall Architecture 업데이트: `Gateway` → `ApiGateway`
- [x] 관련 문서 업데이트 (3개 파일)
- [x] 용어 정의 문서화 (본 문서)
- [x] 영향 범위 분석 완료
- [x] 기존 `Gate` / `~Gateway` 용어는 그대로 유지

---

## 📖 네이밍 가이드라인

### 향후 컴포넌트 네이밍 시 참고 사항

1. **API 라우팅 관련**
   - ✅ `Api` 접두사 사용: `ApiGateway`, `ApiGatewayController`
   - ❌ 단독 `Gateway` 사용 금지

2. **물리적 장치 제어**
   - ✅ 명사 사용: `Gate`, `Equipment`, `Device`
   - ✅ 제어 로직: `GateController`, `EquipmentController`

3. **외부 시스템 통합**
   - ✅ Adapter Pattern: `~Gateway` suffix
   - ✅ 예시: `EquipmentGateway`, `FcmPushGateway`, `PaymentGateway`

4. **혼동 방지 체크리스트**
   - [ ] 시스템 레벨 진입점? → `Api` 접두사
   - [ ] 도메인 객체/장치? → 구체적 명사
   - [ ] 외부 시스템 어댑터? → `~Gateway` suffix

---

## 📅 변경 이력

| 날짜 | 변경 내용 | 사유 |
|------|-----------|------|
| 2025-11-11 | Gateway → ApiGateway 리팩토링 | 출입문 제어(Gate)와 용어 혼동 방지 |

---

**리팩토링 완료: 용어 명확화를 통한 아키텍처 가독성 향상** ✅

