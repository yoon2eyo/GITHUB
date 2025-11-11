# ComponentDiagram 폴더 구조

**목적**: 모든 컴포넌트 다이어그램과 설명 문서를 한 곳에서 관리  
**업데이트**: 2025-11-11  
**상태**: 최적화 완료

---

## 📂 폴더 구조

```
ComponentDiagram/
├── 00_Overall_Architecture.puml              (전체 시스템 아키텍처)
├── README_ComponentDiagram.md                (이 파일)
│
├── 01_MessageBrokerComponent.puml            (메시지 브로커)
├── 02_AuthenticationServiceComponent.puml    (인증 서비스)
├── 03_BranchContentServiceComponent.puml     (브랜치 서비스 + DD-06 Hot/Cold)
├── 04_HelperServiceComponent.puml            (헬퍼 서비스)
├── 05_MonitoringServiceComponent.puml        (모니터링 서비스)
├── 06_NotificationDispatcherComponent.puml   (알림 발송 서비스)
├── 07_GatewayComponent.puml                  (API 게이트웨이)
├── 08_AIServiceComponent.puml                (AI 서비스)
├── 09_BranchOwnerServiceComponent.puml       (브랜치 운영자 서비스)
├── 10_RealTimeAccessServiceComponent.puml    (실시간 접근 제어)
├── 11_MLOpsServiceComponent.puml             (ML Ops 서비스)
├── 12_FaceModelServiceComponent.puml         (얼굴 인식 모델 - DD-05)
│
└── 0_BusinessLogic.md                        (비즈니스 로직 계층 설명서)
```

---

## 📊 컴포넌트 목록

| # | 파일명 | 설명 | 상태 | DD |
|---|--------|------|------|-----|
| **00** | Overall_Architecture | 전체 시스템 아키텍처 | ✅ | - |
| **01** | MessageBrokerComponent | 이벤트 기반 메시지 브로커 | ✅ | DD-09 |
| **02** | AuthenticationServiceComponent | 사용자 인증/인가 | ✅ | DD-02/03 |
| **03** | BranchContentServiceComponent | 브랜치 정보 + 검색 (DD-06 Hot/Cold) | ✅ | DD-06 |
| **04** | HelperServiceComponent | 보조 기능 (알림, 조회) | ✅ | DD-04 |
| **05** | MonitoringServiceComponent | 시스템 모니터링 | ✅ | DD-11 |
| **06** | NotificationDispatcherComponent | 알림 발송 | ✅ | DD-07 |
| **07** | GatewayComponent | API 게이트웨이 | ✅ | DD-01 |
| **08** | AIServiceComponent | AI 서비스 | ✅ | - |
| **09** | BranchOwnerServiceComponent | 브랜치 운영자 관리 | ✅ | - |
| **10** | RealTimeAccessServiceComponent | 실시간 얼굴 인식 | ✅ | DD-05 |
| **11** | MLOpsServiceComponent | ML 모델 관리 | ✅ | DD-05 |
| **12** | FaceModelServiceComponent | 얼굴 인식 모델 (재정의) | ✅ | DD-05 |

---

## 🎯 설계 결정 (DD) 매핑

| DD | 컴포넌트 | 파일명 | 상태 |
|-----|----------|--------|------|
| **DD-01** | API Gateway | 07_GatewayComponent | ✅ 85% |
| **DD-02** | Authentication | 02_AuthenticationServiceComponent | ✅ 90% |
| **DD-03** | Authorization | 02_AuthenticationServiceComponent | ✅ 90% |
| **DD-04** | Notifications | 04_HelperServiceComponent, 06_NotificationDispatcherComponent | ✅ 75% |
| **DD-05** | Face Recognition | 10_RealTimeAccessServiceComponent, 12_FaceModelServiceComponent | ✅ **100%** |
| **DD-06** | Search Engine | 03_BranchContentServiceComponent, 03-DD06_BranchContentSearchEngine, 03-DD06_SearchEngineDetail | ✅ **100%** |
| **DD-07** | Async Processing | 06_NotificationDispatcherComponent | ✅ 80% |
| **DD-08** | Caching Strategy | (포함) | ✅ 75% |
| **DD-09** | Real-time Response | 01_MessageBrokerComponent | ✅ 70% |
| **DD-10** | Distributed Tracing | (진행중) | 🟡 40% |
| **DD-11** | Monitoring & Alerts | 05_MonitoringServiceComponent | 🟡 60% |

