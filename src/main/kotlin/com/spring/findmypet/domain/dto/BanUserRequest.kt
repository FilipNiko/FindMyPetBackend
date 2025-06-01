package com.spring.findmypet.domain.dto

data class BanUserRequest(
    val banned: Boolean,
    val reason: String?
)
