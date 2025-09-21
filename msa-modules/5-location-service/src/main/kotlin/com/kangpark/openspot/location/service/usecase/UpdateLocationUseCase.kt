package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.valueobject.CategoryType
import com.kangpark.openspot.location.domain.valueobject.Coordinates
import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 수정 Use Case
 */
@Component
@Transactional
class UpdateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(UpdateLocationUseCase::class.java)

    /**
     * 장소 정보를 수정합니다.
     */
    fun execute(
        locationId: UUID,
        userId: UUID,
        name: String,
        description: String?,
        address: String?,
        category: CategoryType,
        phoneNumber: String? = null,
        websiteUrl: String? = null,
        businessHours: String? = null
    ): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update inactive location" }
        require(location.createdBy == userId) { "Only location creator can update location" }

        // 기본 정보 업데이트
        val updatedBasicInfo = location.updateBasicInfo(name, description, address, category)

        // 연락처 정보 업데이트
        val updatedLocation = updatedBasicInfo.updateContactInfo(phoneNumber, websiteUrl, businessHours)

        val savedLocation = locationRepository.save(updatedLocation)

        // 장소 수정 이벤트 발행
        locationEventPublisher.publishLocationUpdated(savedLocation)

        logger.info("Location updated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }

    /**
     * 장소를 비활성화합니다.
     */
    fun deactivate(locationId: UUID, userId: UUID): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Location is already inactive" }
        require(location.createdBy == userId) { "Only location creator can deactivate location" }

        val deactivatedLocation = location.deactivate()
        val savedLocation = locationRepository.save(deactivatedLocation)

        // 장소 비활성화 이벤트 발행
        locationEventPublisher.publishLocationDeactivated(savedLocation)

        logger.info("Location deactivated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }

    /**
     * 장소 좌표를 수정합니다.
     */
    fun updateCoordinates(locationId: UUID, userId: UUID, coordinates: Coordinates): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update coordinates of inactive location" }
        require(location.createdBy == userId) { "Only location creator can update coordinates" }

        val updatedLocation = location.updateCoordinates(coordinates)
        val savedLocation = locationRepository.save(updatedLocation)

        locationEventPublisher.publishLocationUpdated(savedLocation)

        logger.info("Location coordinates updated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }
}