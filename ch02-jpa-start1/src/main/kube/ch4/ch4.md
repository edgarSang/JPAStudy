##4 리플리케이션과 그 밖의 컨트롤러: 파드 배포
 - 파드는 배포 가능한 기본단위이다
 - 파드를 수동생성 했지만, 실환경에서 애플리케이션 개입없이 안정적을 원한다.
 - 그래서 직접 파드를 생성할 일은 없을것이다. 레플리 케이션 컨트롤러 또는 디플로이먼트와 같은 유형의 리소스를 생성해 파드를 생성하고 관리한다.
 - 4장에선 쿠버가 해당 컨테이너를 모니터링하고 실패하면 자동으로 다시 시작하는 방법을 배울것,
 - 그러나 노드전체에 장애가 발생하면 노드에 있는 파드는 유실, 레플리케이션 or 컨트롤러가 해당파드를 관리하지 않으면 재실행 x
 - 어떻게 쿠버가 컨테이너 헛빗 체크하고 죽었을경우 살리는지 살펴볼 것.
 - 관리되는 파드를 이용해 지속적으로 실행되는 파드를 실행하는 방법 및 한가지 작업만 수행한 뒤 중지되는 파드를 실행하는 방법 배울것,

#### 4.1 파드 안정적으로 유지하기.
 - 쿠버 장점: 컨테이너 목록 쿠버에 제공시, 컨테이너를 계속 클러스터어딘가에서 실행시킴
 - 파드 생성, 파드 워커노드 지정 해당노드에서 파의 컨테이너가 실행되도록 함으로써 이작업 수행.
 - 컨테이너가 죽는다면? kubelet이 파드를 실행 및 파드가 존재하는한 컨테이너 계속실행시킴
 - 컨테이너안 주 프로세스 크러시 발생시 kuberlet이 컨테이너 다시시작.
 - 즉 애플리케이션 다시시작 없이 in 쿠버 만으로 자동치유 능력 생김
 - but App은 프로세스 크래시 없이도 작동 중단되는 경우가 있음
    - 예로 자바 OutOfMemoryErrors를 발생시키더라도 JVM프로세스는 계속됨
    - 쿠버에 신호를 보내서 자동 재시작하게 하면 좋을텐데.
    - 그러나 앱이 무한루프나 데드락에 빠져서 응답을 하지않는다면? 모니터링해야됨
    
#### 4.1.1. 라이브니브 프로브
 - 라이브니브 프로브를 통해 컨테이너가 살아있는지 확인할 수 있다.
 - 파드의 스펙에 각 컨테이너의 라이브니스 프로브 지정가능
 - kuber가 주기적으로 프로브 실행 후 프로브 실패시 컨테이너 재시작 (레디니스 프로브도 있지만 전혀다름 착각 NO)
 - 쿠버는 세가지 매너키즘을 이용해 컨테이너에 프로브를 실행
    1. Http get 프로브는 지정한 주소:포트에 get요청을 날린뒤 에러가 없으면 성공이라 간주, 에러 반환 및 응답안하면 컨테이너 다시시작
    2. TCP 소켓 프로브는 컨테이너에 지정된 포트에 tcp 연결 시도
    3. exec 프로브는 컨테이너 내의 임의 명령을 실행하고, 명령의 종료상태 코드 확인. 상태코드 0 성공 다른거 다실패.
    
#### 4.1.2. Http 기반 라이브니스 프로브 생성
 - 5번째 요청이후 http가 에러를 내도록 node.js 앱 수정해보자
 - 크래시된 컨테이너의 애플리케이션 로그얻기
 - 3장에서의 로그는 현재컨테이너의 명령을 표시한다.
 - warn: 라이브니스 야믈파일 책대로하면안됨

```
# 죽기전 컨테이너의 로그얻기
kubectl logs mypod --previous
# 컨테이너가 다시시작된후 파드의 디스크립션
kubectl describe po kubia-liveness
```
 - last state exit code 란을보면 종료코드 137이 보인다. 외부신호에 의해 종료됐음을 나타낸다. 137은 두숫자를 합한값으로 128+x다
 - 여기서 x는 프로세스에 전송된 시그널번호이다. 이시그널로 종료되었다
 - x는 sigkill 시그널번호인 9이다.
 - 아래쪽에 나열된 event는 종료된 이유를 보여준다. 컨테이너가 비정사앗ㅇ태임을 보여줌
 #### 컨테이너가 종료되면 재시작이 아닌 완전 새로운컨테이너가 생성됨

