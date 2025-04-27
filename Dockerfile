# syntax=docker/dockerfile:1.4
FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

# wrapper 관련 파일만 복사
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

# dependencies 스테이지 그대로 재사용
COPY --from=dependencies /build /build
COPY src src


RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew clean build -x test --no-daemon --configuration-cache

FROM openjdk:17-jdk-slim
ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
