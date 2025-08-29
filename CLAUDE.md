# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Open-Spot** is a B2B web service that provides AI-based commercial district analysis reports for prospective entrepreneurs using Seoul city public data. The project aims to help small business owners make data-driven decisions for successful startups by providing affordable, comprehensive commercial district analysis.

## Target Users
- **Primary**: 30-40s prospective entrepreneurs planning to start cafes, small restaurants, or gyms in Seoul
- **Secondary**: Franchise headquarters store development teams, commercial real estate agents and consultants

## MVP Core Features
- **Location Analysis**: Click on map or input address to analyze specific locations
- **Business Type Analysis**: Initially focused on cafes only  
- **Analysis Radius**: 300m, 500m, 1km options
- **Report Generation**: Automated analysis reports with unique IDs
- **Competitor Analysis**: Visualization of competing cafes in the area
- **Foot Traffic Analysis**: Hourly/gender/age demographics visualization
- **Sales Analysis**: Quarterly average sales and trends for similar businesses
- **Overall Score**: A-F grade "commercial attractiveness" rating
- **User Authentication**: Google social login
- **Report History**: View past analysis reports

## Development Principles

### Clean Code & Clean Architecture
- Follow Clean Code principles: meaningful names, small functions, clear structure
- Implement Clean Architecture layers: Domain, Application, Infrastructure, Presentation
- Dependency inversion: high-level modules should not depend on low-level modules
- Separation of concerns: each class/module has a single responsibility

### API Design Standards
- **REST API Prefix**: All APIs must use `/api/v1` prefix for versioning
- **Pagination**: All query APIs must support pagination by default
- **Consistent Response Format**: Standardized JSON response structure across all services

### Development Methodology
- **Test-Driven Development (TDD)**: Write tests first, then implement functionality
- **Red-Green-Refactor**: Fail test → Pass test → Refactor code cycle
- **High Test Coverage**: Aim for comprehensive unit, integration, and E2E test coverage

## Planned Architecture

### Microservices Architecture (MSA)
The system will be built as a microservices architecture following Clean Architecture principles:

#### Backend Services (Kotlin/Spring Boot)
- **config-service**: Centralized configuration management (Spring Cloud Config Server)
- **gateway-service**: API Gateway with routing, authentication, logging (Spring Cloud Gateway + Circuit Breaker)
- **auth-service**: Google OAuth2 authentication, user management, JWT token handling (Circuit Breaker for Google API)
- **analysis-service**: Core business logic for commercial district data analysis and report generation
- **notification-service**: Async notification handling via Kafka events

#### Spring Cloud Components
- **Spring Cloud Config**: Centralized configuration management across all services
- **Spring Cloud Gateway**: API Gateway pattern with routing, CORS, authentication
- **Spring Cloud Circuit Breaker (Resilience4j)**: Fault tolerance for external API calls

#### Clean Architecture Layers (per service)
```
Presentation Layer (Controllers, DTOs)
├── Application Layer (Use Cases, Services)  
├── Domain Layer (Entities, Value Objects, Repositories)
└── Infrastructure Layer (Database, External APIs, Configurations)
```

#### Data Pipeline (Python)
- **data-pipeline**: ETL scripts for processing Seoul public data using Pandas, GeoPandas

#### Infrastructure
- **Database**: PostgreSQL with PostGIS for spatial data
- **Cache**: Redis for performance optimization
- **Message Queue**: Kafka for async communication
- **External APIs**: Google OAuth2 (with Circuit Breaker protection)
- **Maps**: Naver Map API
- **Charts**: ECharts.js
- **Deployment**: Docker, docker-compose(local, dev), Kubernetes (AWS EKS/ECS)(prod-추 후 적용), GitHub Actions

### Repository Structure
```
open_spot/          # Mono-repo for all backend services
├── build.gradle.kts       # Multi-module Gradle configuration
├── settings.gradle.kts
└── msa-modules/
    ├── 0-common-core/     # Common core components (exceptions, domain, utils)
    ├── 1-common-web/      # Common web components (dto, exception handlers)
    ├── 2-config-service/  # Infrastructure service
    ├── 3-gateway-service/ # Infrastructure service
    ├── 4-auth-service/    # User/Authentication domain
    ├── 5-analysis-service/ # Commercial analysis domain
    └── 6-notification-service/ # Notification domain

open_spot_data_pipeline/    # Python ETL scripts
├── scripts/
└── pyproject.toml
```

