package com.spring.findmypet.exception.auth

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.validation.ValidationMessages
import org.apache.juli.logging.Log
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.spring.findmypet.controller.auth"])
class AuthExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.mapNotNull { error ->
            val errorCode = when (error.field) {
                "fullName" -> when (error.defaultMessage) {
                    ValidationMessages.FIELD_REQUIRED -> AuthErrorCodes.FULLNAME_FIELD_REQUIRED
                    ValidationMessages.FIELD_INVALID_FORMAT -> AuthErrorCodes.FULLNAME_FIELD_INVALID_FORMAT
                    else -> null
                }
                "email" -> when (error.defaultMessage) {
                    ValidationMessages.FIELD_REQUIRED -> AuthErrorCodes.EMAIL_FIELD_REQUIRED
                    ValidationMessages.FIELD_INVALID_FORMAT -> AuthErrorCodes.EMAIL_FIELD_INVALID_FORMAT
                    else -> null
                }
                "phoneNumber" -> when (error.defaultMessage) {
                    ValidationMessages.FIELD_REQUIRED -> AuthErrorCodes.PHONE_FIELD_REQUIRED
                    ValidationMessages.FIELD_INVALID_FORMAT -> AuthErrorCodes.PHONE_FIELD_INVALID_FORMAT
                    else -> null
                }
                "password" -> when (error.defaultMessage) {
                    ValidationMessages.FIELD_REQUIRED -> AuthErrorCodes.PASSWORD_FIELD_REQUIRED
                    ValidationMessages.FIELD_TOO_SHORT -> AuthErrorCodes.PASSWORD_FIELD_TOO_SHORT
                    ValidationMessages.PASSWORD_MISSING_UPPERCASE -> AuthErrorCodes.PASSWORD_FIELD_MISSING_UPPERCASE
                    ValidationMessages.PASSWORD_MISSING_NUMBER -> AuthErrorCodes.PASSWORD_FIELD_MISSING_NUMBER
                    ValidationMessages.PASSWORD_MISSING_SPECIAL_CHAR -> AuthErrorCodes.PASSWORD_FIELD_MISSING_SPECIAL_CHAR
                    else -> null
                }
                else -> null
            }

            errorCode?.let {
                ApiError(
                    errorCode = it.code,
                    errorDescription = it.message
                )
            }
        }.distinctBy { it.errorCode }

        return ResponseEntity
            .badRequest()
            .body(ApiResponse(success = false, errors = errors))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = when (ex.message) {
            ValidationMessages.EMAIL_ALREADY_REGISTERED -> AuthErrorCodes.EMAIL_ALREADY_REGISTERED
            else -> AuthErrorCodes.AUTH_SYSTEM_ERROR
        }
        
        val error = ApiError(
            errorCode = errorCode.code,
            errorDescription = errorCode.message
        )
        
        return ResponseEntity
            .badRequest()
            .body(ApiResponse(success = false, errors = listOf(error)))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ApiResponse<Nothing>> {
        val error = ApiError(
            errorCode = AuthErrorCodes.LOGIN_INVALID_CREDENTIALS.code,
            errorDescription = AuthErrorCodes.LOGIN_INVALID_CREDENTIALS.message
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(success = false, errors = listOf(error)))
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val error = ApiError(
            errorCode = AuthErrorCodes.AUTH_SYSTEM_ERROR.code,
            errorDescription = AuthErrorCodes.AUTH_SYSTEM_ERROR.message
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, errors = listOf(error)))
    }
} 