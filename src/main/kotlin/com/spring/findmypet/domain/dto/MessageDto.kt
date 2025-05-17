package com.spring.findmypet.domain.dto

import java.time.LocalDateTime

/**
 * DTO za prenos podataka o porukama
 */
data class MessageDto(
    val id: Long? = null,
    val senderId: Long,
    val senderName: String,
    val content: String,
    val sentAt: LocalDateTime,
    val isRead: Boolean,
    val readAt: LocalDateTime? = null
) 