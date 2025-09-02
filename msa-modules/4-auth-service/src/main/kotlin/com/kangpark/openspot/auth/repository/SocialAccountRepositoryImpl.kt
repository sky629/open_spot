package com.kangpark.openspot.auth.repository

import com.kangpark.openspot.auth.domain.SocialAccount
import com.kangpark.openspot.auth.domain.SocialAccountRepository
import com.kangpark.openspot.auth.domain.SocialProvider
import com.kangpark.openspot.auth.repository.entity.SocialAccountJpaEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SocialAccount Repository 구현체
 * Domain Layer의 SocialAccountRepository 인터페이스를 Infrastructure Layer에서 구현
 */
@Repository
@Transactional
class SocialAccountRepositoryImpl(
    private val socialAccountJpaRepository: SocialAccountJpaRepository
) : SocialAccountRepository {
    
    override fun save(socialAccount: SocialAccount): SocialAccount {
        val jpaEntity = SocialAccountJpaEntity.fromDomain(socialAccount)
        val savedEntity = socialAccountJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findById(id: UUID): SocialAccount? {
        return socialAccountJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    @Transactional(readOnly = true)
    override fun findByUserId(userId: UUID): List<SocialAccount> {
        return socialAccountJpaRepository.findByUserId(userId)
            .map { it.toDomain() }
    }
    
    @Transactional(readOnly = true)
    override fun findByUserIdAndProvider(userId: UUID, provider: SocialProvider): SocialAccount? {
        return socialAccountJpaRepository.findByUserIdAndProvider(userId, provider)
            ?.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount? {
        return socialAccountJpaRepository.findByProviderAndProviderId(provider, providerId)
            ?.toDomain()
    }
    
    override fun deleteById(id: UUID) {
        socialAccountJpaRepository.deleteById(id)
    }
    
    override fun deleteByUserId(userId: UUID) {
        socialAccountJpaRepository.deleteByUserId(userId)
    }
    
    @Transactional(readOnly = true)
    override fun existsByProviderAndProviderId(provider: SocialProvider, providerId: String): Boolean {
        return socialAccountJpaRepository.existsByProviderAndProviderId(provider, providerId)
    }
    
    @Transactional(readOnly = true)
    override fun existsByUserIdAndProvider(userId: UUID, provider: SocialProvider): Boolean {
        return socialAccountJpaRepository.existsByUserIdAndProvider(userId, provider)
    }
}