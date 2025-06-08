package com.spring.findmypet.domain.dto

data class MessagePageResponse(
    val conversationId: Long?,
    val otherUserName: String,
    val otherUserPhone: String?,
    val otherUserAvatarId: String,
    val content: List<MessageDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
) 