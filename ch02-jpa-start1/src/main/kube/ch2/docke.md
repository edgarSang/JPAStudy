
#### 도커 파일로 컨테이너 이미지 말기
```
docker build -t kubia-unhealthy .

#도커 이미지 조회
docker images

# kubia 이미지로 kubiacontianer라는 컨테이너 이미지실행
docker run --name kubiacontainer -p 8080:8080 -d kubia

#컨테이너 접근
curl localhost:8080
docker ps
#컨테이너 내부 셀 실행
docker exec -it kubia-container bash
(i 표준입력 오픈 상태 t  터미널 할당)

#실행중인 프로세스 조회
ps aux | grep app.js

#컨테이너 중지[삭제]
docker stop[rm] kubia-container

#추가 태그로 이미지 지정 [내이름/이미지]
docker tag kubia luska/kubia

#도커허브에 이미지 푸시
docker push luska/kubia

#다른머신에서 실행
docker run -p 8080:8080 -d luska/kubia
```