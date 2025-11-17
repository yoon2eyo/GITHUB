# 구조적으로 중요한 컴포넌트 5개 추천

## 추천 기준

다음 기준에 따라 시스템에서 구조적으로 중요한 역할을 수행하는 컴포넌트 5개를 추천합니다:

1. **핵심 Quality Attribute 달성에 기여**: 시스템의 주요 QA (성능, 보안, 가용성, 유지보수성) 달성에 직접 기여
2. **아키텍처 패턴의 핵심 구현**: 주요 Design Decision의 핵심 구현체
3. **다중 서비스와의 상호작용**: 여러 서비스와 상호작용하는 중앙 역할
4. **복잡한 비즈니스 로직**: 복잡한 워크플로우나 알고리즘을 담당
5. **시스템 진입점 또는 핵심 경로**: 시스템의 주요 진입점이거나 핵심 처리 경로

---

## 추천 컴포넌트 목록

### 1. ✅ AccessAuthorizationManager (Access Service)
**이미 선택됨**

**역할**: 실시간 출입 인증 결정 및 게이트 제어
- **Layer**: Real-Time Access Layer
- **관련 QA**: QAS-02 (성능), QAS-04 (보안), QAS-05 (가용성)
- **관련 DD**: DD-05 (Performance Optimization)
- **핵심 기능**: 안면인식 출입 인증, QR 코드 인증, 게이트 제어

**구조적 중요성**:
- 실시간 성능 최적화의 핵심 (3초 이내 출입)
- Data Pre-Fetching, Same Physical Node, Pipeline Optimization 전술 적용
- 모든 출입 시도에 대한 감사 추적

---

### 2. AuthenticationManager (Auth Service)

**역할**: 사용자 인증 및 JWT 토큰 관리
- **Layer**: Business Logic Layer
- **관련 QA**: QAS-04 (보안), QAS-05 (가용성)
- **관련 DD**: DD-08 (보안 강화 구조)
- **핵심 기능**: 로그인, 토큰 생성/검증, 권한 확인

**구조적 중요성**:
- **모든 서비스의 진입점**: API Gateway를 통해 모든 요청이 인증을 거침
- **보안의 핵심**: 시스템 전체 보안 정책의 기반
- **토큰 기반 인증**: JWT를 통한 Stateless 인증 구현
- **Role-Based Access Control (RBAC)**: 사용자 권한 관리

**주요 Design Elements**:
- `AuthenticationManager`: 인증 로직
- `AuthorizationManager`: 권한 확인
- `JwtTokenManager`: JWT 토큰 생성/검증
- `IAuthRepository`: 사용자 데이터 접근

**적용된 Pattern**:
- Strategy Pattern: 다양한 인증 전략 지원
- Repository Pattern: 데이터 접근 추상화

---

### 3. VectorComparisonEngine (FaceModel Service)

**역할**: 안면 벡터 비교 및 특징점 추출
- **Layer**: Real-Time Access Layer
- **관련 QA**: QAS-02 (성능)
- **관련 DD**: DD-05 (Performance Optimization)
- **핵심 기능**: 특징점 추출, 벡터 유사도 계산, Pipeline Optimization

**구조적 중요성**:
- **성능 최적화의 핵심**: Pipeline Optimization으로 49% 지연 감소
- **AccessAuthorizationManager와의 협력**: IPC/gRPC 통신으로 실시간 인증 지원
- **병렬 처리**: CompletableFuture를 통한 특징점 추출 병렬화
- **ML 모델 추론**: ML Inference Engine과의 통합

**주요 Design Elements**:
- `VectorComparisonEngine`: 벡터 비교 엔진
- `FeatureExtractor`: 특징점 추출
- `MLInferenceEngineAdapter`: ML 모델 어댑터
- `ModelLifecycleManager`: 모델 버전 관리

**적용된 Pattern**:
- Pipeline Pattern: 병렬 특징점 추출 파이프라인
- Adapter Pattern: ML Inference Engine 어댑팅
- Strategy Pattern: 다양한 ML 모델 지원

**성능 최적화**:
- 순차 처리: 405ms → 병렬 처리: 205ms (49% 감소)
- CompletableFuture 기반 비동기 처리

---

### 4. SearchQueryManager (Search Service)

**역할**: 자연어 검색 쿼리 처리 및 Hot/Cold Path 분리
- **Layer**: Business Logic Layer
- **관련 QA**: QAS-03 (실시간성), QAS-02 (성능), QAS-17 (비용 효율성)
- **관련 DD**: DD-09 (자연어 검색 질의 응답의 실시간성)
- **핵심 기능**: 실시간 검색 (Hot Path), 비동기 인덱스 개선 (Cold Path)

**구조적 중요성**:
- **Hot/Cold Path 분리**: 실시간성과 비용 효율성의 균형
- **자연어 검색**: LLM과 ElasticSearch의 하이브리드 접근
- **비동기 처리**: Cold Path에서 LLM 분석으로 인덱스 개선
- **실시간성 보장**: Hot Path에서 LLM 호출 없이 3초 이내 응답