### Common Modules Structure

#### Common Core Module (common-core)
```
common-core/src/main/kotlin/com/openspot/common/core/
├── exception/
│   ├── BaseException.kt           # 기본 예외 클래스
│   ├── BusinessException.kt       # 비즈니스 예외  
│   ├── ValidationException.kt     # 유효성 검증 예외
│   ├── ExternalApiException.kt    # 외부 API 예외
│   └── ErrorCode.kt               # 에러 코드 enum
├── domain/
│   ├── BaseEntity.kt              # 공통 엔터티 (createdAt, updatedAt)
│   ├── BaseValueObject.kt         # 공통 값 객체
│   └── DomainEvent.kt             # 도메인 이벤트 인터페이스
├── util/
│   ├── DateTimeUtil.kt            # 날짜/시간 유틸리티
│   ├── ValidationUtil.kt          # 유효성 검증 유틸리티
│   └── Constants.kt               # 공통 상수
└── config/
    ├── JpaConfig.kt               # 공통 JPA 설정
    └── CircuitBreakerConfig.kt    # 공통 Circuit Breaker 설정
```

#### Common Web Module (common-web)
```
common-web/src/main/kotlin/com/openspot/common/web/
├── dto/
│   ├── ApiResponse.kt             # 공통 API 응답 래퍼
│   ├── PageRequest.kt             # 페이징 요청
│   ├── PageResponse.kt            # 페이징 응답
│   └── ErrorResponse.kt           # 에러 응답
├── exception/
│   ├── GlobalExceptionHandler.kt  # 전역 예외 처리
│   └── ApiExceptionHandler.kt     # API 예외 처리
├── interceptor/
│   ├── LoggingInterceptor.kt      # 로깅 인터셉터
│   └── AuthInterceptor.kt         # 인증 인터셉터
├── filter/
│   └── CorsFilter.kt              # CORS 필터
└── config/
    ├── WebConfig.kt               # 웹 공통 설정
    ├── SecurityConfig.kt          # 보안 공통 설정
    └── SwaggerConfig.kt           # Swagger 공통 설정
```

### Clean Architecture Layered Pattern Structure
```
📂 {service-name}/
└── src/main/kotlin/com/openspot/{domain}/
    ├── controller/     # Presentation Layer
    │   ├── {Domain}Controller.kt
    │   ├── dto/
    │   │   ├── request/
    │   │   ├── response/ 
    │   │   └── mapper/
    │   └── exception/
    ├── service/        # Application Layer
    │   ├── {Domain}ApplicationService.kt
    │   └── usecase/
    ├── repository/     # Infrastructure Layer
    │   ├── {Domain}RepositoryImpl.kt
    │   ├── external/
    │   ├── cache/
    │   └── messaging/
    └── domain/         # Domain Layer
        ├── {Entity}.kt
        ├── {ValueObject}.kt
        ├── {Domain}Repository.kt (interface)
        └── {Domain}DomainService.kt
```

## Data Sources & Architecture

### Data Collection (Python Pipeline)
Seoul Open Data Portal APIs (collected by separate Python ETL pipeline):
- Restaurant license information
- Commercial district pedestrian traffic (quarterly)  
- Commercial district estimated sales (annual/monthly)
- Commercial district area definitions
- Commercial district catchment areas

**Data Flow**: `Seoul Open Data APIs → Python ETL → PostgreSQL/PostGIS → MSA Services`

### External API Integrations (MSA Services)
- **Google OAuth2 API**: User authentication (Auth Service only)
- **Naver Map API**: Frontend map display (not MSA backend)

## Development Roadmap

### Phase 1: Infrastructure & Data
1. Set up Git repositories (backend, data-pipeline, config-repo)
2. Configure Docker Compose for local development (PostgreSQL+PostGIS, Redis, Kafka)  
3. Implement Python ETL scripts to populate store data

