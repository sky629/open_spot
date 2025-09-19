package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.LocationVisit
import com.kangpark.openspot.location.domain.VisitPurpose
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface LocationVisitRepository : JpaRepository<LocationVisit, UUID> {

    /**
     * 특정 사용자의 방문 기록 (최신순)
     */
    fun findByUserIdOrderByVisitedAtDesc(
        userId: UUID,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 장소의 방문 기록 (최신순)
     */
    fun findByLocationIdOrderByVisitedAtDesc(
        locationId: UUID,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 사용자의 특정 장소 방문 기록
     */
    fun findByLocationIdAndUserIdOrderByVisitedAtDesc(
        locationId: UUID,
        userId: UUID,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 사용자의 즐겨찾기 장소들
     */
    fun findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(
        userId: UUID,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 사용자가 특정 장소를 방문했는지 확인
     */
    fun existsByLocationIdAndUserId(locationId: UUID, userId: UUID): Boolean

    /**
     * 특정 사용자의 최근 방문 (즐겨찾기 여부 무관)
     */
    @Query(
        """
        SELECT lv FROM LocationVisit lv
        WHERE lv.userId = :userId
        ORDER BY lv.visitedAt DESC
        """
    )
    fun findRecentVisitsByUser(
        @Param("userId") userId: UUID,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 장소의 총 방문 횟수
     */
    fun countByLocationId(locationId: UUID): Long

    /**
     * 특정 장소의 고유 방문자 수
     */
    @Query(
        """
        SELECT COUNT(DISTINCT lv.userId)
        FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        """
    )
    fun countUniqueVisitorsByLocationId(@Param("locationId") locationId: UUID): Long

    /**
     * 특정 사용자의 총 방문 횟수
     */
    fun countByUserId(userId: UUID): Long

    /**
     * 특정 사용자가 방문한 고유 장소 수
     */
    @Query(
        """
        SELECT COUNT(DISTINCT lv.locationId)
        FROM LocationVisit lv
        WHERE lv.userId = :userId
        """
    )
    fun countUniqueLocationsByUserId(@Param("userId") userId: UUID): Long

    /**
     * 특정 기간 내 특정 장소의 방문 기록
     */
    @Query(
        """
        SELECT lv FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        AND lv.visitedAt >= :startDate
        AND lv.visitedAt <= :endDate
        ORDER BY lv.visitedAt DESC
        """
    )
    fun findByLocationIdAndDateRange(
        @Param("locationId") locationId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 기간 내 특정 장소의 방문 횟수
     */
    @Query(
        """
        SELECT COUNT(lv)
        FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        AND lv.visitedAt >= :startDate
        AND lv.visitedAt <= :endDate
        """
    )
    fun countByLocationIdAndDateRange(
        @Param("locationId") locationId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * 특정 기간 내 특정 장소의 고유 방문자 수
     */
    @Query(
        """
        SELECT COUNT(DISTINCT lv.userId)
        FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        AND lv.visitedAt >= :startDate
        AND lv.visitedAt <= :endDate
        """
    )
    fun countUniqueVisitorsByLocationIdAndDateRange(
        @Param("locationId") locationId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * 특정 목적으로 방문한 기록
     */
    fun findByVisitPurposeOrderByVisitedAtDesc(
        visitPurpose: VisitPurpose,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 사용자의 목적별 방문 기록
     */
    fun findByUserIdAndVisitPurposeOrderByVisitedAtDesc(
        userId: UUID,
        visitPurpose: VisitPurpose,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 특정 장소의 목적별 방문 통계
     */
    @Query(
        """
        SELECT lv.visitPurpose as purpose, COUNT(lv) as count
        FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        AND lv.visitPurpose IS NOT NULL
        GROUP BY lv.visitPurpose
        ORDER BY COUNT(lv) DESC
        """
    )
    fun getVisitPurposeStatsByLocationId(@Param("locationId") locationId: UUID): List<VisitPurposeStats>

    /**
     * 특정 장소의 즐겨찾기 수
     */
    fun countByLocationIdAndIsFavoriteTrue(locationId: UUID): Long

    /**
     * 특정 사용자의 즐겨찾기 수
     */
    fun countByUserIdAndIsFavoriteTrue(userId: UUID): Long

    /**
     * 인기 장소 (방문 횟수 기준)
     */
    @Query(
        """
        SELECT lv.locationId as locationId, COUNT(lv) as visitCount
        FROM LocationVisit lv
        GROUP BY lv.locationId
        ORDER BY COUNT(lv) DESC
        """
    )
    fun findPopularLocationsByVisitCount(pageable: Pageable): List<LocationPopularity>

    /**
     * 인기 장소 (고유 방문자 수 기준)
     */
    @Query(
        """
        SELECT lv.locationId as locationId, COUNT(DISTINCT lv.userId) as uniqueVisitors
        FROM LocationVisit lv
        GROUP BY lv.locationId
        ORDER BY COUNT(DISTINCT lv.userId) DESC
        """
    )
    fun findPopularLocationsByUniqueVisitors(pageable: Pageable): List<LocationPopularity>

    /**
     * 특정 사용자의 가장 최근 방문
     */
    @Query(
        """
        SELECT lv FROM LocationVisit lv
        WHERE lv.userId = :userId
        ORDER BY lv.visitedAt DESC
        LIMIT 1
        """
    )
    fun findLatestVisitByUser(@Param("userId") userId: UUID): LocationVisit?

    /**
     * 특정 장소의 가장 최근 방문
     */
    @Query(
        """
        SELECT lv FROM LocationVisit lv
        WHERE lv.locationId = :locationId
        ORDER BY lv.visitedAt DESC
        LIMIT 1
        """
    )
    fun findLatestVisitByLocation(@Param("locationId") locationId: UUID): LocationVisit?

    interface VisitPurposeStats {
        val purpose: VisitPurpose
        val count: Long
    }

    interface LocationPopularity {
        val locationId: UUID
        val visitCount: Long?
        val uniqueVisitors: Long?
    }
}