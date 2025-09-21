package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.valueobject.CategoryType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Location Domain Repository Interface
 * 순수 도메인 레이어의 Repository 인터페이스
 */
interface LocationRepository {

    /**
     * 장소 저장
     */
    fun save(location: Location): Location

    /**
     * 장소 조회
     */
    fun findById(id: UUID): Location?

    /**
     * 장소 삭제
     */
    fun deleteById(id: UUID)

    /**
     * 특정 반경 내의 장소 검색
     */
    fun findByCoordinatesWithinRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        category: CategoryType? = null,
        pageable: Pageable
    ): Page<Location>

    /**
     * 카테고리별 장소 검색
     */
    fun findByCategory(category: CategoryType, pageable: Pageable): Page<Location>

    /**
     * 키워드로 장소 검색
     */
    fun findByKeyword(keyword: String, pageable: Pageable): Page<Location>

    /**
     * 인기 장소 목록 (조회수 기준)
     */
    fun findPopularLocations(pageable: Pageable): Page<Location>

    /**
     * 최고 평점 장소 목록
     */
    fun findTopRatedLocations(pageable: Pageable): Page<Location>

    /**
     * 최근 등록 장소 목록
     */
    fun findRecentLocations(pageable: Pageable): Page<Location>

    /**
     * 사용자가 생성한 장소 목록
     */
    fun findByCreatedBy(createdBy: UUID, pageable: Pageable): Page<Location>

    /**
     * 카테고리별 장소 개수
     */
    fun countByCategory(): Map<CategoryType, Long>

    /**
     * 장소 존재 확인
     */
    fun existsById(id: UUID): Boolean
}