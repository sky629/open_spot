package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.PageResponse
import com.kangpark.openspot.location.controller.dto.*
import com.kangpark.openspot.location.domain.VisitPurpose
import com.kangpark.openspot.location.service.LocationVisitService
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

@Tag(name = "Visit", description = "장소 방문 기록 관리 API")
@RestController
@RequestMapping("/api/v1")
class LocationVisitController(
    private val locationVisitService: LocationVisitService
) {
    private val logger = LoggerFactory.getLogger(LocationVisitController::class.java)

    @Operation(summary = "장소 방문 기록", description = "특정 장소에 대한 방문을 기록합니다.")
    @PostMapping("/locations/{locationId}/visits")
    fun recordVisit(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: RecordVisitRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationVisitResponse>> {
        return try {
            val visit = locationVisitService.recordVisit(
                locationId = locationId,
                userId = userId,
                visitedAt = request.visitedAt,
                memo = request.memo,
                visitDurationMinutes = request.visitDurationMinutes,
                companionCount = request.companionCount,
                visitPurpose = request.visitPurpose
            )

            val response = LocationVisitResponse.from(visit)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid visit record request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<LocationVisitResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to record visit for location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationVisitResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "방문 기록 상세 조회", description = "특정 방문 기록의 상세 정보를 조회합니다.")
    @GetMapping("/visits/{visitId}")
    fun getVisit(
        @Parameter(description = "방문 기록 ID", required = true)
        @PathVariable visitId: UUID
    ): ResponseEntity<ApiResponse<LocationVisitResponse>> {
        val visit = locationVisitService.getVisitById(visitId)
            ?: return ResponseEntity.notFound().build()

        val response = LocationVisitResponse.from(visit)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "사용자의 방문 기록 목록", description = "특정 사용자의 방문 기록 목록을 조회합니다.")
    @GetMapping("/users/self/visits")
    fun getMyVisits(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationVisitSummaryResponse>>> {
        val visitPage = locationVisitService.getVisitsByUser(userId, pageable)
        val responseList = visitPage.content.map { LocationVisitSummaryResponse.from(it) }
        val pageResponse = PageResponse.from(visitPage, responseList)
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "특정 장소의 방문 기록 목록", description = "특정 장소의 방문 기록 목록을 조회합니다.")
    @GetMapping("/locations/{locationId}/visits")
    fun getVisitsByLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationVisitSummaryResponse>>> {
        val visitPage = locationVisitService.getVisitsByLocation(locationId, pageable)
        val responseList = visitPage.content.map { LocationVisitSummaryResponse.from(it) }
        val pageResponse = PageResponse.from(visitPage, responseList)
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "사용자의 즐겨찾기 장소 목록", description = "사용자가 즐겨찾기로 설정한 장소 목록을 조회합니다.")
    @GetMapping("/users/self/favorites")
    fun getMyFavorites(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationVisitSummaryResponse>>> {
        val favoritePage = locationVisitService.getFavoritesByUser(userId, pageable)
        val responseList = favoritePage.content.map { LocationVisitSummaryResponse.from(it) }
        val pageResponse = PageResponse.from(favoritePage, responseList)
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "방문 기록 수정", description = "자신의 방문 기록을 수정합니다.")
    @PutMapping("/visits/{visitId}")
    fun updateVisit(
        @Parameter(description = "방문 기록 ID", required = true)
        @PathVariable visitId: UUID,
        @Valid @RequestBody request: UpdateVisitRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationVisitResponse>> {
        return try {
            val visit = locationVisitService.updateVisit(
                visitId = visitId,
                userId = userId,
                memo = request.memo,
                visitDurationMinutes = request.visitDurationMinutes,
                companionCount = request.companionCount,
                visitPurpose = request.visitPurpose
            )

            val response = LocationVisitResponse.from(visit)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid visit update request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<LocationVisitResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update visit: {}", visitId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<LocationVisitResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "즐겨찾기 토글", description = "특정 장소의 즐겨찾기 상태를 토글합니다.")
    @PostMapping("/locations/{locationId}/favorite/toggle")
    fun toggleFavorite(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<FavoriteToggleResponse>> {
        return try {
            val visit = locationVisitService.toggleFavorite(locationId, userId)
            val response = FavoriteToggleResponse.from(locationId, visit.isFavorite)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid favorite toggle request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<FavoriteToggleResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to toggle favorite for location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<FavoriteToggleResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "즐겨찾기 설정", description = "특정 장소의 즐겨찾기 상태를 설정합니다.")
    @PostMapping("/locations/{locationId}/favorite")
    fun setFavorite(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: FavoriteRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<FavoriteToggleResponse>> {
        return try {
            val visit = locationVisitService.setFavorite(locationId, userId, request.isFavorite)
            val response = FavoriteToggleResponse.from(locationId, visit.isFavorite)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid favorite set request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<FavoriteToggleResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to set favorite for location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<FavoriteToggleResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "목적별 방문 기록", description = "특정 목적으로 방문한 기록을 조회합니다.")
    @GetMapping("/visits/purpose/{visitPurpose}")
    fun getVisitsByPurpose(
        @Parameter(description = "방문 목적", required = true)
        @PathVariable visitPurpose: VisitPurpose,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<LocationVisitSummaryResponse>>> {
        val visitPage = locationVisitService.getVisitsByPurpose(visitPurpose, pageable)
        val responseList = visitPage.content.map { LocationVisitSummaryResponse.from(it) }
        val pageResponse = PageResponse.from(visitPage, responseList)
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "장소의 방문 통계", description = "특정 장소의 방문 통계 정보를 조회합니다.")
    @GetMapping("/locations/{locationId}/visits/stats")
    fun getVisitStats(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID
    ): ResponseEntity<ApiResponse<VisitStatsResponse>> {
        val stats = locationVisitService.getVisitStats(locationId)
        val response = VisitStatsResponse(
            locationId = stats.locationId,
            totalVisits = stats.totalVisits,
            uniqueVisitors = stats.uniqueVisitors,
            favoriteCount = stats.favoriteCount,
            purposeDistribution = VisitPurposeStatsResponse.from(stats.purposeDistribution)
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "사용자 방문 통계", description = "특정 사용자의 방문 통계 정보를 조회합니다.")
    @GetMapping("/users/self/visits/stats")
    fun getMyVisitStats(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<UserVisitStatsResponse>> {
        val stats = locationVisitService.getUserVisitStats(userId)
        val response = UserVisitStatsResponse(
            userId = stats.userId,
            totalVisits = stats.totalVisits,
            uniqueLocations = stats.uniqueLocations,
            favoriteCount = stats.favoriteCount
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "인기 장소 (방문수 기준)", description = "방문 횟수 기준 인기 장소 목록을 조회합니다.")
    @GetMapping("/locations/popular/by-visits")
    fun getPopularLocationsByVisits(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<LocationPopularityResponse>>> {
        val popularLocations = locationVisitService.getPopularLocationsByVisitCount(pageable)
        val responseList = popularLocations.map {
            LocationPopularityResponse(
                locationId = it.locationId,
                visitCount = it.visitCount,
                uniqueVisitors = it.uniqueVisitors
            )
        }
        return ResponseEntity.ok(ApiResponse.success(responseList))
    }

    @Operation(summary = "인기 장소 (고유 방문자 기준)", description = "고유 방문자 수 기준 인기 장소 목록을 조회합니다.")
    @GetMapping("/locations/popular/by-visitors")
    fun getPopularLocationsByVisitors(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<List<LocationPopularityResponse>>> {
        val popularLocations = locationVisitService.getPopularLocationsByUniqueVisitors(pageable)
        val responseList = popularLocations.map {
            LocationPopularityResponse(
                locationId = it.locationId,
                visitCount = it.visitCount,
                uniqueVisitors = it.uniqueVisitors
            )
        }
        return ResponseEntity.ok(ApiResponse.success(responseList))
    }

    @Operation(summary = "방문 여부 확인", description = "사용자가 특정 장소를 방문했는지 확인합니다.")
    @GetMapping("/locations/{locationId}/visits/check")
    fun checkUserVisited(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Boolean>>> {
        val hasVisited = locationVisitService.hasUserVisited(locationId, userId)
        val response = mapOf("hasVisited" to hasVisited)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}