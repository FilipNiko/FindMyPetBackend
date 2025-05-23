package com.spring.findmypet.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.spring.findmypet.domain.dto.FirebaseMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FirebaseMessagingService {
    private val logger = LoggerFactory.getLogger(FirebaseMessagingService::class.java)

    fun sendNotification(token: String?, firebaseMessage: FirebaseMessage) {
        if (token.isNullOrBlank()) {
            logger.warn("Firebase token je null ili prazan, notifikacija nije poslata")
            return
        }

        try {
            val message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(firebaseMessage.title)
                        .setBody(firebaseMessage.body)
                        .build()
                )
                .putData("type", firebaseMessage.type.name)
                .also { builder ->
                    firebaseMessage.data.forEach { (key, value) ->
                        builder.putData(key, value)
                    }
                }
                .build()

            val response = FirebaseMessaging.getInstance().send(message)
            logger.info("Firebase notifikacija uspešno poslata: $response")
        } catch (e: Exception) {
            logger.error("Greška pri slanju Firebase notifikacije", e)
        }
    }
} 