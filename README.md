# Open-Spot MSA Backend

Open-Spot은 서울시 공공데이터를 활용한 상권분석 서비스의 MSA 백엔드입니다.

## 🏗️ 아키텍처

### Microservices
- **config-service** (9999): Spring Cloud Config Server
- **gateway-service** (8080): Spring Cloud Gateway  
- **auth-service** (8081): 인증/사용자 관리
- **analysis-service** (8082): 상권 분석 (PostGIS, Redis, Kafka)
- **notification-service** (8083): 알림 (Kafka Consumer)

### 기술 스택
- **Framework**: Spring Boot 3.5.5, Kotlin
- **Build**: Gradle 8.11.1, Java 21
- **Database**: PostgreSQL + PostGIS
- **Cache**: Redis
- **Message Queue**: Kafka
- **Architecture**: Clean Architecture + MSA

## 🚀 실행 방법

### 1. 인프라 서비스 시작
```bash
./start-infrastructure.sh
```
이 명령어는 다음을 실행합니다:
- Docker Compose (PostgreSQL, Redis, Kafka)
- Config Service (9999)
- Gateway Service (8080)

### 2. 도메인 서비스 시작
```bash
./start-services.sh
```
이 명령어는 다음을 실행합니다:
- Auth Service (8081)
- Analysis Service (8082)
- Notification Service (8083)

### 3. 전체 서비스 종료
```bash
./stop-all.sh
```

## 🔍 헬스 체크

서비스 실행 후 다음 엔드포인트로 상태 확인:

```bash
# Gateway를 통한 헬스 체크
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/stores/health

# 직접 서비스 헬스 체크
curl http://localhost:8081/api/v1/auth/health
curl http://localhost:8082/api/v1/stores/health
curl http://localhost:8083/api/v1/notifications/health

# 인프라 서비스 체크
curl http://localhost:9999/actuator/health  # Config Service
curl http://localhost:8080/actuator/health  # Gateway
```

## 🧪 개발 및 테스트

### 개별 서비스 테스트
```bash
# 전체 테스트
./gradlew test

# 개별 서비스 테스트
./gradlew :auth-service:test
./gradlew :analysis-service:test
./gradlew :notification-service:test

# 레이어별 테스트 (TDD)
./gradlew :auth-service:test --tests="**/domain/**"
./gradlew :auth-service:test --tests="**/service/**"
./gradlew :auth-service:test --tests="**/repository/**"
./gradlew :auth-service:test --tests="**/controller/**"
```

### 개별 서비스 실행
```bash
./gradlew :config-service:bootRun
./gradlew :gateway-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :analysis-service:bootRun
./gradlew :notification-service:bootRun
```

## 📁 프로젝트 구조

```
open-spot-backend/
├── msa-modules/
│   ├── 0-common-core/          # 공통 도메인 컴포넌트
│   ├── 1-common-web/           # 공통 웹 컴포넌트
│   ├── 2-config-service/       # Spring Cloud Config
│   ├── 3-gateway-service/      # Spring Cloud Gateway
│   ├── 4-auth-service/         # 인증 도메인
│   ├── 5-analysis-service/     # 상권분석 도메인
│   └── 6-notification-service/ # 알림 도메인
├── docker-compose.yml          # 인프라 서비스
├── start-infrastructure.sh     # 인프라 시작 스크립트
├── start-services.sh          # 도메인 서비스 시작 스크립트
└── stop-all.sh               # 전체 종료 스크립트
```

### Clean Architecture 레이어 구조
각 서비스는 다음 레이어드 패턴을 따릅니다:

```
src/main/kotlin/com/kangpark/openspot/{service}/
├── controller/                 # Presentation Layer
│   ├── dto/{request,response,mapper}
│   └── exception/
├── service/                   # Application Layer
│   └── usecase/
├── repository/               # Infrastructure Layer
│   ├── external/
│   ├── cache/
│   └── messaging/
└── domain/                   # Domain Layer
```

## 🔧 설정

### 환경 변수
```bash
# Google OAuth (Auth Service)
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT Secret (Auth Service)  
export JWT_SECRET=your-jwt-secret-key
```

### 데이터베이스
Docker Compose로 시작되는 PostgreSQL:
- **Host**: localhost:5432
- **User**: openspot
- **Password**: openspot123
- **Databases**: openspot_auth, openspot_analysis

## 📊 모니터링

### Actuator 엔드포인트
- Config Service: http://localhost:9999/actuator
- Gateway Service: http://localhost:8080/actuator  
- Auth Service: http://localhost:8081/actuator
- Analysis Service: http://localhost:8082/actuator
- Notification Service: http://localhost:8083/actuator

## 🔄 개발 워크플로우

1. **TDD 개발 순서**: Domain → Application → Infrastructure → Presentation
2. **서비스 독립 개발**: 각 MSA 서비스별로 독립적 개발/테스트
3. **API 우선 설계**: `/api/v1` prefix, 페이징 기본 지원
4. **클린 아키텍처**: 의존성 방향 준수 (Domain ← Application ← Infrastructure)

## 🚨 문제 해결

### 서비스가 시작되지 않는 경우
1. Docker 서비스 상태 확인: `docker-compose ps`
2. Config Service 상태 확인: `curl http://localhost:9999/actuator/health`
3. 포트 충돌 확인: `lsof -i :8080,8081,8082,8083,8888`
4. 로그 확인: `docker-compose logs -f`

### 데이터베이스 연결 오류
1. PostgreSQL 컨테이너 확인: `docker-compose logs postgresql`
2. 데이터베이스 생성 확인: `docker-compose exec postgresql psql -U openspot -l`