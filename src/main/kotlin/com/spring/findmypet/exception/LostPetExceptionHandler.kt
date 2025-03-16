package com.spring.findmypet.exception

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.validation.LostPetValidationTags
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.core.annotation.Order
import org.springframework.core.Ordered

@RestControllerAdvice(basePackages = ["com.spring.findmypet.controller"])
@Order(Ordered.HIGHEST_PRECEDENCE)
class LostPetExceptionHandler {

    @ExceptionHandler(InvalidFormatException::class)
    fun handleInvalidFormatException(e: InvalidFormatException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = when {
            e.targetType.isEnum -> {
                val enumValues = e.targetType.enumConstants.joinToString(", ") { it.toString() }
                "Neispravna vrednost '${e.value}'. Dozvoljene vrednosti su: [$enumValues]"
            }
            else -> e.originalMessage
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse(
                    success = false,
                    errors = listOf(
                        ApiError(
                            errorCode = LostPetErrorCodes.INVALID_FORMAT.code,
                            errorDescription = errorMessage
                        )
                    )
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors
            .groupBy { it.field }
            .mapNotNull { (field, fieldErrors) ->
                val error = fieldErrors.firstNotNullOf { error ->
                    when (field) {
                        "title" -> when {
                            error.defaultMessage == LostPetValidationTags.TITLE_EMPTY ->
                                LostPetErrorCodes.TITLE_EMPTY
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "description" -> when {
                            error.defaultMessage == LostPetValidationTags.DESCRIPTION_SIZE ->
                                LostPetErrorCodes.DESCRIPTION_SIZE
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "color" -> when {
                            error.defaultMessage == LostPetValidationTags.COLOR_EMPTY ->
                                LostPetErrorCodes.COLOR_EMPTY
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "gender" -> when {
                            error.defaultMessage == LostPetValidationTags.GENDER_INVALID ->
                                LostPetErrorCodes.GENDER_INVALID
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "address" -> when {
                            error.defaultMessage == LostPetValidationTags.ADDRESS_EMPTY ->
                                LostPetErrorCodes.ADDRESS_EMPTY
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "latitude", "longitude" -> when {
                            error.defaultMessage == LostPetValidationTags.COORDINATES_INVALID ->
                                LostPetErrorCodes.COORDINATES_INVALID
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        "photos" -> when {
                            error.defaultMessage == LostPetValidationTags.NO_PHOTOS ->
                                LostPetErrorCodes.NO_PHOTOS
                            error.defaultMessage == LostPetValidationTags.TOO_MANY_PHOTOS ->
                                LostPetErrorCodes.TOO_MANY_PHOTOS
                            else -> LostPetErrorCodes.INVALID_REQUEST
                        }
                        else -> LostPetErrorCodes.INVALID_REQUEST
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

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val error = ApiError(
            errorCode = LostPetErrorCodes.INVALID_REQUEST.code,
            errorDescription = LostPetErrorCodes.INVALID_REQUEST.message
        )
        
        return ResponseEntity
            .badRequest()
            .body(ApiResponse(success = false, errors = listOf(error)))
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val error = ApiError(
            errorCode = LostPetErrorCodes.SYSTEM_ERROR.code,
            errorDescription = LostPetErrorCodes.SYSTEM_ERROR.message
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, errors = listOf(error)))
    }
} 