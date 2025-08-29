package com.kangpark.openspot.common.core.exception

/**
 * Base business exception for domain-specific business rule violations
 */
abstract class BusinessException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.message, cause)

/**
 * Error code interface for consistent error handling
 */
interface ErrorCode {
    val code: String
    val message: String
}

/**
 * Common error codes used across all services
 */
enum class CommonErrorCode(
    override val code: String,
    override val message: String
) : ErrorCode {
    INVALID_REQUEST("COMMON_001", "잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR("COMMON_002", "내부 서버 오류가 발생했습니다"),
    UNAUTHORIZED("COMMON_003", "인증이 필요합니다"),
    FORBIDDEN("COMMON_004", "권한이 없습니다"),
    NOT_FOUND("COMMON_005", "요청한 리소스를 찾을 수 없습니다")
}