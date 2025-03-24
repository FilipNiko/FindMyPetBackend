package com.spring.findmypet.exception.auth

import com.spring.findmypet.exception.ErrorCode

/**
 * Kodovi grešaka vezani za autentifikaciju
 */
enum class AuthErrorCodes(
    override val code: String,
    override val message: String
) : ErrorCode {
    AUTH_ERROR(
        "AUTH_ERROR",
        "Greška pri autentifikaciji"
    ),
    INVALID_CREDENTIALS(
        "INVALID_CREDENTIALS",
        "Pogrešan email ili lozinka"
    ),
    TOKEN_EXPIRED(
        "TOKEN_EXPIRED",
        "Token je istekao"
    ),
    TOKEN_INVALID(
        "TOKEN_INVALID",
        "Token nije validan"
    ),
    EMAIL_ALREADY_EXISTS(
        "EMAIL_ALREADY_EXISTS",
        "Email adresa je već registrovana"
    ),
    EMAIL_FIELD_REQUIRED(
        "EMAIL_FIELD_REQUIRED",
        "Polje je obavezno"
    ),
    EMAIL_INVALID_FORMAT(
        "EMAIL_INVALID_FORMAT",
        "Email nije u ispravnom formatu"
    ),
    PASSWORD_FIELD_REQUIRED(
        "PASSWORD_FIELD_REQUIRED",
        "Polje je obavezno"
    ),
    PASSWORD_TOO_SHORT(
        "PASSWORD_TOO_SHORT",
        "Lozinka mora imati najmanje 6 karaktera"
    ),
    PASSWORD_MISSING_UPPERCASE(
        "PASSWORD_MISSING_UPPERCASE",
        "Lozinka mora sadržati bar jedno veliko slovo"
    ),
    PASSWORD_MISSING_NUMBER(
        "PASSWORD_MISSING_NUMBER",
        "Lozinka mora sadržati bar jedan broj"
    ),
    PASSWORD_MISSING_SPECIAL_CHAR(
        "PASSWORD_MISSING_SPECIAL_CHAR",
        "Lozinka mora sadržati bar jedan specijalni karakter"
    ),
    FULLNAME_REQUIRED(
        "FULLNAME_REQUIRED",
        "Ime i prezime je obavezno polje"
    ),
    FULLNAME_INVALID_FORMAT(
        "FULLNAME_INVALID_FORMAT",
        "Ime i prezime može sadržati samo slova i razmake"
    ),
    PHONE_REQUIRED(
        "PHONE_REQUIRED",
        "Broj telefona je obavezno polje"
    ),
    PHONE_INVALID_FORMAT(
        "PHONE_INVALID_FORMAT",
        "Broj telefona mora početi sa +381 ili 06"
    )
} 