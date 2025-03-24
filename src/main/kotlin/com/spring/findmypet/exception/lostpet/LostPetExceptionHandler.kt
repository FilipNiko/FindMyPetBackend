package com.spring.findmypet.exception.lostpet

import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.exception.BaseExceptionHandler
import com.spring.findmypet.exception.ErrorCode
import com.spring.findmypet.exception.ValidationCodes
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

/**
 * Handler za greške vezane za nestale ljubimce
 */
@RestControllerAdvice(basePackages = ["com.spring.findmypet.controller.pet"])
@Order(Ordered.HIGHEST_PRECEDENCE)
class LostPetExceptionHandler : BaseExceptionHandler() {

    /**
     * Obrađuje exception za neispravne zahteve vezane za nestale ljubimce
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = when (ex.message) {
            "TITLE_EMPTY" -> LostPetErrorCodes.TITLE_EMPTY
            "DESCRIPTION_SIZE" -> LostPetErrorCodes.DESCRIPTION_SIZE
            "COLOR_EMPTY" -> LostPetErrorCodes.COLOR_EMPTY
            "GENDER_INVALID" -> LostPetErrorCodes.GENDER_INVALID
            "ADDRESS_EMPTY" -> LostPetErrorCodes.ADDRESS_EMPTY
            "COORDINATES_INVALID" -> LostPetErrorCodes.COORDINATES_INVALID
            "NO_PHOTOS" -> LostPetErrorCodes.NO_PHOTOS
            "TOO_MANY_PHOTOS" -> LostPetErrorCodes.TOO_MANY_PHOTOS
            else -> LostPetErrorCodes.INVALID_REQUEST
        }
        
        logger.error("Neispravan zahtev za nestale ljubimce: ${ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorCode)
    }
    
    /**
     * Obrađuje exception za prevelike fajlove
     */
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Premašena maksimalna veličina upload-a: ${ex.message}")
        return createErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, LostPetErrorCodes.TOO_MANY_PHOTOS)
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
        
        logger.error("Validacione greške za ljubimce: ${errors.map { it.errorCode }}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, errors)
    }
    
    /**
     * Mapira polje i kod validacije na odgovarajući kod greške
     */
    private fun mapFieldToErrorCode(fieldError: FieldError): ErrorCode {
        val fieldName = fieldError.field
        val validationCode = getValidationCode(fieldError)
        
        return when (fieldName) {
            "title" -> mapTitleErrorCode(validationCode)
            "description" -> mapDescriptionErrorCode(validationCode)
            "color" -> mapColorErrorCode(validationCode)
            "gender" -> mapGenderErrorCode(validationCode)
            "address" -> mapAddressErrorCode(validationCode)
            "latitude", "longitude" -> LostPetErrorCodes.COORDINATES_INVALID
            "photos" -> mapPhotosErrorCode(validationCode)
            else -> LostPetErrorCodes.INVALID_REQUEST
        }
    }
    
    /**
     * Mapira kod validacije za naslov na odgovarajući kod greške
     */
    private fun mapTitleErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> LostPetErrorCodes.TITLE_EMPTY
            else -> LostPetErrorCodes.TITLE_EMPTY
        }
    }
    
    /**
     * Mapira kod validacije za opis na odgovarajući kod greške
     */
    private fun mapDescriptionErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.SIZE, ValidationCodes.MIN, ValidationCodes.MAX -> LostPetErrorCodes.DESCRIPTION_SIZE
            else -> LostPetErrorCodes.DESCRIPTION_SIZE
        }
    }
    
    /**
     * Mapira kod validacije za boju na odgovarajući kod greške
     */
    private fun mapColorErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> LostPetErrorCodes.COLOR_EMPTY
            else -> LostPetErrorCodes.COLOR_EMPTY
        }
    }
    
    /**
     * Mapira kod validacije za pol na odgovarajući kod greške
     */
    private fun mapGenderErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.PATTERN -> LostPetErrorCodes.GENDER_INVALID
            else -> LostPetErrorCodes.GENDER_INVALID
        }
    }
    
    /**
     * Mapira kod validacije za adresu na odgovarajući kod greške
     */
    private fun mapAddressErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY, ValidationCodes.NOT_BLANK -> LostPetErrorCodes.ADDRESS_EMPTY
            else -> LostPetErrorCodes.ADDRESS_EMPTY
        }
    }
    
    /**
     * Mapira kod validacije za fotografije na odgovarajući kod greške
     */
    private fun mapPhotosErrorCode(validationCode: String): LostPetErrorCodes {
        return when (validationCode) {
            ValidationCodes.NOT_NULL, ValidationCodes.NOT_EMPTY -> LostPetErrorCodes.NO_PHOTOS
            ValidationCodes.SIZE, ValidationCodes.MAX -> LostPetErrorCodes.TOO_MANY_PHOTOS
            else -> LostPetErrorCodes.NO_PHOTOS
        }
    }
} 