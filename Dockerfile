# syntax=docker/dockerfile:1.4
FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY --from=yongjun0511/clokey-docker:dependency-cache /build /build

COPY src src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=secret,id=gradle-cache-config \
    export $(cat /run/secrets/gradle-cache-config | xargs) && \
    ./gradlew clean build -x test --no-daemon --configuration-cache --build-cache

FROM openjdk:17-jdk-slim
ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

