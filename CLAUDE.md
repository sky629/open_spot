# CLAUDE.md - Open-Spot Project Guide

**Open-Spot**: Map-based location sharing platform with personal ratings. Spring Boot 3.5.5, Kotlin, Clean Architecture.

**Tech Stack**: PostgreSQL 15.4 + PostGIS, Redis 7.2, Kafka 7.4, Google OAuth2, JWT, UUIDv7, KotlinJDSL 3.5.0

## Architecture Overview

### Microservices (Spring Cloud MSA)
- **config-service** (9999): Spring Cloud Config Server
- **gateway-service** (8080): Spring Cloud Gateway with security
- **auth-service** (8081): Google OAuth2 + JWT authentication
- **location-service** (8082): PostGIS spatial queries, Redis cache, Kafka events
- **notification-service** (8083): Kafka consumer for async notifications

### Common Modules (Shared Libraries)
- **common-core** (0-common-core): BaseEntity, exceptions, utilities (no Spring Web)
- **common-web** (1-common-web): ApiResponse, GlobalExceptionHandler, security configs

**Module Naming Convention**: Services are prefixed with numbers (0-6) to indicate dependency order and startup sequence.

### Infrastructure Stack
- **Build**: Gradle 8.11.1 multi-module, Java 21, Kotlin 1.9.25
- **Data**: PostgreSQL 15.4 + PostGIS, Redis 7.2, Kafka 7.4
- **Migration**: Flyway 10.0.0 (PostgreSQL 15.4 compatibility)
- **Container**: Docker Compose for local development
- **API Documentation**: OpenAPI 3.1.1 specification (`openapi.yaml`) for TypeScript client generation

## Development Commands

### Environment Setup
```bash
# 1. Copy environment template
cp .env.example .env

# 2. Edit .env with your credentials
# Required variables:
# - GOOGLE_CLIENT_ID: Google OAuth2 client ID
# - GOOGLE_CLIENT_SECRET: Google OAuth2 client secret
# - JWT_SECRET: Secret key for JWT signing
# - OAUTH2_REDIRECT_URI: Frontend OAuth2 callback URL
# Optional:
# - FRONTEND_BASE_URL: Frontend application URL
# - REDIS_HOST, REDIS_PORT: Redis connection
# - KAFKA_BOOTSTRAP_SERVERS: Kafka brokers
```

### Quick Start (Local Development with Docker Compose)
```bash
# 1. Start infrastructure + config + gateway (최초 실행)
./start-infrastructure.sh

# 2. Start domain services
./start-services.sh

# 3. Check health (all services should return 200)
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/locations/health
```

### Service Management
```bash
# 서비스 중지
./stop-services.sh          # 도메인 서비스만 중지 (Docker 유지)
./stop-infrastructure.sh    # Config + Gateway만 중지 (Docker 컨테이너 유지)
./stop-all.sh              # 모든 서비스 + Docker 중지

# View service logs
tail -f logs/config-service.log
tail -f logs/gateway-service.log
tail -f logs/auth-service.log
tail -f logs/location-service.log
tail -f logs/notification-service.log
```

### Kubernetes Deployment (Minikube + LoadBalancer)
```bash
# 1. Create Minikube cluster
cd k8s/scripts
./2-create-cluster.sh

# 2. Build Docker images
./3-build-images.sh

# 3. Deploy with Helm (Gateway는 자동으로 LoadBalancer 타입)
./4-deploy.sh

# 4. Check deployment status
kubectl get pods -n openspot
kubectl get svc gateway-service -n openspot
# Gateway에 EXTERNAL-IP가 할당됨 (보통 127.0.0.1)

# 5. Test via LoadBalancer (localhost:8080)
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/locations/health

# 6. Cleanup (delete cluster)
./7-cleanup.sh
```

### Cloudflare Production Deployment (HTTPS)
1. Cert: origin_cert/ (Cloudflare Origin SSL)
2. K8s: `./k8s/scripts/2-create-cluster.sh` → `./3-build-images.sh` → `./4-deploy.sh`
3. Tunnel: `./5-start-tunnel.sh` (Minikube → localhost:443)
4. Flow: Browser → Cloudflare → Router (443:443) → Nginx Ingress (HTTPS) → Gateway (8080)

### Code Style and Formatting
```bash
# Ktlint (Kotlin code formatter)
./gradlew ktlintFormat           # Auto-format all code
./gradlew ktlintCheck            # Check formatting without modifying

# Build system
./gradlew build                  # Build all modules
./gradlew clean build            # Clean build
```

