# 외부 LLM 서비스 사용 분석

**분석 날짜**: 2025-11-11
**역할**: 오직 자연어 분석 (키워드/성향 추출)

---

## 🎯 LLM 서비스 정의

### 외부 인터페이스 (명세서 539-548줄)
```
[유형] System Interface: HTTPS
[역할] 
- 시스템은 고객의 자연어 검색어를 '상용 LLM 서비스'로 전송하고, 
  분석된 결과를 회신받는다.

[특성]
- 데이터: 텍스트 쿼리 (JSON, 100자 내외)
- 빈도: 일 평균 2,000건
- 피크 타임: 시간당 500건 (18~20시)
```

### 서비스 제공자
- **상용 LLM 서비스** (외부 파트너)
- 예: OpenAI GPT, Claude, Google Gemini 등

---

## 📍 LLM 서비스 사용 위치

### ✅ **유일한 사용 위치: Search Service (Cold Path)**

**컴포넌트**: `03_BranchContentServiceComponent.puml`

```
package "Business Layer" {
  component PreferenceAnalyzer
  
  ' Cold Path: LLM 호출
  ContentRegistrationManager ..( IPreferenceAnalysisService
  PreferenceAnalyzer ..( ILLMAnalysisServiceClient : <<HTTPS>>
}
```

---

## 🔍 LLM 사용 시나리오 (2개 UC, Cold Path만)

### **1. UC-10: 고객 리뷰 등록 (Cold Path)**

#### 흐름:
```
1. 고객이 지점 리뷰 텍스트 입력
   예: "샤워실이 정말 넓고 깨끗해요. 운동 기구도 다양하고 트레이너 분들이 친절합니다."

2. ContentRegistrationManager가 리뷰 저장

3. PreferenceAnalyzer가 ILLMAnalysisService 호출 (비동기)
   - 입력: 리뷰 텍스트
   - 출력: 추출된 키워드 및 성향 태그

4. LLM 응답 예시:
   {
     "keywords": ["샤워실", "넓다", "깨끗", "운동기구", "다양", "트레이너", "친절"],
     "preferences": [
       {"category": "청결", "score": 0.95},
       {"category": "기구 다양성", "score": 0.88},
       {"category": "서비스 품질", "score": 0.92}
     ],
     "sentiment": "매우 긍정적"
   }

5. 추출된 데이터를 SearchEngine (DS-07)에 인덱싱

6. BranchPreferenceCreatedEvent 발행
```

**역할**: 리뷰 텍스트에서 **지점 성향 데이터 추출**

---

### **2. UC-18: 지점 정보 등록 (Cold Path)**

#### 흐름:
```
1. 지점주가 지점 소개글 입력
   예: "24시간 운영, 최신 런닝머신 30대, 프리웨이트 존, 여성 전용 구역, 샤워실 10개"

2. ContentRegistrationManager가 지점 정보 저장

3. PreferenceAnalyzer가 ILLMAnalysisService 호출 (비동기)
   - 입력: 지점 소개 텍스트
   - 출력: 추출된 키워드 및 지점 특성

4. LLM 응답 예시:
   {
     "keywords": ["24시간", "런닝머신", "프리웨이트", "여성전용", "샤워실"],
     "facilities": [
       {"type": "유산소", "count": 30, "equipment": "런닝머신"},
       {"type": "웨이트", "zone": "프리웨이트"},
       {"type": "편의시설", "count": 10, "facility": "샤워실"}
     ],
     "features": ["24시간 운영", "여성 친화적", "대형 시설"]
   }

5. 추출된 데이터를 SearchEngine (DS-07)에 인덱싱

6. BranchPreferenceCreatedEvent 발행 → UC-11 맞춤형 알림 트리거
```

**역할**: 지점 소개 텍스트에서 **지점 특성 데이터 추출**

---

## 🚫 LLM을 사용하지 **않는** 부분

### ❌ **UC-09: 자연어 지점 검색 (Hot Path)**

**DD-09 Approach 3 채택**: Hot Path에서 LLM 완전 제거

#### 기존 우려 (DD-09):
```
문제: 외부 LLM 호출 시 2.9초 → QAS-03 (3초 이내) SLA 위반 위험
```

#### 해결책 (현재 구조):
```
Hot Path (UC-09 실시간 검색):
  Customer Query → SimpleKeywordTokenizer (로컬) 
                → SearchEngine (DS-07)
                → Results (< 0.5초)

NO LLM CALL!
```

