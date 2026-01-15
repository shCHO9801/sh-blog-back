# Java 17 slim 이미지 사용
FROM eclipse-temurin:17-jdk-alpine

# JAR 파일을 컨테이너에 복사
ARG JAR_FILE=app.jar
COPY build/libs/*.jar ${JAR_FILE}

# 환경변수 주입을 위한 .env 지원은 docker-compose에서 처리할 예정

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]