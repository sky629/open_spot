package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.vo.CategoryType
import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.*

/**
 * 장소 검색 Use Case
 */
@Component
class SearchLocationUseCase(
    private val locationRepository: LocationRepository
) {
    private val logger = LoggerFactory.getLogger(SearchLocationUseCase::class.java)

    /**
     * 반경 내 장소 검색
     */
    fun searchByRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        category: CategoryType? = null,
        pageable: Pageable
    ): Page<Location> {
        return locationRepository.findByCoordinatesWithinRadius(
            latitude, longitude, radiusMeters, category, pageable
        )
    }

    /**
     * 카테고리별 장소 검색
     */
    fun searchByCategory(category: CategoryType, pageable: Pageable): Page<Location> {
        return locationRepository.findByCategory(category, pageable)
    }

    /**
     * 키워드로 장소 검색
     */
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Location> {
        return if (keyword.isBlank()) {
            locationRepository.findRecentLocations(pageable)
        } else {
            locationRepository.findByKeyword(keyword.trim(), pageable)
        }
    }

    /**
     * 인기 장소 목록 (조회수 기준)
     */
    fun getPopularLocations(pageable: Pageable): Page<Location> {
        return locationRepository.findPopularLocations(pageable)
    }

    /**
     * 최고 평점 장소 목록
     */
    fun getTopRatedLocations(pageable: Pageable): Page<Location> {
        return locationRepository.findTopRatedLocations(pageable)
    }

    /**
     * 최근 추가된 장소 목록
     */
    fun getRecentLocations(pageable: Pageable): Page<Location> {
        return locationRepository.findRecentLocations(pageable)
    }

    /**
     * 사용자가 생성한 장소 목록
     */
    fun getLocationsByUser(userId: UUID, pageable: Pageable): Page<Location> {
        return locationRepository.findByCreatedBy(userId, pageable)
    }

    /**
     * 주변 장소 간단 검색 (제한된 개수)
     */
    fun getNearbyLocations(
        latitude: Double,
        longitude: Double,
        category: CategoryType? = null,
        limit: Int = 10
    ): List<Location> {
        val maxRadius = 10000.0 // 10km
        val pageable = PageRequest.of(0, limit)

        return locationRepository.findByCoordinatesWithinRadius(
            latitude, longitude, maxRadius, category, pageable
        ).content
    }
}