package com.spring.findmypet.domain.dto

import java.time.LocalDateTime

/**
 * DTO za prikaz konverzacije u inbox-u
 */
data class ConversationDto(
    val id: Long,
    val otherUserId: Long,
    val otherUserName: String,
    val lastMessage: String?,
    val lastMessageTime: LocalDateTime?,
    val unreadCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 