package com.spring.findmypet.util

/**
 * Utility klasa za rad sa stringovima
 */
object StringsUtil {
    
    /**
     * Mapira engleski naziv pola u lokalizovani srpski
     * @param gender engleski naziv pola ("MALE" ili "FEMALE")
     * @return lokalizovani srpski naziv pola ("Muški" ili "Ženski")
     */
    fun mapGender(gender: String): String {
        return when (gender.trim().uppercase()) {
            "MALE" -> "Muški"
            "FEMALE" -> "Ženski"
            else -> gender
        }
    }
} 