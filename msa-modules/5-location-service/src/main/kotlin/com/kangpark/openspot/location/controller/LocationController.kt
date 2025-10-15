package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.PageInfo
import com.kangpark.openspot.common.web.dto.PageResponse
import com.kangpark.openspot.location.controller.dto.request.*
import com.kangpark.openspot.location.controller.dto.response.*
import com.kangpark.openspot.location.domain.vo.Coordinates
import com.kangpark.openspot.location.service.LocationApplicationService
import com.kangpark.openspot.location.service.usecase.command.*
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

@Tag(name = "Location", description = "개인 장소 기록 API")
@RestController
@RequestMapping("/api/v1/locations")
class LocationController(
    private val locationApplicationService: LocationApplicationService
) {
    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @Operation(
        summary = "장소 목록 조회 (어드민/일반 공용)",
        description = """
            다양한 조건으로 장소를 조회합니다.
            - bounds 파라미터: 지도 영역 내 검색 (우선순위 1, groupId 함께 사용 가능)
            - radius 파라미터: 반경 검색 (우선순위 2, groupId 함께 사용 가능)
            - groupId: 그룹 필터 (우선순위 3, 기본 정렬: createdAt)
            - categoryId: 카테고리 필터 (우선순위 4, 기본 정렬: createdAt)
            - keyword: 키워드 검색 (우선순위 5, 기본 정렬: 관련도)
            - sortBy: 정렬 기준 (기본 조회 시에만 적용, RATING 또는 CREATED_AT)
            - targetUserId: 조회할 사용자 (미지정 시 본인, 어드민/친구 기능용)
        """
    )
    @GetMapping
    fun getLocations(
        @Parameter(description = "검색 조건")
        @ModelAttribute request: LocationSearchRequest,
        @Parameter(description = "사용자 ID (Gateway에서 자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> {
        val userUuid = UUID.fromString(userId)
        // 조회 대상 사용자 (향후 친구 기능 확장용)
        val targetUserId = request.targetUserId ?: userUuid

        val locationPage = when {
            // 1. 지도 영역(bounds) 검색 (우선순위 최상)
            request.hasBounds() -> {
                locationApplicationService.searchLocationsByBounds(
                    userId = targetUserId,
                    northEastLat = request.northEastLat!!,
                    northEastLon = request.northEastLon!!,
                    southWestLat = request.southWestLat!!,
                    southWestLon = request.southWestLon!!,
                    categoryId = request.categoryId,
                    groupId = request.groupId,
                    pageable = pageable
                )
            }
            // 2. 반경 검색
            request.hasRadius() -> {
                locationApplicationService.searchLocationsByRadius(
                    userId = targetUserId,
                    latitude = request.latitude!!,
                    longitude = request.longitude!!,
                    radiusMeters = request.radiusMeters!!,
                    categoryId = request.categoryId,
                    groupId = request.groupId,
                    pageable = pageable
                )
            }
            // 3. 그룹 검색
            request.groupId != null -> {
                locationApplicationService.getLocationsByUserAndGroup(targetUserId, request.groupId, pageable)
            }
            // 4. 카테고리 검색
            request.categoryId != null -> {
                locationApplicationService.getLocationsByCategory(targetUserId, request.categoryId, pageable)
            }
            // 5. 키워드 검색
            !request.keyword.isNullOrBlank() -> {
                locationApplicationService.searchLocationsByKeyword(targetUserId, request.keyword, pageable)
            }
            // 6. 기본: sortBy에 따라 정렬
            else -> {
                when (request.sortBy ?: LocationSortBy.CREATED_AT) {
                    LocationSortBy.RATING -> locationApplicationService.getTopRatedLocations(targetUserId, pageable)
                    LocationSortBy.CREATED_AT -> locationApplicationService.getRecentLocations(targetUserId, pageable)
                }
            }
        }

        val responseList = locationPage.content.map { (location, category) ->
            val distance = if (request.latitude != null && request.longitude != null) {
                location.distanceTo(
                    Coordinates.of(
                        request.latitude, request.longitude
                    )
                )
            } else null

            LocationResponse.from(location, category, distance)
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

    @Operation(summary = "새 장소 생성", description = "사용자가 새로운 개인 장소를 등록합니다.")
    @PostMapping
    fun createLocation(
        @Valid @RequestBody request: CreateLocationRequest,
        @Parameter(description = "JWT 토큰 (자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        val userUuid = UUID.fromString(userId)
        return try {
            val command = CreateLocationCommand(
                name = request.name,
                description = request.description,
                address = request.address,
                categoryId = request.categoryId,
                coordinates = request.toCoordinates(),
                iconUrl = request.iconUrl,
                rating = request.rating,
                review = request.review,
                tags = request.tags,
                groupId = request.groupId
            )

            val (location, category) = locationApplicationService.createLocation(userUuid, command)

            val response = LocationResponse.from(location, category)
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

    @Operation(summary = "장소 상세 조회", description = "특정 장소의 상세 정보를 조회합니다. 본인의 장소만 조회 가능합니다.")
    @GetMapping("/{locationId}")
    fun getLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "JWT 토큰 (자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        val userUuid = UUID.fromString(userId)
        val result = locationApplicationService.getLocationById(locationId, userUuid)
            ?: return ResponseEntity.notFound().build()

        val (location, category) = result
        val response = LocationResponse.from(location, category)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "장소 정보 통합 수정",
        description = """
            장소의 모든 정보를 통합하여 수정합니다 (부분 업데이트 지원).
            제공된 필드만 업데이트되며, null인 필드는 기존 값을 유지합니다.

            수정 가능한 정보:
            - 기본 정보: name, description, address, categoryId, iconUrl
            - 평가 정보: rating, review, tags
            - 그룹: groupId
            - 좌표: coordinates (latitude, longitude는 함께 업데이트됨)
        """
    )
    @PutMapping("/{locationId}")
    fun updateLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: UpdateLocationRequest,
        @Parameter(description = "JWT 토큰 (자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<LocationResponse>> {
        val userUuid = UUID.fromString(userId)
        return try {
            val command = UpdateLocationCommand(
                name = request.name,
                description = request.description,
                address = request.address,
                categoryId = request.categoryId,
                iconUrl = request.iconUrl,
                rating = request.rating,
                review = request.review,
                tags = request.tags,
                groupId = request.groupId,
                coordinates = request.coordinates?.toCoordinates()
            )

            val (location, category) = locationApplicationService.updateLocation(locationId, userUuid, command)

            val response = LocationResponse.from(location, category)
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

    @Operation(summary = "장소 비활성화", description = "장소를 비활성화합니다 (논리적 삭제).")
    @DeleteMapping("/{locationId}")
    fun deactivateLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "JWT 토큰 (자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val userUuid = UUID.fromString(userId)
        return try {
            locationApplicationService.deactivateLocation(locationId, userUuid)

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


    @Operation(
        summary = "내 장소 목록 (일반 사용자용)",
        description = """
            내가 생성한 모든 장소 목록을 조회합니다.
            - sortBy: 정렬 기준 (RATING: 평점순, CREATED_AT: 최근 등록순, 기본값: CREATED_AT)
            - 본인의 장소만 조회 가능 (X-User-Id 자동 사용)
        """
    )
    @GetMapping("/self")
    fun getMyLocations(
        @Parameter(description = "정렬 기준", required = false)
        @RequestParam(required = false) sortBy: LocationSortBy?,
        @Parameter(description = "JWT 토큰 (자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> {
        val userUuid = UUID.fromString(userId)

        val locationPage = when (sortBy ?: LocationSortBy.CREATED_AT) {
            LocationSortBy.RATING -> locationApplicationService.getTopRatedLocations(userUuid, pageable)
            LocationSortBy.CREATED_AT -> locationApplicationService.getRecentLocations(userUuid, pageable)
        }

        val responseList = locationPage.content.map { (location, category) ->
            LocationResponse.from(location, category)
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
}