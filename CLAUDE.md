# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Open-Spot** is a map-based location sharing platform where users can record ratings and information about places they've visited. Built with Spring Boot 3.5.5, Kotlin, and Clean Architecture principles.

### Purpose
사용자가 방문한 장소에 대한 개인적인 평점과 정보를 기록하고 관리하는 지도 기반 위치 공유 서비스입니다. 장소들을 그룹으로 관리할 수 있으며, 추후 친구들과 방문 장소 그룹을 공유할 수 있는 소셜 기능이 추가될 예정입니다.

### Core Features
- **개인 장소 기록**: 사용자가 원하는 위치에 평점과 정보를 기록하고 관리
- **그룹 관리**: 방문한 장소들을 카테고리별로 그룹화하여 체계적으로 관리
- **지도 기반 검색**: PostGIS 공간 쿼리로 내 주변 또는 특정 반경 내 기록된 장소 검색
- **리뷰 및 메모**: 각 장소에 대한 상세한 리뷰, 사진, 방문 일자 등 기록
- **방문 이력**: 언제, 어디를, 누구와 방문했는지 추적
- **즐겨찾기**: 다시 방문하고 싶은 장소 북마크
- **소셜 공유 (예정)**: 친구들과 장소 그룹 및 추천 공유

### Technical Features
- **위치 기반 서비스**: PostGIS를 활용한 공간 쿼리 (반경 내 장소 검색, 거리 계산)
- **사용자 인증**: Google OAuth2 + JWT 기반 인증
- **실시간 알림**: Kafka 기반 이벤트 처리
- **API 문서화**: Swagger UI를 통한 한국어 API 문서 제공
- **캐싱**: Redis를 활용한 성능 최적화

## Architecture Overview

### Microservices (Spring Cloud MSA)
- **config-service** (9999): Spring Cloud Config Server
- **gateway-service** (8080): Spring Cloud Gateway with security
- **auth-service** (8081): Google OAuth2 + JWT authentication
- **location-service** (8082): PostGIS spatial queries, Redis cache, Kafka events
- **notification-service** (8083): Kafka consumer for async notifications

### Common Modules (Shared Libraries)
- **common-core**: BaseEntity, exceptions, utilities (no Spring Web)
- **common-web**: ApiResponse, GlobalExceptionHandler, security configs

### Infrastructure Stack
- **Build**: Gradle multi-module, Java 21, Kotlin 1.9.25
- **Data**: PostgreSQL 15.4 + PostGIS, Redis, Kafka
- **Migration**: Flyway 10.0.0 (PostgreSQL 15.4 compatibility)
- **Container**: Docker Compose for local development

## Development Commands

### Quick Start
```bash
# Start infrastructure + config + gateway
./start-infrastructure.sh

# Start domain services  
./start-services.sh

# Stop everything
./stop-all.sh
```

### Individual Service Development
```bash
# Run individual services
./gradlew :config-service:bootRun
./gradlew :gateway-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :location-service:bootRun

# Test by service and layer (TDD approach)
./gradlew :auth-service:test                    # Full service test
./gradlew :auth-service:test --tests="**/domain/**"      # Domain layer only
./gradlew :auth-service:test --tests="**/service/**"     # Application layer only
./gradlew :auth-service:test --tests="**/repository/**"  # Infrastructure layer only
./gradlew :auth-service:test --tests="**/controller/**"  # Presentation layer only

# Continuous TDD development
./gradlew :location-service:test --tests="LocationTest" --continuous

# Build and test all services
./gradlew build
./gradlew test
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

### Swagger UI Access
```bash
# API Documentation (Swagger UI)
http://localhost:8081/swagger-ui.html    # Auth service API docs
http://localhost:8082/swagger-ui.html    # Location service API docs
http://localhost:8083/swagger-ui.html    # Notification service API docs

