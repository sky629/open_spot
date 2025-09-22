package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.vo.CategoryType
import com.kangpark.openspot.location.repository.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationJpaRepository : JpaRepository<LocationJpaEntity, UUID> {

    /**
     * 특정 반경 내의 장소 검색 (PostGIS 공간 쿼리)
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.is_active = true
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
    fun findByCoordinatesWithinRadius(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusMeters") radiusMeters: Double,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 카테고리별 장소 검색
     */
    fun findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(
        category: CategoryType,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 키워드로 장소 검색 (이름, 설명, 주소에서 검색)
     */
    @Query(
        value = """
        SELECT * FROM location.locations l
        WHERE l.is_active = true
        AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY l.created_at DESC
        """,
        nativeQuery = true
    )
    fun findByKeywordOrderByCreatedAtDesc(
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 인기 장소 목록 (조회수 기준)
     */
    fun findByIsActiveTrueOrderByViewCountDescCreatedAtDesc(pageable: Pageable): Page<LocationJpaEntity>

    /**
     * 최고 평점 장소 목록
     */
    fun findByIsActiveTrueAndAverageRatingIsNotNullOrderByAverageRatingDescReviewCountDescCreatedAtDesc(
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 최근 등록 장소 목록
     */
    fun findByIsActiveTrueOrderByCreatedAtDesc(pageable: Pageable): Page<LocationJpaEntity>

    /**
     * 사용자가 생성한 장소 목록
     */
    fun findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(
        createdBy: UUID,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 카테고리별 장소 개수
     */
    @Query(
        "SELECT l.category as category, COUNT(l) as count " +
        "FROM LocationJpaEntity l " +
        "WHERE l.isActive = true " +
        "GROUP BY l.category"
    )
    fun countByCategory(): List<CategoryCountProjection>

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE LocationJpaEntity l SET l.viewCount = l.viewCount + 1 WHERE l.id = :locationId")
    fun incrementViewCount(@Param("locationId") locationId: UUID)

    /**
     * 평점 정보 업데이트
     */
    @Modifying
    @Query(
        "UPDATE LocationJpaEntity l " +
        "SET l.averageRating = :averageRating, l.reviewCount = :reviewCount " +
        "WHERE l.id = :locationId"
    )
    fun updateRatingInfo(
        @Param("locationId") locationId: UUID,
        @Param("averageRating") averageRating: java.math.BigDecimal?,
        @Param("reviewCount") reviewCount: Long
    )

    interface CategoryCountProjection {
        fun getCategory(): CategoryType
        fun getCount(): Long
    }
}