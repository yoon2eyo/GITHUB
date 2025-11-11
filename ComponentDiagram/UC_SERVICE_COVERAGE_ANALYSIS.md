# UC-Service 커버리지 분석 보고서

**분석 날짜**: 2025-11-11
**분석 기준**: UC 시나리오 정상 수행 가능 여부

---

## 📊 현재 서비스 구조 (총 11개 서비스 + 2개 인프라)

### Core Business Services (9개)
1. **API Gateway** (RequestRouter)
2. **Auth Service** (AuthenticationManager)
3. **Access Service** (AccessAuthorizationManager)
4. **FaceModel Service** (VectorComparisonEngine)
5. **Helper Service** (TaskManagementManager)
6. **Search Service** (BranchContentService)
7. **BranchOwner Service** (BranchOwnerManager)
8. **Monitoring Service** (StatusReceiverManager)
9. **Notification Service** (NotificationDispatcherConsumer)

### AI/ML Services (2개)
10. **MLOps Service** (MLOpsTrainingService)
11. **MLInferenceEngine** (Internal ML Platform)

### Infrastructure (2개)
12. **Message Broker** (RabbitMQ)
13. **AI Service** (LLMKeywordExtractor) - **검토 필요**

---

## 🔍 UC별 서비스 매핑 분석 (총 24개 UC)

### **1. 계정 및 인증 그룹 (UC-01 ~ UC-06)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-01** | 고객 계정 등록 | Auth Service | ✅ | + UC-05, UC-06 include |
| **UC-02** | 헬퍼 계정 등록 | Auth Service | ✅ | + UC-06 include |
| **UC-03** | 지점주 계정 등록 | Auth Service | ✅ | 안면사진 없음 |
| **UC-04** | 로그인 | Auth Service | ✅ | 토큰 발급 |
| **UC-05** | 본인 인증 수행 | Auth Service | ✅ | + 외부 CC 연동 |
| **UC-06** | 안면 사진 등록 | Auth Service → FaceModel | ✅ | 벡터 생성 및 저장 |

**결론**: ✅ **완전 커버** (6/6)
- Auth Service가 모든 계정 관련 UC 처리
- FaceModel Service와 협력하여 안면 벡터 생성

---

### **2. 출입 관리 그룹 (UC-07, UC-08, UC-22)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-07** | 안면인식 출입 인증 | Access Service + FaceModel | ✅ | IPC 최적화 (DD-05) |
| **UC-08** | QR코드 수동 출입 | Access Service | ✅ | 토큰 검증 + 게이트 개방 |
| **UC-22** | 게이트 개방 실행 | Access Service | ✅ | Equipment Gateway 제어 |

**결론**: ✅ **완전 커버** (3/3)
- Access Service: 출입 제어 전담
- FaceModel Service: 안면 벡터 비교 (IPC)
- Equipment Gateway를 통한 물리 제어

---

### **3. 검색 및 리뷰 그룹 (UC-09, UC-10, UC-11)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-09** | 자연어 지점 검색 | Search Service | ✅ | Hot Path (로컬 검색) |
| **UC-10** | 고객 리뷰 등록 | Search Service | ✅ | Cold Path (LLM 분석) + UC-11 |
| **UC-11** | 맞춤형 알림 발송 | Search Service (Consumer) | ✅ | PreferenceMatchConsumer |

**결론**: ✅ **완전 커버** (3/3)
- Search Service: Hot/Cold Path 분리 (DD-06)
- PreferenceMatchConsumer: 성향 매칭 (DD-07)
- 외부 LLM 연동 (Cold Path)

**참고**: AI Service (08_AIServiceComponent.puml)는 **Search Service와 기능 중복**
- Search Service 내부에 LLM 연동 로직 포함
- AI Service는 **불필요한 중복 서비스**로 판단

---

### **4. 헬퍼 작업 관리 그룹 (UC-12 ~ UC-17)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-12** | 작업 사진 등록 | Helper Service | ✅ | TaskSubmissionManager |
| **UC-13** | AI 세탁물 작업 1차 판독 | Helper Service (Consumer) | ✅ | AITaskAnalysisConsumer + MLInferenceEngine |
| **UC-14** | 세탁물 작업 결과 검수/컨펌 | BranchOwner Service | ✅ | RewardConfirmationManager |
| **UC-15** | 세탁물 판독 결과 수정 | BranchOwner Service | ✅ | 판독 결과 override |
| **UC-16** | 보상 잔고 갱신 | Helper Service (Consumer) | ✅ | RewardUpdateConsumer |
| **UC-17** | 보상 잔고 조회 | Helper Service | ✅ | HelperRewardApi |

