package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationGroupJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * LocationGroup JPA Repository
 */
@Repository
interface LocationGroupJpaRepository : JpaRepository<LocationGroupJpaEntity, UUID> {

    /**
     * 사용자의 그룹 목록 조회 (order 기준 오름차순 정렬)
     */
    fun findByUserIdOrderByOrderAsc(userId: UUID): List<LocationGroupJpaEntity>

    /**
     * 사용자의 특정 이름을 가진 그룹 존재 여부 확인
     */
    fun existsByUserIdAndName(userId: UUID, name: String): Boolean

    /**
     * 사용자의 마지막 order 값 조회
     */
    @Query("SELECT MAX(lg.order) FROM LocationGroupJpaEntity lg WHERE lg.userId = :userId")
    fun findMaxOrderByUserId(@Param("userId") userId: UUID): Int?
}