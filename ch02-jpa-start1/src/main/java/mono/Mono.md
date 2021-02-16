### MoNo와 플렉스의 차이 
 - Reactor는 Pivotal의 오픈소스 프로젝트로, JVM 위에서 동작하는 논블럭킹 애플리케이션을 만들기 위한 리액티브 라이브러리입니다
 - Mono는 0-1 의결과만을 처리하기 위한 리액터 객체
 - Flux는 0-N 을 처리하기 위한 객체
 - 여러 스트림을 하나의 결과를 모아줄 때 Mono를 씀,
 - Mono를 합쳐서 여러 개의 값을 처리하는 Flux
 - Reactor에서 제공하는 풍부한 연산자들(operators)의 조합을 통해 스트림을 표현할 수 있다. 
    - 예를 들어 Flux에서 하나의 결과로 값을 모아주는 reduce연산자는 Mono를 리턴함
    - Mono에서 flatMapMany라는 연산자를 사용하면 하나의 값으로부터 여러 개의 값을 취급하는 Flux를 리턴
    - Publisher인터페이스에 정의된 subscribe메서드를 호출함으로써 Mono나 Flux가 동작하도록 할 수 있다
      
### 과일 바구니 예제
 - basket1~basket3 까지 총 3개의 과일바구니 존재
 - 이 바구니를 가지는 List Baskets List 존재
 - Flux.fromIterable에 Iterable type의 인자를 넘기면 이 Iterable을 Flux로 변환해줌
 - 스프링에서 WebClient를 이용하여 특정 HTTP API를 호출하고 받은 JSON 응답에 여러 배열이 중첩되어 있고, 여기서 또 다른 API를 호출하거나 데이터를 조작하는 경우가 있

### 바구니 속 과일 종류(중복 없이) 및 각 종류별 개수 나누기
 - basketFlux로부터 중복 없이 각 과일 종류를 나열, 각각 바구니마다 과일이 몇개씩 있는지 출력코드
 - 함수 프로그래밍이 아닌 JAVA 절차지향적으로 생각해보면 안어려움 , foreach를 돌면서 Set or Map
 - flux 연산자 들로 어떻게 이를 할 수 있을까? 중복없이 값 처리 distinct 
 - 각 key 별로 스트림을 관리 하기 원한다면 group By가 있다
 - 스트림에서 값을 내려주는 count 라는 연산자도 존재
 - 새로운 Publisher 바꿔주는 3개의 연산자 존재 flatMap, flatMapSequential, concatMap
 - 플랫맵은 Publisher가 비동기로 동작할 때 순서를 보장하지 x
 -  flatMapSequential과 concatMap은 순서대를 보장하지만
 ```
  concatMap은 인자로 지정된 함수에서 리턴하는 Publisher의 스트림이 
  다 끝난 후에 그다음 넘어오는 값의 Publisher스트림을 처리하지만,
  flatMapSequential은 일단 오는 대로 구독하고 결과는 순서에 맞게 리턴하는 역할을 해서, 
  비동기 환경에서 동시성을 지원하면서도 순서를 보장할 때 쓰이는 것이 차이점입니다.
 ```