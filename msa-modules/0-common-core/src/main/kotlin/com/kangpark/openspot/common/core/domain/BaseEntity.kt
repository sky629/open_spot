package com.kangpark.openspot.common.core.domain

import com.github.f4b6a3.uuid.UuidCreator
import java.time.LocalDateTime
import java.util.UUID

/**
 * Base Entity for Domain Models
 * Pure domain object without any infrastructure dependencies
 * Uses UUIDv7 for time-ordered, sortable IDs
 */
data class BaseEntity(
    val id: UUID = UuidCreator.getTimeOrderedEpoch(),  // UUIDv7: 시간 순서 보장
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)