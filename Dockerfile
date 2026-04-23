FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup --system --gid 1001 app && \
    adduser --system --uid 1001 app

COPY --from=builder /app/build/libs/*.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