#### 4.1.4 라이브니스 프로브의 추가 속성 설정
 - describe 옵션으로 라이브니스의 속성에는 delay timeout period success failure 등이 있다는것을 알수있다.
 - delay는 컨테이너가 시작된후 언제 시작될지, timeout이 1이므로 1초안에 응답. 컨테이너는  period 10초마다 프로브 동작 3번연속 실패하면 컨테이너가 다시시작된다.
 - 이런 변수는 yaml파일에 추가로 지정할 수 있다.
 - tip 컨테이너가 실행되면 받을준비가 안될수도있으므로 딜레이를 좀주자
 - kubelctl decribe를 보면 137 또는 143으로 인해 라이브니스 프로브의 실패로 컨테이너 재시작을 파악할 수 있을것이다.
 - 137 128+9(sigkill) 143 128+15(sigterm)에 해당

#### 4.1.5 효과적인 라이브니스 프로브 생성
 - 실행 환경중 파드는 반드시 라이브니스 프로브를 정의해아함
 - 라이브니스 프로브가 확인해야할 사항
   - 단순히 서버응답만 검사하나 그래도 재시작시키는것에 의미
   - 더나은 라이브니스 프로브를 위해 url경로도 구성가능함(인증 없앰등)
   - 외부 영향을 받지 않도록 한다.(db 등)
 - 라이브니스 프로브는 가볍게 유지(1초내 응답)
 - tip 컨테이너에서 자바애플 실행시 exec 프로브 보다는 http get 라이브를 사용하자

 - 프로브 재시도에 루프를 구현마라
   - 프로브가 자체적 재시동 하니 루프하지마라
   
 - 프로브가 실패한경우 재시작은 호스팅하는 노드의 kubelet에서 수행한다.
 - 그러나 노드 자체에 크래시는 파드의 대체 파드를 생성하는것은 컨트롤 플레인에 몫이다.
 - 지금까지 한것은 직접생성했기때문에 아무것도할수없다.
 - 그로인해 레플리케이션 컨트롤러 또는 유사한 매커니즘으로 파드를 관리해야한다

### 4.2 레플리케이션 컨트롤러 소개
 - 레플리케이션 컨트롤러는 파드가 항상 실행되도록 보장한다.
 - 사라진 파드를 감지해 교ㅛ체파드를 생성한다.
 - 애플리케이션 컨트롤러가 관리하는 파드만 재생성된다.
 - rc는 파드의 레플리카를(복제본)을 작성하고 관리하기 위한것이다. 이것이 이름으 유래다

#### 4.2.1 레플리케이션 컨트롤러의 동작
 - 실행중인 파드를 지속적으로 모니터링 하고 유형의 실제 파드수가 의도하는 수와 일치하는지 계속확인.
 - 이런파드가 적다면 새 복제본생성 너무많으면 복제본 제거
 - 어떻게 많이 생길 수 있나?
   - 누군가 같은유형의 파드 수동생성
   - 누군가 기존파드 타입변경
   - 누군가 의도하는 파드수를 줄인다.
   
 - 파드 type이란 말을썻으나 그런건 실존하지않음.
 - 파드는 유형이아니라 레이블 셀렉터와 일치하는 파드세트에 작동한다.

 #### 레플리케이션 컨트롤러의 세가지 요소 이해
   - 레이블 셀렉터는 레플리케이션 컨트롤러 범위에 있는 파드를 결정한다
   - 레플리카 수는 실행할 파드의 의도하는 수를 지정한다
   - 파드 템플릿은 새로운 ㅍㅏ드 레플리카를 만들때 사용된다.
 - 레플리카 수, 레이블 셀렉터, 파드 템플릿은 언제든 수정가능하지만 레플리카의 수 변경만 기존파드에 영향을 미침

 #### 컨트롤러의 레이블 셀렉터 또는 파드 템플릿 변경의 영향 이해
 - 레이블 셀렉터와 파드 템플릿을 변경해도 기존 파드에는 영향을 미치지 않음.
 - 레이블 셀렉터를 변경하면 기존 파드가 rc범위를 벗어나므로 해당파드에 관리를중지
 - rc는 파드 생성후 파드의 콘텐츠(컨테이너 이미지, 환경변수 등)에 신경을 쓰지않음
 - 파드 템플릿은 새 파드를 생성할때만 영향을 미침
 - 파드 템플릿은 새 파드를 찍어내기위한 쿠키커터 이다

 #### 레플리케이션 컨트롤러 사용시 이점
 - 기존파드가 사라지면 새 파드를 시작 파드가 항상 실행되게함
 - 클러스터 노드에 장애가 발생하면 발생 노드에서 실행중인 모든 파드에 관한 교체 복제본이 생성.
 - 수동 또는 자동으로 파드를 쉽게 수평확장 할 수 있게한다.(수평적 파드오토스케일링)
   (Note: 파드인스턴스는 다른 노드로 재배치가 아니라 교체되는 인스턴스와 관련없는 새로운 파드 인스턴스를 만듬.)
   
