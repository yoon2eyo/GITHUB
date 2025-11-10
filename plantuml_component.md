# MLOps 컴포넌트 다이어그램 작성 가이드

## 0. 주의사항 및 제한사항

### 0.1 컴포넌트 내부 표현 제한
```plantuml
' 잘못된 방법 (❌)
component "DataCollector" as DC {
    + collectDailyTrainingData()
    - processingLogic()
}

' 올바른 방법 (✅)
component "DataCollector" as DC
note right of DC
  Responsibilities:
  - Daily data collection
  - Data processing
end note
```
- 컴포넌트 내부에 메소드나 필드를 직접 표시하지 않음
- 대신 note를 사용하여 컴포넌트의 책임과 기능을 설명

### 0.2 인터페이스 메소드 표현
```plantuml
' 올바른 방법 (✅)
interface "IAuthRepository" as IAuth {
    + findRecentUsers(date: Date): List
}

' 메소드가 많은 경우 주요 메소드만 표시
interface "IComplexService" as ICS {
    + mainOperation()
    .. other operations ..
}
```
- 인터페이스는 메소드 시그니처 표시 가능
- 너무 많은 메소드가 있는 경우 주요 메소드만 표시

## 1. 구조적 표현 원칙

### 1.1 레이어드 아키텍처
```
package "MLOps Core" {
    package "Interface Layer" { ... }    // 외부 시스템과의 통신 담당
    package "Service Layer" { ... }      // 핵심 비즈니스 로직
    package "Storage Layer" { ... }      // 영속성 계층
}
```
- 계층별 명확한 책임 분리
- 각 계층은 자신의 역할에 맞는 컴포넌트만 포함

### 1.2 내부 인터페이스 배치
```
package "MLOps Core" {
    interface "IServiceName" { ... }     // 내부 인터페이스는 Core 패키지 안에 배치
    package "Layer" { ... }              // 실제 구현 컴포넌트
}
```

## 2. 인터페이스 표현 규칙

### 2.1 제공 인터페이스 (Provided Interface)
```
Component ..|> Interface    // 볼 표기법 사용
```
- 컴포넌트가 구현하는 인터페이스
- `..|>` 표기로 인터페이스 구현 표시

### 2.2 요구 인터페이스 (Required Interface)
```
Component -( Interface     // 소켓 표기법 사용
```
- 컴포넌트가 필요로 하는 인터페이스
- `-(` 표기로 인터페이스 의존성 표시

## 3. 포트 사용 규칙

### 3.1 외부 통신용 포트
```
package "Core" {
    portin "external_in" as in_port      // 외부에서 들어오는 데이터
    portout "external_out" as out_port   // 외부로 나가는 데이터
}
```
- Core 패키지 경계에만 포트 정의
- 외부 시스템과의 통신 지점을 명시적으로 표현

### 3.2 내부 통신
```
ComponentA --> ComponentB   // 포트 없이 직접 연결
```
- 내부 컴포넌트 간 통신은 포트 없이 직접 연결
- 의존성 방향을 화살표로 명확히 표시

## 4. 스타일링 설정
```plantuml
' 기본 설정
!pragma layout smetana
allowmixing
skinparam componentStyle rectangle
skinparam backgroundColor transparent
skinparam packageStyle rectangle
skinparam portStyle solid
skinparam interfaceStyle rectangle
```

## 5. 문서화 요소

### 5.1 노트 사용
```
note right of Component : Description
note left of Layer : Layer Responsibility
```
- 컴포넌트나 계층의 책임을 명확히 설명
- 가장 단순한 형태의 note 구문 사용
- 한글 사용 시 인코딩 문제가 발생할 수 있으므로 영문 사용 권장
- as 식별자나 end note 블록 구문 사용 지양
- 예시:
  ```
  // 권장
  note right of IRepo : Generic CRUD Operations
  
  // 비권장
  note right of IRepo as N1
    한글 설명
  end note
  ```

### 5.2 범례 추가
```
legend right
  시스템 구조 설명
  - 주요 기능 1
  - 주요 기능 2
end legend
```

## 6. 데이터 흐름 표현

### 6.1 외부 데이터 흐름
```
external_port --> core_port : "데이터 설명"
core_port --> internal_component
```
- 외부 시스템과의 데이터 흐름은 포트를 통해 표현

### 6.2 내부 데이터 흐름
```
ComponentA --> ComponentB : "데이터 설명"
```
- 내부 컴포넌트 간 데이터 흐름은 직접 연결

## 7. 도메인 모델 표현 규칙

### 7.1 도메인 모델과 인터페이스 관계
```plantuml
' 잘못된 방법 (❌) - 도메인 모델이 직접 인터페이스를 제공하는 것처럼 보임
package "Domain Layer" {
    class "UserAccount" as UA
    interface "IUserAccount" as IUA
    UA ..|> IUA
}

' 올바른 방법 (✅) - 인터페이스가 도메인 모델을 사용하는 관계 표현
package "Domain Layer" {
    class "UserAccount" as UA
}
package "Interface Layer" {
    interface "IUserRepository" as IUR
}
' 점선 화살표로 "uses" 관계 표현
IUR ..> UA : uses
```

### 7.2 도메인 모델 표현 지침
- 도메인 모델은 데이터 전송 객체(DTO) 역할을 함
- 인터페이스가 도메인 모델을 사용하는 관계는 점선 화살표(`..>`)로 표현
- `uses` 레이블로 관계의 의미를 명확히 표시
- 도메인 모델은 독립적인 패키지로 구성
- 도메인 모델 클래스는 순수한 데이터 구조만 포함

## 8. 권장 사항 요약
1. 컴포넌트 내부 구현 세부사항은 표시하지 않음 (메소드, 필드 등)
2. 인터페이스는 핵심 메소드만 표시
3. 복잡한 다이어그램은 계층별로 분리
4. 컴포넌트의 책임은 note로 문서화
5. 중요한 데이터 흐름은 관계선에 레이블로 표시
6. 시스템 전체 맥락은 legend로 제공
7. 명확한 명명 규칙 사용 (PascalCase for components, interfaces)
8. 컴포넌트 간 책임과 관계를 명확히 표현
9. 외부 의존성과 내부 의존성을 구분하여 표현
10. 계층화를 통한 관심사 분리
11. 인터페이스를 통한 결합도 관리
12. 포트를 통한 외부 통신 경계 명확화
