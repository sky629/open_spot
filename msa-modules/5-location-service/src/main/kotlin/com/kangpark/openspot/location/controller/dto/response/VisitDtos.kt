package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.LocationVisit
import com.kangpark.openspot.location.domain.vo.VisitPurpose
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class RecordVisitRequest(
    val visitedAt: LocalDateTime = LocalDateTime.now(),

    @field:Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    val memo: String? = null,

    @field:Min(value = 1, message = "방문 시간은 최소 1분 이상이어야 합니다")
    @field:Max(value = 1440, message = "방문 시간은 최대 24시간(1440분) 이하이어야 합니다")
    val visitDurationMinutes: Int? = null,

    @field:Min(value = 0, message = "동반자 수는 0명 이상이어야 합니다")
    @field:Max(value = 100, message = "동반자 수는 100명 이하이어야 합니다")
    val companionCount: Int? = null,

    val visitPurpose: VisitPurpose? = null
)

data class UpdateVisitRequest(
    @field:Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    val memo: String? = null,

    @field:Min(value = 1, message = "방문 시간은 최소 1분 이상이어야 합니다")
    @field:Max(value = 1440, message = "방문 시간은 최대 24시간(1440분) 이하이어야 합니다")
    val visitDurationMinutes: Int? = null,

    @field:Min(value = 0, message = "동반자 수는 0명 이상이어야 합니다")
    @field:Max(value = 100, message = "동반자 수는 100명 이하이어야 합니다")
    val companionCount: Int? = null,

    val visitPurpose: VisitPurpose? = null
)

data class FavoriteRequest(
    @field:NotNull(message = "즐겨찾기 여부는 필수입니다")
    val isFavorite: Boolean
)

// Response DTOs
data class LocationVisitResponse(
    val id: UUID,
    val locationId: UUID,
    val userId: UUID,
    val visitedAt: LocalDateTime,
    val memo: String?,
    val visitDurationMinutes: Int?,
    val companionCount: Int?,
    val visitPurpose: VisitPurpose?,
    val visitPurposeDisplayName: String?,
    val isFavorite: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(visit: LocationVisit): LocationVisitResponse {
            return LocationVisitResponse(
                id = visit.id!!,
                locationId = visit.locationId,
                userId = visit.userId,
                visitedAt = visit.visitedAt,
                memo = visit.memo,
                visitDurationMinutes = visit.visitDurationMinutes,
                companionCount = visit.companionCount,
                visitPurpose = visit.visitPurpose,
                visitPurposeDisplayName = visit.visitPurpose?.displayName,
                isFavorite = visit.isFavorite,
                createdAt = visit.createdAt!!,
                updatedAt = visit.updatedAt!!
            )
        }
    }
}

data class LocationVisitSummaryResponse(
    val id: UUID,
    val locationId: UUID,
    val visitedAt: LocalDateTime,
    val visitPurpose: VisitPurpose?,
    val visitPurposeDisplayName: String?,
    val isFavorite: Boolean
) {
    companion object {
        fun from(visit: LocationVisit): LocationVisitSummaryResponse {
            return LocationVisitSummaryResponse(
                id = visit.id!!,
                locationId = visit.locationId,
                visitedAt = visit.visitedAt,
                visitPurpose = visit.visitPurpose,
                visitPurposeDisplayName = visit.visitPurpose?.displayName,
                isFavorite = visit.isFavorite
            )
        }
    }
}

data class VisitStatsResponse(
    val locationId: UUID,
    val totalVisits: Long,
    val uniqueVisitors: Long,
    val favoriteCount: Long,
    val purposeDistribution: List<VisitPurposeStatsResponse>
)

data class VisitPurposeStatsResponse(
    val purpose: VisitPurpose,
    val purposeDisplayName: String,
    val count: Long,
    val percentage: Double
) {
    companion object {
        fun from(purposeDistribution: Map<VisitPurpose, Long>): List<VisitPurposeStatsResponse> {
            val totalVisits = purposeDistribution.values.sum()

            return purposeDistribution.map { (purpose, count) ->
                val percentage = if (totalVisits > 0) {
                    (count.toDouble() / totalVisits.toDouble()) * 100.0
                } else {
                    0.0
                }

                VisitPurposeStatsResponse(
                    purpose = purpose,
                    purposeDisplayName = purpose.displayName,
                    count = count,
                    percentage = String.format("%.1f", percentage).toDouble()
                )
            }.sortedByDescending { it.count }
        }
    }
}

data class UserVisitStatsResponse(
    val userId: UUID,
    val totalVisits: Long,
    val uniqueLocations: Long,
    val favoriteCount: Long
)

data class LocationPopularityResponse(
    val locationId: UUID,
    val visitCount: Long,
    val uniqueVisitors: Long
)

data class FavoriteToggleResponse(
    val locationId: UUID,
    val isFavorite: Boolean,
    val message: String
) {
    companion object {
        fun from(locationId: UUID, isFavorite: Boolean): FavoriteToggleResponse {
            return FavoriteToggleResponse(
                locationId = locationId,
                isFavorite = isFavorite,
                message = if (isFavorite) "즐겨찾기에 추가되었습니다" else "즐겨찾기에서 제거되었습니다"
            )
        }
    }
}