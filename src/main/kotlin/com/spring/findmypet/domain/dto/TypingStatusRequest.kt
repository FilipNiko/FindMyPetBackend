package com.spring.findmypet.domain.dto

data class TypingStatusRequest(
    val receiverId: Long,
    val conversationId: Long,
    val isTyping: Boolean
) 