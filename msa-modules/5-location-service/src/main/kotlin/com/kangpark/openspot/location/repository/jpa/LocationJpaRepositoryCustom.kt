package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * LocationJpaRepository Custom Interface
 *
 * Native Query + 동적 조건 처리
 * - GIS 공간 함수 (ST_DWithin, ST_Contains)
 * - 동적 WHERE 조건 (categoryId, groupId optional)
 * - 키워드 검색, 집계 쿼리
 */
interface LocationJpaRepositoryCustom {

    /**
     * 반경 내 장소 검색 (동적 조건: categoryId, groupId)
     * Native Query (PostGIS ST_DWithin 사용)
     */
    fun findByCoordinatesWithinRadiusDynamic(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        categoryId: UUID? = null,
        groupId: UUID? = null,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 지도 영역(bounds) 내 장소 검색 (동적 조건: categoryId, groupId)
     * Native Query (PostGIS ST_Contains 사용)
     */
    fun findByCoordinatesWithinBoundsDynamic(
        userId: UUID,
        northEastLat: Double,
        northEastLon: Double,
        southWestLat: Double,
        southWestLon: Double,
        categoryId: UUID? = null,
        groupId: UUID? = null,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 키워드로 장소 검색 (이름, 설명, 주소)
     * QueryDSL 사용
     */
    fun findByUserIdAndKeyword(
        userId: UUID,
        keyword: String,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 카테고리별 장소 개수
     * QueryDSL 사용
     */
    fun countByUserIdGroupByCategory(userId: UUID): Map<UUID, Long>
}
