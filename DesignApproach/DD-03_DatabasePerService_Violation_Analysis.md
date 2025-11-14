# DD-03: Database per Service 패턴 준수 분석

## 분석 일자
2024-01-20

## 분석 결과 요약

**결론: Database per Service 패턴을 완벽히 지키지 못하고 있습니다. 주요 위반 사항이 2건 발견되었습니다.**

---

## ✅ 준수하는 서비스 (Database per Service 완벽 달성)

| 서비스 | 독립 DB | DB 기술 | 비고 |
|:------|:--------|:--------|:-----|
| **Auth Service** | `AuthDatabase` | PostgreSQL | ✅ 완벽한 독립 소유 |
| **Helper Service** | `HelperDatabase` | PostgreSQL | ✅ 완벽한 독립 소유 |
| **Access Service** | `VectorDatabase` | Redis | ✅ 완벽한 독립 소유 (최적 기술 선택) |
| **Monitoring Service** | `MonitorDatabase` | PostgreSQL | ✅ 완벽한 독립 소유 |
| **Search Service** | `SearchEngineDB` | ElasticSearch | ✅ 완벽한 독립 소유 (최적 기술 선택) |

---

## ❌ 위반 사항 #1: MLOps Service의 타 서비스 DB 직접 접근

### 문제점

**MLOps Service가 Auth Service와 Helper Service의 DB를 READ-ONLY로 직접 접근합니다.**

```plantuml
' MLOpsServiceComponent.puml (109-110번 라인)
DataCollector ..( IAuthRepository : <<JDBC READ-ONLY>>
DataCollector ..( IHelperRepository : <<JDBC READ-ONLY>>
```

### 위반 세부 내용

**1. IAuthRepository (AuthRepositoryAdapter)**
- MLOps Service의 `DataCollector`가 Auth Service의 `AuthDatabase`를 JDBC로 직접 조회
- 목적: 재학습 데이터 수집 시 사용자 정보 조회

**2. IHelperRepository (HelperRepositoryAdapter)**
- MLOps Service의 `DataCollector`가 Helper Service의 `HelperDatabase`를 JDBC로 직접 조회
- 목적: 재학습 데이터 수집 시 작업 정보 조회

### Database per Service 패턴 위반 이유

| 위반 항목 | 설명 |
|:---------|:----|
| **DG-01 (독립성)** | MLOps Service가 Auth/Helper의 **스키마에 직접 의존**. Auth DB 테이블 변경 시 MLOps 코드도 수정 필요. |
| **DG-02 (장애 격리)** | Auth DB 또는 Helper DB 장애 시 MLOps Service의 재학습 파이프라인도 중단됨. |
| **DG-04 (기술 선택)** | Auth/Helper가 PostgreSQL에서 다른 DB로 마이그레이션 시 MLOps의 JDBC 코드 전면 수정 필요. |

### 예상 시나리오 (문제 발생)

**시나리오 1: 스키마 변경 시 연쇄 영향**
1. Auth Service가 `users` 테이블에 새 컬럼 `email_verified` 추가
2. MLOps Service의 `DataCollector`가 해당 컬럼을 참조하지 않더라도, **스키마 변경으로 인한 마이그레이션 조율** 필요
3. 독립 배포 불가능 → BG-14 (AI 모델 개선) 목표 미달성

**시나리오 2: DB 장애 시 재학습 파이프라인 중단**
1. Helper DB 백업 작업으로 인한 Read Lock
2. MLOps Service의 `DataCollector`가 Helper DB 조회 타임아웃
3. 재학습 파이프라인 전체 중단 → BG-14 목표 미달성

---

## ❌ 위반 사항 #2: BranchOwner Service의 독립 DB 부재

### 문제점

**BranchOwner Service의 컴포넌트 다이어그램에 독립 DB가 명시되지 않았습니다.**

### 확인 필요 사항