# Through Gateway (if configured)
http://localhost:8080/auth/swagger-ui.html
http://localhost:8080/locations/swagger-ui.html
http://localhost:8080/notifications/swagger-ui.html
```

## Architecture Patterns

### Clean Architecture (Per Service)
```
📂 {service-name}/src/main/kotlin/com/kangpark/openspot/{domain}/
├── controller/                 # Presentation Layer (HTTP, DTOs)
│   └── dto/{request,response}/
├── service/                    # Application Layer (Use Cases, orchestration)
│   └── usecase/
├── repository/                 # Infrastructure Layer (JPA implementations)
│   └── entity/                 # JPA entities with database mappings
└── domain/                     # Domain Layer (pure business logic)
    ├── entity/                 # Pure domain entities (no JPA annotations)
    ├── repository/             # Domain repository interfaces
    └── vo/            # Value objects and enums
```

### Dependency Flow
```
Presentation → Application → Domain ← Infrastructure
```
- **Domain Layer**: Pure business logic, no external dependencies, immutable entities
- **Application Layer**: Use cases, depends only on Domain interfaces
- **Infrastructure Layer**: Implements Domain interfaces, handles DB mapping via JPA entities
- **Presentation Layer**: HTTP controllers, depends on Application layer

### Entity Architecture Pattern (Domain/JPA Separation)
```kotlin
// Domain Entity (pure business logic)
class Location(
    val name: String,
    val description: String?,
    val category: CategoryType,
    val coordinates: Coordinates,
    val createdBy: UUID
) : BaseEntity() {
    fun updateRating(newRating: BigDecimal?): Location {
        return Location(/* updated fields */)
    }
}

// JPA Entity (database mapping)
@Entity
@Table(name = "locations", schema = "location")
class LocationJpaEntity(
    @Id @UuidGenerator val id: UUID? = null,
    @Column(name = "name") val name: String,
    // ... JPA annotations and mappings
) {
    fun toDomain(): Location { /* conversion logic */ }
    companion object {
        fun fromDomain(location: Location): LocationJpaEntity { /* conversion logic */ }
    }
}
```

### Repository Pattern (Domain/Infrastructure Separation)
```kotlin
// Domain repository interface (pure business contract)
interface LocationRepository {
    fun save(location: Location): Location
    fun findById(id: UUID): Location?
    fun findByCoordinatesWithinRadius(latitude: Double, longitude: Double, radiusMeters: Double): List<Location>
}

// Infrastructure implementation (JPA bridge)
@Repository
class LocationRepositoryImpl(
    private val jpaRepository: LocationJpaRepository
) : LocationRepository {
    override fun save(location: Location): Location {
        val jpaEntity = LocationJpaEntity.fromDomain(location)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }
}
```

### Common Module Usage
```kotlin
// All domain entities extend BaseEntity (common-core)
class User : BaseEntity() {
    // Automatically includes id: UUID, createdAt: LocalDateTime, updatedAt: LocalDateTime
}

// Consistent API responses (common-web)
@GetMapping("/api/v1/users/{id}")
fun getUser(@PathVariable id: String): ApiResponse<UserResponse> {
    return ApiResponse.success(userService.getUser(id))
}

// Paginated responses (common-web)
fun getLocations(pageable: Pageable): ApiResponse<PageResponse<LocationResponse>>
```

### Security Configuration
Each service has its own SecurityFilterChain to avoid conflicts:
- **Auth Service**: OAuth2 login, JWT refresh, public health endpoints
- **Location Service**: JWT authentication, public health and categories endpoints
- **Gateway Service**: Routes requests, no authentication (delegates to services)
- **Common Security**: Disabled by default to prevent FilterChain conflicts

## Key Implementation Patterns

### Domain-Driven Design with Clean Architecture
```kotlin
// 1. Pure Domain Entity (no external dependencies)
class Location(
    val name: String,
    val description: String?,
    val category: CategoryType,
    val coordinates: Coordinates,
    val createdBy: UUID,
    val averageRating: BigDecimal? = null,
    val reviewCount: Long = 0,
    val isActive: Boolean = true
) : BaseEntity() {

    fun updateRating(newAverageRating: BigDecimal?, newReviewCount: Long): Location {
        require(newReviewCount >= 0) { "리뷰 수는 0 이상이어야 합니다" }
        return Location(/* all fields with updates */)
    }

    fun deactivate(): Location {
        return Location(/* all fields with isActive = false */)
    }
}

