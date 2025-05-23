package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.validation.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RegisterRequest(
    @field:NotBlank(message = "Ime i prezime je obavezno polje")
    @field:Pattern(
        regexp = "^[\\p{L}\\s]{2,100}$",
        message = "Ime i prezime može sadržati samo slova i razmake"
    )
    val fullName: String,

    @field:NotBlank(message = "Email adresa je obavezna")
    @field:Email(message = "Email nije u ispravnom formatu")
    val email: String,

    @field:NotBlank(message = "Broj telefona je obavezan")
    @field:Pattern(
        regexp = "^(\\+3816|06)[0-9]{6,11}$",
        message = "Broj telefona mora početi sa +381 ili 06"
    )
    val phoneNumber: String,

    @field:Password
    val password: String,

    val firebaseToken: String? = null
)

data class LoginRequest(
    @field:NotBlank(message = "Email adresa je obavezna")
    @field:Email(message = "Email nije u ispravnom formatu")
    val email: String,

    @field:NotBlank(message = "Lozinka je obavezna")
    val password: String,

    val firebaseToken: String? = null
)

data class AuthResponse(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val fullName: String,
    val email: String,
    val role: String
) 