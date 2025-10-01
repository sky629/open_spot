package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 조회 Use Case (개인 기록 서비스)
 */
@Component
@Transactional(readOnly = true)
class GetLocationUseCase(
    private val locationRepository: LocationRepository
) {
    private val logger = LoggerFactory.getLogger(GetLocationUseCase::class.java)

    /**
     * 장소 정보를 조회합니다
     */
    fun execute(locationId: UUID, userId: UUID): Location? {
        val location = locationRepository.findById(locationId)
            ?: return null

        if (!location.isActive) {
            return null
        }

        // 본인의 장소만 조회 가능
        if (location.userId != userId) {
            logger.warn("User {} attempted to access location {} owned by {}", userId, locationId, location.userId)
            return null
        }

        return location
    }
}