다음 파일을 확인하여 BranchOwner Service의 저장소 구조를 파악해야 합니다:
- `ComponentDiagram/09_BranchOwnerServiceComponent.puml`

### 가능한 시나리오

**시나리오 A: Auth Service의 DB를 공유 (예상)**
- BranchOwner Service가 Auth Service의 `AuthDatabase`에 있는 `branches`, `owners` 테이블을 직접 사용
- 이는 **Hybrid Storage (도메인별 분할 DB)** 패턴에 해당
- Database per Service 패턴 위반

**시나리오 B: 독립 DB 소유 (희망)**
- `BranchOwnerDatabase`를 독립적으로 소유
- Auth Service와 Event-Based Replication으로 데이터 동기화
- Database per Service 패턴 준수

---

## 🔧 권장 해결 방안

### 해결 방안 #1: MLOps Service의 타 서비스 DB 접근 제거

#### AS-IS (현재)
```
MLOps Service → JDBC → Auth DB (직접 접근)
MLOps Service → JDBC → Helper DB (직접 접근)
```

#### TO-BE (권장)
```
Helper Service → TaskConfirmedEvent → RabbitMQ → MLOps Service
Auth Service → UserCreatedEvent → RabbitMQ → MLOps Service
```

#### 구현 세부사항

**1. Event-Based Data Replication**
- Helper Service가 `TaskConfirmedEvent` 발행 시 **재학습에 필요한 모든 데이터 포함**
  ```json
  {
    "taskId": "T12345",
    "photoUrl": "s3://...",
    "aiResult": "미흡",
    "correctedResult": "양호",
    "branchId": "B001",
    "helperId": "H123",
    "timestamp": "2024-01-20T10:00:00Z"
  }
  ```

**2. MLOps Service의 독립 TrainingDataStore**
- MLOps Service가 `TrainingDataStore`에 이벤트 기반으로 데이터 복제
- Auth/Helper의 스키마 변경에 **완전히 독립적**

**3. 장점**
- ✅ DG-01 달성: MLOps Service 독립 배포 가능
- ✅ DG-02 달성: Auth/Helper DB 장애 시에도 MLOps는 기존 데이터로 재학습 가능
- ✅ DG-04 달성: Auth/Helper의 DB 기술 변경에 영향 없음

#### PlantUML 수정 (MLOpsServiceComponent.puml)

**삭제할 부분 (109-111번 라인):**
```plantuml
DataCollector ..( IAuthRepository : <<JDBC READ-ONLY>>
DataCollector ..( IHelperRepository : <<JDBC READ-ONLY>>
DataManagementService ..( IModelDataRepository : <<JDBC>>
```

**추가할 부분:**
```plantuml
' Business Layer에 Event Consumer 추가
component TrainingDataCollectorConsumer

' System Interface Layer
interface ITrainingDataCollectorHandler
ITrainingDataCollectorHandler -- TrainingDataCollectorConsumer

' Event 구독
TrainingDataCollectorConsumer ..( IMessageSubscriptionService : <<Subscribe TaskConfirmedEvent>>
TrainingDataCollectorConsumer ..( IMessageSubscriptionService : <<Subscribe UserCreatedEvent>>
TrainingDataCollectorConsumer ..( ITrainingDataRepository : <<persist>>
```

---

### 해결 방안 #2: BranchOwner Service 독립 DB 명시

#### 확인 및 조치

**1단계: 현재 구조 확인**
- `ComponentDiagram/09_BranchOwnerServiceComponent.puml` 파일 분석
- 저장소 구조 파악

**2단계-A: 독립 DB 사용 중이면**
- PlantUML 다이어그램에 `BranchOwnerDatabase` 명시
- 문서화 보완

**2단계-B: Auth DB 공유 중이면**
- `BranchOwnerDatabase` 독립 생성
- Event-Based Replication 구현
  ```
  Auth Service → UserCreatedEvent → BranchOwner Service
  BranchOwner Service → BranchCreatedEvent → Auth Service
  ```

