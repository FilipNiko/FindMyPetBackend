package com.spring.findmypet.exception

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.ApiError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Neočekivana greška", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiResponse(
                    success = false,
                    errors = listOf(
                        ApiError(
                            errorCode = "SYSTEM_ERROR",
                            errorDescription = e.message ?: "Došlo je do sistemske greške"
                        )
                    )
                )
            )
    }
} 