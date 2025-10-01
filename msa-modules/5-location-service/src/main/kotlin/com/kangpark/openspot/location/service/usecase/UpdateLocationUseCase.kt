package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.vo.Coordinates
import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.CategoryRepository
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import com.kangpark.openspot.location.service.usecase.command.UpdateLocationCommand
import com.kangpark.openspot.location.service.usecase.command.UpdateLocationEvaluationCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 수정 Use Case (개인 기록)
 */
@Component
@Transactional
class UpdateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(UpdateLocationUseCase::class.java)

    /**
     * 개인 장소 기본 정보를 수정합니다.
     */
    fun execute(locationId: UUID, userId: UUID, command: UpdateLocationCommand): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update inactive location" }
        require(location.isOwnedBy(userId)) { "Only location owner can update location" }

        // 카테고리 존재 확인
        val category = categoryRepository.findById(command.categoryId)
            ?: throw IllegalArgumentException("Category not found: ${command.categoryId}")

        if (!category.isActive) {
            throw IllegalArgumentException("Category is not active: ${command.categoryId}")
        }

        // 기본 정보 업데이트
        val updatedLocation = location.updateBasicInfo(
            name = command.name,
            description = command.description,
            address = command.address,
            categoryId = command.categoryId,
            iconUrl = command.iconUrl
        )

        val savedLocation = locationRepository.save(updatedLocation)

        // 장소 수정 이벤트 발행
        locationEventPublisher.publishLocationUpdated(savedLocation)

        logger.info("Location updated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }

    /**
     * 개인 평가 정보를 수정합니다.
     */
    fun updatePersonalEvaluation(
        locationId: UUID,
        userId: UUID,
        command: UpdateLocationEvaluationCommand
    ): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update inactive location" }
        require(location.isOwnedBy(userId)) { "Only location owner can update evaluation" }

        val updatedLocation = location.updatePersonalEvaluation(
            personalRating = command.personalRating,
            personalReview = command.personalReview,
            tags = command.tags
        )

        val savedLocation = locationRepository.save(updatedLocation)

        logger.info("Location evaluation updated: id={}, rating={}", locationId, command.personalRating)
        return savedLocation
    }

    /**
     * 장소 그룹을 변경합니다.
     */
    fun changeGroup(
        locationId: UUID,
        userId: UUID,
        groupId: UUID?
    ): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update inactive location" }
        require(location.isOwnedBy(userId)) { "Only location owner can change group" }

        val updatedLocation = location.changeGroup(groupId)
        val savedLocation = locationRepository.save(updatedLocation)

        logger.info("Location group changed: id={}, newGroupId={}", locationId, groupId)
        return savedLocation
    }

    /**
     * 장소를 비활성화합니다 (삭제).
     */
    fun deactivate(locationId: UUID, userId: UUID): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Location is already inactive" }
        require(location.isOwnedBy(userId)) { "Only location owner can deactivate location" }

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
        require(location.isOwnedBy(userId)) { "Only location owner can update coordinates" }

        val updatedLocation = location.updateCoordinates(coordinates)
        val savedLocation = locationRepository.save(updatedLocation)

        locationEventPublisher.publishLocationUpdated(savedLocation)

        logger.info("Location coordinates updated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }
}