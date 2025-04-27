# syntax=docker/dockerfile:1.4

#### 1) 의존성만 다운로드
FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

#### 2) 전체 빌드 (캐시 마운트 적용)
FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

# 1단계에서 받은 의존성 결과만 통째로 복사
COPY --from=dependencies /build /build
COPY src src

# BuildKit 캐시 마운트로 ~/.gradle 디렉토리 유지
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    --mount=type=cache,target=/root/.gradle/build-cache \
    chmod +x gradlew && \
    ./gradlew clean build -x test --no-daemon

#### 3) 실행용 이미지
FROM openjdk:17-jdk-slim

ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime \
 && echo $TZ > /etc/timezone

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
