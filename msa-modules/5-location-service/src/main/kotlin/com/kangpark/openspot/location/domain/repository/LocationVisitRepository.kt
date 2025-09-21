package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.LocationVisit
import com.kangpark.openspot.location.domain.valueobject.VisitPurpose
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

/**
 * LocationVisit Domain Repository Interface
 * 순수 도메인 레이어의 Repository 인터페이스
 */
interface LocationVisitRepository {

    /**
     * 방문 기록 저장
     */
    fun save(locationVisit: LocationVisit): LocationVisit

    /**
     * 방문 기록 조회
     */
    fun findById(id: UUID): LocationVisit?

    /**
     * 방문 기록 삭제
     */
    fun deleteById(id: UUID)

    /**
     * 특정 사용자의 방문 기록 목록
     */
    fun findByUserId(userId: UUID, pageable: Pageable): Page<LocationVisit>

    /**
     * 특정 장소의 방문 기록 목록
     */
    fun findByLocationId(locationId: UUID, pageable: Pageable): Page<LocationVisit>

    /**
     * 특정 사용자의 특정 장소 방문 기록
     */
    fun findByUserIdAndLocationId(userId: UUID, locationId: UUID, pageable: Pageable): Page<LocationVisit>

    /**
     * 특정 기간 내 방문 기록
     */
    fun findByUserIdAndVisitedAtBetween(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<LocationVisit>

    /**
     * 방문 기록 개수
     */
    fun countByLocationId(locationId: UUID): Long

    /**
     * 방문 기록 존재 확인
     */
    fun existsById(id: UUID): Boolean
}