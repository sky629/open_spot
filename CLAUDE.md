# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Open-Spot** is a microservices platform providing AI-based commercial district analysis reports for Seoul entrepreneurs using public data. Built with Spring Boot 3.5.5, Kotlin, and Clean Architecture principles.

### Target: Seoul cafe/restaurant entrepreneurs need data-driven location analysis with A-F scoring based on competitor density, foot traffic, and sales data.

## Architecture Overview

### Microservices (Spring Cloud MSA)
- **config-service** (9999): Spring Cloud Config Server
- **gateway-service** (8080): Spring Cloud Gateway with security
- **auth-service** (8081): Google OAuth2 + JWT authentication  
- **analysis-service** (8082): PostGIS spatial queries, Redis cache, Kafka events
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
./gradlew :analysis-service:bootRun

# Test by service and layer (TDD approach)
./gradlew :auth-service:test                    # Full service test
./gradlew :auth-service:test --tests="**/domain/**"      # Domain layer only
./gradlew :auth-service:test --tests="**/service/**"     # Application layer only
./gradlew :auth-service:test --tests="**/repository/**"  # Infrastructure layer only
./gradlew :auth-service:test --tests="**/controller/**"  # Presentation layer only

# Continuous TDD development
./gradlew :auth-service:test --tests="UserTest" --continuous

# Build and test all services
./gradlew build
./gradlew test
```

### Health Check Endpoints
```bash
# Through Gateway (port 8080)
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/stores/health

# Direct service access
curl http://localhost:8081/api/v1/auth/health     # Auth service
curl http://localhost:8082/api/v1/stores/health   # Analysis service
curl http://localhost:9999/actuator/health        # Config service
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
- **Analysis Service**: JWT authentication, public health endpoints  
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
// Analysis service publishes events
@Component
class ReportEventPublisher {
    fun publishReportGenerated(reportId: UUID) {
        kafkaTemplate.send("report-events", ReportGeneratedEvent(reportId))
    }
}

// Notification service consumes events
@KafkaListener(topics = ["report-events"])  
fun handleReportGenerated(event: ReportGeneratedEvent)
```

## Database Architecture

### Single Database with Service Separation
- **Database**: `openspot` (PostgreSQL + PostGIS)
- **Auth tables**: `users`, `refresh_tokens`
- **Analysis tables**: `stores`, `reports`, `analysis_results`  
- **Notification tables**: `notifications`

### Spatial Queries (PostGIS)
```sql
-- Find stores within radius (Analysis Service)
SELECT * FROM stores 
WHERE ST_DWithin(
    location::geography, 
    ST_SetSRID(ST_MakePoint(?longitude, ?latitude), 4326)::geography,
    ?radius
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
testImplementation("org.testcontainers:postgresql")     # Analysis Service  
testImplementation("org.testcontainers:redis")         # Analysis Service
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
4. Domain Services (8081-8083) - Auth, Analysis, Notification

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
POST /api/v1/auth/login          # Google OAuth2 login
GET  /api/v1/users/self            # Current user profile
POST /api/v1/auth/token/refresh  # JWT token refresh

# Analysis Service
GET  /api/v1/stores?lat={}&lon={}&radius={}  # Find stores in radius (paginated)
POST /api/v1/reports             # Generate analysis report  
GET  /api/v1/reports/{id}        # Get specific report
GET  /api/v1/reports             # User's report history (paginated)
```