package com.spring.findmypet.domain.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token je obavezan")
    val refreshToken: String
) 