package com.spring.findmypet.domain.dto

/**
 * DTO za slanje statusa o kucanju poruke
 */
data class TypingStatusRequest(
    val receiverId: Long,
    val conversationId: Long,
    val isTyping: Boolean
) 