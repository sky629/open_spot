package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import com.kangpark.openspot.location.domain.repository.LocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 위치 그룹 삭제 Use Case
 */
@Component
@Transactional
class DeleteLocationGroupUseCase(
    private val locationGroupRepository: LocationGroupRepository,
    private val locationRepository: LocationRepository
) {
    private val logger = LoggerFactory.getLogger(DeleteLocationGroupUseCase::class.java)

    /**
     * 위치 그룹을 삭제합니다.
     * 그룹에 속한 장소들은 그룹에서 제외됩니다 (groupId = null)
     */
    fun execute(
        groupId: UUID,
        userId: UUID
    ) {
        logger.info("Deleting location group: id={}, userId={}", groupId, userId)

        val locationGroup = locationGroupRepository.findById(groupId)
            ?: throw IllegalArgumentException("존재하지 않는 그룹입니다: $groupId")

        // 소유자 확인
        if (!locationGroup.isOwnedBy(userId)) {
            throw IllegalArgumentException("그룹을 삭제할 권한이 없습니다")
        }

        // 그룹에 속한 장소들을 그룹에서 제외
        val locations = locationRepository.findByGroupId(groupId)
        locations.forEach { location ->
            val updatedLocation = location.changeGroup(null)
            locationRepository.save(updatedLocation)
        }

        // 그룹 삭제
        locationGroupRepository.delete(locationGroup)

        logger.info("Location group deleted successfully: id={}, affected locations={}", groupId, locations.size)
    }
}