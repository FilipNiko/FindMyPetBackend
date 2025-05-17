package com.spring.findmypet.domain.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * DTO za zahtev za slanje poruke
 */
data class MessageRequest(
    @field:NotNull(message = "ID primaoca je obavezan")
    val receiverId: Long,
    
    @field:NotBlank(message = "Sadržaj poruke ne može biti prazan")
    val content: String
) 