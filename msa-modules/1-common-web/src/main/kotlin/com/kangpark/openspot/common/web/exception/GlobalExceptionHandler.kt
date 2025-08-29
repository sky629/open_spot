package com.kangpark.openspot.common.web.exception

import com.kangpark.openspot.common.core.exception.BusinessException
import com.kangpark.openspot.common.core.exception.CommonErrorCode
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for all controllers
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business exception occurred: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.errorCode.message
        )
        
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(errorResponse))
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Validation exception occurred: {}", ex.message)
        
        val details = ex.bindingResult.fieldErrors.associate { 
            it.field to (it.defaultMessage ?: "Invalid value")
        }
        
        val errorResponse = ErrorResponse(
            code = CommonErrorCode.INVALID_REQUEST.code,
            message = CommonErrorCode.INVALID_REQUEST.message,
            details = details
        )
        
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(errorResponse))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception occurred: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            code = CommonErrorCode.INTERNAL_SERVER_ERROR.code,
            message = CommonErrorCode.INTERNAL_SERVER_ERROR.message
        )
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(errorResponse))
    }
}