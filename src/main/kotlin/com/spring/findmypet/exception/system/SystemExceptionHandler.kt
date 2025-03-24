package com.spring.findmypet.exception.system

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.exception.BaseExceptionHandler
import com.spring.findmypet.exception.SystemErrorCodes
import com.spring.findmypet.exception.ValidationCodes
import com.spring.findmypet.exception.ValidationErrorCodes
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

/**
 * Handler za sistemske greške koje nisu obrađene drugim handlerima
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class SystemExceptionHandler : BaseExceptionHandler() {

    /**
     * Obrađuje greške kada nije pronađen odgovarajući handler
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Resurs nije pronađen: {}", ex.requestURL)
        return createErrorResponse(HttpStatus.NOT_FOUND, SystemErrorCodes.RESOURCE_NOT_FOUND)
    }

    /**
     * Obrađuje sve neočekivane greške koje nisu uhvaćene drugim handlerima
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Neočekivana greška", ex)
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SystemErrorCodes.SYSTEM_ERROR)
    }
    
    /**
     * Implementacija apstraktne metode za obradu validacionih grešaka
     * Služi kao fallback u slučaju da specifični handleri ne obrade validaciju
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    override fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val prioritizedErrors = getPrioritizedErrors(ex.bindingResult.fieldErrors)
        val errors = prioritizedErrors.map { fieldError ->
            val errorCode = mapValidationCode(fieldError)
            toApiError(errorCode)
        }
        
        logger.error("Sistemske validacione greške: ${errors.map { it.errorCode }}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errors)
    }
    
    /**
     * Mapira kod validacije na odgovarajući kod greške
     */
    private fun mapValidationCode(fieldError: FieldError): ValidationErrorCodes {
        val validationCode = getValidationCode(fieldError)
        
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> 
                ValidationErrorCodes.FIELD_REQUIRED
                
            ValidationCodes.EMAIL, ValidationCodes.PATTERN -> 
                ValidationErrorCodes.FIELD_INVALID_FORMAT
                
            ValidationCodes.MIN, ValidationCodes.SIZE -> 
                if (fieldError.rejectedValue.toString().length < (fieldError.arguments?.getOrNull(1) as? Int ?: 0))
                    ValidationErrorCodes.FIELD_TOO_SHORT
                else
                    ValidationErrorCodes.FIELD_TOO_LONG
                
            ValidationCodes.MAX -> 
                ValidationErrorCodes.FIELD_TOO_LONG
                
            else -> ValidationErrorCodes.VALIDATION_ERROR
        }
    }
} 