// 2. JPA Entity for Database Mapping
@Entity
@Table(name = "locations", schema = "location")
class LocationJpaEntity(
    @Id @UuidGenerator val id: UUID? = null,
    @Column(name = "name", nullable = false, length = 100) val name: String,
    @Column(name = "description", length = 500) val description: String? = null,
    @Enumerated(EnumType.STRING) @Column(name = "category") val category: CategoryType,
    @Embedded val coordinates: CoordinatesEmbeddable,
    @Column(name = "created_by", nullable = false) val createdBy: UUID,
    @Column(name = "average_rating", precision = 3, scale = 2) val averageRating: BigDecimal? = null,
    @Column(name = "review_count", nullable = false) val reviewCount: Long = 0,
    @Column(name = "is_active", nullable = false) val isActive: Boolean = true,
    @CreatedDate @Column(name = "created_at", nullable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate @Column(name = "updated_at", nullable = false) val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Location {
        return Location(/* conversion from JPA to Domain */).apply {
            // BaseEntity 필드 설정 (리플렉션 사용)
            setBaseEntityFields(this@LocationJpaEntity.id!!, this@LocationJpaEntity.createdAt, this@LocationJpaEntity.updatedAt)
        }
    }

    companion object {
        fun fromDomain(location: Location): LocationJpaEntity {
            return LocationJpaEntity(/* conversion from Domain to JPA */)
        }
    }
}

// 3. Repository Interface (Domain Layer)
interface LocationRepository {
    fun save(location: Location): Location
    fun findById(id: UUID): Location?
    fun findByCoordinatesWithinRadius(latitude: Double, longitude: Double, radiusMeters: Double, category: CategoryType?, pageable: Pageable): Page<Location>
    fun findByCreatedBy(userId: UUID, pageable: Pageable): Page<Location>
    fun findPopularLocations(pageable: Pageable): Page<Location>
}

// 4. Repository Implementation (Infrastructure Layer)
@Repository
class LocationRepositoryImpl(
    private val jpaRepository: LocationJpaRepository
) : LocationRepository {

    override fun save(location: Location): Location {
        val jpaEntity = LocationJpaEntity.fromDomain(location)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }

    override fun findByCoordinatesWithinRadius(
        latitude: Double, longitude: Double, radiusMeters: Double,
        category: CategoryType?, pageable: Pageable
    ): Page<Location> {
        val jpaPage = jpaRepository.findByCoordinatesWithinRadius(latitude, longitude, radiusMeters, category, pageable)
        return jpaPage.map { it.toDomain() }
    }
}
```

### Circuit Breaker (External API Protection)
```kotlin
// Auth service → Google OAuth2 API
@CircuitBreaker(name = "google-oauth")
@Retry(name = "google-oauth")  
@TimeLimiter(name = "google-oauth")
fun authenticateWithGoogle(token: String): GoogleUserInfo
```

### Event-Driven Communication (Kafka)
```kotlin
// Location service publishes events
@Component
class LocationEventPublisher {
    fun publishLocationCreated(location: Location) {
        kafkaTemplate.send("location-events", LocationCreatedEvent(location))
    }

    fun publishReviewCreated(review: Review) {
        kafkaTemplate.send("review-events", ReviewCreatedEvent(review))
    }
}

// Notification service consumes events
@KafkaListener(topics = ["location-events", "review-events"])
fun handleLocationEvent(event: LocationEvent)
```

## Database Architecture

### Single Database with Service Separation
- **Database**: `openspot` (PostgreSQL + PostGIS)
- **Auth tables**: `users`, `refresh_tokens`
- **Location tables**: `locations`, `reviews`, `location_visits`, `location_stats`
- **Notification tables**: `notifications`

### Spatial Queries (PostGIS)
```sql
-- Find locations within radius (Location Service)
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

## TDD Development Approach

### Layer-by-Layer TDD (Per Service)
Clean Architecture 순서에 따른 TDD 개발:

1. **Domain First**: Pure business logic tests (no Spring context)
   ```bash
   ./gradlew :location-service:test --tests="**/domain/**"
   ```

2. **Application Layer**: Use case tests with mocked domain repositories
   ```bash
   ./gradlew :location-service:test --tests="**/service/**"
   ```

3. **Infrastructure Layer**: Integration tests with JPA repositories and Testcontainers
   ```bash
   ./gradlew :location-service:test --tests="**/repository/**"
   ```

4. **Presentation Layer**: MockMvc tests for HTTP endpoints
   ```bash
   ./gradlew :location-service:test --tests="**/controller/**"
   ```

### Clean Architecture Testing Strategy
```kotlin
// 1. Domain Layer Test (Pure business logic)
class LocationTest {
    @Test
    fun `should update rating when valid values provided`() {
        val location = Location.create(/* parameters */)
        val updated = location.updateRating(BigDecimal("4.5"), 10)
        assertThat(updated.averageRating).isEqualTo(BigDecimal("4.5"))
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
- **BaseEntity import 오류**: `com.kangpark.openspot.common.core.BaseEntity` 대신 `com.kangpark.openspot.common.core.domain.BaseEntity` 사용
- **Domain/JPA Entity 혼재**: Domain Entity는 순수 비즈니스 로직만, JPA Entity는 별도 클래스로 분리
- **Repository 구현 오류**: Domain Repository 인터페이스와 JPA Repository 구현체를 명확히 분리

#### Infrastructure Issues
- **Port conflicts**: Config (9999), Gateway (8080), services (8081-8083)
- **PostgreSQL version**: Requires Flyway 10.0.0 for PostgreSQL 15.4 compatibility
- **SecurityFilterChain conflicts**: Each service must have @Primary on its SecurityFilterChain
- **Config Service dependency**: All services need `spring.config.import` property

#### Development Issues
- **Swagger 의존성 누락**: `springdoc-openapi-starter-webmvc-ui:2.5.0` 의존성 확인
- **Clean Architecture 위반**: 레이어 간 의존성 방향 준수 (Domain ← Application ← Infrastructure)
- **Entity 변환 오류**: Domain ↔ JPA Entity 변환 시 BaseEntity 필드 처리 확인

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

**Endpoint Convention**: 사용자 개인 정보 관련 엔드포인트는 `/users/self` 패턴 사용

```bash
# Auth Service (8081)
GET  /api/v1/auth/google/login    # Google OAuth2 login
GET  /api/v1/users/self           # Current user profile
POST /api/v1/auth/token/refresh   # JWT token refresh
POST /api/v1/auth/logout          # Logout

# Location Service (8082)
GET    /api/v1/categories         # Available location categories (public)
POST   /api/v1/locations          # Create new location
GET    /api/v1/locations/{id}     # Get location details (increments view count)
PUT    /api/v1/locations/{id}     # Update location info
DELETE /api/v1/locations/{id}     # Deactivate location
GET    /api/v1/locations/search   # Search locations by various criteria
GET    /api/v1/locations/popular  # Popular locations by views
GET    /api/v1/users/self/locations  # User's created locations

# Location Reviews
POST   /api/v1/locations/{id}/reviews     # Create review for location
GET    /api/v1/locations/{id}/reviews     # Get reviews for location
GET    /api/v1/reviews/{id}               # Get specific review details
PUT    /api/v1/reviews/{id}               # Update own review
DELETE /api/v1/reviews/{id}               # Delete own review
POST   /api/v1/reviews/{id}/helpful       # Mark review as helpful
POST   /api/v1/reviews/{id}/report        # Report inappropriate review

# Location Visits & Favorites
POST   /api/v1/locations/{id}/visits            # Record location visit
PUT    /api/v1/visits/{id}                      # Update visit details
POST   /api/v1/locations/{id}/favorite/toggle   # Toggle favorite status
GET    /api/v1/users/self/visits                # User's visit history
GET    /api/v1/users/self/favorites             # User's favorite locations

# Notification Service (8083)
GET    /api/v1/notifications        # Get user notifications
PUT    /api/v1/notifications/{id}/read  # Mark notification as read
```

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