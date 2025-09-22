package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.vo.*
import com.kangpark.openspot.location.domain.repository.*
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 생성 Use Case
 */
@Component
@Transactional
class CreateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(CreateLocationUseCase::class.java)

    /**
     * 새로운 장소를 생성합니다.
     */
    fun execute(
        name: String,
        description: String?,
        address: String?,
        category: CategoryType,
        coordinates: Coordinates,
        createdBy: UUID,
        phoneNumber: String? = null,
        websiteUrl: String? = null,
        businessHours: String? = null
    ): Location {
        logger.info("Creating new location: name={}, category={}, createdBy={}", name, category, createdBy)

        val location = Location.create(
            name = name,
            description = description,
            address = address,
            category = category,
            coordinates = coordinates,
            createdBy = createdBy,
            phoneNumber = phoneNumber,
            websiteUrl = websiteUrl,
            businessHours = businessHours
        )

        val savedLocation = locationRepository.save(location)

        // 장소 생성 이벤트 발행
        locationEventPublisher.publishLocationCreated(savedLocation)

        logger.info("Location created successfully: id={}", savedLocation.id)
        return savedLocation
    }
}