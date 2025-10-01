package com.kangpark.openspot.location.service

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.repository.CategoryRepository
import com.kangpark.openspot.location.domain.vo.Coordinates
import com.kangpark.openspot.location.service.usecase.*
import com.kangpark.openspot.location.service.usecase.command.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

/**
 * 장소 애플리케이션 서비스 (개인 기록)
 * Use Case들을 조합하여 애플리케이션 비즈니스 플로우를 관리
 */
@Service
class LocationApplicationService(
    private val createLocationUseCase: CreateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val searchLocationUseCase: SearchLocationUseCase,
    private val categoryRepository: CategoryRepository
) {

    // ====== Location Operations ======

    /**
     * 새로운 개인 장소 기록 생성
     */
    fun createLocation(userId: UUID, command: CreateLocationCommand): Pair<Location, Category> {
        val location = createLocationUseCase.execute(userId, command)
        val category = categoryRepository.findById(command.categoryId)
            ?: throw IllegalStateException("Category not found after location creation")
        return Pair(location, category)
    }

    /**
     * 장소 정보 조회
     */
    fun getLocationById(locationId: UUID, userId: UUID): Pair<Location, Category>? {
        val location = getLocationUseCase.execute(locationId, userId) ?: return null
        val category = categoryRepository.findById(location.categoryId) ?: return null
        return Pair(location, category)
    }

    /**
     * 장소 기본 정보 수정
     */
    fun updateLocation(
        locationId: UUID,
        userId: UUID,
        command: UpdateLocationCommand
    ): Pair<Location, Category> {
        val location = updateLocationUseCase.execute(locationId, userId, command)
        val category = categoryRepository.findById(command.categoryId)
            ?: throw IllegalStateException("Category not found after location update")
        return Pair(location, category)
    }

    /**
     * 개인 평가 정보 수정
     */
    fun updateLocationEvaluation(
        locationId: UUID,
        userId: UUID,
        command: UpdateLocationEvaluationCommand
    ): Pair<Location, Category> {
        val location = updateLocationUseCase.updatePersonalEvaluation(locationId, userId, command)
        val category = categoryRepository.findById(location.categoryId)
            ?: throw IllegalStateException("Category not found")
        return Pair(location, category)
    }

    /**
     * 장소 그룹 변경
     */
    fun changeLocationGroup(
        locationId: UUID,
        userId: UUID,
        groupId: UUID?
    ): Pair<Location, Category> {
        val location = updateLocationUseCase.changeGroup(locationId, userId, groupId)
        val category = categoryRepository.findById(location.categoryId)
            ?: throw IllegalStateException("Category not found")
        return Pair(location, category)
    }

    /**
     * 장소 좌표 수정
     */
    fun updateLocationCoordinates(
        locationId: UUID,
        userId: UUID,
        coordinates: Coordinates
    ): Pair<Location, Category> {
        val location = updateLocationUseCase.updateCoordinates(locationId, userId, coordinates)
        val category = categoryRepository.findById(location.categoryId)
            ?: throw IllegalStateException("Category not found")
        return Pair(location, category)
    }

    /**
     * 장소 비활성화
     */
    fun deactivateLocation(locationId: UUID, userId: UUID): Location {
        return updateLocationUseCase.deactivate(locationId, userId)
    }

    // ====== Search Operations ======

    /**
     * 사용자의 반경 내 장소 검색
     */
    fun searchLocationsByRadius(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        categoryId: UUID? = null,
        pageable: Pageable
    ): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.searchByRadius(
            userId, latitude, longitude, radiusMeters, categoryId, pageable
        )
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자의 지도 영역(bounds) 내 장소 검색
     */
    fun searchLocationsByBounds(
        userId: UUID,
        northEastLat: Double,
        northEastLon: Double,
        southWestLat: Double,
        southWestLon: Double,
        categoryId: UUID? = null,
        pageable: Pageable
    ): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.searchByBounds(
            userId, northEastLat, northEastLon, southWestLat, southWestLon, categoryId, pageable
        )
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자의 카테고리별 장소 검색
     */
    fun getLocationsByCategory(userId: UUID, categoryId: UUID, pageable: Pageable): Page<Pair<Location, Category>> {
        val category = categoryRepository.findById(categoryId)
            ?: throw IllegalArgumentException("Category not found: $categoryId")
        val locationPage = searchLocationUseCase.searchByCategory(userId, categoryId, pageable)
        return locationPage.map { Pair(it, category) }
    }

    /**
     * 사용자의 키워드로 장소 검색
     */
    fun searchLocationsByKeyword(userId: UUID, keyword: String, pageable: Pageable): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.searchByKeyword(userId, keyword, pageable)
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자의 최고 평점 장소 목록
     */
    fun getTopRatedLocations(userId: UUID, pageable: Pageable): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.getTopRatedLocations(userId, pageable)
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자의 최근 등록 장소 목록
     */
    fun getRecentLocations(userId: UUID, pageable: Pageable): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.getRecentLocations(userId, pageable)
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자가 생성한 장소 목록
     */
    fun getLocationsByUser(userId: UUID, pageable: Pageable): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.getLocationsByUser(userId, pageable)
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 사용자의 특정 그룹에 속한 장소 목록
     */
    fun getLocationsByUserAndGroup(userId: UUID, groupId: UUID?, pageable: Pageable): Page<Pair<Location, Category>> {
        val locationPage = searchLocationUseCase.getLocationsByUserAndGroup(userId, groupId, pageable)
        return mapLocationsWithCategories(locationPage)
    }

    /**
     * 장소 목록을 카테고리와 함께 매핑 (N+1 방지를 위한 배치 조회)
     */
    private fun mapLocationsWithCategories(locationPage: Page<Location>): Page<Pair<Location, Category>> {
        if (locationPage.isEmpty) {
            return Page.empty(locationPage.pageable)
        }

        // 1. 모든 카테고리 ID 수집
        val categoryIds = locationPage.content.map { it.categoryId }.distinct()

        // 2. 배치로 카테고리 조회 (단일 쿼리)
        val categories = categoryRepository.findAllById(categoryIds)
            .associateBy { it.id }

        // 3. 장소와 카테고리 매핑
        return locationPage.map { location ->
            val category = categories[location.categoryId]
                ?: throw IllegalStateException("Category not found: ${location.categoryId}")
            Pair(location, category)
        }
    }

}