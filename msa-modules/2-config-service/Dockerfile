# ==============================================================================
# Build Stage: 빌드 도구와 소스 코드로 JAR 파일 생성
# ==============================================================================
FROM gradle:8.11.1-jdk21 AS builder

WORKDIR /app

# Gradle 의존성 파일만 먼저 복사 (캐싱 최적화)
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

# Common modules 복사
COPY msa-modules/0-common-core msa-modules/0-common-core
COPY msa-modules/1-common-web msa-modules/1-common-web

# Config service 복사
COPY msa-modules/2-config-service msa-modules/2-config-service

# 빌드 (테스트 제외하여 빌드 시간 단축)
RUN gradle :config-service:build -x test --no-daemon

# ==============================================================================
# Runtime Stage: 경량 JRE 이미지로 실행
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 보안: non-root 유저로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Build stage에서 생성된 JAR 파일만 복사 (이미지 크기 최소화)
COPY --from=builder /app/msa-modules/2-config-service/build/libs/*.jar app.jar

# Health check 설정 (Docker/Kubernetes에서 활용)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9999/actuator/health || exit 1

# Port 노출
EXPOSE 9999

# JVM 최적화 옵션과 함께 실행
# - UseContainerSupport: 컨테이너 환경 인식
# - MaxRAMPercentage: 컨테이너 메모리의 75%까지 사용
# - Spring Profile: 환경변수로 주입 (기본값: prod)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
    "-jar", "app.jar"]