#### 4.2.2. 레플리케이션 컨트롤러 생성
 - 템플릿 파드 레이블은 레플리케이션의 레이블 셀렉터와 반드시 일치해야한다. 그렇지않으면 파드 무한정 생산.
 - 이런경우를 방지하기위해 rc정의를 검증하고 잘못구성되면 이를 받아들이지 않음
 - 셀렉터를 지정하는것도 선택가능함
 - 셀렉터를 지정하지 않으면 템플릿의 레이블로 자동설정됨
#### tip: rc정의시 파드 셀렉터를 지정하지말라, 쿠버네티스가 템플리셍서 추출하도록 해라, 이렇게하면 yaml을 좀더 간결하게 유지가

#### 4.2.3 레플리케이션 컨트롤러 작동 확인
```
kubectl get pod -o wide
kubectl get rc
```
 - rc를 조회하면 DESIRED(의도)   CURRENT(현재)   READY(준비된) 파드수를 보여준다.
 - 5장에서 레디니스 프로브를 이야기할때 준비된 파드수를 알게 될것이다.

#### 컨트롤러가 새로운 파드를 생성한 원인 정확히 이해하기
 - rc가 새교체파드를 만들어 파드삭제에 대응함.
 - 삭제 그자체에 대응이아닌 결과적인 상태(부족한 파드 수) 에 대응 하는것
 - rc는 삭제파드에 대해 즉시 통지를 받지만, 이 통지 자체가 생성시키진않음
 - 컨트롤러가 실제파드수를 확인하고 적절한조취를 위한 트리거 역할

#### 노드장애 대응
 - 노드중 하나의 연결을 끊어보자
   (민쿠베는 이 예제 확인 불가능)
   
#### 4.2.4 레플리케이션 컨트롤러의 범위 안팎으로 파드 이동
 - 레플리케이션 컨트롤러가 생성한 파드는 어떤식으로든 rc와 엮이지 않음
 - rc레이블 셀렉터와 일치하는 파드만 관리
 - 파드의 레이블 변경시 rc범위서 제거나 추가될 수있다.
 - 파드가 rc에 묶여는 있지만 파드는 meatadata.ownerreferences 필드에서 rc를 참조 이필드를 사용해 파드가 속한 rc찾기가능
 - app=kubia 인 파드를 레이블을 변경시커 rc 범위 밖으로 이동시켜보자
```
kubectl label pod kubia-dmdck type=special
#변화없음 type만 변화했기때문에
kubectl get pods --show-labels 
# 오버라이트 안붙이면 경고만 표시후 레이블 안바꿈 기존 레이블 안바꾸려고
kubectl label pod kubia-dmdck app=foo --overwrite
kubectl get pods -L app
```

#### 컨트롤러에서 파드를 삭제하는 실제사례
 - 일정 시간이 지난후 특정이벤트가 발생한 파드가 제대로 동작하지않는 버그가 있다가정
 - 파드가 오작동을 안다면 rc범위 밖으로 빼네 새파드로 교체하도록 한다.
 - 원하는 방식으로 파드를 디버그하거나 문제를 재연해보고 완료후 삭제
#### rc의 레이블 셀렉터 변경
 - rc완전히 이해했는지 다음질문에 답변 해봐
 - 레이블 변경대신 레플리케이션 컨트롤러의 셀렉터 변경하면?
 - 새로운 세개 파드생성하게 될것이라면 정답
 - rc는 레이블 셀렉터를 변경허요하지만, 다른 리소스는 안그럼