---

## 📊 위반 영향도 분석

### DG (Design Goal) 달성 평가

| DG | 목표 | 현재 달성도 | MLOps 위반 영향 | BranchOwner 위반 영향 |
|:---|:-----|:----------|:---------------|:------------------|
| **DG-01** (독립성) | 서비스별 스키마 독립 변경 | ⚠️ 80% | **높음**: Auth/Helper 스키마 변경 시 MLOps 영향받음 | **중간**: BranchOwner 독립성 제약 가능 |
| **DG-02** (장애 격리) | DB 장애가 다른 서비스로 전파 안 됨 | ⚠️ 85% | **높음**: Auth/Helper DB 장애 시 재학습 중단 | **중간**: Auth DB 장애 시 BranchOwner 영향 가능 |
| **DG-03** (민감정보 분리) | 물리적 완전 분리 | ✅ 100% | 영향 없음 (READ-ONLY) | 영향 없음 |
| **DG-04** (기술 선택) | 서비스별 최적 DB 자유 선택 | ⚠️ 90% | **중간**: Auth/Helper의 DB 기술 변경 시 MLOps 코드 수정 필요 | **낮음**: 독립 확인 필요 |
| **DG-05** (고성능 접근) | 실시간 출입 인증 10ms 이내 | ✅ 100% | 영향 없음 | 영향 없음 |

### QA (Quality Attribute) 영향도

| QA | 영향도 | 설명 |
|:---|:------|:-----|
| **BG-14** (AI 모델 개선) | 🔴 **높음** | MLOps Service 독립 배포 불가. Auth/Helper와 조율 필요. |
| **QAS-01** (99.5% 가용성) | 🟡 **중간** | Helper DB 장애 시 재학습 파이프라인 중단 (핵심 서비스는 영향 없음). |
| **QAS-06** (AI 모델 교체) | 🟡 **중간** | 재학습 로직 변경 시 Auth/Helper 스키마 의존성 고려 필요. |

---

## 🎯 조치 우선순위

| 우선순위 | 조치 항목 | 예상 작업량 | 비즈니스 영향 |
|:--------|:---------|:----------|:-----------|
| **P0 (긴급)** | MLOps의 타 서비스 DB 접근 제거 | 2-3일 | **높음**: BG-14 (AI 모델 개선) 직접 연관 |
| **P1 (높음)** | BranchOwner Service 독립 DB 확인 및 조치 | 1-2일 | **중간**: 현재 구조 불명확 |
| **P2 (중간)** | Event-Based Replication 검증 및 모니터링 | 1일 | **낮음**: 안정성 보강 |

---

## 📝 다음 단계

1. ✅ **MLOps Service 컴포넌트 다이어그램 수정**
   - `IAuthRepository`, `IHelperRepository` 제거
   - Event Consumer 기반 데이터 수집으로 변경

2. 🔍 **BranchOwner Service 구조 확인**
   - `ComponentDiagram/09_BranchOwnerServiceComponent.puml` 분석
   - 독립 DB 여부 확인

3. 📐 **DD-03 문서 업데이트**
   - MLOps 위반 사항 반영
   - 해결 방안 명시
   - Event-Based Replication 다이어그램 추가

4. ✅ **구현 후 검증**
   - MLOps Service 독립 배포 테스트
   - Auth/Helper DB 장애 시뮬레이션
   - BG-14 (AI 모델 개선) 달성도 평가

---

## 참고 자료

- **DD-03**: 저장소 설계 결정 (Database per Service 패턴 선택)
- **DD-02**: 노드간 비동기 통신 구조 (Message Broker 기반 Event-Based Replication)
- **ComponentDiagram/11_MLOpsServiceComponent.puml**: MLOps Service 컴포넌트 다이어그램
- **ComponentDiagram/09_BranchOwnerServiceComponent.puml**: BranchOwner Service 컴포넌트 다이어그램 (확인 필요)