### Phase 2: Service-Independent Domain Layer Implementation (TDD First)
**각 서비스별 독립적인 Domain-First 접근**

#### Auth Service Domain Layer (독립 개발)
```bash
# Auth Service 독립 테스트 실행
./gradlew :auth-service:test --tests="**/domain/**"
```
1. `UserTest.kt`: 사용자 엔터티 비즈니스 규칙 테스트
2. `EmailTest.kt`, `UserIdTest.kt`: Value Object 불변성 테스트
3. `AuthDomainServiceTest.kt`: 인증 도메인 로직 테스트
4. Domain entities 구현 및 Repository interface 정의

#### Analysis Service Domain Layer (독립 개발)
```bash
# Analysis Service 독립 테스트 실행  
./gradlew :analysis-service:test --tests="**/domain/**"
```
1. `StoreTest.kt`, `ReportTest.kt`: 매장/리포트 엔터티 테스트
2. `LocationTest.kt`, `ScoreTest.kt`: 위치/점수 Value Object 테스트
3. `AnalysisDomainServiceTest.kt`: 상권 분석 비즈니스 로직 테스트
4. Domain entities 구현 및 Repository interface 정의

### Phase 3: Service-Independent Application Layer Implementation (TDD)
**각 서비스별 Use Case-Driven Development**

#### Auth Service Application Layer (독립 개발)
```bash
# Auth Service Application Layer 테스트
./gradlew :auth-service:test --tests="**/service/**"
```
1. `AuthenticateUserUseCaseTest.kt`: 인증 Use Case 테스트
2. `GetUserProfileUseCaseTest.kt`: 프로필 조회 Use Case 테스트
3. `AuthApplicationServiceTest.kt`: Application Service 테스트
4. Use Case 구현 및 Application Service 조합

#### Analysis Service Application Layer (독립 개발)
```bash  
# Analysis Service Application Layer 테스트
./gradlew :analysis-service:test --tests="**/service/**"
```
1. `FindStoresInRadiusUseCaseTest.kt`: 매장 조회 Use Case (페이징 포함)
2. `GenerateReportUseCaseTest.kt`: 리포트 생성 Use Case 테스트
3. Service 구현 및 Use Case 조합

### Phase 4: Service-Independent Infrastructure Layer Implementation (TDD)
**각 서비스별 Outside-In Integration Testing**

#### Auth Service Infrastructure Layer (독립 개발)
```bash
# Auth Service Infrastructure 통합 테스트
./gradlew :auth-service:test --tests="**/repository/**"
```
1. `UserRepositoryImplTest.kt`: PostgreSQL + Testcontainers 테스트
2. `GoogleOAuthClientTest.kt`: 외부 API 통합 테스트
3. Repository 구현체 및 External API 클라이언트 구현

#### Analysis Service Infrastructure Layer (독립 개발)
```bash
# Analysis Service Infrastructure 통합 테스트
./gradlew :analysis-service:test --tests="**/repository/**"
```
1. `StoreRepositoryImplTest.kt`: PostGIS 공간 쿼리 테스트
2. `StoreRedisCacheTest.kt`: Redis 캐시 통합 테스트
3. `ReportEventPublisherTest.kt`: Kafka Producer 테스트

### Phase 5: Service-Independent Presentation Layer Implementation (TDD)
**각 서비스별 API-First Development**

#### Auth Service Presentation Layer (독립 개발)
```bash
# Auth Service Controller 테스트
./gradlew :auth-service:test --tests="**/controller/**"
```
1. `AuthControllerTest.kt`: `/api/v1/auth/**` 엔드포인트 테스트
2. `UserControllerTest.kt`: `/api/v1/users/**` 엔드포인트 테스트
3. Controller 구현 및 DTO 매핑

#### Analysis Service Presentation Layer (독립 개발)
```bash
# Analysis Service Controller 테스트  
./gradlew :analysis-service:test --tests="**/controller/**"
```
1. `StoreControllerTest.kt`: `/api/v1/stores` 페이징 API 테스트
2. `ReportControllerTest.kt`: `/api/v1/reports` CRUD API 테스트
3. Controller 구현 및 페이징 처리

