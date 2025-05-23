package com.spring.findmypet.domain.dto

import java.time.LocalDateTime

data class MessageDto(
    val id: Long? = null,
    val senderId: Long,
    val senderName: String,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val sentAt: LocalDateTime,
    val isRead: Boolean,
    val readAt: LocalDateTime? = null
) 