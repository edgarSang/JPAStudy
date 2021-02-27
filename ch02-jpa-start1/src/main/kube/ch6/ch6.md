1. emptyDir 볼륨을 사용하여 fortune pod 생성해보고 curl 요청보내어 응답받아보기.
2. 볼륨을 사용하는 파드를 생성하고, 파드 내 MongoDB 데이터베이스에 도큐먼트를 추가해 퍼시스턴트 스토리지에 데이터쓰기 (find 명령으로 추가한 도큐먼트 확인)
3. 스토리지 클래스를 지정하지 않고 퍼시스턴트 볼륨 클레임 생성하기 (생성된 PVC 확인하기)


```
docker build -t edgarsang/fortune .
docker images
docker push edgarsang/fortune
kubectl create -f fortune-pod.yaml
kubectl port-forward fortune 8080:80
```