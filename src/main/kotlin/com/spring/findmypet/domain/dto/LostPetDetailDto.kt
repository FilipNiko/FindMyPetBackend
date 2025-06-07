package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.model.PetType
import java.time.LocalDateTime

data class LostPetDetailResponse(
    val id: Long,
    val petType: PetType,
    val title: String,
    val breed: String?,
    val color: String?,
    val description: String,
    val gender: String,
    val hasChip: Boolean,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: LocalDateTime,
    val timeAgo: String,
    val photos: List<String>,
    val distance: String,
    val distanceInMeters: Double,
    val owner: OwnerInfo,
    val found: Boolean,
    val foundAt: LocalDateTime?
)

data class OwnerInfo(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val avatarId: String
) 