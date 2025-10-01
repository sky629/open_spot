# Open-Spot MSA Backend

**Open-Spot**은 사용자가 방문한 장소에 대한 평점과 정보를 기록하고 관리하는 지도 기반 위치 공유 서비스입니다.

## 💡 프로젝트 목적

사용자가 원하는 위치에 개인적인 평점과 정보를 기록하여 관리하고, 방문한 장소들을 그룹으로 체계적으로 관리할 수 있습니다. 추후에는 친구들과 방문 장소 그룹을 공유하는 소셜 기능이 추가될 예정입니다.

### 핵심 기능
- 📍 **개인 장소 기록**: 내가 방문한 장소의 평점, 리뷰, 메모 기록
- 📂 **그룹 관리**: 카테고리별(맛집, 카페, 쇼핑 등)로 장소 그룹화
- 🗺️ **지도 기반 검색**: 내 주변 또는 특정 반경 내 기록된 장소 찾기
- 📝 **방문 이력 추적**: 언제, 어디를, 누구와 방문했는지 기록
- ⭐ **즐겨찾기**: 다시 가고 싶은 장소 북마크
- 🤝 **소셜 공유 (예정)**: 친구들과 장소 추천 공유

## 🏗️ 아키텍처

### Microservices
- **config-service** (9999): Spring Cloud Config Server
- **gateway-service** (8080): Spring Cloud Gateway  
- **auth-service** (8081): 인증/사용자 관리
- **location-service** (8082): 장소 정보 관련 서비스 (PostGIS, Redis, Kafka)
- **notification-service** (8083): 알림 (Kafka Consumer)

### 기술 스택
- **Framework**: Spring Boot 3.5.5, Kotlin 1.9.25
- **Build**: Gradle 8.11.1, Java 21
- **Database**: PostgreSQL 15.4 + PostGIS (공간 데이터)
- **Cache**: Redis 7.2
- **Message Queue**: Apache Kafka 7.4
- **Security**: Google OAuth2 + JWT
- **Documentation**: Swagger UI (한국어)
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
- Location Service (8082)
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
curl http://localhost:8080/api/v1/locations/health

# 직접 서비스 헬스 체크
curl http://localhost:8081/api/v1/auth/health         # Auth service
curl http://localhost:8082/api/v1/locations/health   # Location service
curl http://localhost:8083/api/v1/notifications/health # Notification service

# 인프라 서비스 체크
curl http://localhost:9999/actuator/health           # Config service
curl http://localhost:8080/actuator/health           # Gateway
```

## 🧪 개발 및 테스트

### 개별 서비스 테스트
```bash
# 전체 테스트
./gradlew test

# 개별 서비스 테스트
./gradlew :auth-service:test
./gradlew :location-service:test
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
./gradlew :location-service:bootRun
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
│   ├── 5-location-service/     # 장소 정보 관련 도메인
│   └── 6-notification-service/ # 알림 도메인
├── docker-compose.yml          # 인프라 서비스
├── start-infrastructure.sh     # 인프라 시작 스크립트
├── start-services.sh          # 도메인 서비스 시작 스크립트
└── stop-all.sh               # 전체 종료 스크립트
```

### Clean Architecture 레이어 구조
각 서비스는 다음 Clean Architecture 패턴을 따릅니다:

```
src/main/kotlin/com/kangpark/openspot/{service}/
├── controller/                 # Presentation Layer (HTTP, DTOs)
│   └── dto/{request,response}/
├── service/                    # Application Layer (Use Cases, orchestration)
│   └── usecase/
├── repository/                 # Infrastructure Layer (JPA implementations)
│   └── entity/                 # JPA entities with database mappings
└── domain/                     # Domain Layer (pure business logic)
    ├── entity/                 # Pure domain entities (no JPA annotations)
    ├── repository/             # Domain repository interfaces
    └── valueobject/            # Value objects and enums