### Individual Service Development
```bash
# Run individual services (module names without number prefixes)
./gradlew :config-service:bootRun
./gradlew :gateway-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :location-service:bootRun
./gradlew :notification-service:bootRun

# Test by service and layer (TDD approach)
./gradlew :auth-service:test                    # Full service test
./gradlew :auth-service:test --tests="**/domain/**"      # Domain layer only
./gradlew :auth-service:test --tests="**/service/**"     # Application layer only
./gradlew :auth-service:test --tests="**/repository/**"  # Infrastructure layer only
./gradlew :auth-service:test --tests="**/controller/**"  # Presentation layer only

# Continuous TDD development
./gradlew :location-service:test --tests="LocationTest" --continuous

# Build specific service only
./gradlew :location-service:build
```

### Health Check Endpoints
```bash
# Through Gateway (port 8080)
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/locations/health

# Direct service access
curl http://localhost:8081/api/v1/auth/health         # Auth service
curl http://localhost:8082/api/v1/locations/health   # Location service
curl http://localhost:8083/api/v1/notifications/health # Notification service
curl http://localhost:9999/actuator/health           # Config service
```

### API Documentation
```bash
# Swagger UI (각 서비스별 한국어 문서)
http://localhost:8081/swagger-ui.html    # Auth service API docs
http://localhost:8082/swagger-ui.html    # Location service API docs
http://localhost:8083/swagger-ui.html    # Notification service API docs

# Through Gateway (if configured)
http://localhost:8080/auth/swagger-ui.html
http://localhost:8080/locations/swagger-ui.html
http://localhost:8080/notifications/swagger-ui.html

# OpenAPI 3.1.1 Specification (프론트엔드 TypeScript 클라이언트 생성용)
# File: openapi.yaml
# 사용법:
# - openapi-generator-cli를 사용하여 TypeScript/React Query 클라이언트 생성
# - 모든 API는 Gateway(8080)를 통해 호출해야 함
# - Bearer Token 인증 방식 사용 (Authorization: Bearer <token>)
```

## Architecture Patterns

### Gradle Module Structure
```
open_spot/
├── settings.gradle.kts          # Module declarations (without number prefixes)
├── build.gradle.kts            # Root build configuration
└── msa-modules/
    ├── 0-common-core/          # project(":common-core")
    ├── 1-common-web/           # project(":common-web")
    ├── 2-config-service/       # project(":config-service")
    ├── 3-gateway-service/      # project(":gateway-service")
    ├── 4-auth-service/         # project(":auth-service")
    ├── 5-location-service/     # project(":location-service")
    └── 6-notification-service/ # project(":notification-service")
```

**Important**: When referencing modules in Gradle commands, use the project name without the number prefix (e.g., `:auth-service`, not `:4-auth-service`).

### Clean Architecture (Per Service)
```
📂 {service-name}/src/main/kotlin/com/kangpark/openspot/{domain}/
├── controller/                 # Presentation Layer (HTTP, DTOs)
│   └── dto/{request,response}/
├── service/                    # Application Layer (Use Cases, orchestration)
│   └── usecase/
├── repository/                 # Infrastructure Layer (JPA implementations)
│   ├── jpa/                    # JPA repositories (Spring Data JPA)
│   │   └── entity/            # JPA entities with database mappings
│   └── impl/                   # Domain repository implementations
└── domain/                     # Domain Layer (pure business logic)
    ├── entity/                 # Pure domain entities (no JPA annotations)
    ├── repository/             # Domain repository interfaces
    └── vo/                     # Value objects and enums
```


### Common Module Usage
```kotlin
// Domain entities use BaseEntity via composition (common-core)
data class User(
    val email: String,
    val name: String,
    val baseEntity: BaseEntity = BaseEntity()  // 합성 패턴
) {
    // 편의 프로퍼티로 BaseEntity 필드 접근
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt
}

// Consistent API responses (common-web)
@GetMapping("/api/v1/users/{id}")
fun getUser(@PathVariable id: String): ApiResponse<UserResponse> {
    return ApiResponse.success(userService.getUser(id))
}

// Paginated responses (common-web)
@GetMapping("/api/v1/locations")
fun getLocations(pageable: Pageable): ApiResponse<PageResponse<LocationResponse>> {
    return ApiResponse.success(locationService.getLocations(pageable))
}
```

