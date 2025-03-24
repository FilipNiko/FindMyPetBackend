package com.spring.findmypet.exception

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Konstantni validacioni kodovi koje Spring koristi
 * Ovo su standardni kodovi koje Spring koristi za različite validacione anotacije
 */
object ValidationCodes {
    // Bean Validation kodovi
    const val NOT_NULL = "NotNull"
    const val NOT_EMPTY = "NotEmpty"
    const val NOT_BLANK = "NotBlank"
    const val MIN = "Min"
    const val MAX = "Max"
    const val SIZE = "Size"
    const val PATTERN = "Pattern"
    const val EMAIL = "Email"
    const val VALID = "Valid"
}

/**
 * Bazna klasa za sve handlere izuzetaka
 * Sadrži osnovne funkcije za kreiranje odgovora
 */
abstract class BaseExceptionHandler {
    
    protected val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Pretvara ErrorCode u ApiError
     */
    protected fun toApiError(errorCode: ErrorCode): ApiError {
        return ApiError(
            errorCode = errorCode.code,
            errorDescription = errorCode.message
        )
    }

    /**
     * Kreira standardizovani odgovor sa greškama
     */
    protected fun createErrorResponse(
        status: HttpStatus, 
        errors: List<ApiError>
    ): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(status)
            .body(ApiResponse(success = false, errors = errors))
    }

    /**
     * Kreira standardizovani odgovor sa jednom greškom
     */
    protected fun createErrorResponse(
        status: HttpStatus, 
        errorCode: ErrorCode
    ): ResponseEntity<ApiResponse<Nothing>> {
        return createErrorResponse(status, listOf(toApiError(errorCode)))
    }
    
    /**
     * Obrađuje greške validacije
     * Svaka implementacija treba da definiše kako će se mapirati polja i greške
     */
    protected abstract fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>>
    
    /**
     * Vraća najspecifičniji kod validacije za polje
     * Metoda koju podklase mogu koristiti za dobijanje kodova grešaka
     */
    protected fun getValidationCode(fieldError: FieldError): String {
        // Prvo proveriti code koji dolazi direktno od validacione anotacije
        return fieldError.code ?: "UNKNOWN_VALIDATION_ERROR"
    }

    /**
     * Grupira greške po poljima i vraća samo najvažniju grešku za svako polje
     * Prioritizacija je sledeća:
     * 1. Greške za obavezna polja (NOT_NULL, NOT_EMPTY, NOT_BLANK)
     * 2. Greške za format (EMAIL, PATTERN)
     * 3. Greške za dužinu (SIZE, MIN, MAX)
     * 4. Ostale greške
     */
    protected fun getPrioritizedErrors(fieldErrors: List<FieldError>): List<FieldError> {
        return fieldErrors
            .groupBy { it.field }
            .mapValues { (_, errors) ->
                errors.sortedWith(compareBy { fieldError ->
                    when (getValidationCode(fieldError)) {
                        ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> 1
                        ValidationCodes.EMAIL, ValidationCodes.PATTERN -> 2
                        ValidationCodes.SIZE, ValidationCodes.MIN, ValidationCodes.MAX -> 3
                        else -> 4
                    }
                }).first()
            }
            .values
            .toList()
    }
} 