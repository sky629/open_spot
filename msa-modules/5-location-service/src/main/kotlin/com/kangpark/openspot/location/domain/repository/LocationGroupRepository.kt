package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.LocationGroup
import java.util.*

/**
 * 위치 그룹 도메인 리포지토리 인터페이스
 */
interface LocationGroupRepository {
    /**
     * 그룹 저장
     */
    fun save(locationGroup: LocationGroup): LocationGroup

    /**
     * ID로 그룹 조회
     */
    fun findById(id: UUID): LocationGroup?

    /**
     * 사용자의 그룹 목록 조회 (order 기준 오름차순 정렬)
     */
    fun findByUserIdOrderByOrder(userId: UUID): List<LocationGroup>

    /**
     * 사용자의 특정 이름을 가진 그룹 존재 여부 확인
     */
    fun existsByUserIdAndName(userId: UUID, name: String): Boolean

    /**
     * 사용자의 마지막 order 값 조회 (새 그룹 생성 시 사용)
     */
    fun findMaxOrderByUserId(userId: UUID): Int?

    /**
     * 그룹 삭제
     */
    fun delete(locationGroup: LocationGroup)

    /**
     * 여러 그룹의 order 일괄 업데이트
     */
    fun updateOrders(groups: List<LocationGroup>): List<LocationGroup>
}