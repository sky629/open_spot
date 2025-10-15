package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.CategoryRepository
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import com.kangpark.openspot.location.service.usecase.command.UpdateLocationCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 장소 수정 Use Case (통합 업데이트)
 * 제공된 필드만 업데이트합니다.
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
     * 장소 정보를 통합 수정합니다 (부분 업데이트).
     * 제공된 필드만 업데이트됩니다.
     */
    fun execute(locationId: UUID, userId: UUID, command: UpdateLocationCommand): Location {
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")

        require(location.isActive) { "Cannot update inactive location" }
        require(location.isOwnedBy(userId)) { "Only location owner can update location" }

        var updatedLocation = location

        // 기본 정보 업데이트 (최소 하나의 필드라도 제공되면)
        if (command.name != null || command.description != null ||
            command.address != null || command.categoryId != null || command.iconUrl != null) {

            // categoryId가 제공되면 카테고리 존재 확인
            if (command.categoryId != null) {
                val category = categoryRepository.findById(command.categoryId)
                    ?: throw IllegalArgumentException("Category not found: ${command.categoryId}")

                if (!category.isActive) {
                    throw IllegalArgumentException("Category is not active: ${command.categoryId}")
                }
            }

            updatedLocation = updatedLocation.updateBasicInfo(
                name = command.name ?: updatedLocation.name,
                description = command.description ?: updatedLocation.description,
                address = command.address ?: updatedLocation.address,
                categoryId = command.categoryId ?: updatedLocation.categoryId,
                iconUrl = command.iconUrl ?: updatedLocation.iconUrl
            )
        }

        // 평가 정보 업데이트
        if (command.rating != null || command.review != null || command.tags != null) {
            updatedLocation = updatedLocation.updateEvaluation(
                rating = command.rating ?: updatedLocation.rating,
                review = command.review ?: updatedLocation.review,
                tags = command.tags ?: updatedLocation.tags
            )
        }

        // 그룹 변경
        if (command.groupId != updatedLocation.groupId) {
            updatedLocation = updatedLocation.changeGroup(command.groupId)
        }

        // 좌표 업데이트
        if (command.coordinates != null) {
            updatedLocation = updatedLocation.updateCoordinates(command.coordinates)
        }

        val savedLocation = locationRepository.save(updatedLocation)

//        try {
//            // 장소 수정 이벤트 발행
//            locationEventPublisher.publishLocationUpdated(savedLocation)
//        } catch (e: Exception) {
//            logger.error("Failed to publish event. update location", e)
//        }

        logger.info("Location updated successfully: id={}, userId={}", locationId, userId)
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

//        try {
//            // 장소 비활성화 이벤트 발행
//            locationEventPublisher.publishLocationDeactivated(savedLocation)
//        } catch (e: Exception) {
//            logger.error("Failed to publish event. deactivate location", e)
//        }

        logger.info("Location deactivated successfully: id={}, userId={}", locationId, userId)
        return savedLocation
    }
}