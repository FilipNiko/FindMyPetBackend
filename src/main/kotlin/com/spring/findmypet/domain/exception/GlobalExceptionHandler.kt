package com.spring.findmypet.domain.exception

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.ApiError
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "VALIDATION_ERROR",
            errorDescription = ex.message ?: "Validaciona greška"
        )
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "EMAIL_ERROR",
            errorDescription = ex.message ?: "Email adresa je već registrovana"
        )
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(InvalidCredentialsException::class, BadCredentialsException::class)
    fun handleCredentialsException(ex: RuntimeException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "LOGIN_INVALID_CREDENTIALS",
            errorDescription = ex.message ?: "Greška pri autentifikaciji"
        )
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "NOT_FOUND_ERROR",
            errorDescription = ex.message ?: "Resurs nije pronađen"
        )
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "TOKEN_ERROR",
            errorDescription = ex.message ?: "Nevažeći token"
        )
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(UserBannedException::class)
    fun handleUserBannedException(ex: UserBannedException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "USER_BANNED",
            errorDescription = ex.message + (ex.reason?.let { ": $it" } ?: "")
        )
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "VALIDATION_ERROR",
            errorDescription = ex.message ?: "Validaciona greška"
        )
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            ApiError(
                errorCode = error.field.uppercase() + "_ERROR",
                errorDescription = error.defaultMessage ?: "Validaciona greška"
            )
        }
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, errors = errors))
    }
    
    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "TOKEN_EXPIRED",
            errorDescription = "Token je istekao, potrebno je osvežavanje"
        )
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Any>> {
        val apiError = ApiError(
            errorCode = "SERVER_ERROR",
            errorDescription = "Došlo je do greške na serveru"
        )
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
} 