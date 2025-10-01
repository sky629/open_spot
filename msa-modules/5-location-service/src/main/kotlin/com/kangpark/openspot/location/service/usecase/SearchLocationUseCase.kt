package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 검색 Use Case (개인 기록)
 */
@Component
@Transactional(readOnly = true)
class SearchLocationUseCase(
    private val locationRepository: LocationRepository
) {
    private val logger = LoggerFactory.getLogger(SearchLocationUseCase::class.java)

    /**
     * 사용자의 반경 내 장소 검색
     */
    fun searchByRadius(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        categoryId: UUID? = null,
        pageable: Pageable
    ): Page<Location> {
        return locationRepository.findByCoordinatesWithinRadius(
            userId, latitude, longitude, radiusMeters, categoryId, pageable
        )
    }

    /**
     * 사용자의 지도 영역(bounds) 내 장소 검색
     */
    fun searchByBounds(
        userId: UUID,
        northEastLat: Double,
        northEastLon: Double,
        southWestLat: Double,
        southWestLon: Double,
        categoryId: UUID? = null,
        pageable: Pageable
    ): Page<Location> {
        return locationRepository.findByCoordinatesWithinBounds(
            userId, northEastLat, northEastLon, southWestLat, southWestLon, categoryId, pageable
        )
    }

    /**
     * 사용자의 카테고리별 장소 검색
     */
    fun searchByCategory(userId: UUID, categoryId: UUID, pageable: Pageable): Page<Location> {
        return locationRepository.findByUserIdAndCategoryId(userId, categoryId, pageable)
    }

    /**
     * 사용자의 키워드로 장소 검색
     */
    fun searchByKeyword(userId: UUID, keyword: String, pageable: Pageable): Page<Location> {
        return if (keyword.isBlank()) {
            locationRepository.findRecentLocationsByUser(userId, pageable)
        } else {
            locationRepository.findByUserIdAndKeyword(userId, keyword.trim(), pageable)
        }
    }

    /**
     * 사용자의 최고 평점 장소 목록
     */
    fun getTopRatedLocations(userId: UUID, pageable: Pageable): Page<Location> {
        return locationRepository.findTopRatedLocationsByUser(userId, pageable)
    }

    /**
     * 사용자의 최근 추가 장소 목록
     */
    fun getRecentLocations(userId: UUID, pageable: Pageable): Page<Location> {
        return locationRepository.findRecentLocationsByUser(userId, pageable)
    }

    /**
     * 사용자가 생성한 장소 목록
     */
    fun getLocationsByUser(userId: UUID, pageable: Pageable): Page<Location> {
        return locationRepository.findByUserId(userId, pageable)
    }

    /**
     * 사용자의 특정 그룹에 속한 장소 목록
     */
    fun getLocationsByUserAndGroup(userId: UUID, groupId: UUID?, pageable: Pageable): Page<Location> {
        return locationRepository.findByUserIdAndGroupId(userId, groupId, pageable)
    }

    /**
     * 사용자의 주변 장소 간단 검색 (제한된 개수)
     */
    fun getNearbyLocations(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        categoryId: UUID? = null,
        limit: Int = 10
    ): List<Location> {
        val maxRadius = 10000.0 // 10km
        val pageable = PageRequest.of(0, limit)

        return locationRepository.findByCoordinatesWithinRadius(
            userId, latitude, longitude, maxRadius, categoryId, pageable
        ).content
    }
}