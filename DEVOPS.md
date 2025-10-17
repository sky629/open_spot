# Open-Spot DevOps Guide

이 문서는 Open-Spot 프로젝트의 DevOps 관련 가이드입니다.

## 📋 목차

- [로컬 개발 환경](#로컬-개발-환경)
- [Docker 빌드 및 배포](#docker-빌드-및-배포)
- [모니터링](#모니터링)
- [성능 테스트](#성능-테스트)
- [CI/CD](#cicd)
- [보안](#보안)

---

## 로컬 개발 환경

### 필수 요구사항

- Java 21
- Gradle 8.11.1
- Docker & Docker Compose
- PostgreSQL 15.4 + PostGIS
- Redis 7.2
- Kafka 7.4

### 빠른 시작

```bash
# 1. 인프라 서비스 시작 (PostgreSQL, Redis, Kafka)
./start-infrastructure.sh

# 2. 애플리케이션 서비스 시작
./start-services.sh

# 3. 헬스 체크
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/locations/health
curl http://localhost:8080/api/v1/notifications/health

# 4. 모든 서비스 종료
./stop-all.sh
```

---

## Docker 빌드 및 배포

### 멀티스테이지 Dockerfile

모든 서비스는 멀티스테이지 빌드를 사용하여 이미지 크기를 50% 감소시켰습니다.

**주요 개선사항:**
- ✅ JDK → JRE Alpine 이미지 사용 (800MB → 400MB)
- ✅ Gradle 캐싱 최적화로 빌드 시간 단축
- ✅ Health check 내장
- ✅ Non-root 유저로 실행 (보안 강화)
- ✅ JVM 메모리 최적화

### Docker Compose로 전체 스택 실행

```bash
# 프로덕션 모드로 실행 (Docker로 모든 서비스 실행)
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스만 재시작
docker-compose -f docker-compose.prod.yml restart location-service

# 전체 종료
docker-compose -f docker-compose.prod.yml down
```

### 모니터링과 함께 실행

```bash
# 애플리케이션 + 모니터링 스택 실행
docker-compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

# Grafana 접속: http://localhost:3000 (admin/admin)
# Prometheus 접속: http://localhost:9090
```

---

## 모니터링

### Prometheus + Grafana 스택

**구성 요소:**
- **Prometheus**: 메트릭 수집 및 저장
- **Grafana**: 대시보드 및 시각화
- **Loki**: 로그 수집 및 저장
- **Promtail**: 로그 전송 에이전트

### 모니터링 시작

```bash
# 모니터링 스택 시작
docker-compose -f docker-compose.monitoring.yml up -d

# Grafana 접속
open http://localhost:3000
# Username: admin
# Password: admin

# Prometheus 접속
open http://localhost:9090
```

### 주요 메트릭

**JVM 메트릭:**
- Heap Memory 사용량
- GC 활동
- Thread 수

**HTTP 메트릭:**
- Request Rate (요청 빈도)
- Response Time (응답 시간) - p50, p95, p99
- Error Rate (에러율)

**비즈니스 메트릭:**
- Database 커넥션 풀 사용량
- Redis 캐시 히트율
- Kafka Consumer Lag

### Grafana 대시보드

기본 제공 대시보드:
- **Spring Boot Services Overview**: 전체 서비스 모니터링

추가 대시보드 (공식 Grafana 대시보드 가져오기):
- Spring Boot 2.1 Statistics (ID: 10280)
- JVM (Micrometer) (ID: 4701)

---

## 성능 테스트

### Artillery 기반 부하 테스트

**설치:**
```bash
npm install -g artillery
```

### 테스트 실행

```bash
# 빠른 테스트 (1분)
./performance/run-tests.sh quick

# 전체 테스트 (5분 - Warm-up, Ramp-up, Sustained, Peak, Cool-down)
./performance/run-tests.sh full

# 스트레스 테스트 (고부하)
./performance/run-tests.sh stress

# 스파이크 테스트 (급격한 트래픽 증가)
./performance/run-tests.sh spike
```

### 테스트 시나리오

1. **Warm-up (30초)**: 5 req/sec
2. **Ramp-up (60초)**: 5 → 50 req/sec
3. **Sustained (120초)**: 50 req/sec
4. **Peak (60초)**: 50 → 100 req/sec
5. **Cool-down (30초)**: 10 req/sec

### 성능 기준 (SLA)

- **Error Rate**: < 1%
- **Response Time (p95)**: < 500ms
- **Response Time (p99)**: < 1000ms

### 리포트 확인

테스트 완료 후 HTML 리포트가 자동 생성됩니다:
```bash
open performance/reports/report_full_YYYYMMDD_HHMMSS.html
```

---

## CI/CD

### GitHub Actions 파이프라인

**워크플로우:**
1. **CI Pipeline** (`.github/workflows/ci.yml`)
   - 코드 품질 검사 (Kotlin Lint)
   - 단위 테스트 (서비스별 병렬 실행)
   - 통합 테스트 (Testcontainers)
   - 보안 스캔 (Trivy, TruffleHog)

2. **Docker Build** (`.github/workflows/docker-build.yml`)
   - Docker 이미지 빌드 (서비스별 병렬)
   - GitHub Container Registry에 푸시
   - 이미지 보안 스캔

### 트리거 조건

- `push` to `main` or `develop`
- `pull_request` to `main` or `develop`
- `tag` push (v*)

### GitHub Secrets 설정

Repository Settings → Secrets and variables → Actions에 추가:

```
CODECOV_TOKEN           # (선택) 코드 커버리지
```

**참고**: GITHUB_TOKEN은 자동으로 제공됩니다.

### 로컬에서 CI 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 서비스별 테스트
./gradlew :auth-service:test
./gradlew :location-service:test

# 코드 커버리지 리포트 생성
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 보안

### Secrets 관리

**⚠️ 중요: 절대로 실제 비밀 정보를 Git에 커밋하지 마세요!**

### 로컬 개발 환경 (`.env` 파일)

로컬 개발에서는 `.env` 파일을 사용합니다 (간단하고 편리함):

```bash
# 1. .env.example 파일 복사
cp .env.example .env

# 2. .env 파일 편집 (실제 값으로 변경)
vim .env
```

**`.env` 파일 예시:**
```bash
# Google OAuth2 설정
GOOGLE_CLIENT_ID=your-google-client-id-here
GOOGLE_CLIENT_SECRET=your-google-client-secret-here

# JWT 설정
JWT_SECRET=your-strong-jwt-secret-key-at-least-32-characters

# 데이터베이스 설정
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=openspot
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka 설정
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**주의사항:**
- ✅ `.env` 파일은 `.gitignore`에 포함되어 Git 추적 제외됨
- ✅ `.env.example`은 템플릿으로 Git에 포함됨
- ⚠️ `.env` 파일을 절대 커밋하지 마세요!

---

### 프로덕션 환경 (AWS Secrets Manager)

프로덕션 배포 시에는 **AWS Secrets Manager**를 사용합니다.

#### 1. AWS Secrets Manager에 Secret 생성

```bash
# AWS CLI로 Secret 생성
aws secretsmanager create-secret \
  --name open-spot/prod/jwt-secret \
  --secret-string "your-production-jwt-secret" \
  --region ap-northeast-2

aws secretsmanager create-secret \
  --name open-spot/prod/google-oauth \
  --secret-string '{
    "client_id": "your-google-client-id",
    "client_secret": "your-google-client-secret"
  }' \
  --region ap-northeast-2

aws secretsmanager create-secret \
  --name open-spot/prod/database \
  --secret-string '{
    "host": "your-rds-endpoint",
    "port": "5432",
    "database": "openspot",
    "username": "postgres",
    "password": "your-secure-password"
  }' \
  --region ap-northeast-2
```

#### 2. ECS Task Definition에서 Secrets 참조

```json
{
  "containerDefinitions": [
    {
      "name": "auth-service",
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:open-spot/prod/jwt-secret"
        },
        {
          "name": "GOOGLE_CLIENT_ID",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:open-spot/prod/google-oauth:client_id::"
        },
        {
          "name": "GOOGLE_CLIENT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:open-spot/prod/google-oauth:client_secret::"
        }
      ]
    }
  ]
}
```

#### 3. IAM 권한 설정

ECS Task Execution Role에 필요한 권한:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:open-spot/prod/*"
      ]
    }
  ]
}
```

#### 4. Secret Rotation (선택적)

```bash
# RDS 비밀번호 자동 로테이션 (30일마다)
aws secretsmanager rotate-secret \
  --secret-id open-spot/prod/database \
  --rotation-lambda-arn arn:aws:lambda:ap-northeast-2:ACCOUNT_ID:function:SecretsManagerRotation \
  --rotation-rules AutomaticallyAfterDays=30
```

---

### 보안 체크리스트

**로컬 개발:**
- [ ] `.env` 파일 생성 및 실제 값 입력
- [ ] `.env` 파일이 `.gitignore`에 포함됨 확인
- [ ] `.env.example`은 템플릿으로 유지 (실제 값 포함 안됨)

**프로덕션:**
- [ ] AWS Secrets Manager에 모든 Secret 생성
- [ ] ECS Task Definition에서 Secrets 참조 설정
- [ ] IAM 권한 최소화 (Least Privilege Principle)
- [ ] Secret Rotation 활성화
- [ ] HTTPS/TLS 인증서 설정 (ACM)
- [ ] Rate Limiting 적용
- [ ] Security Group 최소화 (필요한 포트만 열기)

---

### 스모크 테스트

배포 후 서비스가 정상 동작하는지 확인:

```bash
# 로컬 환경
./scripts/smoke-tests.sh http://localhost:8080

# 스테이징 환경
./scripts/smoke-tests.sh https://staging.open-spot.com

# 프로덕션 환경
./scripts/smoke-tests.sh https://api.open-spot.com
```

---

## 트러블슈팅

### 포트 충돌

```bash
# 사용 중인 포트 확인
lsof -i :8080
lsof -i :5432

# 프로세스 종료
kill -9 <PID>
```

### Docker 이미지 빌드 실패

```bash
# Docker 캐시 삭제 후 재빌드
docker system prune -a
docker-compose -f docker-compose.prod.yml build --no-cache
```

### 서비스 헬스 체크 실패

```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs location-service

# 컨테이너 내부 접속
docker exec -it openspot-location-service sh
wget --spider http://localhost:8082/actuator/health
```

### 메모리 부족

JVM 메모리 설정 조정:
```yaml
# docker-compose.prod.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m
```

---

## 추가 리소스

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Artillery Documentation](https://www.artillery.io/docs)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

## 문의 및 지원

이슈가 발생하면 GitHub Issues에 등록해주세요.
