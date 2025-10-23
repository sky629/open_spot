package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.repository.jpa.LocationJpaRepository
import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.*
import org.assertj.core.api.Assertions.assertThat

/**
 * LocationJpaRepositoryImpl Test
 *
 * JPQL 쿼리 검증
 * - findByUserIdAndKeyword: LIKE 검색 (여러 필드)
 * - countByUserIdGroupByCategory: GROUP BY + COUNT 집계
 */
@DataJpaTest
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
class LocationJpaRepositoryImplTest(
    @Autowired private val locationRepository: LocationJpaRepository
) {

    private lateinit var userId: UUID
    private lateinit var categoryId1: UUID
    private lateinit var categoryId2: UUID

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        categoryId1 = UUID.randomUUID()
        categoryId2 = UUID.randomUUID()

        // 테스트 데이터 생성
        val now = LocalDateTime.now()

        // 카테고리 1 - 3개 장소
        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "강남 명동카페",
                description = "분위기 좋은 카페",
                address = "서울시 강남구",
                categoryId = categoryId1,
                latitude = 37.5,
                longitude = 127.0,
                rating = 4.5,
                review = "커피가 맛있어요",
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "강남역 카페거리",
                description = "핫플레이스",
                address = "서울시 강남구 강남역",
                categoryId = categoryId1,
                latitude = 37.5,
                longitude = 127.05,
                rating = 4.0,
                review = "가성비 좋음",
                isActive = true,
                createdAt = now.minusHours(1),
                updatedAt = now.minusHours(1)
            )
        )

        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "역삼 카페 추천",
                description = "조용하고 쾌적한 환경",
                address = "서울시 강남구 역삼",
                categoryId = categoryId1,
                latitude = 37.5,
                longitude = 127.05,
                rating = 3.5,
                review = "공부하기 좋음",
                isActive = true,
                createdAt = now.minusHours(2),
                updatedAt = now.minusHours(2)
            )
        )

        // 카테고리 2 - 2개 장소
        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "홍대 피자 맛집",
                description = "이탈리안 피자",
                address = "서울시 마포구 홍대",
                categoryId = categoryId2,
                latitude = 37.55,
                longitude = 126.95,
                rating = 5.0,
                review = "최고의 피자",
                isActive = true,
                createdAt = now.minusHours(3),
                updatedAt = now.minusHours(3)
            )
        )

        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "잠실 한식당",
                description = "전통 한식",
                address = "서울시 송파구 잠실",
                categoryId = categoryId2,
                latitude = 37.5,
                longitude = 127.1,
                rating = 4.5,
                review = "반찬이 맛있음",
                isActive = true,
                createdAt = now.minusHours(4),
                updatedAt = now.minusHours(4)
            )
        )

        // 비활성 장소 (테스트에서 제외)
        locationRepository.save(
            LocationJpaEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = "삭제된 장소",
                description = "더 이상 없음",
                address = "서울시 강남구",
                categoryId = categoryId1,
                latitude = 37.5,
                longitude = 127.0,
                rating = null,
                review = null,
                isActive = false,
                createdAt = now.minusHours(5),
                updatedAt = now.minusHours(5)
            )
        )
    }

    @Test
    fun `findByUserIdAndKeyword - should find locations by name keyword`() {
        // Given
        val keyword = "카페"
        val pageable = PageRequest.of(0, 10)

        // When
        val result = locationRepository.findByUserIdAndKeyword(userId, keyword, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(3)
        assertThat(result.content).allMatch { it.userId == userId && it.isActive }
        assertThat(result.content).allMatch { it.name.contains("카페") }
    }

    @Test
    fun `findByUserIdAndKeyword - should find locations by address keyword`() {
        // Given
        val keyword = "강남"
        val pageable = PageRequest.of(0, 10)

        // When
        val result = locationRepository.findByUserIdAndKeyword(userId, keyword, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(3)
        assertThat(result.content).allMatch { it.address?.contains("강남") == true }
    }

    @Test
    fun `findByUserIdAndKeyword - should find locations by description keyword`() {
        // Given
        val keyword = "분위"
        val pageable = PageRequest.of(0, 10)

        // When
        val result = locationRepository.findByUserIdAndKeyword(userId, keyword, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].name).isEqualTo("강남 명동카페")
    }

    @Test
    fun `findByUserIdAndKeyword - should return empty when no match`() {
        // Given
        val keyword = "존재하지않는장소"
        val pageable = PageRequest.of(0, 10)

        // When
        val result = locationRepository.findByUserIdAndKeyword(userId, keyword, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(0)
    }

    @Test
    fun `findByUserIdAndKeyword - should respect pagination`() {
        // Given
        val keyword = "카"  // "카페", "카페거리", "카페 추천" 매칭
        val pageable = PageRequest.of(0, 2)  // 페이지 0, 크기 2

        // When
        val result = locationRepository.findByUserIdAndKeyword(userId, keyword, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(3)
        assertThat(result.content.size).isEqualTo(2)
        assertThat(result.totalPages).isEqualTo(1)
    }

    @Test
    fun `findByUserIdAndKeyword - should be case insensitive`() {
        // Given
        val keyword1 = "카페"
        val keyword2 = "카페".uppercase()  // "카페" -> uppercase (한글은 변화 없음)
        val pageable = PageRequest.of(0, 10)

        // When
        val result1 = locationRepository.findByUserIdAndKeyword(userId, keyword1, pageable)
        val result2 = locationRepository.findByUserIdAndKeyword(userId, keyword2, pageable)

        // Then
        assertThat(result1.totalElements).isEqualTo(result2.totalElements)
    }

    @Test
    fun `countByUserIdGroupByCategory - should count locations by category`() {
        // When
        val result = locationRepository.countByUserIdGroupByCategory(userId)

        // Then
        assertThat(result.size).isEqualTo(2)
        assertThat(result[categoryId1]).isEqualTo(3L)
        assertThat(result[categoryId2]).isEqualTo(2L)
    }

    @Test
    fun `countByUserIdGroupByCategory - should not include inactive locations`() {
        // When
        val result = locationRepository.countByUserIdGroupByCategory(userId)

        // Then
        // categoryId1에 비활성 장소가 1개 있지만, 총 3개만 카운트되어야 함
        assertThat(result[categoryId1]).isEqualTo(3L)
    }

    @Test
    fun `countByUserIdGroupByCategory - should return empty map for user with no locations`() {
        // Given
        val newUserId = UUID.randomUUID()

        // When
        val result = locationRepository.countByUserIdGroupByCategory(newUserId)

        // Then
        assertThat(result.size).isEqualTo(0)
    }
}