```

### 의존성 방향
```
Presentation → Application → Domain ← Infrastructure
```

- **Domain Layer**: 순수 비즈니스 로직, 외부 의존성 없음, 불변 엔터티
- **Application Layer**: 유스케이스, Domain 인터페이스에만 의존
- **Infrastructure Layer**: Domain 인터페이스 구현, DB 매핑 담당 (JPA Entity 포함)
- **Presentation Layer**: HTTP 컨트롤러, Application 레이어에 의존

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
- **User**: postgres
- **Password**: postgres
- **Databases**: openspot

## 📊 모니터링 및 문서화

### Actuator 엔드포인트
- Config Service: http://localhost:9999/actuator
- Gateway Service: http://localhost:8080/actuator
- Auth Service: http://localhost:8081/actuator
- Location Service: http://localhost:8082/actuator
- Notification Service: http://localhost:8083/actuator

### API 문서 (Swagger UI)
```bash
# 각 서비스별 한국어 API 문서
http://localhost:8081/swagger-ui.html    # Auth Service API 문서
http://localhost:8082/swagger-ui.html    # Location Service API 문서
http://localhost:8083/swagger-ui.html    # Notification Service API 문서

# Gateway를 통한 접근 (구성된 경우)
http://localhost:8080/auth/swagger-ui.html
http://localhost:8080/locations/swagger-ui.html
http://localhost:8080/notifications/swagger-ui.html
```

## 🔄 개발 워크플로우

1. **TDD 개발 순서**: Domain → Application → Infrastructure → Presentation
2. **서비스 독립 개발**: 각 MSA 서비스별로 독립적 개발/테스트
3. **API 우선 설계**: `/api/v1` prefix, 사용자 엔드포인트는 `/users/self` 패턴 사용
4. **클린 아키텍처**: 의존성 방향 준수 (Domain ← Application ← Infrastructure)
5. **Entity 분리**: Domain Entity(순수 비즈니스)와 JPA Entity(DB 매핑) 완전 분리

### 레이어별 TDD 개발
```bash
# 1. Domain Layer 테스트 (순수 비즈니스 로직)
./gradlew :location-service:test --tests="**/domain/**"

# 2. Application Layer 테스트 (유스케이스, 모킹된 리포지토리)
./gradlew :location-service:test --tests="**/service/**"

# 3. Infrastructure Layer 테스트 (JPA 통합 테스트)
./gradlew :location-service:test --tests="**/repository/**"

# 4. Presentation Layer 테스트 (HTTP 엔드포인트)
./gradlew :location-service:test --tests="**/controller/**"

# 지속적 TDD 개발 (파일 변경 감지)
./gradlew :location-service:test --tests="LocationTest" --continuous
```

## 🚨 문제 해결

### 서비스가 시작되지 않는 경우
1. Docker 서비스 상태 확인: `docker-compose ps`
2. Config Service 상태 확인: `curl http://localhost:9999/actuator/health`
3. 포트 충돌 확인: `lsof -i :8080,8081,8082,8083,9999`
4. 로그 확인: `docker-compose logs -f`

### 아키텍처 관련 이슈
1. **BaseEntity import 오류**: `com.kangpark.openspot.common.core.domain.BaseEntity` 사용
2. **Domain/JPA Entity 혼재**: Domain Entity는 순수 비즈니스 로직만, JPA Entity는 별도 클래스
3. **Repository 구현 오류**: Domain Repository 인터페이스와 JPA Repository 구현체 분리
4. **Clean Architecture 위반**: 레이어 간 의존성 방향 준수 필요

### 데이터베이스 연결 오류
1. PostgreSQL 컨테이너 확인: `docker-compose logs postgresql`
2. 데이터베이스 생성 확인: `docker-compose exec postgresql psql -U postgres -l`
3. PostGIS 확장 확인: `docker-compose exec postgresql psql -U postgres -d openspot -c "\dx"`

### Swagger 문서화 이슈
- **의존성 누락**: `springdoc-openapi-starter-webmvc-ui:2.5.0` 확인
- **한국어 설정**: `@Operation`, `@ApiResponse` 어노테이션 한국어로 작성

## 🎯 주요 기능

### 위치 기반 서비스
- **공간 쿼리**: PostGIS를 활용한 반경 내 장소 검색
- **좌표계**: WGS84 (SRID:4326) 사용
- **거리 계산**: Geography 타입으로 정확한 거리 측정

### 인증 및 보안
- **OAuth2**: Google 소셜 로그인
- **JWT**: 액세스/리프레시 토큰 방식
- **보안 설정**: 각 서비스별 독립적인 SecurityFilterChain

### 이벤트 드리븐 아키텍처
- **Kafka**: 서비스 간 비동기 메시징
- **이벤트**: 장소 생성, 리뷰 작성, 방문 기록 등
- **알림**: 실시간 사용자 알림 처리