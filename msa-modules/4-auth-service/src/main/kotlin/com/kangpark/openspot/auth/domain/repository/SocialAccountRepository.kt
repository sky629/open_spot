package com.kangpark.openspot.auth.domain.repository

import com.kangpark.openspot.auth.domain.entity.SocialAccount
import com.kangpark.openspot.auth.domain.vo.SocialProvider
import java.util.UUID

/**
 * 소셜 계정 리포지토리 인터페이스 (Domain Layer)
 * 소셜 계정 관련 데이터 접근 추상화
 */
interface SocialAccountRepository {
    
    /**
     * ID로 소셜 계정 조회
     */
    fun findById(id: UUID): SocialAccount?
    
    /**
     * 사용자 ID로 모든 소셜 계정 조회
     */
    fun findByUserId(userId: UUID): List<SocialAccount>
    
    /**
     * 사용자 ID와 제공자로 소셜 계정 조회
     */
    fun findByUserIdAndProvider(userId: UUID, provider: SocialProvider): SocialAccount?
    
    /**
     * 제공자와 제공자 ID로 소셜 계정 조회
     */
    fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount?
    
    /**
     * 소셜 계정 저장
     */
    fun save(socialAccount: SocialAccount): SocialAccount
    
    /**
     * 소셜 계정 삭제
     */
    fun deleteById(id: UUID)
    
    /**
     * 사용자의 모든 소셜 계정 삭제
     */
    fun deleteByUserId(userId: UUID)
    
    /**
     * 제공자와 제공자 ID로 소셜 계정 존재 여부 확인
     */
    fun existsByProviderAndProviderId(provider: SocialProvider, providerId: String): Boolean
    
    /**
     * 사용자와 제공자로 소셜 계정 존재 여부 확인
     */
    fun existsByUserIdAndProvider(userId: UUID, provider: SocialProvider): Boolean
}