#### 4.2.5 파드템플릿 변경
 - rc의 파드템플릿은 언제나 수정 가능하다
 - 쿠키커터 교체하는것 이미 쿠키에는 영향 x
```
kubectl edit rc kubia
```

#### 4.2.6 수평파드 스케일링
 - 원하는 복제분 변경 매우쉬움, 수평 확장 매우쉬움
 - 리소스의 레플리카 필드값만 변경하면 됨

#### 스케일 업 하기
```
kubectl scale rc kubia --replicas=10
#or
kubectl edit rc kubia
#scale down
kubectl scale rc kubia --replicas=3
```

#### 스케일링에 대한 선언적 접근법 이해
 - 의도하는 상태를 지정함
 - 선언적 접근방식을통해 클러스터와 쉽게 상호작용가능

### 4.2.7 레플리케이션 컨트롤러 삭제
 - kubectl delete를 통해 rc를 삭제하면 파드도 삭제됨
 - rc만 삭제하고 파드 둘수있음
```
kubectl delete rc kubia --cascade=false
```

### 4.3 레플리케이션 컨트롤러 대신 레플리카셋 사용
 - 후에 rc와비슷한 레플리카셋이 도입됨
 - 차세대 이며 완전히 대체할것

#### 4.3.1 레플리카셋과 레플리케이션 컨트롤러 비교
 - 둘이 비슷하지만 레플셋이 좀 더 풍부한 표현식을 갖고있음
 - 예를들어 rc는 env=production과 env=devel인 파드를 동시매칭 불가능, 레플리카셋은 가능
 - rc는 레이블 키의 존재만으로 매칭 x 레셋은 가능
 - 즉 모든 env 레이블을 가진 파드 가능

```
#잠깐 이전파드 삭제하기
kubectl delete nodes -l app=kubia
kubectl create -f kubia-rset.yaml
```
#### API 버전 속성에 대해
 - API그룹 (apps)
 - API 버전 (v1beta2)
 - 어떤건 api version 필드를 지정할필요가 없다. 
 - 레플셋 beta2 중지 apps/v1 으로 사용
```
kubectl get rs
```

#### 4.3.4 레플리카셋의 더욱 표현적인 레이블 셀레터 사용
 - in 은 레이블의 값이 지정된 값중 하나와 일치
 - not in은 레이블 값 일치하지 말아야함
 - exists 파드는 지정된 키를 가진 레이블이 포함되어야한다(값x)
 - doesnotexist는 키를 가진 레이블이 포함돼 있지 않야야함

### 4.4 데몬셋을 사용해 각 노드에서 정확히 한개의 파드 실행하기.
 - 예를들어 노드수집기등 모든 노드당 하나의 파드만 실행하는경우를 보자
 - 일반 환경에선 보통 systemd 를 이용해 데모을시작함.
 - 위같이하면 쿠버의기능 활용불가

#### 4.4.1 데몬셋으 모든 노드에 파드 실행하기
 - 모든노드 파드하나실행시 데몬셋 obj 사용해야됨
 - rs rc와 매우 비슷하나 타깃노드가 지정, 쿠버 스케줄러 건너뜀
 - 데몬셋엔 복제본수라는 개념이없음
 - 노드가 다운되도 다른 곳에서 파드 생성 x

#### 4.4.2 데몬셋을 사용해 특정 노드에섬나 파드 실행
 - 일부 노드배포 안하려면 node-Selector속성을 지정하면된다.
   
#### 예제를 통한 데몬셋 
 - 4개중 3개의 노드에만 ssd가 있다가정하자, ssd 모니터를 배포해보자

```
kubectl get nodes
kubectl label nodes <node-name> <label-key>=<label-value> 
kubectl get nodes --show-labels
```

#### 노드에서 레이블제거하기
```
kubectl label node multinode disk=hdd --overwrite
kubectl get po
```

### 4.5 완료 가능한 단일 테스크를 수행하는 파드 실행

#### 4.5.1 잡 리소스소개
 - 프로세스가 성공적으로 수행되면 파드를 다시실행 안함
 - 장애가 발생시 다른 노드로 다시 스케줄링
 - kubectl scale job batch-job-multi --replicas 3 명령어 안먹음
```
kubectl delete all --all
```