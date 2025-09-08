FROM openjdk:17-jdk-slim as stage1
WORKDIR /app

# 저장경로
# /app - gradle - gradle(파일)
#      - src - src
#      - build.gradle
#      - settings.gradle
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew . 
RUN chmod +x gradlew
RUN ./gradlew bootJar

# // *---- 스테이지로 나눴을 때 장점 ---*//
# 1. build 자동화
# 2. 환경 통제

# 두번째 스테이지
# 이미지 경량화를 위해 스테이지 분리
FROM openjdk:17-jdk-slim
WORKDIR /app
# stage1의 jar파일을 stage2로 COPY 
COPY --from=stage1 /app/build/libs/*.jar app.jar 

# 실행 :
# jar이름은 settings.gradle의 {rootProject.name} + {build.gradle의 version = '0.0.1-SNAPSHOT'}
ENTRYPOINT [ "java", "-jar", "app.jar"]

# 도커 이미지 빌드
# docker build -t ordersystem:v1.0 .
# 도커컨테이너 실행
# docker 내부에서 localhost를 찾는 설정은 루프백 문제 발생
# docker run --name my-ordersystem -d -p 8080:8080 ordersystem:v1.0

# 도커컨테이너 실행시점에 docker.host.internal을 환경변수로 주입
# docker run --name my-ordersystem -d -p 8080:8080 
#                   -e SPRING_REDIS_HOST = host.docker.internal 
#                   -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3307/ordersystem
#                       ordersystem:v1.0
