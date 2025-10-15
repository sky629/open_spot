package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationJpaRepository : JpaRepository<LocationJpaEntity, UUID>, LocationJpaRepositoryCustom {
    /**
     * 사용자의 카테고리별 장소 검색
     */
    fun findByUserIdAndCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(
        userId: UUID,
        categoryId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>


    /**
     * 사용자의 최고 평점 장소 목록
     */
    fun findByUserIdAndIsActiveTrueAndRatingIsNotNullOrderByRatingDescCreatedAtDesc(
        userId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자가 생성한 장소 목록
     */
    fun findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(
        userId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 특정 그룹에 속한 장소 목록
     */
    fun findByGroupIdAndIsActiveTrue(groupId: UUID): List<LocationJpaEntity>

    /**
     * 사용자의 특정 그룹에 속한 장소 목록
     */
    fun findByUserIdAndGroupIdAndIsActiveTrueOrderByCreatedAtDesc(
        userId: UUID,
        groupId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 그룹 미지정 장소 목록
     */
    fun findByUserIdAndGroupIdIsNullAndIsActiveTrueOrderByCreatedAtDesc(
        userId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

}