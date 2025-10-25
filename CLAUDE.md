# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Open-Spot** is a map-based location sharing platform where users can record ratings and information about places they've visited. Built with Spring Boot 3.5.5, Kotlin, and Clean Architecture principles.

### Purpose
사용자가 방문한 장소에 대한 개인적인 평점과 정보를 기록하고 관리하는 지도 기반 위치 공유 서비스입니다. 장소들을 그룹으로 관리할 수 있으며, 추후 친구들과 방문 장소 그룹을 공유할 수 있는 소셜 기능이 추가될 예정입니다.

### Core Features
- **개인 장소 기록**: 사용자가 원하는 위치에 평점과 정보를 기록하고 관리
- **그룹 관리**: 방문한 장소들을 사용자 정의 그룹으로 분류하여 체계적으로 관리
- **지도 기반 검색**: PostGIS 공간 쿼리로 내 주변 또는 특정 반경 내 기록된 장소 검색
- **개인 평가**: 각 장소에 대한 평점, 리뷰, 태그 기록
- **카테고리 분류**: 음식점, 카페, 관광지 등 사전 정의된 카테고리로 장소 분류
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
```

### Local Development (Docker Compose)
```bash
# 1. Start infrastructure + config + gateway (최초 실행)
./start-infrastructure.sh

# 2. Start domain services
./start-services.sh

# 서비스 중지
./stop-services.sh          # 도메인 서비스만 중지 (Docker 유지)
./stop-infrastructure.sh    # Config + Gateway만 중지
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

### Cloudflare 프로덕션 배포 (HTTPS + Origin 인증서)

**목표**: `https://api.kang-labs.com`을 내 컴퓨터의 Kubernetes Gateway로 연결

#### 전제 조건
```bash
# 1. Cloudflare Origin 인증서 발급 및 다운로드
# - Cloudflare 대시보드 → SSL/TLS → Origin Server
# - 인증서 생성 및 다운로드:
#   - Origin Certificate: kang-labs_origin_cert.pem
#   - Private Key: kang-labs_private.key
# - origin_cert/ 폴더에 저장

# 2. .env 설정
cp .env.example .env
# GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, JWT_SECRET 입력

# 3. 공유기 포트포워딩 설정 (사전 설정)
# - 프로토콜: TCP
# - 외부 포트: 443 (HTTPS)
# - 내부 호스트: 컴퓨터 IP (예: 192.168.1.100)
# - 내부 포트: 443
```

#### 배포 순서
```bash
# 1. Minikube 클러스터 생성 (Helm Ingress 자동 설치)
cd k8s/scripts
./2-create-cluster.sh
# → Nginx Ingress Controller가 LoadBalancer 타입으로 설치됨
# → HTTPS 포트 443 자동 활성화

# 2. Docker 이미지 빌드
./3-build-images.sh

# 3. Kubernetes에 배포 (자동으로 Origin 인증서 로드)
./4-deploy.sh
# → origin_cert/ 폴더에서 인증서 자동 감지
# → cloudflare-origin-tls Secret 생성
# → Ingress에 TLS 설정 자동 적용

# 4. Minikube tunnel 실행 (다른 터미널)
./5-start-tunnel.sh
# → Localhost:443에서 Ingress 접근 가능

# 5. 배포 확인
kubectl get pods -n openspot
kubectl get ingress -n openspot
kubectl get secret -n openspot | grep cloudflare
```

#### HTTPS 테스트
```bash
# 로컬 테스트 (host 헤더 필요)
curl -H "Host: openspot.local" https://localhost/api/v1/auth/health
curl -H "Host: api.kang-labs.com" https://localhost/api/v1/auth/health

# 공개 테스트 (Cloudflare DNS 설정 후)
curl https://api.kang-labs.com/api/v1/auth/health
```

#### Cloudflare SSL/TLS 설정
```
대시보드 → SSL/TLS → 개요
- 암호화 모드: Full (strict) 선택
  ✅ End-to-End HTTPS 암호화 (브라우저 ↔ Cloudflare ↔ 서버)
```

#### 아키텍처 흐름
```
브라우저 (HTTPS 443)
    ↓
Cloudflare (api.kang-labs.com)
    ↓ Full (strict) SSL
공유기 포트포워딩 (443:443)
    ↓
Nginx Ingress Controller (HTTPS, LoadBalancer)
    ↓ TLS Termination (Cloudflare Origin 인증서)
    ↓ HTTP
Gateway Service (8080)
```

