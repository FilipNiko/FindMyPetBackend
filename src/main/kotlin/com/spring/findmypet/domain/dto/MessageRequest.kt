package com.spring.findmypet.domain.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MessageRequest(
    @field:NotNull(message = "ID primaoca je obavezan")
    val receiverId: Long,
    
    @field:NotBlank(message = "Sadržaj poruke ne može biti prazan")
    val content: String,
    
    val messageType: MessageType = MessageType.TEXT,
    
    val latitude: Double? = null,
    
    val longitude: Double? = null,
    
    val address: String? = null
) 