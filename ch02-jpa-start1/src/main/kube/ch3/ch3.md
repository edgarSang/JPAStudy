## 쿠버 3장
3.2 yaml 또는 json 디스크립터로 파드 생성
(시작시 “did you specify the right host or port” 오류로 안됏을때 도커부터 실행할것)
```
## 노드 2개만듬 mulinode 접두사로
minikube start --nodes 2 -p multinode 
```
— 3.3 레이블을 이용한 파드 구성.
이제 파드 두개가 클러스터에서 실행되고 있다. 레이블을 통해 파드와 기타 다른 쿠버네티스의 오브젝트의 조직화가 이뤄진다.
— 3.3.1 레이블
레이블은 리소스에 첨부하는 키-값 쌍으로, 레이블 셀렉터를 사용해 리소스를 선택할 때 활용된다. 레이블은 리소스내에서 고유하다.

— 3.3.2 라벨표시
```
 kubectl get po --show-labels
 kubectl get po -L creation_method,env
```
— 3.3.3 기존 파드 레이블 수정

```
kubectl label po kubia-manual creation_method=manual
## 기존 레이블을 변경시에는 오버라잇옵션필요
kubectl label po kubia-manual-v2 env=debug --overwrite
```

— 3.4 레이블 셀렉터를 이용한 파드 부분집합 나열
```
kubectl get po -l creation_method=manual
##env 레이블은 가지고 있으나 무엇이든 다볼땐
kubectl get po -l env
## env 안가지고 있는것 볼땐
kubectl get po -l '!env'
## 레이블 가지고있으나 매뉴얼이 아닌것
kubectl get po -l creation_method!=manual
```

— 3.4.2 레이블 셀렉터에서 여러조건 사용
```
##app이 pc이면서 라벨 베타
app=pc,rel=beta

```

— 3.5 레이블과 셀럭터를 이용해 파드 스케줄링 제한
- 지금까지는 워커노드 전체에 무작위로 스케줄링
- 이건 쿠버네티스에서 적절함, 모든노드를 하나의 대규모 ㅍ ㅡㄹ랫폼으로 노출, 스케줄링은 중요x
- 그러나 파드 스케줄링 위치가 약간 영향인 경우있음.  가령 일부는 HDD 나머지 SDD 특정파드를 한구룹에 나머지를 다른구룹에 또는 GPU 가속을 제공하는 노드에만 계산이필요한 파드를 배정 등

— 3.5.1 워커노드 분류에 레이블사용
- 앞서 말한것처럼 파드만이 유일한 레이블 부착가능한 쿠버 리소스아님
- 노드 포함 모든 리소스에 레이블 부착가능.
- 클라우드에 GPU를 가지고 있는 노드가 있다고 가정, gpu=true 레이블추가
```
kubectl label node minikube gpu=true
##조회 
kubectl get nodes -l gpu=true
##gpu 속성 show
kubectl get nodes -L gpu 
```

— 3.5.3
- 그러나 지정한 node가 오프라인 상태인경우 파드가 스케줄링 되지 않을 수 있다.

— 3.6 파드에 어노테이션 달기
- 파드 및 다른오브젝트는 라벨 말고도 어노태이션을 가질 수 있다.
- 레이블은 오브젝트를 묶을수 있지만, 어노테이션은 그럴 수 없다.
- 어노테이션은 더 많은 정보를 보유 할 수 있다.
- 어노테이션이 유용한 경우는 파드나 다른 API오브젝트에 설명을 추가해 두는것이다.
- 오브젝트를 만든 사람 이름을 어노테이션으로 지정해 두면 클러스터 작업시 좀더 쉽게 협업가능

— 3.6.1
Annotation 조회
```
kubectl get po kubia-manual -o yaml
## Kubernetes.io/create-by 어노테이션이 보일것이다. 비교적 라벨보다 큰정보 포함가능 256kb까지
```

— 3.6.2 파드에 어노테이션 추가
```
kubectl annotate pod kubia-manual mycompany.com/someannoation="foo bar"
##조회
kubectl describe pod kubia-manual
```

— 3.7 네임스페이스를 사용한 리소스 그룹화
- 레이블을 이용해 파드와 다른오브젝트를 그룹으로 묶었다.
- 오브젝트 그룹은 서로 겹칠 수 있다.
- 오브젝트를 겹치지않는 그룹으로 분할 하고 자 할때는 어떻게 해야할까?
- 프로세스를 격리하는 리눅스 네임스페이스가 아님
- 오브젝트의 이름의 범위를 제공함
- 모든 리소스를 하나의 단일 네임스페이스에 두는 대신에 여러 네임스페이스로 분할가능
- 네임스페이스는 같은 리소스의 이름을 다른 네임스페이스에 걸쳐 여러번 사용 가능

— 3.7.
-  리소스를 프로덕션,개발,QA환경으로 나누어 사용 할 수 있다.
-  서로다른 두 네임스페이스는 동일한 이름의 리소스를 가질 수 있다.
- 리소스 이름은 네임스페이스 안에서만 고유함.

— 3.7.2. 다른 네임스페이스와 파드 보기
```
kubectl get ns
## namespaceeotls -n 가능
kubectl get po --namespace kube-system
```
- 지금까진 default 네임스페이스에서만 작업진행함.

3.7.3 네임스페이스 생성
```
kubectl create -f custom-namespace.yaml
```
3.7.4 다른 네임스페이스의 오브젝트 관리
 —n항목을 넣거나 kubectl create를 할때 네임스페이스를 지정한다.
```
kubectl create -f kubia-manual.yaml -n custom-namespace
```
 — 동일한 이름을 가진 파드가 default 네임스페이스와 custom-namespace안에 있다.
 — 다른네임스페이스 오브젝트를 나열하거나 수정,삭제할때는 -n을 추가해야한다.
 - 네임스페이스를 ㅣ쩡하지않으면 현재 컨텍스트에 있는 기본네임스페이스에서 작업을 수행한다.
 - 현재 컨텍스트와 네임스페이스는 kubectl config 명령으로 변경 가능하다.
```
## kcd some=namepsace명령으로 네임스페이스 전환가능
alias kcd='kubectl config set-context $(kubectl config current-context) --namespace '
```

3.7.5 네임스페이스 격리 이해
- - 실행중인 오브젝트에 대한 격리는 제공 안함.
- - 예를들어 다른 사용자들이 서로 다른 네임스페이스에 파드를 배포 할 때 해당파드가 서로 격리돼 통신할 수없다고 생각하지만 그건 아니다.
- - 쿠버네티스와 함께 배포되는 네트워킹 솔루션에 따라 다르다
- - 네트워크 솔루션이 네임스페이스간 격리를 제공하지 않는경우 네임스페이스간 IP주소를 알고있었다면, HTTP 요청과 같은 트래픽을 다른 파드로 보낼수 있음

3.8 파드의 중지와 제거
레이블 셀렉터를 이용해 파드를 다 삭제할 수도 있다.
```
#네임스페이스 전체삭제
Kubectl delete ns custom-namespace
#네임스페이스 유지하면서 파드 모두삭제
Kubectl delete po -all
Kubectl delete all —all
```