### Phase 6: Frontend Integration
1. Develop React/TypeScript frontend
2. Implement end-to-end flow: login → authentication → API calls → map visualization

## Success Metrics
- **User Adoption**: 100+ user signups within 1 month of launch
- **Engagement**: Average 3+ reports generated per user
- **Satisfaction**: 50%+ positive responses to "Did the report help your business decision?"

## Domain Services Structure

### Auth Service (User/Authentication Domain) - 공통 모듈 적용 후
```
auth-service/src/main/kotlin/com/openspot/auth/
├── controller/                    # Presentation Layer
│   ├── AuthController.kt          # /api/v1/auth/**
│   ├── UserController.kt          # /api/v1/users/**
│   ├── dto/                       # Auth 서비스 특화 DTO만
│   │   ├── request/
│   │   │   ├── LoginRequest.kt
│   │   │   └── RefreshTokenRequest.kt
│   │   ├── response/
│   │   │   ├── UserResponse.kt
│   │   │   └── TokenResponse.kt
│   │   └── mapper/
│   │       └── AuthDtoMapper.kt
│   └── exception/
│       └── AuthExceptionHandler.kt    # Auth 특화 예외만 (OAuth2 등)
├── service/                       # Application Layer  
│   ├── AuthApplicationService.kt
│   ├── UserApplicationService.kt
│   └── usecase/
│       ├── AuthenticateUserUseCase.kt
│       ├── RefreshTokenUseCase.kt
│       └── GetUserProfileUseCase.kt
├── repository/                    # Infrastructure Layer
│   ├── UserRepositoryImpl.kt
│   ├── RefreshTokenRepositoryImpl.kt
│   └── external/
│       └── GoogleOAuthClient.kt       # Circuit Breaker 적용
└── domain/                        # Domain Layer
    ├── User.kt (extends BaseEntity)      # 공통 BaseEntity 상속
    ├── RefreshToken.kt (extends BaseEntity)
    ├── Email.kt (extends BaseValueObject) # 공통 BaseValueObject 상속
    ├── UserId.kt (extends BaseValueObject)
    ├── UserRepository.kt          # Repository Interface
    └── AuthDomainService.kt       # Domain Service

# 공통 모듈로 이동된 것들:
# - 페이징 관련: PageRequest, PageResponse → common-web
# - 공통 예외: GlobalExceptionHandler → common-web  
# - 기본 설정: JpaConfig, CircuitBreakerConfig → common-core
# - 공통 응답: ApiResponse, ErrorResponse → common-web
```

### Analysis Service (Commercial Analysis Domain) - 공통 모듈 적용 후  
```
analysis-service/src/main/kotlin/com/openspot/analysis/
├── controller/                    # Presentation Layer
│   ├── StoreController.kt         # /api/v1/stores
│   ├── ReportController.kt        # /api/v1/reports  
│   ├── dto/                       # Analysis 서비스 특화 DTO만
│   │   ├── request/
│   │   │   ├── StoreSearchRequest.kt
│   │   │   └── ReportCreateRequest.kt
│   │   ├── response/
│   │   │   ├── StoreResponse.kt
│   │   │   └── ReportResponse.kt
│   │   └── mapper/
│   │       └── AnalysisDtoMapper.kt
│   └── exception/
│       └── AnalysisExceptionHandler.kt  # Analysis 특화 예외만
├── service/                       # Application Layer
│   ├── StoreApplicationService.kt
│   ├── ReportApplicationService.kt  
│   └── usecase/
│       ├── FindStoresInRadiusUseCase.kt
│       ├── GenerateReportUseCase.kt
│       ├── GetReportUseCase.kt
│       └── GetUserReportsUseCase.kt
├── repository/                    # Infrastructure Layer
│   ├── StoreRepositoryImpl.kt     # PostGIS implementation
│   ├── ReportRepositoryImpl.kt    # JPA implementation
│   ├── cache/
│   │   └── StoreRedisCache.kt
│   └── messaging/
│       └── ReportEventPublisher.kt
└── domain/                        # Domain Layer
    ├── Store.kt (extends BaseEntity)         # 공통 BaseEntity 상속
    ├── Report.kt (extends BaseEntity)        # 공통 BaseEntity 상속
    ├── AnalysisResult.kt (extends BaseEntity) # 공통 BaseEntity 상속
    ├── Location.kt (extends BaseValueObject)  # 공통 BaseValueObject 상속
    ├── BusinessType.kt (extends BaseValueObject)
    ├── Score.kt (extends BaseValueObject)
    ├── Radius.kt (extends BaseValueObject)
    ├── StoreRepository.kt        # Repository Interface
    ├── ReportRepository.kt       # Repository Interface
    ├── AnalysisDomainService.kt  # Domain Service
    └── ScoreCalculationService.kt # Domain Service

# 공통 모듈로 이동된 것들:
# - 페이징 관련: PageRequest, PageResponse → common-web
# - 공통 예외: GlobalExceptionHandler → common-web
# - 공통 응답: ApiResponse, ErrorResponse → common-web
# - Redis, JPA 설정 → common-core
```

