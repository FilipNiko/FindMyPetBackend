package com.spring.findmypet.exception

import com.spring.findmypet.exception.auth.AuthErrorCodes
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

/**
 * Konfiguracija sistema za obradu grešaka
 * Inicijalizuje i registruje sve kodove grešaka
 */
@Configuration
class ErrorMessagesConfig {
    private val logger = LoggerFactory.getLogger(ErrorMessagesConfig::class.java)
    
    @PostConstruct
    fun init() {
        logger.info("Inicijalizacija sistema za obradu grešaka")

        var totalErrorCount = 0
        
        val systemErrorCount = SystemErrorCodes.entries.size
        logger.info("Sistemski kodovi grešaka: {}", systemErrorCount)
        totalErrorCount += systemErrorCount
        
        val validationErrorCount = ValidationErrorCodes.entries.size
        logger.info("Validacioni kodovi grešaka: {}", validationErrorCount)
        totalErrorCount += validationErrorCount
        
        val authErrorCount = AuthErrorCodes.entries.size
        logger.info("Autentifikacioni kodovi grešaka: {}", authErrorCount)
        totalErrorCount += authErrorCount
        
        logger.info("Registrovano ukupno {} kodova grešaka", totalErrorCount)
        logger.info("Sistem za obradu grešaka je uspešno inicijalizovan")
    }
} 