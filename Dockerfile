# syntax=docker/dockerfile:1.4
FROM gradle:8.5-jdk17 AS dependencies
WORKDIR /build

COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

COPY --from=dependencies /build /build
COPY src src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=secret,id=gradle-cache-config \
    export $(cat /run/secrets/gradle-cache-config | xargs) && \
    ./gradlew clean build -x test --no-daemon --configuration-cache --build-cache

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
