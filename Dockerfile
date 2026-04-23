FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 1단계: 빌드 설정만 복사해 의존성 warm-up. src 변경이 의존성 해결을 무효화하지 않도록
# 레이어를 분리한다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# 2단계: 소스 복사 후 bootJar 산출. src 만 바뀌면 이 레이어만 재빌드된다.
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# non-root 실행. K8s 매니페스트의 runAsUser 값도 1001 로 맞춰야 한다.
RUN addgroup --system --gid 1001 app && \
    adduser --system --uid 1001 app

COPY --from=builder /app/build/libs/app.jar app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# MaxRAMPercentage: 컨테이너 메모리 limit 기반으로 힙 상한을 75% 로 잡는다.
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
