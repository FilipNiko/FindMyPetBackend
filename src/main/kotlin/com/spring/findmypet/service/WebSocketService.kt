package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.MessageDto
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

/**
 * Servis za slanje poruka korisnicima preko WebSocket-a
 */
@Service
class WebSocketService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(WebSocketService::class.java)

    /**
     * Šalje poruku obema stranama u konverzaciji
     *
     * @param senderUserId ID pošiljaoca poruke
     * @param receiverUserId ID primaoca poruke
     * @param messageDto Podaci o poruci
     * @param conversationId ID konverzacije
     */
    fun sendMessageToBothUsers(
        senderUserId: Long,
        receiverUserId: Long,
        messageDto: MessageDto,
        conversationId: Long
    ) {
        // Konstruišemo objekat notifikacije koji sadrži i ID konverzacije
        val messageNotification = mapOf(
            "messageData" to messageDto,
            "conversationId" to conversationId
        )

        // Šaljemo notifikaciju primaocu poruke
        logger.info("WebSocket: Šaljem notifikaciju primaocu $receiverUserId na destinaciju /user/$receiverUserId/queue/messages")
        messagingTemplate.convertAndSendToUser(
            receiverUserId.toString(),
            "/queue/messages",
            messageNotification
        )

        // Šaljemo notifikaciju i pošiljaocu (da vidi svoju poruku)
        logger.info("WebSocket: Šaljem notifikaciju pošiljaocu $senderUserId na destinaciju /user/$senderUserId/queue/messages")
        messagingTemplate.convertAndSendToUser(
            senderUserId.toString(),
            "/queue/messages",
            messageNotification
        )

        // Takođe šaljemo notifikaciju na topic konverzacije za real-time ažuriranje
        logger.info("WebSocket: Šaljem notifikaciju na topic konverzacije /topic/conversations/$conversationId")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId",
            messageNotification
        )
    }

    /**
     * Šalje notifikaciju o statusu pročitane poruke drugom korisniku
     *
     * @param userId ID korisnika kome se šalje notifikacija 
     * @param conversationId ID konverzacije
     * @param messageIds Lista ID-eva poruka koje su označene kao pročitane
     * @param readByUserId ID korisnika koji je pročitao poruke
     * @param readByUserName Korisničko ime korisnika koji je pročitao poruke
     */
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

        // Šaljemo notifikaciju o pročitanim porukama korisniku
        logger.info("WebSocket: Šaljem notifikaciju o pročitanim porukama korisniku $userId na destinaciju /user/$userId/queue/read-status")
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/read-status",
            readStatusNotification
        )

        // Takođe šaljemo notifikaciju na topic konverzacije za real-time ažuriranje
        logger.info("WebSocket: Šaljem notifikaciju o pročitanim porukama na topic konverzacije /topic/conversations/$conversationId/read-status")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId/read-status",
            readStatusNotification
        )
    }

    /**
     * Šalje notifikaciju o tome da korisnik trenutno kuca poruku
     *
     * @param senderUserId ID korisnika koji kuca poruku
     * @param senderUserName Korisničko ime korisnika koji kuca poruku
     * @param receiverUserId ID korisnika kome se šalje notifikacija
     * @param conversationId ID konverzacije
     * @param isTyping da li korisnik kuca ili je prestao da kuca
     */
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

        // Šaljemo notifikaciju samo primaocu poruke
        logger.info("WebSocket: Šaljem notifikaciju o kucanju poruke korisniku $receiverUserId na destinaciju /user/$receiverUserId/queue/typing-status")
        messagingTemplate.convertAndSendToUser(
            receiverUserId.toString(),
            "/queue/typing-status",
            typingStatusNotification
        )

        // Takođe šaljemo notifikaciju na topic konverzacije za real-time ažuriranje
        logger.info("WebSocket: Šaljem notifikaciju o kucanju poruke na topic konverzacije /topic/conversations/$conversationId/typing-status")
        messagingTemplate.convertAndSend(
            "/topic/conversations/$conversationId/typing-status",
            typingStatusNotification
        )
    }
} 