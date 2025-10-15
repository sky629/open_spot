package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.repository.*
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import com.kangpark.openspot.location.service.usecase.command.CreateLocationCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 생성 Use Case (개인 기록)
 */
@Component
@Transactional
class CreateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(CreateLocationUseCase::class.java)

    /**
     * 새로운 개인 장소 기록을 생성합니다.
     */
    fun execute(userId: UUID, command: CreateLocationCommand): Location {
        logger.info("Creating new location: name={}, categoryId={}, userId={}",
            command.name, command.categoryId, userId)

        // 카테고리 존재 확인
        val category = categoryRepository.findById(command.categoryId)
            ?: throw IllegalArgumentException("Category not found: ${command.categoryId}")

        if (!category.isActive) {
            throw IllegalArgumentException("Category is not active: ${command.categoryId}")
        }

        val location = Location.create(
            userId = userId,
            name = command.name,
            description = command.description,
            address = command.address,
            categoryId = command.categoryId,
            coordinates = command.coordinates,
            iconUrl = command.iconUrl,
            rating = command.rating,
            review = command.review,
            tags = command.tags,
            groupId = command.groupId
        )

        val savedLocation = locationRepository.save(location)

//        try {
//            // 장소 생성 이벤트 발행
//            locationEventPublisher.publishLocationCreated(savedLocation)
//        } catch (e: Exception) {
//            logger.error("Failed to publish event. create location", e)
//        }

        logger.info("Location created successfully: id={}", savedLocation.id)
        return savedLocation
    }
}