**검색 흐름**:
```java
// Hot Path: NO LLM
public List<Branch> searchBranches(String query) {
    // 1. 로컬 토크나이저 (LLM 없음)
    List<String> keywords = simpleKeywordTokenizer.tokenize(query);
    // 예: "샤워실이 넓은 곳" → ["샤워실", "넓다"]
    
    // 2. 미리 인덱싱된 SearchEngine 쿼리
    return searchEngine.query(keywords); // ElasticSearch
}
```

**성능**:
- 토크나이저: 10ms (로컬)
- SearchEngine 쿼리: 200ms (ElasticSearch)
- 네트워크 + 기타: 290ms
- **총: ~500ms** ✅ (QAS-03 3초 이내 보장)

---

### ❌ **UC-13: AI 세탁물 작업 1차 판독**

**사용 모델**: MLInferenceEngine (내부 ML 플랫폼)

```
Helper 사진 업로드 → AITaskAnalysisConsumer
                  → MLInferenceEngine.analyzeImage()
                  → 세탁물 분류 결과
```

**역할**: 컴퓨터 비전 (이미지 분류)
- **LLM 아님!** (Vision Transformer 또는 CNN 모델)
- 자연어가 아닌 **이미지 분석**

---

### ❌ **UC-06, UC-07: 안면 인식**

**사용 모델**: MLInferenceEngine (내부 ML 플랫폼)

```
Face Photo → MLInferenceEngine.extractFeatures()
          → Feature Vector (128-dim)
          → Vector Comparison
```

**역할**: 얼굴 특징 추출 (Face Recognition)
- **LLM 아님!** (FaceNet, ArcFace 등 Face Embedding 모델)
- 자연어가 아닌 **이미지 처리**

---

## 📊 LLM 사용 통계

| 항목 | 값 |
|------|-----|
| **사용 UC** | 2개 (UC-10, UC-18) |
| **미사용 UC** | 22개 |
| **실행 경로** | Cold Path (비동기) |
| **성능 영향** | 실시간 응답 없음 (SLA 영향 없음) |
| **일일 호출 빈도** | ~2,000건 (리뷰 + 지점 등록) |

---

## 🏗️ 아키텍처 다이어그램

### Overall Architecture (00_Overall_Architecture.puml)

```plantuml
cloud "External Partners" {
  component "ILLMAnalysisService" as ExtLLM
}

package "Business Logic Layer" {
  component "BranchContentService" as Search
}

' LLM 사용: Cold Path만
Search --> ExtLLM : <<HTTP>> Cold Path: Keyword extraction
```

**주석**:
```
' External System Integration
Search --> ExtLLM : <<HTTP>> Cold Path: Keyword extraction
```

---

### Search Service Component (03_BranchContentServiceComponent.puml)

```plantuml
package "System Interface Layer" {
  interface ILLMAnalysisServiceClient
  component LLMServiceClient
  
  ILLMAnalysisServiceClient -- LLMServiceClient
}

package "Business Layer" {
  component PreferenceAnalyzer
  
  ' Cold Path: LLM 호출
  PreferenceAnalyzer ..( ILLMAnalysisServiceClient : <<HTTPS>>
}
```

**주석**:
```
note left of ContentRegistrationManager
  **Cold Path: Async Indexing**
  1. Review/BranchInfo created
  2. LLM keyword extraction (external)  ← 여기!
  3. Index to SearchEngine (DS-07)
  4. Publish BranchPreferenceCreatedEvent
end note
```

---

## 📋 LLM API 인터페이스 설계

### ILLMAnalysisService

```java
/**
 * 외부 상용 LLM 서비스 클라이언트
 * Provider: OpenAI, Claude, Google Gemini 등
 */
public interface ILLMAnalysisService {
    
    /**
     * 텍스트에서 키워드 및 성향 추출
     * 
     * @param text 분석할 텍스트 (리뷰 또는 지점 소개)
     * @param analysisType 분석 유형 (REVIEW, BRANCH_INFO)
     * @return 추출된 키워드 및 성향 데이터
     * @throws LLMServiceException 외부 LLM 호출 실패 시
     */
    AnalysisResult extractKeywordsAndPreferences(
        String text, 
        AnalysisType analysisType
    );
    
    /**
     * 배치 분석 (여러 텍스트 동시 처리)
     * 
     * @param texts 분석할 텍스트 리스트
     * @return 각 텍스트별 분석 결과
     */
    List<AnalysisResult> batchAnalyze(List<String> texts);
}

/**
 * LLM 분석 결과
 */
public class AnalysisResult {
    private List<String> keywords;           // 추출된 키워드
    private List<Preference> preferences;    // 성향 점수
    private String sentiment;                // 감성 분석 (긍정/부정)
    private Map<String, Object> metadata;    // 추가 메타데이터
}

/**
 * 지점 성향 데이터
 */
public class Preference {
    private String category;   // 성향 카테고리 (청결, 기구 다양성 등)
    private double score;      // 점수 (0.0 ~ 1.0)
    private String evidence;   // 근거 텍스트
}

/**
 * 분석 유형
 */
public enum AnalysisType {
    REVIEW,        // 고객 리뷰 분석
    BRANCH_INFO    // 지점 소개 분석
}
```

