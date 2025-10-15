package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

/**
 * LocationJpaRepository Custom Implementation
 *
 * Native Query + 동적 조건 처리
 * - GIS 공간 함수 (PostGIS ST_*)
 * - 동적 WHERE 조건 생성
 * - 키워드 검색, 집계 쿼리
 */
@Repository
class LocationJpaRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : LocationJpaRepositoryCustom {

    override fun findByCoordinatesWithinRadiusDynamic(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        categoryId: UUID?,
        groupId: UUID?,
        pageable: Pageable
    ): Page<LocationJpaEntity> {

        // 동적 WHERE 조건 생성
        val whereConditions = mutableListOf<String>()
        whereConditions.add("l.user_id = :userId")
        whereConditions.add("l.is_active = true")
        whereConditions.add("""
            ST_DWithin(
                l.coordinates::geography,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                :radiusMeters
            )
        """.trimIndent())

        if (categoryId != null) {
            whereConditions.add("l.category_id = :categoryId")
        }
        if (groupId != null) {
            whereConditions.add("l.group_id = :groupId")
        }

        val whereClause = whereConditions.joinToString(" AND ")

        // Native Query 생성
        val querySql = """
            SELECT * FROM location.locations l
            WHERE $whereClause
            ORDER BY ST_Distance(
                l.coordinates::geography,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
            )
        """.trimIndent()

        val countSql = """
            SELECT COUNT(*) FROM location.locations l
            WHERE $whereClause
        """.trimIndent()

        // Query 실행
        val query = entityManager.createNativeQuery(querySql, LocationJpaEntity::class.java)
            .setParameter("userId", userId)
            .setParameter("latitude", latitude)
            .setParameter("longitude", longitude)
            .setParameter("radiusMeters", radiusMeters)

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId)
        }
        if (groupId != null) {
            query.setParameter("groupId", groupId)
        }

        // Pagination
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<LocationJpaEntity>

        // Count Query 실행
        val countQuery = entityManager.createNativeQuery(countSql)
            .setParameter("userId", userId)
            .setParameter("latitude", latitude)
            .setParameter("longitude", longitude)
            .setParameter("radiusMeters", radiusMeters)

        if (categoryId != null) {
            countQuery.setParameter("categoryId", categoryId)
        }
        if (groupId != null) {
            countQuery.setParameter("groupId", groupId)
        }

        val total = (countQuery.singleResult as Number).toLong()

        return PageImpl(results, pageable, total)
    }

    override fun findByCoordinatesWithinBoundsDynamic(
        userId: UUID,
        northEastLat: Double,
        northEastLon: Double,
        southWestLat: Double,
        southWestLon: Double,
        categoryId: UUID?,
        groupId: UUID?,
        pageable: Pageable
    ): Page<LocationJpaEntity> {

        // 동적 WHERE 조건 생성
        val whereConditions = mutableListOf<String>()
        whereConditions.add("l.user_id = :userId")
        whereConditions.add("l.is_active = true")
        whereConditions.add("""
            ST_Contains(
                ST_MakeEnvelope(:southWestLon, :southWestLat, :northEastLon, :northEastLat, 4326),
                l.coordinates
            )
        """.trimIndent())

        if (categoryId != null) {
            whereConditions.add("l.category_id = :categoryId")
        }
        if (groupId != null) {
            whereConditions.add("l.group_id = :groupId")
        }

        val whereClause = whereConditions.joinToString(" AND ")

        // Native Query 생성
        val querySql = """
            SELECT * FROM location.locations l
            WHERE $whereClause
            ORDER BY l.created_at DESC
        """.trimIndent()

        val countSql = """
            SELECT COUNT(*) FROM location.locations l
            WHERE $whereClause
        """.trimIndent()

        // Query 실행
        val query = entityManager.createNativeQuery(querySql, LocationJpaEntity::class.java)
            .setParameter("userId", userId)
            .setParameter("northEastLat", northEastLat)
            .setParameter("northEastLon", northEastLon)
            .setParameter("southWestLat", southWestLat)
            .setParameter("southWestLon", southWestLon)

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId)
        }
        if (groupId != null) {
            query.setParameter("groupId", groupId)
        }

        // Pagination
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<LocationJpaEntity>

        // Count Query 실행
        val countQuery = entityManager.createNativeQuery(countSql)
            .setParameter("userId", userId)
            .setParameter("northEastLat", northEastLat)
            .setParameter("northEastLon", northEastLon)
            .setParameter("southWestLat", southWestLat)
            .setParameter("southWestLon", southWestLon)

        if (categoryId != null) {
            countQuery.setParameter("categoryId", categoryId)
        }
        if (groupId != null) {
            countQuery.setParameter("groupId", groupId)
        }

        val total = (countQuery.singleResult as Number).toLong()

        return PageImpl(results, pageable, total)
    }

    // ========================================
    // Native Query Methods
    // ========================================

    override fun findByUserIdAndKeyword(
        userId: UUID,
        keyword: String,
        pageable: Pageable
    ): Page<LocationJpaEntity> {
        val trimmedKeyword = keyword.trim()

        val querySql = """
            SELECT * FROM location.locations l
            WHERE l.user_id = :userId
            AND l.is_active = true
            AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY l.created_at DESC
        """.trimIndent()

        val countSql = """
            SELECT COUNT(*) FROM location.locations l
            WHERE l.user_id = :userId
            AND l.is_active = true
            AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """.trimIndent()

        val query = entityManager.createNativeQuery(querySql, LocationJpaEntity::class.java)
            .setParameter("userId", userId)
            .setParameter("keyword", trimmedKeyword)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<LocationJpaEntity>

        val countQuery = entityManager.createNativeQuery(countSql)
            .setParameter("userId", userId)
            .setParameter("keyword", trimmedKeyword)

        val total = (countQuery.singleResult as Number).toLong()

        return PageImpl(results, pageable, total)
    }

    override fun countByUserIdGroupByCategory(userId: UUID): Map<UUID, Long> {
        val querySql = """
            SELECT l.category_id, COUNT(*) as count
            FROM location.locations l
            WHERE l.user_id = :userId
            AND l.is_active = true
            GROUP BY l.category_id
        """.trimIndent()

        val query = entityManager.createNativeQuery(querySql)
            .setParameter("userId", userId)

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>

        return results.associate { row ->
            (row[0] as UUID) to (row[1] as Number).toLong()
        }
    }
}
