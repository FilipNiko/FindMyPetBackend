package com.spring.findmypet.domain.dto

import java.time.LocalDateTime

data class ConversationDto(
    val id: Long,
    val otherUserId: Long,
    val otherUserName: String,
    val otherUserAvatarId: String,
    val lastMessage: String?,
    val lastMessageType: MessageType? = null,
    val lastMessageSenderId: Long? = null,
    val lastMessageTime: LocalDateTime?,
    val unreadCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun getFormattedLastMessage(isCurrentUserSender: Boolean): String {
        return when (lastMessageType) {
            MessageType.TEXT -> lastMessage ?: ""
            MessageType.IMAGE -> if (isCurrentUserSender) "Poslali ste sliku" else "Primili ste sliku"
            MessageType.LOCATION -> if (isCurrentUserSender) "Poslali ste lokaciju" else "Primili ste lokaciju"
            null -> ""
        }
    }
} 

data class ConversationPageResponse(
    val content: List<ConversationDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
) 