### Security Configuration
**인증 방식**: Bearer Token (Access Token) + HttpOnly Cookie (Refresh Token)

#### 인증 흐름 (MSA 표준)
1. 클라이언트 → Auth Service: `/api/v1/auth/login` (Google OAuth2)
2. Auth Service → 클라이언트:
   - Response Body: `{ "accessToken": "...", "refreshToken": "..." }`
   - HttpOnly Cookie: Refresh Token (HTTPS only)
3. 클라이언트 → Gateway: `Authorization: Bearer <accessToken>` (모든 요청)
4. Gateway → 검증: JWT 토큰 검증 (HS256)
5. Gateway → 내부 서비스: `X-User-Id` 헤더 추가하여 요청 전달

#### 각 서비스의 SecurityFilterChain 설정
- **Auth Service** (8081):
  - OAuth2 login endpoint (Google)
  - JWT 발급 (`/api/v1/auth/login`, `/api/v1/auth/token/refresh`)
  - Public: `/api/v1/auth/health`, `/api/v1/auth/login/**`
  - CORS 설정: Frontend 도메인 허용

- **Gateway Service** (8080):
  - OAuth2 Resource Server (Bearer Token 검증)
  - `JwtToHeaderGatewayFilter`: JWT → `X-User-Id` 헤더 변환
  - 라우팅 및 중앙 인증 담당
  - Public: health, login endpoints
  - 모든 라우트에 인증 적용 (health, login 제외)

- **Location Service** (8082):
  - Gateway 신뢰 (JWT 검증 없음, X-User-Id 헤더 사용)
  - `@CurrentUserId` 애노테이션으로 사용자 식별
  - Public: `/api/v1/locations/health`, `/api/v1/categories`
  - 나머지 API는 X-User-Id 필수

- **Notification Service** (8083):
  - Gateway 신뢰 (X-User-Id 헤더 사용)
  - X-User-Id 기반 사용자 식별

- **Common Security**: Disabled by default to prevent FilterChain conflicts

## Key Implementation Patterns

### Clean Architecture Layer Pattern
- **Domain**: Pure business logic (immutable data classes, interfaces)
- **Application**: Use cases, service orchestration (depends only on Domain)
- **Infrastructure**: JPA repos, implementations (depends on Domain interfaces)
- **Presentation**: HTTP controllers, DTOs (depends on Application)

**Entity Pattern**: Domain Entity (data class + composition) ↔ JPA Entity (DB mapping)
```kotlin
// Domain: Pure logic
data class Location(val userId: UUID, val name: String, val baseEntity: BaseEntity = BaseEntity()) {
    val id: UUID get() = baseEntity.id
    fun updateEvaluation(rating: Double?, review: String?, tags: List<String>): Location {
        require(rating == null || (rating >= 0.5 && rating <= 5.0)) { "평점 범위: 0.5-5.0" }
        return copy(rating = rating, review = review, tags = tags,
                   baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now()))
    }
}

// Conversion: Domain ↔ JPA (via toDomain()/fromDomain())
@Entity @Table(name = "locations", schema = "location")
data class LocationJpaEntity(...) {
    fun toDomain() = Location(userId, name, ..., baseEntity = BaseEntity(id!!, createdAt, updatedAt))
    companion object { fun fromDomain(location: Location) = LocationJpaEntity(...) }
}

// Repository: Domain interface → JPA implementation
interface LocationRepository { fun save(location: Location): Location }
@Repository class LocationRepositoryImpl(private val jpaRepo: LocationJpaRepository) : LocationRepository {
    override fun save(location: Location) = jpaRepo.save(LocationJpaEntity.fromDomain(location)).toDomain()
}
```

### Security: JWT + Headers
- **Auth Service** (8081): Issues JWT tokens (Google OAuth2)
- **Gateway** (8080): Validates JWT, injects `X-User-Id` header
- **Internal Services**: Trust Gateway, use `@RequestHeader("X-User-Id")` for user identification
```kotlin
@Component class JwtToHeaderGatewayFilter : GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val userId = jwtTokenProvider.extractUserId(extractToken(exchange.request))
        return chain.filter(exchange.mutate().request(
            exchange.request.mutate().header("X-User-Id", userId).build()).build())
    }
}
```

### Databases
- **KotlinJDSL** (3.5.0): Type-safe JPQL for keyword search, GROUP BY aggregations
- **Native Query**: PostGIS spatial functions (ST_DWithin, ST_Distance) - JPQL doesn't support these
- **UUIDv7**: Time-ordered, better B-tree indexing than UUIDv4

