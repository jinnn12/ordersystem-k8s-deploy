FROM openjdk:17-jdk-alpine as stage1
WORKDIR /app
# 폴더 복사 -> 폴더명 폴더명
# 파일 복사 -> 복사하려는 파일 .
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
# 리눅스 환경에서 실행권한 없을 경우
RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째 스테이지( stage2 )
# 이미지 경량화를 위해 스테이지 분리
FROM openjdk:17-jdk-alpine
WORKDIR /app
# stage1의 jar파일을 stage2로 copy
# app.jar라는 이름으로 복사하겠다
COPY --from=stage1 /app/build/libs/*.jar app.jar

# 실행 : 'CMD' 또는 'ENTRYPOINT'를 통해 컨테이너 실행
ENTRYPOINT [ "java", "-jar", "app.jar" ]
# ENTRYPOINT [ "java", "-jar", "/app/build/libs/xxxx.jar" ]

# 도커이미지 빌드
# docker build -t ordersystem:v1.0 .
# 도커컨테이너 실행, 내부포트는 application-local, prod.yml 의 포트와 동일 해야만 한다
# docker 내부에서 로컬호스트를 찾는 설정은 '루프백 문제' 발생 -> 자기 자신을 찾는 문제
# docker run --name ordersystem -d -p 8080:8080 ordersystem:v1.0

# 도커컨테이너 실행시점에 'docker.host.internal'을 환경변수로 주입한다 
# docker run --name ordersystem -d -p 8080:8080 -e SPRING_REDIS_HOST=host.docker.internal -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3307/ordersystem ordersystem:v1.0