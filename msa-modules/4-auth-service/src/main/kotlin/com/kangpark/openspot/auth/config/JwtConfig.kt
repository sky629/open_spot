package com.kangpark.openspot.auth.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * JWT Encoder/Decoder 설정
 * - Gateway와 동일한 Nimbus JWT 라이브러리 사용
 * - HMAC SHA512 알고리즘으로 토큰 생성/검증
 */
@Configuration
open class JwtConfig {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    /**
     * JWT Encoder Bean
     * - 토큰 생성에 사용
     * - HMAC SHA256 알고리즘 사용
     */
    @Bean
    open fun jwtEncoder(): JwtEncoder {
        val secretKey: SecretKey = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
        return NimbusJwtEncoder(ImmutableSecret(secretKey))
    }

    /**
     * JWT Decoder Bean
     * - 토큰 검증에 사용
     * - Gateway와 동일한 알고리즘 사용 (HS256)
     */
    @Bean
    open fun jwtDecoder(): JwtDecoder {
        val secretKey: SecretKey = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secretKey).build()
    }
}