## Database Architecture

### Single Database with Service Separation
- **Database**: `openspot` (PostgreSQL + PostGIS)
- **Auth tables**: `users`, `social_accounts`
- **Location tables**: `locations`, `categories`, `location_groups`
- **Notification tables**: `notifications`, `device_tokens`, `notification_settings`

### Query Implementation Strategy

#### 1. **KotlinJDSL** - 타입 안전 JPQL DSL (권장)
```kotlin
// 키워드 검색 (LIKE)
val location = Entities.entity(LocationJpaEntity::class)
jpqlExecutor.createQuery(LocationJpaEntity::class) {
    select(location)
    from(location)
    where(
        and(
            location.userId eq userId,
            location.isActive eq true,
            or(
                lower(location.name) like keyword.lowercase(),
                lower(location.description) like keyword.lowercase(),
                lower(location.address) like keyword.lowercase()
            )
        )
    )
    orderBy(location.createdAt.desc())
}

// 집계 쿼리 (GROUP BY)
jpqlExecutor.createQuery(LocationJpaEntity::class) {
    selectMulti(location.categoryId, count(location))
    from(location)
    where(and(location.userId eq userId, location.isActive eq true))
    groupBy(location.categoryId)
}
```

**KotlinJDSL 사용 메서드**:
- `findByUserIdAndKeyword`: LIKE 검색 (여러 필드)
- `countByUserIdGroupByCategory`: GROUP BY + COUNT 집계

**장점**:
- ✅ 타입 안정성: 컴파일 타임에 필드명 오류 검출
- ✅ IDE 지원: 자동완성, 리팩토링 (필드명 변경 시 자동 반영)
- ✅ 가독성: SQL 문자열보다 Kotlin DSL이 더 읽기 쉬움
- ✅ 안정성: 동적 쿼리 생성 시 조건 누락 방지

#### 2. **Native Query** - PostGIS 공간 함수 (필요시만)
```sql
-- Find locations within radius (Location Service)
-- PostGIS Native Query (KotlinJDSL 미지원)
SELECT * FROM location.locations l
WHERE l.is_active = true
AND ST_DWithin(
    l.coordinates::geography,
    ST_SetSRID(ST_MakePoint(?longitude, ?latitude), 4326)::geography,
    ?radiusMeters
)
ORDER BY ST_Distance(
    l.coordinates::geography,
    ST_SetSRID(ST_MakePoint(?longitude, ?latitude), 4326)::geography
);
```

**Native Query 사용 메서드**:
- `findByCoordinatesWithinRadiusDynamic`: 반경 검색 (ST_DWithin)
- `findByCoordinatesWithinBoundsDynamic`: 범위 검색 (ST_Contains)

**이유**:
- ⚠️ PostGIS 함수는 일반 JPQL에서 지원되지 않음
- ⚠️ 지리 좌표계 변환 (4326) 필요
- ⚠️ KotlinJDSL은 JPQL 표준만 지원

### Configuration Management
```yaml
# Config Service (port 9999) serves configuration to all services
spring:
  cloud:
    config:
      server:
        git:
          uri: file:///path/to/config-repo

# All services bootstrap from Config Service
spring:
  config:
    import: "configserver:http://localhost:9999"
```

## Testing Strategy

### Current Test Coverage
**Status**: Minimal coverage - currently only 1 integration test file exists
- `msa-modules/5-location-service/src/test/kotlin/com/kangpark/openspot/location/repository/LocationJpaRepositoryImplTest.kt`
  - Tests JPQL queries: `findByUserIdAndKeyword()`, `countByUserIdGroupByCategory()`
  - Validates pagination, case-insensitivity, GROUP BY aggregation with Testcontainers
  - **High Priority**: Expand test coverage across all layers and services

### Test Dependencies
```kotlin
// Core testing libraries (all services)
testImplementation("org.junit.jupiter:junit-jupiter")
testImplementation("org.assertj:assertj-core")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("org.springframework.boot:spring-boot-starter-test")

// Location Service (PostGIS + Testcontainers)
testImplementation("org.testcontainers:postgresql")

// Auth Service (Security)
testImplementation("org.springframework.security:spring-security-test")

// Notification Service (Kafka)
testImplementation("org.testcontainers:kafka")
```

### TDD Development Approach (Recommended Pattern)

Clean Architecture 순서에 따른 TDD 개발:

