package com.spring.findmypet.exception.auth

enum class AuthErrorCodes(
    val code: String,
    val message: String
) {
    FULLNAME_FIELD_REQUIRED(
        "FULLNAME_FIELD_REQUIRED",
        "Ime i prezime je obavezno polje"
    ),
    FULLNAME_FIELD_INVALID_FORMAT(
        "FULLNAME_FIELD_INVALID_FORMAT",
        "Ime i prezime može sadržati samo slova i razmake"
    ),

    EMAIL_FIELD_REQUIRED(
        "EMAIL_FIELD_REQUIRED",
        "Email je obavezno polje"
    ),
    EMAIL_FIELD_INVALID_FORMAT(
        "EMAIL_FIELD_INVALID_FORMAT",
        "Email nije u ispravnom formatu"
    ),
    EMAIL_ALREADY_REGISTERED(
        "EMAIL_ALREADY_REGISTERED",
        "Email adresa je već registrovana"
    ),

    PHONE_FIELD_REQUIRED(
        "PHONE_FIELD_REQUIRED",
        "Broj telefona je obavezno polje"
    ),
    PHONE_FIELD_INVALID_FORMAT(
        "PHONE_FIELD_INVALID_FORMAT",
        "Broj telefona mora početi sa +381 ili 06"
    ),

    PASSWORD_FIELD_REQUIRED(
        "PASSWORD_FIELD_REQUIRED",
        "Lozinka je obavezno polje"
    ),
    PASSWORD_FIELD_TOO_SHORT(
        "PASSWORD_FIELD_TOO_SHORT",
        "Lozinka mora imati najmanje 6 karaktera"
    ),
    PASSWORD_FIELD_MISSING_UPPERCASE(
        "PASSWORD_FIELD_MISSING_UPPERCASE",
        "Lozinka mora sadržati bar jedno veliko slovo"
    ),
    PASSWORD_FIELD_MISSING_NUMBER(
        "PASSWORD_FIELD_MISSING_NUMBER",
        "Lozinka mora sadržati bar jedan broj"
    ),
    PASSWORD_FIELD_MISSING_SPECIAL_CHAR(
        "PASSWORD_FIELD_MISSING_SPECIAL_CHAR",
        "Lozinka mora sadržati bar jedan specijalni karakter"
    ),

    LOGIN_INVALID_CREDENTIALS(
        "LOGIN_INVALID_CREDENTIALS",
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
    TOKEN_MISSING(
        "TOKEN_MISSING",
        "Token nije prosleđen"
    ),

    AUTH_SYSTEM_ERROR(
        "AUTH_SYSTEM_ERROR",
        "Došlo je do greške prilikom autentifikacije"
    )
} 