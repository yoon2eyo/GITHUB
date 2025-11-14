DD-01 부터 DD1-09 까지 9개의 디자인 디시젼 문서가 있습니다.
우리 시스템은 최종적으로 이 9개의 문서를 기반으로 최종 선택된 안으로 SW를 설계하고 구현하였습니다.
DD-01~DD-09 문서마다 2~3개의 디자인 어프로치가 담겨있습니다. 그리고 최종적으로 우리가 선택한 rationale에 대한 언급이 되어있습니다.
해당 문서의 내용을 아래 가이드에 철저하게 맞춰서 재작성해주세요.
너무 전문적이고 어려운 용어는 사용하지 말고 석사과정의 학생 수준으로 작성해주세요.
문장은 평서문은 이다~같은 짧은 어조로 작성해주세요

	3장에서 식별된 각 QA를 위한 Design decision이 제시되어야 한다. 즉 Architectural Driver 특히 QA를 충족시키기 위한 각 설계 결정 사항들을 나열한다.
	설계측면에서 중요한 우선순위에 따라 넘버링해주시기 바랍니다.
	식별된 QA를 달성하기 위한 Design Decision이 식별되었는가? 예를 들어 성능, 가용성, 유지보수성 별로 Design Decision 필요하다.
	Title은 각 design decision에서 해결하고자 하는 design goal을 포함하는 간결한 이름으로서 제시하였는가?
	각 Design decision은 독립적인가? 기존 Design decision의 결정으로 인해서 새롭게 결정이 필요한 Design decision이 있다면 이것도 제시한다.
	Target Quality Attributes는 해당 결정이 목표하는 Design Goal에 포함된 QA입니다.


4.4.2.	DD-01 Title Description

4.4.2.1.	Design Goal

<작성 방법>
	해당 Design Decision과 관련된 Design Goal을 모두 식별하였는가? 즉 Design Approach을 통해서 달성/확보하고자 하는 세부 목표/목적을 모두 나열하였는가?
	Design goal은 QA에 기반하여 제시되었는가? 
	Design goal을 수립하게 된 기반 QA 번호까지 명시하시기 바랍니다.

4.4.2.2.	Design Approach List

4.4.2.2.1.	Design Approach #1 Description: 인증을 X 컴포넌트에서 수행
<작성 방법>
	Design goal을 달성하기 위한 주요/타당한 Design approach들이 식별되었는가?
	각 Design approach의 설명이 목표로 하는 design goal과 일관성이 있어야 한다.
	이 Design Approach를 적절한 View(Structure, Behavior, Deployment 등)를 통해서 명확하게 표현한다. 
	적용 설계 approach에 대한 일반적인 설명이 아니라 해당 approach를 이 시스템에 적용할 때의 아키텍처 모습을 적절한 View를 통해 구체적으로 그림과 함께 제시한다.
	적용한 설계 방법 즉 패턴, Tactics 등과 함께 설명한다.
	Design goal(즉 QA)을 달성하기 위한 실질적이고 세부적인 design이 제시되어야 한다.
	Layer patter 적용의 경우: Layer의 수 및 각 layer의 역할, Layer interface 설계 등에 대한 decision이 제시될 필요가 있음
	Ping/echo vs Heartbeat에 대한 단순 비교 보다는
	Monitoring 시점/주기: Ping 및 heartbeat의 실행 시점/주기에 대한 decision
	Health check 수준: liveness, state consistency(invariant) 등에 대한 decision

4.4.2.2.2.	Design Approach #2 Description: 인증을 Y 컴포넌트에서 수행 
4.4.2.2.3.	Design Approach #3 Description: 인증을 Z 컴포넌트에서 수행

4.4.2.3.	Decision and Rationale

<작성 방법>
	제시된 Design Approach들 중에서 가장 적합한 Design Approach를 선정하는 근거를 기술한다.
	각 Design Approach 별로 Design Goal에 포함된 QA를 포함하여 3장에서 제시된 모든 관련된 QA 측면 및 필요한 관심사(concerns) 측면에서 장/단점을 제시한다.
	Pros/cons는 QA와 관심사 관점에서 구체적으로 장단점을 기술해야 한다. 즉 각 QA 관점에서 Response Measure에 대한 유/불리 와 Constraint 충족 여부 등이 명시적으로 제시될 필요가 있음


Quality Attribute	Analysis	Design
Approach (DA) #1 
설계결정 제목
(Selected)	…	DA #n 
설계결정 제목 (의미있는 제목을 쓰시기 바랍니다)
ID	Title				
QA-01	입차 성능	Pros	(+) 이미지 전송 오버헤드를 제거		(+)
		Cons	(-) 번호인식 장치 오작동 시 입차 처리 불가		(-) 
QA-02	출차 성능	Pros	(+)		
		Cons	(-) 번호인식 장치 오작동 시 출차 처리 불가		
QA-03	번호판추가 유지보수성	Pros	없음		
		Cons	(--) 		
QA-04	번호인식 정확도	Pros	해당없음		
		Cons	(-) 임베디드 기기 특성상 번호 인식 정확도가 낮을 수 있음		


