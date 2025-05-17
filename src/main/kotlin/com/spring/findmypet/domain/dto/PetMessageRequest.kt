package com.spring.findmypet.domain.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO za zahtev za slanje poruke vlasniku kućnog ljubimca
 */
data class PetMessageRequest(
    @field:NotBlank(message = "Sadržaj poruke ne može biti prazan")
    val content: String
) 