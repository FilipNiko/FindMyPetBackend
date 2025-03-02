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
        val errors = ex.bindingResult.fieldErrors
            .groupBy { it.field }  // Grupišemo po polju
            .mapNotNull { (field, fieldErrors) ->
                // Za svako polje uzimamo samo prvi error po prioritetu
                val error = fieldErrors.firstNotNullOf { error ->
                    when (field) {
                        "fullName" -> when {
                            error.defaultMessage == ValidationMessages.FIELD_REQUIRED -> 
                                AuthErrorCodes.FULLNAME_FIELD_REQUIRED
                            else -> AuthErrorCodes.FULLNAME_FIELD_INVALID_FORMAT
                        }
                        "email" -> when {
                            error.defaultMessage == ValidationMessages.FIELD_REQUIRED -> 
                                AuthErrorCodes.EMAIL_FIELD_REQUIRED
                            else -> AuthErrorCodes.EMAIL_FIELD_INVALID_FORMAT
                        }
                        "phoneNumber" -> when {
                            error.defaultMessage == ValidationMessages.FIELD_REQUIRED -> 
                                AuthErrorCodes.PHONE_FIELD_REQUIRED
                            else -> AuthErrorCodes.PHONE_FIELD_INVALID_FORMAT
                        }
                        "password" -> when {
                            error.defaultMessage == ValidationMessages.FIELD_REQUIRED -> 
                                AuthErrorCodes.PASSWORD_FIELD_REQUIRED
                            error.defaultMessage == ValidationMessages.FIELD_TOO_SHORT -> 
                                AuthErrorCodes.PASSWORD_FIELD_TOO_SHORT
                            error.defaultMessage == ValidationMessages.PASSWORD_MISSING_UPPERCASE -> 
                                AuthErrorCodes.PASSWORD_FIELD_MISSING_UPPERCASE
                            error.defaultMessage == ValidationMessages.PASSWORD_MISSING_NUMBER -> 
                                AuthErrorCodes.PASSWORD_FIELD_MISSING_NUMBER
                            else -> AuthErrorCodes.PASSWORD_FIELD_MISSING_SPECIAL_CHAR
                        }
                        else -> null
                    }
                }

                ApiError(
                    errorCode = error.code,
                    errorDescription = error.message
                )
            }

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