package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.valueobject.CategoryType
import com.kangpark.openspot.location.domain.valueobject.Rating
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.service.usecase.GetLocationUseCase
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import com.kangpark.openspot.location.domain.entity.Location
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * 장소 통계 Use Case
 */
@Component
class GetLocationStatsUseCase(
    private val locationRepository: LocationRepository,
    private val getLocationUseCase: GetLocationUseCase
) {
    private val logger = LoggerFactory.getLogger(GetLocationStatsUseCase::class.java)

    /**
     * 장소 통계 정보
     */
    fun getLocationStats(locationId: UUID): LocationStatsInfo {
        val location = getLocationUseCase.executeWithoutIncrement(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        return LocationStatsInfo(
            locationId = locationId,
            name = location.name,
            viewCount = location.viewCount,
            visitCount = 0L, // TODO: 실제 방문 수 조회 구현 필요
            uniqueVisitorCount = 0L, // TODO: 실제 고유 방문자 수 조회 구현 필요
            reviewCount = location.reviewCount,
            averageRating = location.getAverageRatingAsRating(),
            favoriteCount = 0L, // TODO: 실제 즐겨찾기 수 조회 구현 필요
            createdAt = location.createdAt!!
        )
    }

    /**
     * 카테고리별 장소 개수
     */
    fun getLocationCountByCategory(): Map<CategoryType, Long> {
        return locationRepository.countByCategory()
    }

    /**
     * 반경 내 장소 개수
     */
    fun getLocationCountInRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): Long {
        // TODO: 반경 내 장소 개수 쿼리 구현 필요
        return 0L
    }

    /**
     * 사용자의 즐겨찾기 장소 목록
     * TODO: 즐겨찾기 기능 구현 필요
     */
    fun getFavoriteLocationsByUser(userId: UUID, pageable: Pageable): Page<Location> =
        PageImpl(emptyList<Location>(), pageable, 0)

    data class LocationStatsInfo(
        val locationId: UUID,
        val name: String,
        val viewCount: Long,
        val visitCount: Long = 0L,
        val uniqueVisitorCount: Long = 0L,
        val reviewCount: Long,
        val averageRating: Rating?,
        val favoriteCount: Long = 0L,
        val createdAt: LocalDateTime
    )
}