1. **Domain Layer** (No Spring context): Pure business logic tests
   ```bash
   # Example: Test Location domain entity
   ./gradlew :location-service:test --tests="**/domain/**"

   # Test structure: src/test/kotlin/com/kangpark/openspot/location/domain/entity/LocationTest.kt
   # - Test immutability via copy() method
   # - Test validation logic (e.g., rating 0.5-5.0 range)
   # - Test domain methods (updateEvaluation, deactivate, toggleFavorite)
   ```

2. **Application Layer**: Use case tests with mocked repositories
   ```bash
   ./gradlew :location-service:test --tests="**/service/**"

   # Test structure: src/test/kotlin/com/kangpark/openspot/location/service/usecase/
   # - Mock LocationRepository
   # - Test CreateLocationUseCase, UpdateLocationUseCase, SearchLocationUseCase
   # - Verify domain method calls and repository interactions
   ```

3. **Infrastructure Layer**: Integration tests with JPA repositories
   ```bash
   ./gradlew :location-service:test --tests="**/repository/**"

   # Use Testcontainers for PostgreSQL (with PostGIS)
   # Test actual JPA queries: JPQL + Native PostGIS queries
   # Example: LocationJpaRepositoryImplTest.kt (existing)
   ```

4. **Presentation Layer**: MockMvc tests for HTTP endpoints
   ```bash
   ./gradlew :location-service:test --tests="**/controller/**"

   # Test structure: src/test/kotlin/com/kangpark/openspot/location/controller/
   # - Mock LocationApplicationService
   # - Test request/response serialization
   # - Test error handling and status codes
   # - Test pagination parameters
   ```

### Continuous Test Development
```bash
# Watch for changes and re-run specific test
./gradlew :location-service:test --tests="LocationTest" --continuous

# Run only domain layer tests with continuous watch
./gradlew :location-service:test --tests="**/domain/**" --continuous

# Run all tests with coverage report
./gradlew test --info
```

### Clean Architecture Testing Strategy
```kotlin
// 1. Domain Layer Test (Pure business logic, no Spring context)
class LocationTest {
    @Test
    fun `should update rating when valid values provided`() {
        // Given
        val location = Location.create(
            userId = UUID.randomUUID(),
            name = "맛집",
            description = null,
            address = "서울시 강남구",
            categoryId = UUID.randomUUID(),
            coordinates = Coordinates(37.5, 127.0)
        )

        // When
        val updated = location.updateEvaluation(
            rating = 4.5,
            review = "정말 맛있어요!",
            tags = listOf("맛집", "분위기좋음")
        )

        // Then
        assertThat(updated.rating).isEqualTo(4.5)
        assertThat(updated.review).isEqualTo("정말 맛있어요!")
        assertThat(updated.tags).containsExactly("맛집", "분위기좋음")
    }

    @Test
    fun `should throw exception when rating is out of range`() {
        val location = Location.create(/* ... */)

        assertThrows<IllegalArgumentException> {
            location.updateEvaluation(rating = 6.0, review = null, tags = emptyList())
        }
    }
}

// 2. Application Layer Test (Use cases with mocked dependencies)
class CreateLocationUseCaseTest {
    @Mock private lateinit var locationRepository: LocationRepository
    @Test
    fun `should create location when valid request provided`() {
        // Given: Mock repository behavior
        // When: Execute use case
        // Then: Verify domain logic and repository interaction
    }
}

// 3. Infrastructure Layer Test (JPA integration)
@DataJpaTest
class LocationRepositoryImplTest {
    @Test
    fun `should find locations within radius using PostGIS`() {
        // Test actual spatial queries with test database
    }
}

// 4. Presentation Layer Test (HTTP endpoints)
@WebMvcTest(LocationController::class)
class LocationControllerTest {
    @Test
    fun `should return location details when valid id provided`() {
        // Test HTTP request/response with mocked services
    }
}
```

### Independent Service Testing
각 서비스는 완전히 분리된 테스트 의존성을 가집니다:
```kotlin
// Service-specific test dependencies (각 서비스별로 필요한 것만)
testImplementation("org.testcontainers:postgresql")     // Location Service (PostGIS)
testImplementation("org.testcontainers:redis")         // Location Service (Cache)
testImplementation("org.springframework.security:spring-security-test") // Auth Service
testImplementation("org.testcontainers:kafka")         // Notification Service
```

