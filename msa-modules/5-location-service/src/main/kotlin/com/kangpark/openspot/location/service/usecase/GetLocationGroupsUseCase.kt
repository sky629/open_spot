package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.LocationGroup
import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 사용자의 위치 그룹 목록 조회 Use Case
 */
@Component
@Transactional(readOnly = true)
class GetLocationGroupsUseCase(
    private val locationGroupRepository: LocationGroupRepository
) {
    private val logger = LoggerFactory.getLogger(GetLocationGroupsUseCase::class.java)

    /**
     * 사용자의 그룹 목록을 displayOrder 순서대로 조회합니다.
     */
    fun execute(userId: UUID): List<LocationGroup> {
        logger.debug("Getting location groups for user: userId={}", userId)

        return locationGroupRepository.findByUserIdOrderByDisplayOrder(userId)
    }
}