package com.spring.findmypet.domain.dto

/**
 * DTO koji sadrži informacije o konverzaciji i listu poruka
 */
data class MessageResponseDto(
    val conversationId: Long,
    val otherUserName: String,
    val otherUserPhone: String?,
    val messages: List<MessageDto>
) 