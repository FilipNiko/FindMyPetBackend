package com.spring.findmypet.domain.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FirebaseMessage(
    val title: String,
    val body: String,
    val type: NotificationType,
    val data: Map<String, String> = emptyMap()
) {
    override fun toString(): String {
        return "{\"title\":\"$title\",\"body\":\"$body\",\"type\":\"${type.name}\",\"data\":$data}"
    }
}

enum class NotificationType {
    NEW_MESSAGE,
    LOST_PET_NEARBY,
} 