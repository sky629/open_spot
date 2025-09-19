# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Open-Spot** is a microservices platform where users can record and share locations they have visited with ratings, reviews, and memories. Built with Spring Boot 3.5.5, Kotlin, and Clean Architecture principles.

### Target: Users can pin locations on a map, record information and ratings for various categories (restaurants, cafes, shopping, parks, entertainment, accommodation), and share their experiences with others.

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
├── controller/     # Presentation Layer (HTTP, DTOs)
├── service/        # Application Layer (Use Cases, orchestration)  
├── repository/     # Infrastructure Layer (DB, external APIs, cache)
└── domain/         # Domain Layer (entities, value objects, business rules)
```

### Dependency Flow
```
Presentation → Application → Domain ← Infrastructure
```
- **Domain Layer**: Pure business logic, no external dependencies
- **Application Layer**: Use cases, depends only on Domain  
- **Infrastructure Layer**: Implements Domain interfaces (Repository pattern)
- **Presentation Layer**: HTTP controllers, depends on Application

### Common Module Usage
```kotlin
// All services extend BaseEntity (common-core)
class User : BaseEntity() {
    // Automatically includes id, createdAt, updatedAt
}

// Consistent API responses (common-web)  
@GetMapping("/api/v1/users/{id}")
fun getUser(@PathVariable id: String): ApiResponse<UserResponse> {
    return ApiResponse.success(userService.getUser(id))
}

// Paginated responses (common-web)
fun getStores(pageable: Pageable): ApiResponse<PageResponse<StoreResponse>>
```

### Security Configuration
Each service has its own SecurityFilterChain to avoid conflicts:
- **Auth Service**: OAuth2 login, JWT refresh, public health endpoints
- **Location Service**: JWT authentication, public health and categories endpoints
- **Gateway Service**: Routes requests, no authentication (delegates to services)
- **Common Security**: Disabled by default to prevent FilterChain conflicts

## Key Implementation Patterns

### Repository Pattern (Infrastructure → Domain)
```kotlin
// Domain layer interface
interface UserRepository {
    fun findById(id: UUID): User?
    fun save(user: User): User
}

// Infrastructure implementation  
@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: UUID): User? = 
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
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
1. **Domain First**: Pure business logic tests (no Spring context)
2. **Application Layer**: Use case tests with mocked repositories  
3. **Infrastructure Layer**: Integration tests with Testcontainers
4. **Presentation Layer**: MockMvc tests for HTTP endpoints

### Independent Service Testing
Each service has completely isolated test dependencies:
```kotlin
// Service-specific test dependencies
testImplementation("org.testcontainers:postgresql")     # Location Service
testImplementation("org.testcontainers:redis")         # Location Service
testImplementation("org.springframework.security:spring-security-test") # Auth Service
```

## Troubleshooting

### Common Issues
- **Port conflicts**: Config (9999), Gateway (8080), services (8081-8083)
- **PostgreSQL version**: Requires Flyway 10.0.0 for PostgreSQL 15.4 compatibility
- **SecurityFilterChain conflicts**: Each service must have @Primary on its SecurityFilterChain
- **Config Service dependency**: All services need `spring.config.import` property

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
```bash
# Auth Service
GET  /api/v1/auth/google/login    # Google OAuth2 login
GET  /api/v1/users/self           # Current user profile
POST /api/v1/auth/token/refresh   # JWT token refresh

# Location Service
GET    /api/v1/categories         # Available categories
POST   /api/v1/locations          # Create new location
GET    /api/v1/locations/{id}     # Get location details (increments view count)
PUT    /api/v1/locations/{id}     # Update location info
DELETE /api/v1/locations/{id}     # Deactivate location
GET    /api/v1/locations/search   # Search locations by various criteria
GET    /api/v1/locations/popular  # Popular locations by views
GET    /api/v1/locations/my       # User's created locations

# Reviews
POST   /api/v1/locations/{id}/reviews     # Create review
GET    /api/v1/locations/{id}/reviews     # Get location reviews
GET    /api/v1/reviews/{id}               # Get specific review
PUT    /api/v1/reviews/{id}               # Update review
DELETE /api/v1/reviews/{id}               # Delete review
POST   /api/v1/reviews/{id}/helpful       # Mark review helpful
POST   /api/v1/reviews/{id}/report        # Report review

# Visits & Favorites
POST   /api/v1/locations/{id}/visits      # Record visit
PUT    /api/v1/visits/{id}                # Update visit info
POST   /api/v1/locations/{id}/favorite/toggle  # Toggle favorite
GET    /api/v1/users/self/visits          # User's visit history
GET    /api/v1/users/self/favorites       # User's favorite locations
```