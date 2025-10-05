package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.LocationGroup
import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 위치 그룹 순서 변경 Use Case
 * 드래그 앤 드롭으로 그룹 순서를 변경할 때 사용
 */
@Component
@Transactional
class ReorderLocationGroupsUseCase(
    private val locationGroupRepository: LocationGroupRepository
) {
    private val logger = LoggerFactory.getLogger(ReorderLocationGroupsUseCase::class.java)

    /**
     * 그룹들의 순서를 일괄 변경합니다.
     * @param userId 사용자 ID
     * @param groupIdOrders 그룹 ID와 새로운 displayOrder 값의 맵
     */
    fun execute(
        userId: UUID,
        groupIdOrders: Map<UUID, Int>
    ): List<LocationGroup> {
        logger.info("Reordering location groups: userId={}, count={}", userId, groupIdOrders.size)

        // 모든 그룹 조회 및 소유자 확인
        val groups = groupIdOrders.keys.mapNotNull { groupId ->
            locationGroupRepository.findById(groupId)?.also { group ->
                if (!group.isOwnedBy(userId)) {
                    throw IllegalArgumentException("그룹을 수정할 권한이 없습니다: $groupId")
                }
            }
        }

        // 새로운 displayOrder 적용
        val reorderedGroups = groups.map { group ->
            val newOrder = groupIdOrders[group.id]
                ?: throw IllegalArgumentException("그룹 ID에 대한 displayOrder 값이 없습니다: ${group.id}")
            group.changeOrder(newOrder)
        }

        // 일괄 저장
        val savedGroups = locationGroupRepository.updateOrders(reorderedGroups)

        logger.info("Location groups reordered successfully: count={}", savedGroups.size)
        return savedGroups.sortedBy { it.displayOrder }
    }
}