### Notification Service (Notification Domain) - 공통 모듈 적용 후
```
notification-service/src/main/kotlin/com/openspot/notification/
├── consumer/                      # Presentation Layer (Kafka)
│   └── ReportEventConsumer.kt     # Kafka message consumer
├── service/                       # Application Layer
│   ├── NotificationApplicationService.kt
│   └── usecase/
│       └── SendNotificationUseCase.kt
├── repository/                    # Infrastructure Layer
│   └── external/
│       └── EmailNotificationService.kt
└── domain/                        # Domain Layer
    ├── Notification.kt (extends BaseEntity)    # 공통 BaseEntity 상속
    ├── NotificationType.kt (extends BaseValueObject) # 공통 BaseValueObject 상속
    ├── Message.kt (extends BaseValueObject)    # 공통 BaseValueObject 상속
    └── NotificationDomainService.kt # Domain Service

# 공통 모듈로 이동된 것들:
# - 공통 예외: GlobalExceptionHandler → common-web
# - 공통 설정: Kafka Config → common-core (필요시)
# - 공통 응답: ApiResponse → common-web (Consumer는 HTTP 응답 없음)
```

## 공통 모듈 재사용 패턴

### 1. 공통 예외 처리
모든 서비스에서 `GlobalExceptionHandler`가 기본 예외를 처리하고, 서비스별 특화 예외는 개별 Handler에서 처리:

```kotlin
// common-web의 GlobalExceptionHandler
@RestControllerAdvice  
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse>
}

// auth-service의 AuthExceptionHandler
@RestControllerAdvice
class AuthExceptionHandler : GlobalExceptionHandler() {
    @ExceptionHandler(OAuth2AuthenticationException::class)  
    fun handleOAuth2Exception(ex: OAuth2AuthenticationException): ResponseEntity<ErrorResponse>
}
```

### 2. 공통 API 응답 형식
모든 REST API에서 일관된 응답 형식 사용:

```kotlin
// Controller에서 공통 ApiResponse 사용
@GetMapping("/api/v1/users/{id}")
fun getUser(@PathVariable id: String): ApiResponse<UserResponse> {
    val user = userService.getUser(id)
    return ApiResponse.success(user)
}
```

### 3. 공통 페이징
모든 조회 API에서 동일한 페이징 구조 사용:

```kotlin
// common-web의 PageRequest, PageResponse 재사용
@GetMapping("/api/v1/stores")  
fun getStores(pageRequest: PageRequest): ApiResponse<PageResponse<StoreResponse>>
```

## Key API Endpoints

All APIs follow `/api/v1` prefix and support pagination where applicable:

### Auth Service
- `GET /api/v1/users/me` - Get current user information
- `POST /api/v1/auth/token/refresh` - Refresh JWT token

### Analysis Service  
- `GET /api/v1/stores?lat={lat}&lon={lon}&radius={radius}&page={page}&size={size}` - Get stores in area (paginated)
- `POST /api/v1/reports` - Create analysis report
- `GET /api/v1/reports/{reportId}` - Get specific report
- `GET /api/v1/reports?userId={userId}&page={page}&size={size}` - Get user's reports (paginated)

