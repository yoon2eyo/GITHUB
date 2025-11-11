# Face Model Service 구조 분석 및 의사결정 가이드

**분석 일시:** 2025-11-11  
**주제:** Face Model Service - 독립 vs 통합 구조 결정  
**현재 상태:** ⚠️ 구조 불일치 (다이어그램 vs 소스코드)

---

## 1. 현재 상황 분석

### 1.1 다이어그램 표현 (ComponentDiagram/11_FaceModelServiceComponent.puml)
```
FaceModelServiceComponent (독립 서비스)
├── Interface Layer
│   └── IFaceModelApi, IModelTrainingApi
├── Business Layer
│   ├── FaceModelManager
│   ├── ModelTrainer
│   ├── ModelValidator
│   └── ModelDistributor
└── System Interface Layer
    ├── IModelRepository
    ├── ITrainingDataRepository
    ├── IMLOpsService
    └── IMessagePublisherService
```

**의도:** 완전히 독립적인 마이크로서비스

### 1.2 실제 코드 구조 (RealTime_AccessService/facemodel)
```
RealTime_AccessService/
├── access/                         (Access Control Module)
│   ├── AccessAuthorizationManager
│   ├── GateController
│   └── FaceVectorCache
└── facemodel/                      (Face Model Module) <- 같은 서비스 내!
    ├── ports/
    │   ├── IFaceModelService
    │   ├── IModelManagementPort
    │   └── IFaceModelEventHandler
    ├── internal/
    │   ├── logic/
    │   │   ├── VectorComparisonEngine (implements IFaceModelService)
    │   │   └── ModelLifecycleManager (implements IModelManagementPort)
    │   └── FaceModelProcessor (implements IFaceModelEventHandler)
    ├── model/
    │   ├── FaceModelEvent.java
    │   ├── FaceVerificationEvent.java
    │   └── FaceModelUpdatedEvent.java
    └── events/
        ├── FaceVerificationEvent.java (IDomainEvent)
        └── FaceModelUpdateEvent.java (IDomainEvent)
```

**현실:** RealTime_AccessService 내의 모듈로 구현

---

## 2. 현재 코드 분석

### 2.1 Face Model 컴포넌트 책임
```java
// VectorComparisonEngine (얼굴 벡터 비교)
- calculateSimilarityScore(requestedVector, storedVector)
- extractFeatures(faceVector)
- cosineSimilarity(features1, features2)
- applyThresholds(score, model)

// ModelLifecycleManager (모델 생명주기)
- loadNewModel(modelBinary)
- rollbackToPreviousModel()
- activeModel (AtomicReference)
- modelHistory (ConcurrentHashMap)

// FaceModelProcessor (이벤트 처리)
- handleEvent(IDomainEvent)
- register() / unregister()
```

### 2.2 Access Service와의 연계
```java
// AccessAuthorizationManager에서 사용
public class AccessAuthorizationManager implements IAccessServiceApi {
    private final IFaceModelService faceModelService;
    
    // 얼굴 인증 시 사용
    verifyFaceVector(userId, liveVector)
        -> faceModelService.calculateSimilarityScore(...)
}
```

---

## 3. 의사결정: 두 가지 옵션

### Option A: ✅ 독립 서비스로 분리 (권장)

#### 장점
- ✅ 마이크로서비스 원칙 준수
- ✅ 독립적인 배포/스케일링
- ✅ 모델 학습/배포를 독립적으로 관리
- ✅ 다이어그램과 코드 일치
- ✅ 다른 서비스에서 Face 모델 재사용 가능
- ✅ 컴포넌트 간 느슨한 결합

#### 단점
- ❌ 새로운 마이크로서비스 운영 필요
- ❌ 서비스 간 통신 오버헤드
- ❌ 배포 복잡도 증가
- ❌ 개발/테스트 복잡도 증가

#### 요구 구조
```
SRC/
├── FaceModelService/              (NEW - 독립 서비스)
│   ├── src/main/java/com/smartfitness/
│   │   ├── facemodel/
│   │   │   ├── ports/
│   │   │   ├── internal/
│   │   │   ├── model/
│   │   │   └── FaceModelServiceComponent.java
│   │   ├── persistence/
│   │   └── mlo/
│   └── pom.xml
│
├── RealTime_AccessService/        (MODIFIED - 의존성 제거)
│   └── facemodel/                 (DELETED)
```

---

### Option B: 통합 모듈로 유지 (현재 상태)

#### 장점
- ✅ 배포 단순화
- ✅ 서비스 간 통신 오버헤드 없음
- ✅ 개발/테스트 단순화
- ✅ 강한 일관성 보장

#### 단점
- ❌ 다이어그램과 코드 불일치
- ❌ 모듈 크기 증가 (Single Service에 두 개의 책임)
- ❌ 다른 서비스에서 Face 모델 재사용 불가
- ❌ 스케일링 시 전체 Access Service 확장 필요

#### 변경 사항
```
다이어그램 수정:
- FaceModelServiceComponent.puml 삭제
- RealTime_AccessServiceComponent.puml에 FaceModel 모듈로 추가
- Overall_Architecture.puml에서 FaceModel 제거
```

---

## 4. 비교 분석표

| 기준 | Option A (독립) | Option B (통합) |
|------|-----------------|-----------------|
| **아키텍처 순수성** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **운영 복잡도** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **배포 복잡도** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **성능 (레이턴시)** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **재사용성** | ⭐⭐⭐⭐⭐ | ⭐ |
| **코드 일관성** | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **개발 용이성** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **테스트 용이성** | ⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## 5. 권장 사항: **Option A (독립 서비스)** 🎯

