package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.validation.Password
import com.spring.findmypet.domain.validation.ValidationMessages
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @field:Pattern(
        regexp = "^[\\p{L}\\s]{2,100}$",
        message = ValidationMessages.FIELD_INVALID_FORMAT
    )
    val fullName: String,

    @field:NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @field:Email(message = ValidationMessages.FIELD_INVALID_FORMAT)
    val email: String,

    @field:NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @field:Pattern(
        regexp = "^(\\+3816|06)[0-9]{6,11}$",
        message = ValidationMessages.FIELD_INVALID_FORMAT
    )
    val phoneNumber: String,

    @field:Password
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @field:Email(message = ValidationMessages.FIELD_INVALID_FORMAT)
    val email: String,

    @field:NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val fullName: String,
    val email: String,
    val role: String
) 