### Continuous TDD Development
```bash
# 특정 테스트 지속적 실행 (파일 변경 감지)
./gradlew :location-service:test --tests="LocationTest" --continuous

# 특정 레이어만 반복 테스트
./gradlew :location-service:test --tests="**/domain/**" --continuous
```

## Troubleshooting

### Common Issues

#### Architecture Issues
- **BaseEntity 사용 패턴**: Domain Entity에서 `BaseEntity`를 합성으로 사용 (상속 대신)
  - `val baseEntity: BaseEntity = BaseEntity()` 형태로 선언
  - `val id: UUID get() = baseEntity.id` 형태로 편의 프로퍼티 제공
- **Domain/JPA Entity 분리**:
  - Domain Entity: `domain/entity/` 패키지, 순수 비즈니스 로직만
  - JPA Entity: `repository/jpa/entity/` 패키지, DB 매핑 전용
  - 변환 메서드: `toDomain()`, `fromDomain()` companion object로 제공
- **Repository 패턴**:
  - Domain Repository Interface: `domain/repository/` 패키지
  - JPA Repository: `repository/jpa/` 패키지 (Spring Data JPA)
  - Repository Implementation: `repository/impl/` 패키지 (Domain Repository 구현)

#### Infrastructure Issues
- **Port conflicts**: Config (9999), Gateway (8080), services (8081-8083)
- **PostgreSQL version**: Requires Flyway 10.0.0 for PostgreSQL 15.4 compatibility
- **SecurityFilterChain conflicts**: Each service must have @Primary on its SecurityFilterChain
- **Config Service dependency**: All services need `spring.config.import` property

#### Development Issues
- **Swagger 의존성 누락**: `springdoc-openapi-starter-webmvc-ui:2.5.0` 의존성 확인
- **Clean Architecture 위반**: 레이어 간 의존성 방향 준수 (Domain ← Application ← Infrastructure)
- **Entity 변환 오류**: Domain ↔ JPA Entity 변환 시 BaseEntity 필드 처리 확인

#### KotlinJDSL 마이그레이션 (Location Service)
쿼리 구현 전략: **하이브리드 접근** (KotlinJDSL + Native Query)

**KotlinJDSL 사용 메서드** (타입 안전 JPQL DSL):
```kotlin
// 1. 키워드 검색 (여러 필드 LIKE)
override fun findByUserIdAndKeyword(userId: UUID, keyword: String, pageable: Pageable): Page<LocationJpaEntity> {
    return jpqlExecutor.findPage(pageable) {
        select(entity(LocationJpaEntity::class))
        from(entity(LocationJpaEntity::class))
        where(buildKeywordPredicate(userId, keyword))
        orderBy(path(LocationJpaEntity::createdAt).desc())
    }
}

// 2. 집계 쿼리 (GROUP BY + COUNT)
override fun countByUserIdGroupByCategory(userId: UUID): Map<UUID, Long> {
    val results = jpqlExecutor.createQuery(Array::class) {
        select(
            path(LocationJpaEntity::categoryId),
            Expression.count(entity(LocationJpaEntity::class))
        )
        from(entity(LocationJpaEntity::class))
        where(...)
        groupBy(path(LocationJpaEntity::categoryId))
    }.resultList
}
```

**KotlinJDSL 설정**:
- Configuration: `KotlinJdslConfiguration.kt`
  - `JpqlRenderContext` 빈 (쿼리 렌더링)
  - `KotlinJdslJpqlExecutor` 빈 (쿼리 실행)
- Dependencies:
  ```kotlin
  implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.0")
  implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.0")
  implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.0")
  ```

**Native Query 유지** (PostGIS 공간 함수):
- `findByCoordinatesWithinRadiusDynamic`: ST_DWithin (반경 검색)
- `findByCoordinatesWithinBoundsDynamic`: ST_Contains (범위 검색)
- 이유: PostGIS 함수는 일반 JPQL 미지원

**KotlinJDSL 장점**:
- ✅ 타입 안정성: 컴파일 타임에 필드명 오류 검출
- ✅ IDE 지원: 자동완성, 리팩토링 (필드명 변경 시 자동 반영)
- ✅ 가독성: DSL이 SQL 문자열보다 명확함
- ✅ 안전성: 동적 조건 구성 시 타입 검증
- ✅ 성능: SQL로 변환되어 네이티브 쿼리와 동일

