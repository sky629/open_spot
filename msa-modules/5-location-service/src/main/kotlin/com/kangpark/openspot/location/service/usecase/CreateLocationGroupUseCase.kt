package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.LocationGroup
import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 위치 그룹 생성 Use Case
 */
@Component
@Transactional
class CreateLocationGroupUseCase(
    private val locationGroupRepository: LocationGroupRepository
) {
    private val logger = LoggerFactory.getLogger(CreateLocationGroupUseCase::class.java)

    /**
     * 새로운 위치 그룹을 생성합니다.
     */
    fun execute(
        userId: UUID,
        name: String,
        description: String? = null,
        color: String? = null,
        icon: String? = null
    ): LocationGroup {
        logger.info("Creating new location group: name={}, userId={}", name, userId)

        // 중복 이름 체크
        if (locationGroupRepository.existsByUserIdAndName(userId, name)) {
            throw IllegalArgumentException("이미 존재하는 그룹명입니다: $name")
        }

        // 마지막 displayOrder 값 가져오기 (+1)
        val maxOrder = locationGroupRepository.findMaxOrderByUserId(userId) ?: -1
        val newOrder = maxOrder + 1

        val locationGroup = LocationGroup.create(
            userId = userId,
            name = name,
            description = description,
            color = color,
            icon = icon,
            displayOrder = newOrder
        )

        val savedGroup = locationGroupRepository.save(locationGroup)

        logger.info("Location group created successfully: id={}, displayOrder={}", savedGroup.id, savedGroup.displayOrder)
        return savedGroup
    }
}