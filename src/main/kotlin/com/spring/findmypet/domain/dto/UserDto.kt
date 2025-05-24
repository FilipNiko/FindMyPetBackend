package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.validation.Password

data class UserInfoResponse(
    val fullName: String,
    val email: String,
    val unreadMessagesCount: Int
)

data class UpdateNameRequest(
    val fullName: String
)

data class UpdatePasswordRequest(
    val currentPassword: String,
    
    @field:Password
    val newPassword: String
)

data class UpdatePasswordResponse(
    val success: Boolean
) 