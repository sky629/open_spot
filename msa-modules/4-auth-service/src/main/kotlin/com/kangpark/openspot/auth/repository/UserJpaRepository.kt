package com.kangpark.openspot.auth.repository

import com.kangpark.openspot.auth.repository.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * User JPA Repository
 * Spring Data JPA를 활용한 데이터베이스 접근
 */
@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, UUID> {
    
    /**
     * Social ID로 사용자 조회
     */
    fun findBySocialId(socialId: String): UserJpaEntity?
    
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): UserJpaEntity?
    
    /**
     * social ID 존재 여부 확인
     */
    fun existsBySocialId(socialId: String): Boolean
    
    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 활성 사용자 수 조회 (관리용)
     */
    @Query("SELECT COUNT(u) FROM UserJpaEntity u")
    fun countActiveUsers(): Long
}