### 이유:
1. **아키텍처 정당성**: 다이어그램이 이미 독립 서비스로 표현됨
2. **비즈니스 가치**: 모델 학습/배포를 독립적으로 관리 가능
3. **재사용성**: MLOpsService, 다른 인증 시스템에서 재사용 가능
4. **스케일링**: ML 모델은 독립적인 스케일링 전략 필요
5. **팀 구성**: Data Science 팀이 별도로 관리 가능

### 단, 다음을 전제 조건으로 함:
- ✅ 마이크로서비스 아키텍처 확정
- ✅ 서비스 간 통신 인프라 준비
- ✅ 개발팀이 분산 시스템 경험 보유
- ✅ 모니터링/로깅 시스템 준비

---

## 6. 구현 계획 (Option A 선택 시)

### Phase 1: 준비 (1-2일)
```
1. SRC/FaceModelService 디렉토리 구조 생성
2. 의존성 정의 (pom.xml 작성)
3. 인프라 설정 (데이터베이스, 캐시)
```

### Phase 2: 코드 마이그레이션 (3-5일)
```
1. RealTime_AccessService/facemodel → FaceModelService로 이동
2. Access → FaceModelService 인터페이스로 변경
3. 메시지 브로커 통신 구현
4. 에러 핸들링 추가
```

### Phase 3: 통합 (2-3일)
```
1. 서비스 간 통신 테스트
2. 폴백 메커니즘 구현
3. 모니터링 설정
```

### Phase 4: 배포 (1-2일)
```
1. 스테이징 환경 배포
2. 성능 테스트
3. 프로덕션 배포
```

### 예상 기간: **7-12일**

---

## 7. 구현 계획 (Option B 선택 시)

### Phase 1: 다이어그램 수정 (1일)
```
1. FaceModelServiceComponent.puml 삭제
2. RealTime_AccessServiceComponent.puml 수정
   - facemodel 모듈 포함으로 표현
3. Overall_Architecture.puml 수정
   - FaceModel 서비스 제거
```

### Phase 2: 문서 업데이트 (1일)
```
1. 아키텍처 문서 수정
2. 배포 가이드 수정
3. 개발 가이드 수정
```

### 예상 기간: **2일**

---

## 8. 최종 의사결정 프로세스

```
1. 아키텍처 리뷰 미팅
   └─ 마이크로서비스 전략 확인
   └─ 기술 부채 평가
   └─ 팀 역량 평가

2. 비즈니스 임팩트 분석
   └─ 모델 학습 속도
   └─ 배포 빈도
   └─ 재사용 기회

3. 기술적 리스크 평가
   └─ 서비스 간 통신 성능
   └─ 네트워크 신뢰성
   └─ 모니터링 복잡도

4. 최종 결정
   └─ Option A 또는 Option B 선택
   └─ 이행 계획 수립
```

---

## 9. 추천 의사결정 플로우차트

```
┌─────────────────────────────────────────┐
│ 마이크로서비스 아키텍처를               │
│ 완전히 수용할 준비가 되어있는가?       │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┴─────────┐
        │YES               │NO
        ↓                  ↓
    Option A           Option B
    (독립)            (통합)
    
    ├─ 장기적 이점     ├─ 단기적 효율
    ├─ 높은 복잡도     └─ 낮은 복잡도
    └─ 권장
```

---

## 10. 체크리스트: Option A 선택 시 필수 항목

### 인프라
- [ ] gRPC 또는 REST API 게이트웨이 준비
- [ ] 서비스 디스커버리 (Eureka/Consul)
- [ ] 분산 로깅 (ELK/Splunk)
- [ ] 분산 트레이싱 (Jaeger/Zipkin)
- [ ] 모니터링 (Prometheus/Grafana)

### 개발
- [ ] 서비스 간 통신 테스트 프레임워크
- [ ] 계약 테스트 (Contract Testing)
- [ ] 폴백 메커니즘
- [ ] Circuit Breaker 패턴

### 운영
- [ ] 서비스 배포 파이프라인
- [ ] 롤백 전략
- [ ] 알람 규칙
- [ ] SLA 정의

---

## 11. 결론

### 현재 상태
- ⚠️ 다이어그램: FaceModelService (독립)
- ⚠️ 코드: RealTime_AccessService.facemodel (통합)
- 🔴 **불일치**

### 권장 조치
**Option A 선택:** 독립 서비스로 분리
- 이유: 아키텍처 순수성 + 비즈니스 가치
- 기간: 7-12일
- 복잡도: 높음

### 대안
**Option B 선택:** 다이어그램 수정 (현재 코드 유지)
- 이유: 빠른 수정 + 낮은 복잡도
- 기간: 2일
- 복잡도: 낮음

---

## 12. 다음 단계

### 즉시 결정 필요:
1. **Option A vs Option B 선택**
2. **스테이크홀더 승인**
3. **리소스 할당**

### 의사결정자를 위한 질문:
1. 마이크로서비스 아키텍처로 완전히 전환할 계획인가?
2. ML 모델을 다른 서비스에서도 활용할 예정인가?
3. 모델 학습과 배포를 독립적으로 관리하고 싶은가?
4. 개발팀이 분산 시스템 운영 경험을 충분히 보유하고 있는가?

### 의사결정 후:
- 선택 옵션에 따라 구현 계획 수립
- 프로젝트 일정에 반영
- 팀 공지 및 교육

---

**작성:** Architecture Analysis Team  
**상태:** 의사결정 대기 중 ⏳  
**우선순위:** 높음 🔴

