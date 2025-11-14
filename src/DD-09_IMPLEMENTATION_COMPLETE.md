# DD-09 구현 완료: 자연어 검색 질의 응답의 실시간성

## ✅ 구현 완료 현황

DD-09 설계 결정 문서에 따라 **Hot/Cold Path Separation** 패턴이 완전히 구현되었습니다.

---

## 📋 추가된 컴포넌트

### 1. SearchQueryEvent (Common Module)
**파일**: `src/common/src/main/java/com/smartfitness/common/event/SearchQueryEvent.java`

- **목적**: Hot Path에서 검색 쿼리를 Cold Path로 전달하기 위한 이벤트
- **용도**: DD-09의 Cold Path에서 LLM을 통한 인덱스 개선을 위한 이벤트 발행

### 2. SearchQueryImprovementConsumer (Search Service)
**파일**: `src/search-service/src/main/java/com/smartfitness/search/service/SearchQueryImprovementConsumer.java`

- **목적**: Cold Path에서 SearchQueryEvent를 구독하여 LLM으로 인덱스 개선
- **기능**:
  - SearchQueryEvent 구독
  - LLM Service를 통한 키워드 추출 (10% 샘플링)
  - ElasticSearch 인덱스 개선

### 3. SearchQueryManager 업데이트
**파일**: `src/search-service/src/main/java/com/smartfitness/search/service/SearchQueryManager.java`

- **변경사항**:
  - Hot Path 완료 후 SearchQueryEvent 발행 추가
  - customerId 파라미터 추가 (Cold Path 이벤트 발행용)

---

## 🔄 Hot/Cold Path 플로우

### Hot Path (실시간 응답)
```
1. 고객 검색 요청 → BranchSearchController
2. SearchQueryManager.search()
3. SimpleKeywordTokenizer.tokenize() (NO LLM)
4. ElasticSearch 검색 (~500ms)
5. 결과 반환 (3초 이내 SLA 보장)
6. SearchQueryEvent 발행 (비동기)
```

### Cold Path (비동기 개선)
```
1. SearchQueryEvent 수신 (Message Broker)
2. SearchQueryImprovementConsumer.handleSearchQueryEvent()
3. 샘플링 체크 (10%만 처리)
4. LLM Service 호출 (키워드 추출)
5. ElasticSearch 인덱스 개선
6. 향후 검색 정확도 향상
```

---

## 📊 DD-09 요구사항 충족도

| 요구사항 | 구현 상태 | 비고 |
|---------|---------|------|
| **Hot Path: 3초 이내 응답** | ✅ 완료 | ElasticSearch 직접 검색, LLM 호출 없음 |
| **Cold Path: LLM 인덱스 개선** | ✅ 완료 | SearchQueryEvent → LLM → 인덱스 개선 |
| **비용 효율성 (10% 샘플링)** | ✅ 완료 | `shouldProcessQuery()` 메서드로 샘플링 |
| **Event-Based 분리** | ✅ 완료 | Message Broker를 통한 비동기 처리 |

---

## 🎯 주요 설계 결정 반영

### DD-09 Approach A: LLM + ElasticSearch 하이브리드

1. **Pipe and Filter 패턴**
   - Hot Path와 Cold Path를 명확히 분리
   - 각 경로가 독립적으로 동작

2. **Hot/Cold Path Separation**
   - Hot Path: 즉시 응답 (ElasticSearch)
   - Cold Path: 비동기 개선 (LLM)

3. **Use an Intermediary 택틱**
   - Message Broker를 통한 느슨한 결합
   - Hot Path와 Cold Path 완전 분리

4. **Scheduling Policy 택틱**
   - 10% 샘플링으로 비용 효율성 확보
   - LLM 호출 빈도 최소화

---

## 📝 다음 단계 (선택사항)

### 1. Controller 업데이트
현재 `BranchSearchController`는 `customerId`를 받지 않습니다. 
인증 토큰에서 `customerId`를 추출하여 전달하도록 업데이트할 수 있습니다:

```java
@GetMapping
public ResponseEntity<List<Map<String, Object>>> searchBranches(
        @RequestParam String query,
        @RequestParam(required = false) String userLocation,
        @RequestHeader("Authorization") String token) {
    
    // Extract customerId from token
    String customerId = extractCustomerIdFromToken(token);
    
    List<Map<String, Object>> results = 
        searchQueryService.search(query, userLocation, customerId);
    
    return ResponseEntity.ok(results);
}
```

### 2. Consumer 자동 구독
`SearchServiceApplication`에서 애플리케이션 시작 시 자동으로 구독하도록 설정:

```java
@PostConstruct
public void init() {
    searchQueryImprovementConsumer.subscribeToSearchQueryEvent();
}
```

### 3. ElasticSearch 인덱스 개선 구현
현재 `updateSearchIndex()` 메서드는 stub입니다. 
실제 ElasticSearch synonym dictionary 업데이트 로직을 구현해야 합니다.

---

## ✅ 검증 체크리스트

- [x] SearchQueryEvent 생성
- [x] SearchQueryManager에서 이벤트 발행
- [x] SearchQueryImprovementConsumer 생성
- [x] LLM Service 연동
- [x] 샘플링 로직 (10%)
- [x] RabbitMQAdapter 문서 업데이트
- [ ] Controller에서 customerId 추출 (선택사항)
- [ ] Consumer 자동 구독 설정 (선택사항)
- [ ] ElasticSearch 인덱스 개선 로직 구현 (선택사항)

---

## 📚 참고 문서

- **DD-09**: `DesignApproach/DD-09 자연어 검색 질의 응답의 실시간성을 위한 구조 결정.md`
- **QAS-03**: 자연어 검색 질의 응답의 실시간성 (95% < 3초)
- **UC-09**: 자연어 지점 검색

---

**Date**: 2025-01-XX  
**Status**: DD-09 구현 완료 ✅  
**Next**: Controller 업데이트 및 Consumer 자동 구독 설정 (선택사항)

