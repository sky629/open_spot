package com.kangpark.openspot.auth.repository.impl

import com.kangpark.openspot.auth.domain.entity.User
import com.kangpark.openspot.auth.domain.repository.UserRepository
import com.kangpark.openspot.auth.repository.jpa.UserJpaRepository
import com.kangpark.openspot.auth.repository.jpa.entity.UserJpaEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * User Repository 구현체
 * Domain Layer의 UserRepository 인터페이스를 Infrastructure Layer에서 구현
 */
@Repository
@Transactional
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    
    override fun save(user: User): User {
        val jpaEntity = UserJpaEntity.fromDomain(user)
        val savedEntity = userJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findById(id: UUID): User? {
        return userJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    @Transactional(readOnly = true)
    override fun findBySocialId(socialId: String): User? {
        return userJpaRepository.findBySocialId(socialId)
            ?.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
            ?.toDomain()
    }
    
    @Transactional(readOnly = true)
    override fun existsBySocialId(socialId: String): Boolean {
        return userJpaRepository.existsBySocialId(socialId)
    }
    
    override fun deleteById(id: UUID) {
        userJpaRepository.deleteById(id)
    }
}