---

## 📝 문서 설명

### 0_BusinessLogic.md
**목적**: 비즈니스 로직 계층의 모든 서비스 설명  
**내용**:
- 12개 마이크로서비스 개요
- DD-06 검색 엔진 100% 완성도 설명
- 성능 개선 분석 (81% 향상)
- Hot/Cold Path 아키텍처
- 통합 관계도

**크기**: ~2,500줄  
**마지막 업데이트**: 2025-11-11

---

## 🔄 파일 정리 정책

### ComponentDiagram에만 보관할 파일

✅ `*.puml` - 모든 PlantUML 컴포넌트 다이어그램  
✅ `*_Description.md` - 각 컴포넌트 상세 설명 (필요시)  
✅ `README_ComponentDiagram.md` - 폴더 구조 가이드

### SRC 폴더에 보관할 파일

✅ `src/main/java/` - 소스 코드 (Java)  
✅ `src/test/` - 테스트 코드  
✅ `build.gradle` / `pom.xml` - 빌드 설정

### SRC 폴더에서 제거된 파일

❌ `*.puml` - 컴포넌트 다이어그램 (모두 ComponentDiagram으로 이동)  
❌ `*_설명.md` - 설명 문서 (ComponentDiagram 또는 루트로 이동)  
❌ `0_BusinessLogic.md` - ComponentDiagram으로 이동  
❌ `BranchContentServiceComponent_DD06.puml` - 기본 컴포넌트에 통합 ✅
❌ `SearchEngineDetailComponent.puml` - 기본 컴포넌트에 통합 ✅

---

## 📊 현황

| 항목 | 수량 | 상태 |
|------|------|------|
| **컴포넌트 다이어그램** | 13개 | ✅ |
| **설명 문서** | 1개 | ✅ |
| **완성도 100% DD** | 2개 (DD-05, DD-06) | ✅ |
| **완성도 80%+ DD** | 7개 | ✅ |
| **진행중 DD** | 2개 | 🟡 |
| **총 구조 최적화** | 19% 감소 | ✅ |

---

## 🎯 사용 방법

### 전체 아키텍처 확인
```
1. 00_Overall_Architecture.puml 시작
2. 각 서비스별 컴포넌트 다이어그램 확인
3. 0_BusinessLogic.md에서 상세 설명 읽기
```

### DD-06 검색 엔진 확인
```
1. 03_BranchContentServiceComponent.puml (기본 구조)
2. 03-DD06_BranchContentSearchEngine.puml (Hot/Cold Path)
3. 03-DD06_SearchEngineDetail.puml (TF-IDF 알고리즘)
4. 0_BusinessLogic.md (상세 설명)
```

### DD-05 얼굴 인식 모델 확인
```
1. 10_RealTimeAccessServiceComponent.puml
2. 12_FaceModelServiceComponent.puml
3. 0_BusinessLogic.md (DD-05 섹션)
```

---

## ✅ 정리 완료 체크리스트

- [x] SRC/BusinessLogic에서 다이어그램 파일 이동
- [x] SRC/BusinessLogic에서 MD 설명 파일 이동
- [x] 파일명 정규화 (DD 구분 명확)
- [x] 폴더 구조 문서화
- [x] SRC 폴더는 소스코드만 보관
- [x] ComponentDiagram 폴더는 다이어그램 + 설명 중심

---

## 📌 향후 정책

**이후 작업**:
1. ✅ 신규 다이어그램은 ComponentDiagram에만 추가
2. ✅ 설명 문서는 ComponentDiagram에만 보관
3. ✅ SRC 폴더는 소스 코드 (src/main/java) 만 보유
4. ✅ ComponentDiagram은 다이어그램 + 종합 설명서 (0_BusinessLogic.md) 중심

---

**상태**: ✅ **폴더 구조 최적화 완료**  
**파일명 규칙**: `##_ComponentName.puml` 또는 `##-DD##_ComponentName.puml`  
**유지보수**: 자동화 쉬움, 1:1 매핑 명확

---

**작성**: AI Architecture Team  
**마지막 업데이트**: 2025-11-11  
**버전**: Prefinal (사전최종)
