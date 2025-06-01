package com.spring.findmypet.domain.validation

object ValidationMessages {
    const val FIELD_REQUIRED = "FIELD_REQUIRED"
    const val FIELD_INVALID_FORMAT = "FIELD_INVALID_FORMAT"
    const val FIELD_TOO_SHORT = "FIELD_TOO_SHORT"
    const val FIELD_TOO_LONG = "FIELD_TOO_LONG"

    const val PASSWORD_MISSING_UPPERCASE = "PASSWORD_MISSING_UPPERCASE"
    const val PASSWORD_MISSING_NUMBER = "PASSWORD_MISSING_NUMBER"
    const val PASSWORD_MISSING_SPECIAL_CHAR = "PASSWORD_MISSING_SPECIAL_CHAR"

    const val EMAIL_ALREADY_REGISTERED = "Email adresa je već registrovana"
    const val INVALID_CREDENTIALS = "Pogrešan email ili lozinka"
    const val USER_NOT_FOUND = "Korisnik nije pronađen"
    const val INVALID_TOKEN = "Nevažeći token"
    const val USER_BANNED = "Vaš nalog je banovan"
} 