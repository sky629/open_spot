package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 조회 Use Case
 */
@Component
class GetLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(GetLocationUseCase::class.java)

    /**
     * 장소 정보를 조회합니다 (조회수 증가)
     */
    @Transactional
    fun execute(locationId: UUID, userId: UUID? = null): Location? {
        val location = locationRepository.findById(locationId)
            ?: return null

        if (!location.isActive) {
            return null
        }

        // 조회수 증가
        location.incrementViewCount()
        locationRepository.save(location)

        // 조회 이벤트 발행
        locationEventPublisher.publishLocationViewed(location, userId)

        return location
    }

    /**
     * 장소 정보를 조회합니다 (조회수 증가 없음)
     */
    fun executeWithoutIncrement(locationId: UUID): Location? {
        val location = locationRepository.findById(locationId)
            ?: return null

        return if (location.isActive) location else null
    }
}