---

## 🔒 보안 및 비용 관리

### 1. API 키 관리
```yaml
# application.yml
external:
  llm:
    provider: openai  # or claude, gemini
    api-key: ${LLM_API_KEY}  # 환경변수로 관리
    endpoint: https://api.openai.com/v1/chat/completions
    model: gpt-4o-mini  # 비용 효율적인 모델
    timeout: 10000ms    # 10초 타임아웃
```

### 2. 비용 최적화
```java
@Service
public class LLMServiceClient implements ILLMAnalysisService {
    
    private static final int MAX_TEXT_LENGTH = 500;  // 토큰 절약
    
    @Cacheable(value = "llm-analysis", key = "#text.hashCode()")
    public AnalysisResult extractKeywordsAndPreferences(
        String text, 
        AnalysisType type
    ) {
        // 1. 중복 텍스트는 캐시에서 반환 (비용 절감)
        // 2. 텍스트 길이 제한 (토큰 절약)
        String truncated = truncate(text, MAX_TEXT_LENGTH);
        
        // 3. 외부 LLM 호출
        return callExternalLLM(truncated, type);
    }
}
```

### 3. 에러 핸들링
```java
@Retry(maxAttempts = 3, backoff = @Backoff(delay = 1000))
public AnalysisResult callExternalLLM(String text, AnalysisType type) {
    try {
        return restTemplate.postForObject(
            llmEndpoint, 
            buildRequest(text, type), 
            AnalysisResult.class
        );
    } catch (RestClientException e) {
        // 실패 시 Fallback: 간단한 로컬 키워드 추출
        log.warn("LLM service failed, using fallback", e);
        return fallbackKeywordExtractor.extract(text);
    }
}
```

---

## 📈 성능 영향 분석

### Cold Path 처리 (비동기)

```
UC-10 리뷰 등록 흐름:

[동기 응답]
Customer → API → Save Review → Response (200 OK)
                                ↓ (50ms)
                            [여기서 사용자 응답 완료]

[비동기 처리]
                            MessageBroker → PreferenceAnalyzer
                                         → LLM Call (2~5초)
                                         → Index to SearchEngine
                                         → Event Publish
```

**핵심**:
- ✅ 사용자는 **50ms 이내에 응답** 받음
- ✅ LLM 처리(2~5초)는 **백그라운드**에서 진행
- ✅ **QAS-03 SLA에 영향 없음**

---

### Hot Path 처리 (실시간)

```
UC-09 검색 흐름:

[검색 시점]
Customer → API → SimpleTokenizer (10ms)
              → SearchEngine Query (200ms)
              → Response (500ms 총)

NO LLM CALL!
✅ QAS-03 (3초) 보장
```

---

## ✅ 결론

### LLM 서비스 사용 요약:

**✅ 사용 위치 (2곳)**:
1. **UC-10: 고객 리뷰 등록** (Cold Path)
   - 리뷰 텍스트 → 키워드/성향 추출
2. **UC-18: 지점 정보 등록** (Cold Path)
   - 지점 소개 → 특성/키워드 추출

**❌ 사용하지 않는 곳**:
- UC-09: 자연어 검색 (Hot Path) - **SimpleTokenizer 사용**
- UC-13: 세탁물 판독 - **Vision Model 사용**
- UC-06/07: 안면 인식 - **Face Model 사용**

**역할**:
- ✅ **오직 자연어 분석 (키워드/성향 추출)**
- ✅ Cold Path에서만 사용 (비동기)
- ✅ 실시간 SLA에 영향 없음

**DD 준수**:
- DD-06: 전문 검색 엔진 도입
- DD-09 Approach 3: Hot/Cold Path 분리

**시스템 안정성**:
- ✅ 외부 LLM 장애 시 실시간 검색(UC-09) 영향 없음
- ✅ Fallback 전략 (로컬 키워드 추출)
- ✅ QAS-03 (3초) SLA 100% 보장

---

**LLM은 "지능"을 제공하되, "실시간 성능"은 희생하지 않는 아키텍처!** 🎯