#### Gradle Build Issues
- **Slow builds**: `org.gradle.parallel=true` and `org.gradle.workers.max=N` in gradle.properties
- **Out of memory during build**: Increase heap size in `gradle.properties`: `org.gradle.jvmargs=-Xmx2g`
- **Module not found errors**: Ensure `settings.gradle.kts` declares all modules correctly
  - Module names in Gradle commands must match project names (without number prefixes)
  - Example: `:auth-service` (not `:4-auth-service`)

#### Testing Issues
- **Testcontainers startup timeout**: PostgreSQL with PostGIS can be slow on first run
  - Increase startup timeout: `testcontainers.properties`
  - Use reusable containers in local development: `@Testcontainers(disabledWithoutDocker = true)`
- **Mockk mocking not working**: Ensure `mockk` version matches kotlin version
  - Current: mockk 1.13.10+ compatible with Kotlin 1.9.25
- **Spring context not loading in tests**: Annotate with `@SpringBootTest` (loads context) or `@DataJpaTest` (JPA only)

#### Kubernetes Issues
- **Redis Health Check 실패**: auth-service, location-service에서 Redis 헬스 체크 비활성화됨
  - 설정: `management.health.redis.enabled: false` in config YAML
- **Gateway 라우팅 실패**: 서비스 호스트/포트 환경 변수 미설정
  - 해결: ConfigMap에 `AUTH_SERVICE_HOST`, `LOCATION_SERVICE_HOST` 등 추가
  - 값: Kubernetes 서비스 DNS 이름 (예: `auth-service:8081`)
- **Pod Ready 타임아웃**: 서비스 시작 시간이 오래 걸림 (Config Service 로드)
  - 해결: Helm values.yaml에서 `initialDelaySeconds` 증가
  - 권장값: Config Service (90s), Domain Services (120s)
- **Ingress 포트 80 응답 안 함**: Kubernetes 클러스터 내부 네트워크 문제
  - 대안: `kubectl port-forward` 또는 NodePort 직접 접근 사용

#### Spring Cloud Config Issues
- **Config Service not updating**: Clear config client cache
  ```bash
  # Restart the service or trigger refresh
  curl -X POST http://localhost:8081/actuator/refresh
  ```
- **Profile-specific configs not loading**: Ensure `spring.profiles.active` is set in bootstrap.yml
- **Secrets not being decrypted**: Verify encryption key in application properties

### Service Dependencies
Services must start in order:
1. Docker infrastructure (PostgreSQL, Redis, Kafka)
2. Config Service (9999) - provides configuration to other services
3. Gateway Service (8080) - routes requests to domain services
4. Domain Services (8081-8083) - Auth, Location, Notification

### Database Connection
```yaml
# Local development (via Docker)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/openspot
    username: postgres
    password: postgres
```

## API Standards

### Endpoint Patterns
- **Prefix**: All APIs use `/api/v1` prefix
- **Health**: `/api/v1/{service}/health` (public access)
- **Pagination**: All list endpoints support `page`, `size` parameters
- **Response**: Consistent `ApiResponse<T>` wrapper with success/error structure

### Key API Endpoints

**Endpoint Convention**: 사용자 개인 정보 관련 엔드포인트는 `/users/self`, `/locations/self` 패턴 사용

```bash
# ========================================
# Auth Service (8081)
# ========================================
POST /api/v1/auth/token/refresh                   # JWT 토큰 갱신
POST /api/v1/auth/logout                          # 로그아웃

# Users
GET  /api/v1/users/self                           # 현재 사용자 프로필 조회
GET  /api/v1/users/{userId}                       # 특정 사용자 프로필 조회 (관리자/친구용)

# ========================================
# Location Service (8082)
# ========================================

# Categories (Public - 인증 불필요)
GET  /api/v1/categories                           # 카테고리 목록 조회

# Locations
GET  /api/v1/locations                            # 장소 목록 조회 (다양한 검색 옵션)
     # Query Parameters:
     # - bounds: northEastLat, northEastLon, southWestLat, southWestLon (지도 영역 검색)
     # - radius: latitude, longitude, radiusMeters (반경 검색)
     # - groupId: 그룹 필터
     # - categoryId: 카테고리 필터
     # - keyword: 키워드 검색
     # - sortBy: RATING | CREATED_AT (정렬)
     # - targetUserId: 조회할 사용자 (친구 기능용, 미지정 시 본인)
     # - page, size: 페이지네이션

POST /api/v1/locations                            # 새 장소 생성
GET  /api/v1/locations/{locationId}               # 장소 상세 조회
PUT  /api/v1/locations/{locationId}               # 장소 통합 수정 (부분 업데이트)
     # 수정 가능 필드: name, description, address, categoryId, iconUrl,
     #               rating, review, tags, groupId, coordinates
DELETE /api/v1/locations/{locationId}             # 장소 비활성화 (논리적 삭제)
GET  /api/v1/locations/self?sortBy=RATING         # 내 장소 목록 (정렬 가능)

# Location Groups
GET    /api/v1/locations/groups                   # 그룹 목록 조회
POST   /api/v1/locations/groups                   # 그룹 생성
PUT    /api/v1/locations/groups/{groupId}         # 그룹 수정
DELETE /api/v1/locations/groups/{groupId}         # 그룹 삭제
PUT    /api/v1/locations/groups/reorder           # 그룹 순서 변경 (드래그 앤 드롭)

# ========================================
# Notification Service (8083)
# ========================================
POST /api/v1/notifications/tokens                 # FCM 디바이스 토큰 등록
GET  /api/v1/notifications                        # 알림 목록 조회 (페이지네이션)
GET  /api/v1/notifications/unread-count           # 읽지 않은 알림 수
PUT  /api/v1/notifications/{notificationId}/read  # 알림 읽음 처리
GET  /api/v1/notifications/settings               # 알림 설정 조회
PUT  /api/v1/notifications/settings               # 알림 설정 업데이트
```

