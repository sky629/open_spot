package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.PageInfo
import com.kangpark.openspot.common.web.dto.PageResponse
import com.kangpark.openspot.location.controller.dto.response.*
import com.kangpark.openspot.location.domain.valueobject.CategoryType
import com.kangpark.openspot.location.domain.valueobject.Coordinates
import com.kangpark.openspot.location.domain.valueobject.Rating
import com.kangpark.openspot.location.service.LocationApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Location", description = "장소 관리 API")
@RestController
@RequestMapping("/api/v1/locations")
class LocationController(
    private val locationApplicationService: LocationApplicationService
) {
    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @Operation(summary = "새 장소 생성", description = "사용자가 새로운 장소를 등록합니다.")
    @PostMapping
    fun createLocation(
        @Valid @RequestBody request: CreateLocationRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        return try {
            val location = locationApplicationService.createLocation(
                name = request.name,
                description = request.description,
                address = request.address,
                category = request.category,
                coordinates = request.toCoordinates(),
                createdBy = userId,
                phoneNumber = request.phoneNumber,
                websiteUrl = request.websiteUrl,
                businessHours = request.businessHours
            )

            val response = LocationResponse.from(location)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid location creation request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create location", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "장소 상세 조회", description = "특정 장소의 상세 정보를 조회합니다. 조회수가 증가합니다.")
    @GetMapping("/{locationId}")
    fun getLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "사용자 ID (선택사항)")
        @RequestHeader(value = "X-User-Id", required = false) userId: UUID?
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        val location = locationApplicationService.getLocationById(locationId, userId)
            ?: return ResponseEntity.notFound().build()

        val response = LocationResponse.from(location)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "장소 정보 수정", description = "장소 생성자가 장소 정보를 수정합니다.")
    @PutMapping("/{locationId}")
    fun updateLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: UpdateLocationRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        return try {
            val location = locationApplicationService.updateLocation(
                locationId = locationId,
                userId = userId,
                name = request.name,
                description = request.description,
                address = request.address,
                category = request.category,
                phoneNumber = request.phoneNumber,
                websiteUrl = request.websiteUrl,
                businessHours = request.businessHours
            )

            val response = LocationResponse.from(location)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid location update request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "장소 좌표 수정", description = "장소 생성자가 장소의 좌표를 수정합니다.")
    @PutMapping("/{locationId}/coordinates")
    fun updateLocationCoordinates(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: UpdateLocationCoordinatesRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        return try {
            val location = locationApplicationService.updateLocationCoordinates(
                locationId = locationId,
                userId = userId,
                coordinates = request.toCoordinates()
            )

            val response = LocationResponse.from(location)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid coordinates update request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update location coordinates: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "장소 비활성화", description = "장소 생성자가 장소를 비활성화합니다 (논리적 삭제).")
    @DeleteMapping("/{locationId}")
    fun deactivateLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            locationApplicationService.deactivateLocation(locationId, userId)

            val response = mapOf(
                "locationId" to locationId,
                "message" to "장소가 비활성화되었습니다"
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid location deactivation request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to deactivate location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "장소 검색", description = "다양한 조건으로 장소를 검색합니다.")
    @GetMapping("/search")
    fun searchLocations(
        @Parameter(description = "검색 조건")
        @ModelAttribute request: LocationSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationSummaryResponse>>> {
        val locationPage = when {
            // 반경 검색
            request.latitude != null && request.longitude != null && request.radiusMeters != null -> {
                locationApplicationService.searchLocationsByRadius(
                    latitude = request.latitude,
                    longitude = request.longitude,
                    radiusMeters = request.radiusMeters,
                    category = request.category,
                    pageable = pageable
                )
            }
            // 카테고리 검색
            request.category != null -> {
                locationApplicationService.getLocationsByCategory(request.category, pageable)
            }
            // 키워드 검색
            !request.keyword.isNullOrBlank() -> {
                locationApplicationService.searchLocationsByKeyword(request.keyword, pageable)
            }
            // 기본: 최근 등록순
            else -> {
                locationApplicationService.getRecentLocations(pageable)
            }
        }

        val responseList = locationPage.content.map { location ->
            val distance = if (request.latitude != null && request.longitude != null) {
                location.distanceTo(
                    Coordinates.of(
                        request.latitude, request.longitude
                    )
                )
            } else null

            LocationSummaryResponse.from(location, distance)
        }

        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = locationPage.number,
                size = locationPage.size,
                totalElements = locationPage.totalElements,
                totalPages = locationPage.totalPages,
                first = locationPage.isFirst,
                last = locationPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "인기 장소 목록", description = "조회수 기준 인기 장소 목록을 조회합니다.")
    @GetMapping("/popular")
    fun getPopularLocations(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationSummaryResponse>>> {
        val locationPage = locationApplicationService.getPopularLocations(pageable)
        val responseList = locationPage.content.map { LocationSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = locationPage.number,
                size = locationPage.size,
                totalElements = locationPage.totalElements,
                totalPages = locationPage.totalPages,
                first = locationPage.isFirst,
                last = locationPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "최고 평점 장소 목록", description = "평점 기준 최고 평점 장소 목록을 조회합니다.")
    @GetMapping("/top-rated")
    fun getTopRatedLocations(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationSummaryResponse>>> {
        val locationPage = locationApplicationService.getTopRatedLocations(pageable)
        val responseList = locationPage.content.map { LocationSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = locationPage.number,
                size = locationPage.size,
                totalElements = locationPage.totalElements,
                totalPages = locationPage.totalPages,
                first = locationPage.isFirst,
                last = locationPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "최근 등록 장소 목록", description = "최근 등록된 장소 목록을 조회합니다.")
    @GetMapping("/recent")
    fun getRecentLocations(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationSummaryResponse>>> {
        val locationPage = locationApplicationService.getRecentLocations(pageable)
        val responseList = locationPage.content.map { LocationSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = locationPage.number,
                size = locationPage.size,
                totalElements = locationPage.totalElements,
                totalPages = locationPage.totalPages,
                first = locationPage.isFirst,
                last = locationPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "사용자 생성 장소 목록", description = "특정 사용자가 생성한 장소 목록을 조회합니다.")
    @GetMapping("/my")
    fun getMyLocations(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationSummaryResponse>>> {
        val locationPage = locationApplicationService.getLocationsByUser(userId, pageable)
        val responseList = locationPage.content.map { LocationSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = locationPage.number,
                size = locationPage.size,
                totalElements = locationPage.totalElements,
                totalPages = locationPage.totalPages,
                first = locationPage.isFirst,
                last = locationPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "카테고리별 장소 개수", description = "각 카테고리별 활성 장소 개수를 조회합니다.")
    @GetMapping("/categories/stats")
    fun getCategoryStats(): ResponseEntity<ApiResponse<List<CategoryStatsResponse>>> {
        val categoryStats = locationApplicationService.getLocationCountByCategory()
        val responseList = categoryStats.map { (category, count) ->
            CategoryStatsResponse.from(category, count)
        }
        return ResponseEntity.ok(ApiResponse.success(responseList))
    }

    @Operation(summary = "장소 통계", description = "특정 장소의 통계 정보를 조회합니다.")
    @GetMapping("/{locationId}/stats")
    fun getLocationStats(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID
    ): ResponseEntity<ApiResponse<LocationStatsResponse>> {
        return try {
            val stats = locationApplicationService.getLocationStats(locationId)
            val response = LocationStatsResponse(
                locationId = stats.locationId,
                viewCount = stats.viewCount,
                visitCount = stats.visitCount,
                uniqueVisitorCount = stats.uniqueVisitorCount,
                reviewCount = stats.reviewCount,
                averageRating = stats.averageRating?.score,
                favoriteCount = stats.favoriteCount
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Location not found for stats: {}", locationId)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get location stats: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationStatsResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }
}