**결론**: ✅ **완전 커버** (6/6)
- Helper Service: 작업 등록, 보상 조회/갱신
- AITaskAnalysisConsumer: ML 판독 처리
- BranchOwner Service: 검수 및 컨펌
- Event-driven 흐름 명확 (DD-02)

---

### **5. 지점 관리 그룹 (UC-18, UC-19)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-18** | 지점 정보 등록 | BranchOwner Service | ✅ | + UC-11 trigger |
| **UC-19** | 고객 리뷰 조회 | BranchOwner Service | ✅ | 지점별 리뷰 조회 |

**결론**: ✅ **완전 커버** (2/2)
- BranchOwner Service: 지점 정보 CRUD
- Search Service와 연동 (리뷰 데이터 공유)

---

### **6. 설비 모니터링 그룹 (UC-20, UC-21)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-20** | 설비 상태 보고 | Monitoring Service | ✅ | Heartbeat Receiver |
| **UC-21** | 설비 상태 모니터링 | Monitoring Service | ✅ | Ping/echo + Scheduler |

**결론**: ✅ **완전 커버** (2/2)
- Monitoring Service: Heartbeat + Ping/echo (DD-04)
- Notification Service: 고장 알림 발송
- Equipment Gateway: 상태 보고 수신

---

### **7. AI 모델 학습 그룹 (UC-23, UC-24)**

| UC | 제목 | 담당 서비스 | 커버리지 | 비고 |
|----|------|-----------|----------|------|
| **UC-23** | 안면인식 모델 재학습 | MLOps Service | ✅ | Timer-triggered |
| **UC-24** | 세탁물 모델 재학습 | MLOps Service | ✅ | 수정된 판독 데이터 학습 |

**결론**: ✅ **완전 커버** (2/2)
- MLOps Service: 모델 학습 파이프라인
- MLInferenceEngine: 모델 배포 및 추론
- DD-03 예외: READ-ONLY 데이터 접근

---

## 📈 UC 커버리지 종합

| 그룹 | UC 개수 | 커버된 UC | 커버리지 |
|------|---------|----------|----------|
| 계정 및 인증 | 6 | 6 | ✅ 100% |
| 출입 관리 | 3 | 3 | ✅ 100% |
| 검색 및 리뷰 | 3 | 3 | ✅ 100% |
| 헬퍼 작업 관리 | 6 | 6 | ✅ 100% |
| 지점 관리 | 2 | 2 | ✅ 100% |
| 설비 모니터링 | 2 | 2 | ✅ 100% |
| AI 모델 학습 | 2 | 2 | ✅ 100% |
| **총계** | **24** | **24** | **✅ 100%** |

---

## 🎯 서비스 적절성 분석

### ✅ **적절한 서비스 (11개)**

| # | 서비스 | 역할 | 담당 UC | 상태 |
|---|--------|------|---------|------|
| 1 | **API Gateway** | 라우팅, 보안, 로드밸런싱 | 모든 UC 진입점 | ✅ 필수 |
| 2 | **Auth Service** | 인증, 권한, 회원가입 | UC-01~06 | ✅ 필수 |
| 3 | **Access Service** | 출입 제어 | UC-07, UC-08, UC-22 | ✅ 필수 |
| 4 | **FaceModel Service** | 안면 벡터 비교 (IPC) | UC-06, UC-07 | ✅ 필수 |
| 5 | **Helper Service** | 작업 관리, 보상 | UC-12, UC-13, UC-16, UC-17 | ✅ 필수 |
| 6 | **Search Service** | 검색, 리뷰, 알림 | UC-09, UC-10, UC-11 | ✅ 필수 |
| 7 | **BranchOwner Service** | 지점 관리, 작업 검수 | UC-14, UC-15, UC-18, UC-19 | ✅ 필수 |
| 8 | **Monitoring Service** | 설비 모니터링 | UC-20, UC-21 | ✅ 필수 |
| 9 | **Notification Service** | 알림 발송 | UC-11, UC-20, UC-21 | ✅ 필수 |
| 10 | **MLOps Service** | 모델 학습/배포 | UC-23, UC-24 | ✅ 필수 |
| 11 | **MLInferenceEngine** | ML 추론 플랫폼 | UC-06, UC-07, UC-13 | ✅ 필수 |

---

### ⚠️ **검토 필요 서비스 (1개)**

#### **AI Service (08_AIServiceComponent.puml)**

**현재 정의된 역할**:
- LLMKeywordExtractor
- ContentAnalyzer
- QueryTokenizer

**문제점**:
1. **Search Service와 역할 중복**
   - Search Service 이미 LLM 연동 포함 (Cold Path)
   - QueryTokenizer는 Search Service 내부에 존재
   
