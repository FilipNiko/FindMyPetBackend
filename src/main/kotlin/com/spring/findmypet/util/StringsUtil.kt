package com.spring.findmypet.util

object StringsUtil {

    fun mapGender(gender: String): String {
        return when (gender.trim().uppercase()) {
            "MALE" -> "Muški"
            "FEMALE" -> "Ženski"
            else -> gender
        }
    }
} 