**중요 변경사항**:
- ❌ 삭제된 엔드포인트:
  - `PUT /api/v1/locations/{id}/evaluation` - 통합 수정으로 대체
  - `PUT /api/v1/locations/{id}/group` - 통합 수정으로 대체
  - `PUT /api/v1/locations/{id}/coordinates` - 통합 수정으로 대체
  - `GET /api/v1/locations/top-rated` - GET /locations?sortBy=RATING으로 대체
  - `GET /api/v1/locations/recent` - GET /locations?sortBy=CREATED_AT으로 대체
  - `GET /api/v1/locations/groups/{groupId}` - GET /locations?groupId={groupId}로 대체

- ✅ 통합 엔드포인트:
  - `PUT /api/v1/locations/{id}` - 모든 필드 부분 업데이트 지원
  - `GET /api/v1/locations` - 다양한 쿼리 파라미터로 유연한 검색

### Swagger API Documentation
```bash
# Swagger UI for each service (Korean documentation)
http://localhost:8081/swagger-ui.html    # Auth Service API 문서
http://localhost:8082/swagger-ui.html    # Location Service API 문서
http://localhost:8083/swagger-ui.html    # Notification Service API 문서

# Through Gateway (if configured)
http://localhost:8080/auth/swagger-ui.html
http://localhost:8080/locations/swagger-ui.html
http://localhost:8080/notifications/swagger-ui.html
```

## OpenAPI Specification

프로젝트 루트의 `openapi.yaml` 파일은 OpenAPI 3.1.1 스펙을 따르며, 프론트엔드 TypeScript 클라이언트 자동 생성에 사용됩니다.

### 주요 특징
- **인증 방식**: Bearer Token (Access Token) + HttpOnly Cookie (Refresh Token)
- **MSA 구조**: Gateway(8080)를 통한 통합 API 진입점
- **한국어 문서화**: 모든 엔드포인트와 스키마에 한국어 설명 포함
- **TypeScript 클라이언트 생성**: openapi-generator-cli로 타입 안전한 API 클라이언트 생성 가능

### 인증 플로우 (MSA 표준)
1. 클라이언트 → Gateway: `Authorization: Bearer <token>` 헤더 포함
2. Gateway: JWT 검증 (HS256 알고리즘)
3. Gateway: 검증 성공 시 `X-User-Id` 헤더 추가
4. 내부 서비스: `X-User-Id` 헤더로 사용자 식별 (JWT 검증 불필요)

## Kotlin Patterns
- **Data Classes**: Immutable (val), use copy() for updates, validation in methods
- **Null Safety**: let/elvis (jpaEntity?.let { it.toDomain() } ?: throw Exception())
- **When Expression**: Better than if-else for sealed classes/errors

## Core Design Principles
1. **Domain Entity**: Data class + BaseEntity composition (not inheritance). Use copy() for immutability.
2. **JPA Entity**: Separate package (repository/jpa/entity/). Convert via toDomain()/fromDomain().
3. **Repository**: Domain interface → JPA repo → impl class (3-layer)
4. **Use Cases**: Each use case as independent service class
5. **API Responses**: Always wrap with ApiResponse<T> (common-web)