2. **Overall Architecture에 미표시**
   - `00_Overall_Architecture.puml`에 AI Service 없음
   - Search Service만 ExtLLM 연동

3. **UC 매핑 불명확**
   - 어떤 UC도 명시적으로 AI Service 호출하지 않음

**권고사항**: ❌ **제거 권장**
- Search Service로 기능 통합
- LLM 연동은 Search Service의 Cold Path로 충분
- 불필요한 서비스 계층 제거로 복잡도 감소

---

### 🆕 **누락된 서비스 검토**

#### **검토 항목 1: 별도의 Review Service 필요?**
**현재**: Search Service가 리뷰 등록/조회 담당
**판단**: ✅ **불필요**
- UC-10 (리뷰 등록): Search Service (Cold Path)
- UC-19 (리뷰 조회): BranchOwner Service
- 역할이 명확하게 분리되어 있음

#### **검토 항목 2: 별도의 Gate Control Service 필요?**
**현재**: Access Service가 게이트 제어 포함
**판단**: ✅ **불필요**
- UC-22 (게이트 개방): Access Service 내부
- GateController 컴포넌트로 충분
- 단일 책임 원칙 위배하지 않음

#### **검토 항목 3: 별도의 Payment/Reward Service 필요?**
**현재**: Helper Service가 보상 관리 포함
**판단**: ✅ **불필요**
- UC-16, UC-17: Helper Service 내부 처리
- 간단한 잔고 증감 로직만 존재
- 실제 결제는 명세서 범위 외

---

## 🔍 QA 시나리오 지원 여부 (6개)

| QAS | 제목 | 지원 서비스 | 상태 |
|-----|------|------------|------|
| **QAS-01** | 설비 고장 감지 15초 이내 알림 | Monitoring + Notification | ✅ |
| **QAS-02** | 안면인식 3초 이내 출입 | Access + FaceModel (IPC, Pre-Fetching) | ✅ |
| **QAS-03** | 검색 3초 이내 응답 | Search (Hot Path, NO LLM) | ✅ |
| **QAS-04** | 개인정보 암호화 보호 | Auth + Access (Encrypt, Limit Access) | ✅ |
| **QAS-05** | 주요 서비스 5분 이내 복구 | All Services (Passive Redundancy) | ✅ |
| **QAS-06** | AI 모델 무중단 배포 | FaceModel + MLOps (Hot Swap) | ✅ |

**결론**: ✅ **6/6 QAS 모두 지원**

---

## 📊 최종 분석 결과

### ✅ **종합 평가**

| 항목 | 결과 |
|------|------|
| **총 UC 개수** | 24개 |
| **커버된 UC** | 24개 (100%) ✅ |
| **필수 서비스** | 11개 |
| **중복 서비스** | 1개 (AI Service) ❌ |
| **누락 서비스** | 0개 ✅ |
| **QAS 지원** | 6/6 (100%) ✅ |

---

## 🎯 권고사항

### 1. ❌ **AI Service (08_AIServiceComponent.puml) 제거**

**이유**:
- Search Service와 100% 기능 중복
- UC/QAS 어디에도 직접 언급 없음
- Overall Architecture에도 미포함

**조치**:
```diff
- 08_AIServiceComponent.puml 삭제
- Search Service가 LLM 연동 전담
- DD-06, DD-09 문서는 이미 Search Service 중심으로 작성됨
```

### 2. ✅ **현재 11개 서비스 구조 유지**

**최종 서비스 목록**:
1. API Gateway ✅
2. Auth Service ✅
3. Access Service ✅
4. FaceModel Service ✅
5. Helper Service ✅
6. Search Service ✅
7. BranchOwner Service ✅
8. Monitoring Service ✅
9. Notification Service ✅
10. MLOps Service ✅
11. MLInferenceEngine ✅

**+ Infrastructure**:
- Message Broker
- Search Engine (DS-07)

---

## ✅ 결론

**현재 서비스 구조는 UC 및 QAS를 100% 충족합니다!**

- ✅ 24개 UC 모두 적절한 서비스로 처리
- ✅ 6개 QAS 모두 아키텍처로 지원
- ✅ DD-01 ~ DD-09 설계 결정 반영
- ❌ AI Service만 제거하면 완벽

**권장 최종 구조**: **11개 비즈니스 서비스 + 2개 인프라**

---

## 📝 다음 단계

1. [x] `08_AIServiceComponent.puml` 삭제 ✅ **완료**
2. [x] `Overall Architecture`에 AI Service 참조 제거 확인 ✅ **완료** (참조 없음)
3. [ ] Stub 코드 생성 시 AI Service 제외
4. [ ] 최종 서비스 목록으로 구현 진행

**UC 커버리지 검증 완료!** ✅
**불필요한 서비스 제거 완료!** ✅

