package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.LocationGroup
import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 위치 그룹 수정 Use Case
 */
@Component
@Transactional
class UpdateLocationGroupUseCase(
    private val locationGroupRepository: LocationGroupRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateLocationGroupUseCase::class.java)

    /**
     * 위치 그룹 정보를 수정합니다.
     */
    fun execute(
        groupId: UUID,
        userId: UUID,
        name: String,
        description: String? = null,
        color: String? = null,
        icon: String? = null
    ): LocationGroup {
        logger.info("Updating location group: id={}, userId={}", groupId, userId)

        val locationGroup = locationGroupRepository.findById(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 그룹입니다: $groupId")

        // 소유자 확인
        if (!locationGroup.isOwnedBy(userId)) {
            throw IllegalArgumentException("그룹을 수정할 권한이 없습니다")
        }

        // 이름 변경 시 중복 체크
        if (locationGroup.name != name && locationGroupRepository.existsByUserIdAndName(userId, name)) {
            throw IllegalArgumentException("이미 존재하는 그룹명입니다: $name")
        }

        val updatedGroup = locationGroup.updateBasicInfo(
            name = name,
            description = description,
            color = color,
            icon = icon
        )

        val savedGroup = locationGroupRepository.save(updatedGroup)

        logger.info("Location group updated successfully: id={}", savedGroup.id)
        return savedGroup
    }
}