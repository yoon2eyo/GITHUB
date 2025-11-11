
# 컴포넌트 다이어그램 작성 가이드
레이어는 Interface layer, Business layer, system interface layer 로 나뉜다.
- Interface layer는 외부에서 들어오는 입력을 받는 컴포넌트들의 모음
- Business layer는 기능 구현 레이어
- system interface layer는 외부에 있는 나가는 연결의 컴포넌트들의 모음

## 패키지 작성 방법
- 패키지 내부에서의 연결은 모두 package 안에 작성한다
- 패키지 내부에서 외부로 나가는 연결은 패키지 정의 블럭이 모두 끝난 하단부에 작성한다.
- 상위 레이어로 연결되는 인터페이스는 컴포넌트보다 위쪽에 선언한다. 인터페이스 - 컴포넌트 순서로 연결
- 하위 레이어로 연결되는 인터페이스는 컴포넌트보다 아래쪽에 선언한다. 컴포넌트 - 인터페이스 순서로 연결

## 인터페이스 연결 방법
- 패키지 외부에서 연결시  `..(` 이런 형태로 아래 방향으로 연결되게 작성한다.
- 패키니 내부에서는 `.(` 옆으로 이어지게 연결. 단, 패키지 내부에서 2단을 넘어가는 연결은 아래 방향 `..(` 연결을 사용한다.

## 프로토콜 명시 방법
- 레이어간 연결되는 인터페이스에는 아래처럼 통신 프로토콜을 명시
예) Coordinator ..( IGateway : <<gRPC>>

# 문법
- interface, component 정의는 무조건 아래 형태의 정의를 따를것
- 별칭은 사용하지 말것
- 실제 코드에 있는 인터페이스명, 클래스명등 정의된 이름만 사용할 것

예제) 
```
@startuml
package "Interface Layer" {
  interface IApiGatewayEntry
  component ApiGatewayEntry
  IApiGatewayEntry -- ApiGatewayEntry
}

package "Business Logic Layer" {
  interface IGatewayRoutingService
  interface IRequestSignatureVerifier
  component RequestSignatureVerifier
  interface INetworkZonePolicy
  component NetworkZonePolicy
  interface IDownstreamDispatchCoordinator
  component DownstreamDispatchCoordinator
  
  interface IAuthorizationClient
  component AuthorizationClient
  interface IAuthenticationClient
  component AuthenticationClient
  component RequestRouter
  
  
  IAuthorizationClient -- AuthorizationClient
  IAuthenticationClient -- AuthenticationClient
  INetworkZonePolicy -- NetworkZonePolicy
  IDownstreamDispatchCoordinator -- DownstreamDispatchCoordinator
  IRequestSignatureVerifier -- RequestSignatureVerifier
  IGatewayRoutingService -- RequestRouter
  RequestRouter ..( IRequestSignatureVerifier
  RequestRouter ..( IDownstreamDispatchCoordinator
  RequestRouter ..( INetworkZonePolicy
  RequestRouter ..( IAuthenticationClient
  RequestRouter ..( IAuthorizationClient
  
}

package "System Interface Layer" {
  interface IAuthServiceApi
  interface IAuthorizationServiceApi
  component AuthorizationManager
  interface IAccessServiceGateway
  component AccessServiceGatewayClient
  
  IAccessServiceGateway -- AccessServiceGatewayClient
  IAuthServiceApi -- AuthorizationManager
  IAuthorizationServiceApi -- AuthorizationManager
}

ApiGatewayEntry ..( IGatewayRoutingService : <<HTTP>>
AuthenticationClient ..( IAuthServiceApi : <<HTTP>>
AuthorizationClient ..( IAuthorizationServiceApi : <<HTTP>>

DownstreamDispatchCoordinator ..( IAccessServiceGateway : <<gRPC>>

@enduml
```