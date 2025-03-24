package com.spring.findmypet.exception.auth

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.validation.ValidationMessages.PASSWORD_MISSING_NUMBER
import com.spring.findmypet.domain.validation.ValidationMessages.PASSWORD_MISSING_SPECIAL_CHAR
import com.spring.findmypet.domain.validation.ValidationMessages.PASSWORD_MISSING_UPPERCASE
import com.spring.findmypet.exception.BaseExceptionHandler
import com.spring.findmypet.exception.SystemErrorCodes
import com.spring.findmypet.exception.ValidationCodes
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Handler za izuzetke vezane za autentifikaciju
 * Obrađuje sve izuzetke koji se dese u kontrolerima za autentifikaciju
 */
@RestControllerAdvice(basePackages = ["com.spring.findmypet.controller.auth"])
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthExceptionHandler : BaseExceptionHandler() {

    /**
     * Obrađuje izuzetke za nevalidne kredencijale
     */
    @ExceptionHandler(BadCredentialsException::class, AuthenticationException::class)
    fun handleAuthenticationException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Neuspešna autentifikacija: ${ex.message}")
        return createErrorResponse(HttpStatus.UNAUTHORIZED, AuthErrorCodes.INVALID_CREDENTIALS)
    }

    /**
     * Obrađuje izuzetke za pristup zabranjenim resursima
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Pristup odbijen: ${ex.message}")
        return createErrorResponse(HttpStatus.FORBIDDEN, SystemErrorCodes.ACCESS_DENIED)
    }

    /**
     * Implementacija apstraktne metode za obradu validacionih grešaka
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    override fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val prioritizedErrors = getPrioritizedErrors(ex.bindingResult.fieldErrors)
        val errors = prioritizedErrors.map { fieldError ->
            val errorCode = mapFieldToErrorCode(fieldError)
            toApiError(errorCode)
        }
        
        logger.error("Validacione greške pri autentifikaciji: ${errors.map { it.errorCode }}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errors)
    }
    
    /**
     * Mapira polje i kod validacije na odgovarajući kod greške
     */
    private fun mapFieldToErrorCode(fieldError: FieldError): AuthErrorCodes {
        val fieldName = fieldError.field
        val validationCode = getValidationCode(fieldError)
        
        return when (fieldName) {
            "email" -> mapEmailErrorCode(validationCode)
            "password" -> mapPasswordErrorCode(validationCode, fieldError.defaultMessage ?: "")
            "fullName" -> mapFullNameErrorCode(validationCode)
            "phoneNumber" -> mapPhoneErrorCode(validationCode)
            else -> AuthErrorCodes.AUTH_ERROR
        }
    }
    
    /**
     * Mapira kod validacije za email na odgovarajući kod greške
     */
    private fun mapEmailErrorCode(validationCode: String): AuthErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> AuthErrorCodes.EMAIL_FIELD_REQUIRED
            ValidationCodes.EMAIL, ValidationCodes.PATTERN -> AuthErrorCodes.EMAIL_INVALID_FORMAT
            else -> AuthErrorCodes.EMAIL_INVALID_FORMAT
        }
    }
    
    /**
     * Mapira kod validacije za lozinku na odgovarajući kod greške
     */
    private fun mapPasswordErrorCode(validationCode: String, message: String): AuthErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> AuthErrorCodes.PASSWORD_FIELD_REQUIRED
            ValidationCodes.SIZE, ValidationCodes.MIN -> {
                // Za složenije validacije moramo proveriti i poruku
                when {
                    PASSWORD_MISSING_UPPERCASE in message -> AuthErrorCodes.PASSWORD_MISSING_UPPERCASE
                    PASSWORD_MISSING_NUMBER in message -> AuthErrorCodes.PASSWORD_MISSING_NUMBER
                    PASSWORD_MISSING_SPECIAL_CHAR in message -> AuthErrorCodes.PASSWORD_MISSING_SPECIAL_CHAR
                    else -> AuthErrorCodes.PASSWORD_TOO_SHORT
                }
            }
            else -> AuthErrorCodes.PASSWORD_FIELD_REQUIRED
        }
    }
    
    /**
     * Mapira kod validacije za puno ime na odgovarajući kod greške
     */
    private fun mapFullNameErrorCode(validationCode: String): AuthErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> AuthErrorCodes.FULLNAME_REQUIRED
            ValidationCodes.PATTERN -> AuthErrorCodes.FULLNAME_INVALID_FORMAT
            else -> AuthErrorCodes.FULLNAME_INVALID_FORMAT
        }
    }
    
    /**
     * Mapira kod validacije za telefon na odgovarajući kod greške
     */
    private fun mapPhoneErrorCode(validationCode: String): AuthErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> AuthErrorCodes.PHONE_REQUIRED
            ValidationCodes.PATTERN -> AuthErrorCodes.PHONE_INVALID_FORMAT
            else -> AuthErrorCodes.PHONE_INVALID_FORMAT
        }
    }

    /**
     * Obrađuje specifične izuzetke za autentifikaciju
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = when (ex.message) {
            "EMAIL_ALREADY_EXISTS" -> AuthErrorCodes.EMAIL_ALREADY_EXISTS
            else -> AuthErrorCodes.AUTH_ERROR
        }
        
        logger.error("Greška stanja pri autentifikaciji: ${ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorCode)
    }
} 