package com.spring.findmypet.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FirebaseConfig {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)
    
    @Value("\${firebase.config-file}")
    private lateinit var firebaseConfigFile: String
    
    @Bean
    fun firebaseApp(): FirebaseApp? {
        try {
            val existingApps = FirebaseApp.getApps()
            if (existingApps.isNotEmpty()) {
                logger.info("Firebase App je već inicijalizovan")
                return existingApps[0]
            }
            
            val resource = ClassPathResource(firebaseConfigFile)
            if (!resource.exists()) {
                logger.warn("Firebase konfiguracija nije pronađena na putanji $firebaseConfigFile. " +
                         "Push notifikacije neće raditi.")
                return null
            }
            
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
                .build()
                
            logger.info("Inicijalizacija Firebase App-a sa konfiguracijom iz $firebaseConfigFile")
            return FirebaseApp.initializeApp(options)
        } catch (e: IOException) {
            logger.error("Greška pri inicijalizaciji Firebase App-a", e)
            return null
        }
    }
} 