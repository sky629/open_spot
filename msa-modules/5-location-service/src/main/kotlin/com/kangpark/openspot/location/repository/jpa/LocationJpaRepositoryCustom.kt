package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * LocationJpaRepository Custom Interface
 *
 * 하이브리드 쿼리 전략:
 * 1. **KotlinJDSL** (타입 안전 JPQL DSL):
 *    - 키워드 검색: findByUserIdAndKeyword
 *    - 집계 쿼리: countByUserIdGroupByCategory
 *
 * 2. **Native Query** (PostGIS 공간 함수):
 *    - 반경 검색: findByCoordinatesWithinRadiusDynamic
 *    - 범위 검색: findByCoordinatesWithinBoundsDynamic
 *
 * KotlinJDSL 장점:
 * - ✅ 타입 안정성: 필드명 오류를 컴파일 타임에 검출
 * - ✅ IDE 지원: 자동완성과 리팩토링 (필드 변경 시 자동 반영)
 * - ✅ 가독성: DSL이 문자열 SQL보다 명확함
 * - ✅ 동적 쿼리: 조건부 로직을 안전하게 구성
 */
interface LocationJpaRepositoryCustom {

    /**
     * 반경 내 장소 검색 (동적 조건: categoryId, groupId)
     *
     * 구현: Native Query (PostGIS ST_DWithin 함수 사용)
     * - PostGIS 라이브러리가 일반 JPQL에서 지원되지 않음
     * - 지리 좌표계 변환 (4326) 필요
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
     *
     * 구현: Native Query (PostGIS ST_Contains 함수 사용)
     * - 사각형 범위 내 포함 여부 판정 (ST_MakeEnvelope 사용)
     * - PostGIS 고급 기능 필요
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
     *
     * 구현: KotlinJDSL
     * - 여러 필드에 대한 LIKE 검색 (OR 조건)
     * - 소문자 변환 후 매칭
     * - 페이지네이션 지원
     */
    fun findByUserIdAndKeyword(
        userId: UUID,
        keyword: String,
        pageable: Pageable
    ): Page<LocationJpaEntity>

    /**
     * 사용자의 카테고리별 장소 개수
     *
     * 구현: KotlinJDSL
     * - GROUP BY + COUNT 집계 쿼리
     * - categoryId를 key, 개수를 value로 하는 Map 반환
     */
    fun countByUserIdGroupByCategory(userId: UUID): Map<UUID, Long>
}
