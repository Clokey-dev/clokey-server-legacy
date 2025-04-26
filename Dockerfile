# 1단계: 의존성만 빌드해서 캐시하기
FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

COPY build.gradle settings.gradle gradle.properties ./
COPY gradlew .
COPY gradle gradle

RUN ./gradlew dependencies --no-daemon

# 2단계: 소스 코드 복사 후 전체 빌드
FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY --from=dependencies /build /build
COPY src src

RUN ./gradlew clean build -x test --no-daemon

# 3단계: 실행용 이미지 레이어 생성
FROM openjdk:17-jdk-slim

ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
