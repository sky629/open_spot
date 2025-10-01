package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationJpaRepository : JpaRepository<LocationJpaEntity, UUID> {

    /**
     * 사용자의 반경 내 장소 검색 (PostGIS 공간 쿼리)
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.user_id = :userId
        AND l.is_active = true
        AND ST_DWithin(
            l.coordinates::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        ORDER BY ST_Distance(
            l.coordinates::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        )
        """,
        nativeQuery = true
    )
    fun findByUserIdWithinRadius(
        @Param("userId") userId: UUID,
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusMeters") radiusMeters: Double,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 카테고리별 반경 내 장소 검색
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.user_id = :userId
        AND l.category_id = :categoryId
        AND l.is_active = true
        AND ST_DWithin(
            l.coordinates::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        ORDER BY ST_Distance(
            l.coordinates::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        )
        """,
        nativeQuery = true
    )
    fun findByUserIdAndCategoryIdWithinRadius(
        @Param("userId") userId: UUID,
        @Param("categoryId") categoryId: UUID,
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusMeters") radiusMeters: Double,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 지도 영역(bounds) 내 장소 검색 (PostGIS bbox 쿼리)
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.user_id = :userId
        AND l.is_active = true
        AND ST_Contains(
            ST_MakeEnvelope(:southWestLon, :southWestLat, :northEastLon, :northEastLat, 4326),
            l.coordinates
        )
        ORDER BY l.created_at DESC
        """,
        nativeQuery = true
    )
    fun findByUserIdWithinBounds(
        @Param("userId") userId: UUID,
        @Param("northEastLat") northEastLat: Double,
        @Param("northEastLon") northEastLon: Double,
        @Param("southWestLat") southWestLat: Double,
        @Param("southWestLon") southWestLon: Double,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 카테고리별 지도 영역(bounds) 내 장소 검색
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.user_id = :userId
        AND l.category_id = :categoryId
        AND l.is_active = true
        AND ST_Contains(
            ST_MakeEnvelope(:southWestLon, :southWestLat, :northEastLon, :northEastLat, 4326),
            l.coordinates
        )
        ORDER BY l.created_at DESC
        """,
        nativeQuery = true
    )
    fun findByUserIdAndCategoryIdWithinBounds(
        @Param("userId") userId: UUID,
        @Param("categoryId") categoryId: UUID,
        @Param("northEastLat") northEastLat: Double,
        @Param("northEastLon") northEastLon: Double,
        @Param("southWestLat") southWestLat: Double,
        @Param("southWestLon") southWestLon: Double,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 카테고리별 장소 검색
     */
    fun findByUserIdAndCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(
        userId: UUID,
        categoryId: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 키워드로 장소 검색 (이름, 설명, 주소)
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.user_id = :userId
        AND l.is_active = true
        AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY l.created_at DESC
        """,
        nativeQuery = true
    )
    fun findByUserIdAndKeywordOrderByCreatedAtDesc(
        @Param("userId") userId: UUID,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 최고 평점 장소 목록
     */
    fun findByUserIdAndIsActiveTrueAndPersonalRatingIsNotNullOrderByPersonalRatingDescCreatedAtDesc(
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

    /**
     * 사용자의 카테고리별 장소 개수
     */
    @Query(
        "SELECT l.categoryId as categoryId, COUNT(l) as count " +
        "FROM LocationJpaEntity l " +
        "WHERE l.userId = :userId AND l.isActive = true " +
        "GROUP BY l.categoryId"
    )
    fun countByUserIdAndCategoryId(@Param("userId") userId: UUID): List<CategoryCountProjection>

    interface CategoryCountProjection {
        fun getCategoryId(): UUID
        fun getCount(): Long
    }
}