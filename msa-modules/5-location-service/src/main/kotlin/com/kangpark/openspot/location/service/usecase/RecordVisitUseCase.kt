package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.domain.entity.LocationVisit
import com.kangpark.openspot.location.domain.valueobject.VisitPurpose
import com.kangpark.openspot.location.domain.repository.LocationVisitRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 방문 기록 Use Case
 */
@Component
@Transactional
class RecordVisitUseCase(
    private val locationVisitRepository: LocationVisitRepository,
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(RecordVisitUseCase::class.java)

    /**
     * 장소 방문을 기록합니다.
     */
    fun execute(
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
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")
        require(location.isActive) { "Cannot visit inactive location" }

        // 방문 기록 생성
        val visit = LocationVisit.create(
            locationId = locationId,
            userId = userId,
            visitedAt = visitedAt,
            memo = memo,
            visitDurationMinutes = visitDurationMinutes,
            companionCount = companionCount,
            visitPurpose = visitPurpose
        )

        val savedVisit = locationVisitRepository.save(visit)

        // 방문 기록 이벤트 발행
        locationEventPublisher.publishLocationVisited(savedVisit)

        logger.info("Visit recorded successfully: id={}", savedVisit.id)
        return savedVisit
    }
}