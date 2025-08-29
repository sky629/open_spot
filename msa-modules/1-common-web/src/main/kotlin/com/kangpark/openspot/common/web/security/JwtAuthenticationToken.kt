package com.kangpark.openspot.common.web.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * JWT Authentication Token for Spring Security
 */
class JwtAuthenticationToken(
    private val token: String,
    private val userId: String,
    authorities: Collection<GrantedAuthority> = emptyList()
) : AbstractAuthenticationToken(authorities) {
    
    init {
        isAuthenticated = true
    }
    
    override fun getCredentials(): String = token
    
    override fun getPrincipal(): String = userId
}