## Clean Architecture Dependency Rules

### Dependency Direction (Dependency Rule)
```
Presentation Layer (controller/)
    ↓ depends on
Application Layer (service/)  
    ↓ depends on
Domain Layer (domain/)
    ↑ implements
Infrastructure Layer (repository/)
```

### Layer Responsibilities & Dependencies

**Domain Layer** (`domain/` package):
- **Contains**: Entities, Value Objects, Repository Interfaces, Domain Services
- **Dependencies**: NONE (독립적, 다른 레이어 참조 금지)
- **Purpose**: 순수한 비즈니스 로직과 규칙

**Application Layer** (`service/` package):
- **Contains**: Use Cases, Application Services
- **Dependencies**: Domain Layer ONLY
- **Purpose**: 비즈니스 시나리오 조합, 트랜잭션 경계

**Infrastructure Layer** (`repository/` package):
- **Contains**: Repository Implementations, External API Clients, Cache, Messaging
- **Dependencies**: Domain Layer interfaces 구현
- **Purpose**: 외부 시스템과의 통신, 데이터 영속성

**Presentation Layer** (`controller/` package):
- **Contains**: Controllers, DTOs, Exception Handlers
- **Dependencies**: Application Layer 참조
- **Purpose**: HTTP 요청/응답 처리, API 엔드포인트

### Dependency Injection Strategy
- **Domain** → **Application**: Constructor Injection
- **Application** → **Infrastructure**: Interface를 통한 의존성 주입
- **Infrastructure** → **Domain**: Repository Interface 구현

## Testing Strategy

### TDD Layer-by-Layer Implementation
**테스트 우선 개발 순서**: Domain → Application → Infrastructure → Presentation

### MSA Service-Independent Test Structure

각 MSA 서비스별로 완전히 독립적인 테스트 환경을 구성합니다.

### Test Types by Layer (레이어 패키지명과 일치)

**Domain Layer Tests** (`src/test/kotlin/.../domain/`):
- **Entity Tests**: 비즈니스 규칙, 상태 변경, 유효성 검증
- **Value Object Tests**: 불변성, 동등성, 유효성
- **Domain Service Tests**: 복잡한 비즈니스 로직
- **도구**: JUnit 5, AssertJ (순수 단위 테스트, 외부 의존성 없음)

**Application Layer Tests** (`src/test/kotlin/.../service/`):
- **Use Case Tests**: 비즈니스 시나리오, 도메인 객체 조합
- **Application Service Tests**: 여러 Use Case 조합
- **도구**: JUnit 5, MockK (Domain Layer mocking)

**Infrastructure Layer Tests** (`src/test/kotlin/.../repository/`):
- **Repository Tests**: 데이터베이스 연동, 공간 쿼리 (PostGIS)
- **External API Tests**: 서울 공공데이터, Google OAuth
- **Cache Tests**: Redis 연동
- **Messaging Tests**: Kafka Producer/Consumer
- **도구**: JUnit 5, Testcontainers, @DataJpaTest

**Presentation Layer Tests** (`src/test/kotlin/.../controller/`, `.../consumer/`):
- **Controller Tests**: HTTP 요청/응답, 페이징, 예외 처리
- **Consumer Tests**: Kafka 메시지 소비 테스트
- **DTO Mapping Tests**: 요청/응답 객체 변환
- **도구**: JUnit 5, MockMvc, @WebMvcTest

### Service-Specific Test Structure

