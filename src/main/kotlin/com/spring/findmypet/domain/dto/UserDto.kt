package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.validation.Password
import com.fasterxml.jackson.annotation.JsonInclude

data class UserInfoResponse(
    val fullName: String,
    val email: String,
    val unreadMessagesCount: Int,
    val avatarId: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val receiveNotifications: Boolean? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val notificationRadius: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val notificationLatitude: Double? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val notificationLongitude: Double? = null
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

data class UpdateAvatarRequest(
    val avatarId: String
)

data class UpdateAvatarResponse(
    val success: Boolean,
    val avatarId: String
)

data class NotificationSettingsRequest(
    val receiveNotifications: Boolean,
    val notificationRadius: Int,
    val latitude: Double?,
    val longitude: Double?
)

data class NotificationSettingsResponse(
    val success: Boolean,
    val receiveNotifications: Boolean,
    val notificationRadius: Int,
    val latitude: Double?,
    val longitude: Double?
) 