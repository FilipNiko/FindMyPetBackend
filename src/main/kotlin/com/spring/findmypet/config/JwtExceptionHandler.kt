package com.spring.findmypet.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.ApiResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class JwtExceptionHandler(private val objectMapper: ObjectMapper) {
    
    private val logger = LoggerFactory.getLogger(JwtExceptionHandler::class.java)
    
    fun handleExpiredToken(response: HttpServletResponse, ex: ExpiredJwtException) {
        logger.debug("Token je istekao: ${ex.message}")
        
        val apiError = ApiError(
            errorCode = "TOKEN_EXPIRED",
            errorDescription = "Token je istekao, potrebno je osvežavanje"
        )
        
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, listOf(apiError))
    }
    
    fun handleJwtException(response: HttpServletResponse, ex: JwtException) {
        logger.error("JWT greška: ${ex.message}")
        
        val apiError = ApiError(
            errorCode = "TOKEN_INVALID",
            errorDescription = "Nevažeći token"
        )
        
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, listOf(apiError))
    }
    
    fun handleGenericException(response: HttpServletResponse, ex: Exception) {
        logger.error("Greška pri obradi tokena", ex)
        
        val apiError = ApiError(
            errorCode = "AUTH_ERROR",
            errorDescription = "Greška pri autentifikaciji"
        )
        
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, listOf(apiError))
    }
    
    private fun sendErrorResponse(
        response: HttpServletResponse, 
        status: HttpStatus, 
        errors: List<ApiError>
    ) {
        val apiResponse = ApiResponse<Any>(
            success = false,
            errors = errors
        )
        
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        
        try {
            val jsonResponse = objectMapper.writeValueAsString(apiResponse)
            response.writer.write(jsonResponse)
        } catch (e: Exception) {
            logger.error("Greška pri serijalizaciji odgovora", e)
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Greška pri obradi odgovora")
        }
    }
} 