#### Auth Service Test Structure
```
auth-service/
├── src/main/kotlin/com/openspot/auth/
│   ├── controller/     # Presentation Layer
│   ├── service/        # Application Layer  
│   ├── repository/     # Infrastructure Layer
│   └── domain/         # Domain Layer
└── src/test/kotlin/com/openspot/auth/
    ├── controller/     # Controller Tests (MockMvc, @WebMvcTest)
    │   ├── AuthControllerTest.kt
    │   └── UserControllerTest.kt
    ├── service/        # Service/UseCase Tests (MockK)
    │   ├── AuthApplicationServiceTest.kt
    │   └── usecase/
    │       ├── AuthenticateUserUseCaseTest.kt
    │       └── GetUserProfileUseCaseTest.kt
    ├── repository/     # Repository Integration Tests (Testcontainers)
    │   ├── UserRepositoryImplTest.kt
    │   └── external/
    │       └── GoogleOAuthClientTest.kt
    └── domain/         # Domain Unit Tests (순수 단위)
        ├── UserTest.kt
        ├── EmailTest.kt
        └── AuthDomainServiceTest.kt
```

#### Analysis Service Test Structure  
```
analysis-service/
├── src/main/kotlin/com/openspot/analysis/
│   ├── controller/     # Presentation Layer
│   ├── service/        # Application Layer
│   ├── repository/     # Infrastructure Layer  
│   └── domain/         # Domain Layer
└── src/test/kotlin/com/openspot/analysis/
    ├── controller/     # Controller Tests
    │   ├── StoreControllerTest.kt
    │   └── ReportControllerTest.kt
    ├── service/        # Service/UseCase Tests
    │   ├── StoreApplicationServiceTest.kt
    │   └── usecase/
    │       ├── FindStoresInRadiusUseCaseTest.kt
    │       └── GenerateReportUseCaseTest.kt
    ├── repository/     # Repository Integration Tests
    │   ├── StoreRepositoryImplTest.kt (PostGIS 테스트)
    │   └── cache/
    │       └── StoreRedisCacheTest.kt
    └── domain/         # Domain Unit Tests
        ├── StoreTest.kt
        ├── ReportTest.kt
        ├── LocationTest.kt
        └── AnalysisDomainServiceTest.kt
```

#### Notification Service Test Structure
```
notification-service/
├── src/main/kotlin/com/openspot/notification/
│   ├── consumer/       # Presentation Layer (Kafka)
│   ├── service/        # Application Layer
│   └── domain/         # Domain Layer
└── src/test/kotlin/com/openspot/notification/
    ├── consumer/       # Consumer Tests (Kafka Integration)
    │   └── ReportEventConsumerTest.kt
    ├── service/        # Service Tests
    │   ├── NotificationApplicationServiceTest.kt
    │   └── usecase/
    │       └── SendNotificationUseCaseTest.kt
    └── domain/         # Domain Unit Tests
        ├── NotificationTest.kt
        └── NotificationDomainServiceTest.kt
```

### MSA Service Independent Test Configuration

#### Service Dependencies with Common Modules
각 서비스별 `build.gradle.kts` 의존성 (공통 모듈 포함):

```kotlin
// common-core/build.gradle.kts
dependencies {
    // Spring Boot Core
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    
    // Utilities
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

// common-web/build.gradle.kts  
dependencies {
    // Common Core 의존성
    api(project(":common-core"))
    
    // Spring Web
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    
    // Security (공통 설정용)
    api("org.springframework.boot:spring-boot-starter-security")
    
    // Documentation
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

// config-service/build.gradle.kts
dependencies {
    implementation(project(":common-core"))
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

// gateway-service/build.gradle.kts  
dependencies {
    implementation(project(":common-core"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

// auth-service/build.gradle.kts
dependencies {
    // 공통 모듈 의존성
    implementation(project(":common-core"))
    implementation(project(":common-web"))
    
    // Auth specific dependencies
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.postgresql:postgresql")
    
    // 테스트 의존성
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.security:spring-security-test")
}

// analysis-service/build.gradle.kts  
dependencies {
    // 공통 모듈 의존성
    implementation(project(":common-core"))
    implementation(project(":common-web"))
    
    // Analysis specific dependencies
    implementation("org.hibernate:hibernate-spatial:6.2.7.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql")
    
    // PostGIS, Redis 테스트 추가
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:redis")
    testImplementation("com.redis.testcontainers:testcontainers-redis-junit")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

// notification-service/build.gradle.kts
dependencies {
    // 공통 모듈 의존성
    implementation(project(":common-core"))
    implementation(project(":common-web"))
    
    // Notification specific dependencies
    implementation("org.springframework.kafka:spring-kafka")
    
    // Kafka 테스트 추가
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")
}
```

