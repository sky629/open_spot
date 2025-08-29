package com.kangpark.openspot.common.core.util

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Pagination utility functions
 */
object PageUtils {
    
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    /**
     * Create a pageable with default size and validation
     */
    fun createPageable(page: Int, size: Int, sort: Sort = Sort.unsorted()): Pageable {
        val validatedPage = if (page < 0) 0 else page
        val validatedSize = when {
            size <= 0 -> DEFAULT_PAGE_SIZE
            size > MAX_PAGE_SIZE -> MAX_PAGE_SIZE
            else -> size
        }
        
        return PageRequest.of(validatedPage, validatedSize, sort)
    }
    
    /**
     * Create a pageable with sorting by field name
     */
    fun createPageable(page: Int, size: Int, sortBy: String, direction: Sort.Direction = Sort.Direction.ASC): Pageable {
        val sort = Sort.by(direction, sortBy)
        return createPageable(page, size, sort)
    }
}