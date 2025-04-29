# syntax=docker/dockerfile:1.4

FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

COPY gradlew .
COPY gradle.properties /root/.gradle/gradle.properties
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY --from=dependencies /build /build
COPY src src

RUN --mount=type=secret,id=GRADLE_BUILD_CACHE_URL \
    --mount=type=secret,id=GRADLE_BUILD_CACHE_USERNAME \
    --mount=type=secret,id=GRADLE_BUILD_CACHE_PASSWORD \
    bash -euxo pipefail -c " \
      echo '==== Mounted Secrets ====' && \
      ls -l /run/secrets && \
      export GRADLE_BUILD_CACHE_URL=\$(cat /run/secrets/GRADLE_BUILD_CACHE_URL | tr -d '\r\n') && \
      export GRADLE_BUILD_CACHE_USERNAME=\$(cat /run/secrets/GRADLE_BUILD_CACHE_USERNAME | tr -d '\r\n') && \
      export GRADLE_BUILD_CACHE_PASSWORD=\$(cat /run/secrets/GRADLE_BUILD_CACHE_PASSWORD | tr -d '\r\n') && \
      ./gradlew clean bootJar -x test --build-cache
    "


FROM openjdk:17-jdk-slim

ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ >/etc/timezone

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
