package com.kangpark.openspot.location.service

import com.kangpark.openspot.location.domain.LocationVisit
import com.kangpark.openspot.location.domain.VisitPurpose
import com.kangpark.openspot.location.repository.LocationRepository
import com.kangpark.openspot.location.repository.LocationVisitRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class LocationVisitService(
    private val locationVisitRepository: LocationVisitRepository,
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(LocationVisitService::class.java)

    /**
     * 장소 방문 기록 생성
     */
    @Transactional
    fun recordVisit(
        locationId: UUID,
        userId: UUID,
        visitedAt: LocalDateTime = LocalDateTime.now(),
        memo: String? = null,
        visitDurationMinutes: Int? = null,
        companionCount: Int? = null,
        visitPurpose: VisitPurpose? = null
    ): LocationVisit {
        logger.info("Recording visit: locationId={}, userId={}, visitedAt={}", locationId, userId, visitedAt)

        // 장소 존재 및 활성 상태 확인
        val location = locationRepository.findById(locationId).orElseThrow {
            IllegalArgumentException("Location not found: $locationId")
        }
        require(location.isActive) { "Cannot visit inactive location" }

        // 방문 기록 생성
        val visit = LocationVisit.create(
            locationId = locationId,
            userId = userId,
            visitedAt = visitedAt,
            memo = memo
        )

        // 추가 정보 설정
        visit.updateVisitInfo(memo, visitDurationMinutes, companionCount, visitPurpose)

        val savedVisit = locationVisitRepository.save(visit)

        // 방문 이벤트 발행
        locationEventPublisher.publishLocationVisited(savedVisit)

        logger.info("Visit recorded successfully: id={}", savedVisit.id)
        return savedVisit
    }

    /**
     * 방문 기록 조회
     */
    fun getVisitById(visitId: UUID): LocationVisit? {
        return locationVisitRepository.findById(visitId).orElse(null)
    }

    /**
     * 특정 사용자의 방문 기록 목록
     */
    fun getVisitsByUser(userId: UUID, pageable: Pageable): Page<LocationVisit> {
        return locationVisitRepository.findByUserIdOrderByVisitedAtDesc(userId, pageable)
    }

    /**
     * 특정 장소의 방문 기록 목록
     */
    fun getVisitsByLocation(locationId: UUID, pageable: Pageable): Page<LocationVisit> {
        return locationVisitRepository.findByLocationIdOrderByVisitedAtDesc(locationId, pageable)
    }

    /**
     * 특정 사용자의 특정 장소 방문 기록
     */
    fun getVisitsByUserAndLocation(
        userId: UUID,
        locationId: UUID,
        pageable: Pageable
    ): Page<LocationVisit> {
        return locationVisitRepository.findByLocationIdAndUserIdOrderByVisitedAtDesc(
            locationId, userId, pageable
        )
    }

    /**
     * 특정 사용자의 즐겨찾기 장소들
     */
    fun getFavoritesByUser(userId: UUID, pageable: Pageable): Page<LocationVisit> {
        return locationVisitRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(
            userId, pageable
        )
    }

    /**
     * 방문 기록 수정
     */
    @Transactional
    fun updateVisit(
        visitId: UUID,
        userId: UUID,
        memo: String? = null,
        visitDurationMinutes: Int? = null,
        companionCount: Int? = null,
        visitPurpose: VisitPurpose? = null
    ): LocationVisit {
        val visit = locationVisitRepository.findById(visitId).orElseThrow {
            IllegalArgumentException("Visit not found: $visitId")
        }

        require(visit.userId == userId) { "User can only update their own visits" }

        visit.updateVisitInfo(memo, visitDurationMinutes, companionCount, visitPurpose)
        val updatedVisit = locationVisitRepository.save(visit)

        logger.info("Visit updated successfully: id={}", visitId)
        return updatedVisit
    }

    /**
     * 즐겨찾기 토글
     */
    @Transactional
    fun toggleFavorite(locationId: UUID, userId: UUID): LocationVisit {
        // 기존 방문 기록 찾기 또는 새로 생성
        val existingVisits = locationVisitRepository.findByLocationIdAndUserIdOrderByVisitedAtDesc(
            locationId, userId, org.springframework.data.domain.PageRequest.of(0, 1)
        )

        val visit = if (existingVisits.hasContent()) {
            // 기존 방문 기록이 있으면 가장 최근 것을 업데이트
            val latestVisit = existingVisits.content[0]
            latestVisit.toggleFavorite()
            latestVisit
        } else {
            // 방문 기록이 없으면 새로 생성하고 즐겨찾기 설정
            val newVisit = recordVisit(locationId, userId)
            newVisit.setFavorite(true)
            newVisit
        }

        val savedVisit = locationVisitRepository.save(visit)

        // 즐겨찾기 토글 이벤트 발행
        locationEventPublisher.publishFavoriteToggled(savedVisit)

        logger.info("Favorite toggled: locationId={}, userId={}, isFavorite={}",
                   locationId, userId, savedVisit.isFavorite)
        return savedVisit
    }

    /**
     * 즐겨찾기 설정
     */
    @Transactional
    fun setFavorite(locationId: UUID, userId: UUID, favorite: Boolean): LocationVisit {
        // 기존 방문 기록 찾기 또는 새로 생성
        val existingVisits = locationVisitRepository.findByLocationIdAndUserIdOrderByVisitedAtDesc(
            locationId, userId, org.springframework.data.domain.PageRequest.of(0, 1)
        )

        val visit = if (existingVisits.hasContent()) {
            val latestVisit = existingVisits.content[0]
            latestVisit.setFavorite(favorite)
            latestVisit
        } else {
            val newVisit = recordVisit(locationId, userId)
            newVisit.setFavorite(favorite)
            newVisit
        }

        val savedVisit = locationVisitRepository.save(visit)

        if (savedVisit.isFavorite != (existingVisits.content.getOrNull(0)?.isFavorite ?: false)) {
            locationEventPublisher.publishFavoriteToggled(savedVisit)
        }

        return savedVisit
    }

    /**
     * 사용자가 특정 장소를 방문했는지 확인
     */
    fun hasUserVisited(locationId: UUID, userId: UUID): Boolean {
        return locationVisitRepository.existsByLocationIdAndUserId(locationId, userId)
    }

    /**
     * 특정 목적으로 방문한 기록 조회
     */
    fun getVisitsByPurpose(visitPurpose: VisitPurpose, pageable: Pageable): Page<LocationVisit> {
        return locationVisitRepository.findByVisitPurposeOrderByVisitedAtDesc(visitPurpose, pageable)
    }

    /**
     * 특정 사용자의 목적별 방문 기록
     */
    fun getVisitsByUserAndPurpose(
        userId: UUID,
        visitPurpose: VisitPurpose,
        pageable: Pageable
    ): Page<LocationVisit> {
        return locationVisitRepository.findByUserIdAndVisitPurposeOrderByVisitedAtDesc(
            userId, visitPurpose, pageable
        )
    }

    /**
     * 특정 기간 내 방문 기록
     */
    fun getVisitsByLocationAndDateRange(
        locationId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<LocationVisit> {
        return locationVisitRepository.findByLocationIdAndDateRange(
            locationId, startDate, endDate, pageable
        )
    }

    /**
     * 방문 통계 정보
     */
    fun getVisitStats(locationId: UUID): VisitStatsInfo {
        val totalVisits = locationVisitRepository.countByLocationId(locationId)
        val uniqueVisitors = locationVisitRepository.countUniqueVisitorsByLocationId(locationId)
        val favoriteCount = locationVisitRepository.countByLocationIdAndIsFavoriteTrue(locationId)
        val purposeStats = locationVisitRepository.getVisitPurposeStatsByLocationId(locationId)

        return VisitStatsInfo(
            locationId = locationId,
            totalVisits = totalVisits,
            uniqueVisitors = uniqueVisitors,
            favoriteCount = favoriteCount,
            purposeDistribution = purposeStats.associate { it.purpose to it.count }
        )
    }

    /**
     * 사용자 방문 통계
     */
    fun getUserVisitStats(userId: UUID): UserVisitStatsInfo {
        val totalVisits = locationVisitRepository.countByUserId(userId)
        val uniqueLocations = locationVisitRepository.countUniqueLocationsByUserId(userId)
        val favoriteCount = locationVisitRepository.countByUserIdAndIsFavoriteTrue(userId)

        return UserVisitStatsInfo(
            userId = userId,
            totalVisits = totalVisits,
            uniqueLocations = uniqueLocations,
            favoriteCount = favoriteCount
        )
    }

    /**
     * 인기 장소 (방문 횟수 기준)
     */
    fun getPopularLocationsByVisitCount(pageable: Pageable): List<LocationPopularityInfo> {
        return locationVisitRepository.findPopularLocationsByVisitCount(pageable)
            .map { LocationPopularityInfo(it.locationId, it.visitCount ?: 0, it.uniqueVisitors ?: 0) }
    }

    /**
     * 인기 장소 (고유 방문자 수 기준)
     */
    fun getPopularLocationsByUniqueVisitors(pageable: Pageable): List<LocationPopularityInfo> {
        return locationVisitRepository.findPopularLocationsByUniqueVisitors(pageable)
            .map { LocationPopularityInfo(it.locationId, it.visitCount ?: 0, it.uniqueVisitors ?: 0) }
    }

    /**
     * 특정 사용자의 최근 방문
     */
    fun getRecentVisitsByUser(userId: UUID, pageable: Pageable): Page<LocationVisit> {
        return locationVisitRepository.findRecentVisitsByUser(userId, pageable)
    }

    data class VisitStatsInfo(
        val locationId: UUID,
        val totalVisits: Long,
        val uniqueVisitors: Long,
        val favoriteCount: Long,
        val purposeDistribution: Map<VisitPurpose, Long>
    )

    data class UserVisitStatsInfo(
        val userId: UUID,
        val totalVisits: Long,
        val uniqueLocations: Long,
        val favoriteCount: Long
    )

    data class LocationPopularityInfo(
        val locationId: UUID,
        val visitCount: Long,
        val uniqueVisitors: Long
    )
}