**흐름도**:
```
브라우저/프론트엔드
    ↓ https://api.kang-labs.com
Cloudflare DNS (A 레코드)
    ↓ 203.0.113.42 (공인 IP)
공유기 포트포워딩 (443 → 컴퓨터:8080)
    ↓ http://localhost:8080
Minikube LoadBalancer (gateway-service)
    ↓
Spring Cloud Gateway (8080)
    ↓
Auth/Location/Notification Services
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

# Build and test
./gradlew build                  # Build all modules
./gradlew test                   # Test all modules
./gradlew clean build            # Clean build
./gradlew :location-service:build  # Build specific service
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
// Domain Entity (pure business logic) - BaseEntity 합성 패턴
data class Location(
    val userId: UUID,
    val name: String,
    val description: String? = null,
    val categoryId: UUID,
    val coordinates: Coordinates,
    val rating: Double? = null,              // 0.5-5.0 개인 평점
    val review: String? = null,
    val tags: List<String> = emptyList(),
    val groupId: UUID? = null,
    val isActive: Boolean = true,
    val baseEntity: BaseEntity = BaseEntity()  // 합성 (상속 대신)
) {
    // 편의 프로퍼티로 BaseEntity 필드 접근
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    fun updateEvaluation(rating: Double?, review: String?, tags: List<String>): Location {
        require(rating == null || (rating >= 0.5 && rating <= 5.0)) { "평점은 0.5-5.0 사이여야 합니다" }
        return copy(
            rating = rating,
            review = review,
            tags = tags,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    companion object {
        fun create(userId: UUID, name: String, /* ... */): Location {
            // 팩토리 메서드로 생성
        }
    }
}

// JPA Entity (database mapping)
@Entity
@Table(name = "locations", schema = "location")
data class LocationJpaEntity(
    @Id @UuidGenerator val id: UUID? = null,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "name", nullable = false, length = 100) val name: String,
    // ... JPA annotations and mappings
) {
    fun toDomain(): Location {
        return Location(/* conversion */).copy(
            baseEntity = BaseEntity(id = this.id!!, createdAt = this.createdAt, updatedAt = this.updatedAt)
        )
    }

    companion object {
        fun fromDomain(location: Location): LocationJpaEntity {
            return LocationJpaEntity(
                id = location.id,
                userId = location.userId,
                name = location.name,
                // ...
            )
        }
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

각 서비스의 SecurityFilterChain 설정:
- **Auth Service**:
  - OAuth2 login (Google)
  - JWT 발급 (Access Token: Response Body, Refresh Token: HttpOnly Cookie)
  - Public: health, login endpoints
- **Gateway Service**:
  - OAuth2 Resource Server (Bearer Token 검증)
  - 검증 성공 시 X-User-Id 헤더 추가하여 내부 서비스로 전달
  - 라우팅 및 중앙 인증
- **Location Service**:
  - Gateway 신뢰 (JWT 검증 없음)
  - X-User-Id 헤더 기반 사용자 식별
  - Public: health, categories endpoints
- **Common Security**: Disabled by default to prevent FilterChain conflicts

## Key Implementation Patterns

### Domain-Driven Design with Clean Architecture
```kotlin
// 1. Pure Domain Entity (no external dependencies) - BaseEntity 합성 패턴
data class Location(
    val userId: UUID,                        // 소유자
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val categoryId: UUID,
    val coordinates: Coordinates,
    val iconUrl: String? = null,

    // 개인 평가 정보
    val rating: Double? = null,              // 0.5-5.0 개인 평점 (0.5 단위)
    val review: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,         // 즐겨찾기 여부

    // 그룹 관리
    val groupId: UUID? = null,

    val isActive: Boolean = true,

    // BaseEntity 합성 (상속 대신 합성 사용)
    val baseEntity: BaseEntity = BaseEntity()
) {
    // 편의 프로퍼티로 BaseEntity 필드 접근
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    fun updateEvaluation(rating: Double?, review: String?, tags: List<String>): Location {
        rating?.let {
            require(it >= 0.5 && it <= 5.0) { "평점은 0.5-5.0 사이의 값이어야 합니다" }
            require(it % 0.5 == 0.0) { "평점은 0.5 단위여야 합니다" }
        }
        return copy(
            rating = rating,
            review = review,
            tags = tags,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    fun deactivate(): Location {
        return copy(
            isActive = false,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    fun toggleFavorite(): Location {
        return copy(
            isFavorite = !isFavorite,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    // 도메인 검증 로직
    fun isOwnedBy(checkUserId: UUID): Boolean = userId == checkUserId
    fun belongsToGroup(checkGroupId: UUID): Boolean = groupId == checkGroupId
}

// 2. JPA Entity for Database Mapping
@Entity
@Table(name = "locations", schema = "location")
data class LocationJpaEntity(
    @Id @UuidGenerator(style = UuidGenerator.Style.TIME) val id: UUID? = null,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "name", nullable = false, length = 100) val name: String,
    @Column(name = "description", length = 1000) val description: String? = null,
    @Column(name = "address", length = 200) val address: String? = null,
    @Column(name = "category_id", nullable = false) val categoryId: UUID,
    @Column(name = "latitude", nullable = false) val latitude: Double,
    @Column(name = "longitude", nullable = false) val longitude: Double,
    @Column(name = "icon_url", length = 500) val iconUrl: String? = null,
    @Column(name = "rating") val rating: Double? = null,
    @Column(name = "review", length = 2000) val review: String? = null,
    @Column(name = "tags") @Convert(converter = StringListConverter::class) val tags: List<String> = emptyList(),
    @Column(name = "is_favorite", nullable = false) val isFavorite: Boolean = false,
    @Column(name = "group_id") val groupId: UUID? = null,
    @Column(name = "is_active", nullable = false) val isActive: Boolean = true,
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate @Column(name = "updated_at", nullable = false) val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Location {
        return Location(
            userId = userId,
            name = name,
            description = description,
            address = address,
            categoryId = categoryId,
            coordinates = Coordinates(latitude, longitude),
            iconUrl = iconUrl,
            rating = rating,
            review = review,
            tags = tags,
            isFavorite = isFavorite,
            groupId = groupId,
            isActive = isActive,
            baseEntity = BaseEntity(id = id!!, createdAt = createdAt, updatedAt = updatedAt)
        )
    }

    companion object {
        fun fromDomain(location: Location): LocationJpaEntity {
            return LocationJpaEntity(
                id = location.id,
                userId = location.userId,
                name = location.name,
                description = location.description,
                address = location.address,
                categoryId = location.categoryId,
                latitude = location.coordinates.latitude,
                longitude = location.coordinates.longitude,
                iconUrl = location.iconUrl,
                rating = location.rating,
                review = location.review,
                tags = location.tags,
                isFavorite = location.isFavorite,
                groupId = location.groupId,
                isActive = location.isActive,
                createdAt = location.createdAt,
                updatedAt = location.updatedAt
            )
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
}

// Notification service consumes events
@KafkaListener(topics = ["location-events"])
fun handleLocationEvent(event: LocationEvent)
```

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

## 핵심 설계 원칙

### 1. Domain Entity는 Data Class + Composition 패턴
- **상속 대신 합성**: `BaseEntity`를 상속하지 않고 합성으로 사용
- **불변성**: `data class`와 `copy()` 메서드로 불변 객체 패턴 적용
- **순수 함수**: 모든 도메인 메서드는 새 객체를 반환 (side-effect 없음)

```kotlin
// ✅ Good: Composition pattern
data class Location(
    val name: String,
    val baseEntity: BaseEntity = BaseEntity()
) {
    val id: UUID get() = baseEntity.id
    fun updateName(newName: String) = copy(name = newName)
}

// ❌ Bad: Inheritance pattern
class Location(val name: String) : BaseEntity() {
    fun updateName(newName: String) {
        this.name = newName  // mutable
    }
}
```

### 2. JPA Entity는 별도 패키지에 완전 분리
- **Domain Entity**: `domain/entity/` - 순수 비즈니스 로직
- **JPA Entity**: `repository/jpa/entity/` - DB 매핑 전용
- **변환 책임**: JPA Entity가 Domain Entity로의 변환 책임

### 3. Repository 3-Layer 구조
```
domain/repository/LocationRepository.kt         # Interface (Domain Layer)
repository/jpa/LocationJpaRepository.kt         # Spring Data JPA (Infrastructure)
repository/impl/LocationRepositoryImpl.kt       # Implementation (Infrastructure)
```

### 4. Use Case 단위 서비스 설계
각 Use Case를 독립적인 클래스로 분리:
```
service/usecase/CreateLocationUseCase.kt
service/usecase/UpdateLocationUseCase.kt
service/usecase/SearchLocationUseCase.kt
```

### 5. API 응답 일관성
모든 API는 `ApiResponse<T>` wrapper 사용 (common-web 제공):
```kotlin
ApiResponse.success(data)           // 성공 응답
ApiResponse.error(code, message)    // 에러 응답
```