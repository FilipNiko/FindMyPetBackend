package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.MessageDto
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(WebSocketService::class.java)

    fun sendMessageToBothUsers(
        senderUserId: Long,
        receiverUserId: Long,
        messageDto: MessageDto,
        conversationId: Long
    ) {
        val messageNotification = mapOf(
            "messageData" to messageDto,
            "conversationId" to conversationId
        )

        logger.info("WebSocket: Šaljem notifikaciju primaocu $receiverUserId na destinaciju /user/$receiverUserId/queue/messages")
        messagingTemplate.convertAndSendToUser(
            receiverUserId.toString(),
            "/queue/messages",
            messageNotification
        )

        logger.info("WebSocket: Šaljem notifikaciju pošiljaocu $senderUserId na destinaciju /user/$senderUserId/queue/messages")
        messagingTemplate.convertAndSendToUser(
            senderUserId.toString(),
            "/queue/messages",
            messageNotification
        )

        logger.info("WebSocket: Šaljem notifikaciju na topic konverzacije /topic/conversations/$conversationId")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId",
            messageNotification
        )
    }

    fun sendReadStatusToUser(
        userId: Long,
        conversationId: Long,
        messageIds: List<Long>,
        readByUserId: Long,
        readByUserName: String
    ) {
        val readStatusNotification = mapOf(
            "conversationId" to conversationId,
            "messageIds" to messageIds,
            "readByUserId" to readByUserId,
            "readByUserName" to readByUserName,
            "timestamp" to System.currentTimeMillis()
        )

        logger.info("WebSocket: Šaljem notifikaciju o pročitanim porukama korisniku $userId na destinaciju /user/$userId/queue/read-status")
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/read-status",
            readStatusNotification
        )

        logger.info("WebSocket: Šaljem notifikaciju o pročitanim porukama na topic konverzacije /topic/conversations/$conversationId/read-status")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId/read-status",
            readStatusNotification
        )
    }

    fun sendTypingStatusToUser(
        senderUserId: Long,
        senderUserName: String,
        receiverUserId: Long,
        conversationId: Long,
        isTyping: Boolean
    ) {
        val typingStatusNotification = mapOf(
            "conversationId" to conversationId,
            "senderUserId" to senderUserId,
            "senderUserName" to senderUserName,
            "isTyping" to isTyping,
            "timestamp" to System.currentTimeMillis()
        )

        logger.info("WebSocket: Šaljem notifikaciju o kucanju poruke korisniku $receiverUserId na destinaciju /user/$receiverUserId/queue/typing-status")
        messagingTemplate.convertAndSendToUser(
            receiverUserId.toString(),
            "/queue/typing-status",
            typingStatusNotification
        )

        logger.info("WebSocket: Šaljem notifikaciju o kucanju poruke na topic konverzacije /topic/conversations/$conversationId/typing-status")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId/typing-status",
            typingStatusNotification
        )
    }
} 