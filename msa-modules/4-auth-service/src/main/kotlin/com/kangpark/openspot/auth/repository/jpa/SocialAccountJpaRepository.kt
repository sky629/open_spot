package com.kangpark.openspot.auth.repository.jpa

import com.kangpark.openspot.auth.domain.vo.SocialProvider
import com.kangpark.openspot.auth.repository.jpa.entity.SocialAccountJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SocialAccount JPA Repository
 * Spring Data JPA를 활용한 데이터베이스 접근
 */
@Repository
interface SocialAccountJpaRepository : JpaRepository<SocialAccountJpaEntity, UUID> {
    
    /**
     * 사용자 ID로 모든 소셜 계정 조회
     */
    fun findByUserId(userId: UUID): List<SocialAccountJpaEntity>
    
    /**
     * 사용자 ID와 제공자로 소셜 계정 조회
     */
    fun findByUserIdAndProvider(userId: UUID, provider: SocialProvider): SocialAccountJpaEntity?
    
    /**
     * 제공자와 제공자 ID로 소셜 계정 조회
     */
    fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccountJpaEntity?
    
    /**
     * 제공자와 제공자 ID로 소셜 계정 존재 여부 확인
     */
    fun existsByProviderAndProviderId(provider: SocialProvider, providerId: String): Boolean
    
    /**
     * 사용자와 제공자로 소셜 계정 존재 여부 확인
     */
    fun existsByUserIdAndProvider(userId: UUID, provider: SocialProvider): Boolean
    
    /**
     * 사용자의 모든 소셜 계정 삭제
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SocialAccountJpaEntity sa WHERE sa.userId = :userId")
    fun deleteByUserId(userId: UUID)
    
    /**
     * 제공자별 연결된 계정 수 조회 (관리용)
     */
    @Query("SELECT sa.provider, COUNT(sa) FROM SocialAccountJpaEntity sa GROUP BY sa.provider")
    fun countByProvider(): List<Array<Any>>
    
    /**
     * 최근 연결된 소셜 계정 조회 (관리용)
     */
    @Query("SELECT sa FROM SocialAccountJpaEntity sa ORDER BY sa.connectedAt DESC")
    fun findRecentlyConnected(): List<SocialAccountJpaEntity>
}