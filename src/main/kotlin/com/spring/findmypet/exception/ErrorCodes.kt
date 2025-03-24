package com.spring.findmypet.exception

/**
 * Osnovni interfejs za sve kodove grešaka
 * Svaka specifična kategorija grešaka implementira ovaj interfejs
 */
interface ErrorCode {
    val code: String
    val message: String
}

/**
 * Osnovni sistemski kodovi grešaka
 */
enum class SystemErrorCodes(
    override val code: String,
    override val message: String
) : ErrorCode {
    SYSTEM_ERROR(
        "SYSTEM_ERROR",
        "Došlo je do sistemske greške"
    ),
    RESOURCE_NOT_FOUND(
        "RESOURCE_NOT_FOUND",
        "Resurs nije pronađen"
    ),
    OPERATION_NOT_ALLOWED(
        "OPERATION_NOT_ALLOWED",
        "Operacija nije dozvoljena"
    ),
    ACCESS_DENIED(
        "ACCESS_DENIED",
        "Pristup je odbijen"
    )
}

/**
 * Kodovi grešaka za validaciju
 */
enum class ValidationErrorCodes(
    override val code: String,
    override val message: String
) : ErrorCode {
    VALIDATION_ERROR(
        "VALIDATION_ERROR",
        "Došlo je do greške pri validaciji"
    ),
    FIELD_REQUIRED(
        "FIELD_REQUIRED",
        "Polje je obavezno"
    ),
    FIELD_INVALID_FORMAT(
        "FIELD_INVALID_FORMAT",
        "Neispravan format polja"
    ),
    FIELD_TOO_SHORT(
        "FIELD_TOO_SHORT",
        "Vrednost polja je prekratka"
    ),
    FIELD_TOO_LONG(
        "FIELD_TOO_LONG",
        "Vrednost polja je predugačka"
    )
} 