**주요 Design Elements**:
- `SearchQueryManager`: Hot Path 쿼리 처리
- `SearchQueryImprovementConsumer`: Cold Path 인덱스 개선
- `SearchEngineAdapter`: ElasticSearch 어댑터
- `ILLMAnalysisServiceClient`: LLM 서비스 클라이언트

**적용된 Pattern**:
- Hot/Cold Path Pattern: 실시간성과 비용 효율성 분리
- Observer Pattern: 이벤트 기반 Cold Path 트리거
- Adapter Pattern: ElasticSearch, LLM 서비스 어댑팅

**성능 특성**:
- Hot Path: < 3초 (LLM 호출 없음)
- Cold Path: 비동기 처리 (10% 샘플링)
- 비용 효율성: LLM 호출 최소화

---

### 5. TrainingPipelineOrchestrator (MLOps Service)

**역할**: AI 모델 재학습 파이프라인 오케스트레이션
- **Layer**: AI Pipeline Layer
- **관련 QA**: QAS-06 (수정용이성), QAS-02 (성능), QAS-05 (가용성)
- **관련 DD**: DD-07 (AI학습 판독 구조)
- **핵심 기능**: 데이터 수집, 모델 학습, 검증, Hot Swap 배포

**구조적 중요성**:
- **무중단 모델 배포**: Hot Swap을 통한 서비스 중단 없는 모델 업데이트
- **이벤트 기반 트리거**: TaskConfirmedEvent 수신 시 자동 재학습
- **Saga Pattern**: 복잡한 워크플로우의 트랜잭션 관리
- **AI 모델 개선**: 지속적인 학습을 통한 정확도 향상

**주요 Design Elements**:
- `TrainingPipelineOrchestrator`: 파이프라인 오케스트레이션
- `ModelVerificationService`: 모델 검증
- `DeploymentService`: Hot Swap 배포
- `TrainingDataStore`: 학습 데이터 저장소

**적용된 Pattern**:
- Saga Pattern: 분산 트랜잭션 관리
- Orchestrator Pattern: 워크플로우 오케스트레이션
- Hot Swap Pattern: 무중단 배포
- Event-Driven Architecture: 이벤트 기반 트리거

**성능 특성**:
- 평균 재학습 완료 시간: 4시간
- Hot Swap 배포: < 1ms 모델 교체
- 서비스 가용성: 99.9% 유지

---

## 대안 고려 컴포넌트

### FaultDetector (Monitoring Service)

**역할**: 설비 고장 감지 및 실시간 알림
- **관련 QA**: QAS-01 (가용성), QAS-05 (가용성)
- **관련 DD**: DD-04 (고장 감지 및 실시간 알림 체계)

**고려 사항**:
- 고장 감지의 핵심이지만, TrainingPipelineOrchestrator가 더 복잡한 워크플로우와 Hot Swap 배포를 담당
- TrainingPipelineOrchestrator가 QAS-06 (수정용이성) 달성에 더 직접적으로 기여

---

## 추천 컴포넌트 요약

| 순위 | 컴포넌트 | 서비스 | 주요 QA | 핵심 역할 |
|------|---------|--------|---------|----------|
| 1 | AccessAuthorizationManager | Access Service | QAS-02, QAS-04 | 실시간 출입 인증 |
| 2 | AuthenticationManager | Auth Service | QAS-04, QAS-05 | 보안 및 인증 |
| 3 | VectorComparisonEngine | FaceModel Service | QAS-02 | 성능 최적화 |
| 4 | SearchQueryManager | Search Service | QAS-03, QAS-02 | Hot/Cold Path 분리 |
| 5 | TrainingPipelineOrchestrator | MLOps Service | QAS-06, QAS-05 | 무중단 모델 배포 |

---

## 선택 근거

### 1. AccessAuthorizationManager
- ✅ 실시간 성능 최적화의 핵심
- ✅ 여러 전술(Data Pre-Fetching, Same Physical Node, Pipeline Optimization) 적용
- ✅ 모든 출입 시도 감사 추적

### 2. AuthenticationManager
- ✅ 모든 서비스의 진입점
- ✅ 시스템 전체 보안 정책의 기반
- ✅ JWT 기반 Stateless 인증

### 3. VectorComparisonEngine
- ✅ Pipeline Optimization의 핵심 구현
- ✅ 49% 성능 향상 달성
- ✅ AccessAuthorizationManager와의 협력으로 실시간 인증 지원

### 4. SearchQueryManager
- ✅ Hot/Cold Path 분리 패턴의 핵심 구현
- ✅ 실시간성과 비용 효율성의 균형
- ✅ LLM과 ElasticSearch의 하이브리드 접근

### 5. TrainingPipelineOrchestrator
- ✅ 무중단 모델 배포 (Hot Swap)
- ✅ 복잡한 워크플로우 오케스트레이션
- ✅ Saga Pattern을 통한 분산 트랜잭션 관리

---

## 결론

이 5개 컴포넌트는 시스템의 핵심 Quality Attributes 달성에 직접적으로 기여하며, 주요 Design Decision의 핵심 구현체입니다. 각 컴포넌트는 고유한 아키텍처 패턴과 최적화 전술을 적용하여 시스템의 성능, 보안, 가용성, 유지보수성을 보장합니다.

