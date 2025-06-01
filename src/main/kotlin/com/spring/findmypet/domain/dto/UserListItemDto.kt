package com.spring.findmypet.domain.dto

data class UserListItemDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val banned: Boolean,
    val banReason: String?
)
