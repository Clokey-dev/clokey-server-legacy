# syntax=docker/dockerfile:1.4

FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

COPY gradlew .
COPY gradle.properties ./
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY build.gradle settings.gradle ./

RUN mkdir -p /root/.gradle && echo "org.gradle.caching=true" > /root/.gradle/gradle.properties

RUN ./gradlew dependencies --no-daemon

FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY --from=dependencies /build /build

COPY src src

RUN --mount=type=secret,id=gradle-cache-config \
    bash -c "source /run/secrets/gradle-cache-config && ./gradlew clean build ..."

FROM openjdk:17-jdk-slim

ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