#### Circuit Breaker Configuration

```yaml
# auth-service/src/main/resources/application.yml
resilience4j:
  circuitbreaker:
    instances:
      google-oauth:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
  
  retry:
    instances:
      google-oauth:
        max-attempts: 3
        wait-duration: 1s
        
  timelimiter:
    instances:
      google-oauth:
        timeout-duration: 5s

# gateway-service/src/main/resources/application.yml        
resilience4j:
  circuitbreaker:
    instances:
      auth-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
      analysis-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

#### Independent Test Execution
- **개별 서비스 테스트**: `./gradlew :auth-service:test`
- **특정 레이어 테스트**: `./gradlew :auth-service:test --tests="**/domain/**"`
- **전체 서비스 테스트**: `./gradlew test`

#### Service-Specific Test Profiles
```yaml
# auth-service/src/test/resources/application-test.yml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:tc:postgresql:15:///testdb
  jpa:
    hibernate:
      ddl-auto: create-drop

# analysis-service/src/test/resources/application-test.yml  
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:tc:postgresql:15:///analysisdb
  redis:
    url: redis://tc-redis:6379
  kafka:
    bootstrap-servers: tc-kafka:9092
```

### TDD Red-Green-Refactor Cycle (Service별 독립 실행)
1. **Red**: 실패하는 테스트 작성 (각 서비스별로)
2. **Green**: 테스트를 통과하는 최소한의 코드 작성  
3. **Refactor**: 코드 개선 (테스트 통과 유지)
4. **Repeat**: 다음 기능에 대해 반복 (서비스 간 의존성 없이)

## Development Commands

### Service-Specific Development Commands
각 MSA 서비스별로 독립적인 개발 및 테스트 명령어:

```bash
# Individual Service Test Commands
./gradlew :auth-service:test                    # Auth service 전체 테스트
./gradlew :auth-service:test --tests="**/domain/**"      # Domain layer 테스트만
./gradlew :auth-service:test --tests="**/service/**"     # Application layer 테스트만  
./gradlew :auth-service:test --tests="**/repository/**"  # Infrastructure layer 테스트만
./gradlew :auth-service:test --tests="**/controller/**"  # Presentation layer 테스트만

./gradlew :analysis-service:test                # Analysis service 전체 테스트
./gradlew :analysis-service:test --tests="**/domain/**"
./gradlew :analysis-service:test --tests="**/service/**"
./gradlew :analysis-service:test --tests="**/repository/**"
./gradlew :analysis-service:test --tests="**/controller/**"

./gradlew :notification-service:test           # Notification service 전체 테스트
./gradlew :notification-service:test --tests="**/consumer/**"    # Consumer 테스트
```

### Multi-Service Commands
```bash
./gradlew test              # 모든 서비스 테스트 실행
./gradlew build             # 모든 서비스 빌드
./gradlew :auth-service:bootRun     # Auth service 단독 실행
./gradlew :analysis-service:bootRun # Analysis service 단독 실행
```

### TDD Cycle Commands per Layer
```bash
# Red-Green-Refactor cycle for specific layer
./gradlew :auth-service:test --tests="UserTest" --continuous    # 지속적 테스트 실행
./gradlew :auth-service:test --tests="**/domain/**" --rerun-tasks  # 강제 재실행
```

## Current Status
This project is currently in the planning phase with architectural documentation completed. No code implementation has been started yet. 

### Key Implementation Guidelines
- **Clean Architecture**: 각 서비스는 Domain → Application → Infrastructure → Presentation 레이어 구조
- **MSA Independent Testing**: 각 서비스별로 완전히 독립적인 테스트 환경
- **TDD Methodology**: 레이어별 테스트 우선 개발 (Domain first → Application → Infrastructure → Presentation)
- **API Standards**: 모든 REST API는 `/api/v1` prefix 사용, 페이징 기본 지원
- **Service Independence**: 서비스 간 의존성 없이 독립적 개발/테스트/배포 가능