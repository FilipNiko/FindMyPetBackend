package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.UserRepository
import com.spring.findmypet.service.FileStorageService
import com.spring.findmypet.service.FirebaseMessagingService
import com.spring.findmypet.service.MessageService
import com.spring.findmypet.service.WebSocketService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageService: MessageService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val webSocketService: WebSocketService,
    private val userRepository: UserRepository,
    private val firebaseMessagingService: FirebaseMessagingService,
    private val fileStorageService: FileStorageService
) {
    private val logger = LoggerFactory.getLogger(MessageController::class.java)

    @GetMapping("/conversations")
    fun getConversations(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<ConversationPageResponse>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId traži svoje konverzacije (strana $page, veličina $size)")

        return try {
            val conversationsPage = messageService.findConversationsForUserPaginated(userId, page, size)
            ResponseEntity.ok(ApiResponse(success = true, result = conversationsPage))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri dobavljanju konverzacija")
        }
    }

    @MessageMapping("/chat")
    fun processMessage(@Payload messageRequest: MessageRequest, headerAccessor: SimpMessageHeaderAccessor) {
        logger.info("WEBSOCKET-DEBUG: Primljena nova poruka na /app/chat endpoint: $messageRequest")
        
        try {
            val user = extractUserFromHeaderAccessor(headerAccessor) ?: return
            val userId = user.id!!
            
            logger.info("WebSocket: Korisnik $userId šalje poruku korisniku ${messageRequest.receiverId}")

            val messageDto = messageService.sendMessage(userId, messageRequest)
            val conversation = messageService.findConversationBetweenUsers(userId, messageRequest.receiverId)

            sendWebSocketMessage(userId, messageRequest.receiverId, messageDto)
            sendPushNotification(userId, user, messageRequest.receiverId, messageDto, conversation.id!!)
            
        } catch (e: Exception) {
            logWebSocketError("Greška pri slanju poruke", e)
        }
    }
    
    private fun sendWebSocketMessage(senderUserId: Long, receiverId: Long, messageDto: MessageDto) {
        try {
            val conversationId = messageService.findConversationBetweenUsers(senderUserId, receiverId).id
            val payload = mapOf("messageData" to messageDto, "conversationId" to conversationId)

            sendToUser(receiverId, "/queue/messages", payload, "primaocu")

            sendToUser(senderUserId, "/queue/messages", payload, "pošiljaocu")
            
            logger.info("WebSocket: Poruka uspešno poslata obema stranama")
        } catch (e: Exception) {
            throw RuntimeException("Greška pri slanju WebSocket poruke", e)
        }
    }

    @MessageMapping("/read")
    fun markMessagesAsRead(@Payload conversationId: String, headerAccessor: SimpMessageHeaderAccessor) {
        try {
            val user = extractUserFromHeaderAccessor(headerAccessor) ?: return
            val userId = user.id!!
            
            logger.info("WebSocket: Korisnik $userId označava poruke kao pročitane u konverzaciji $conversationId")
            
            val conversation = messageService.markMessagesAsRead(userId, conversationId.toLong())
            val otherUserId = if (userId == conversation.user1.id) conversation.user2.id else conversation.user1.id

            val readStatusMessage = mapOf(
                "conversationId" to conversationId,
                "readByUserId" to userId
            )
            
            sendToUser(otherUserId!!, "/queue/read-status", readStatusMessage, "za status čitanja")
            logger.info("WebSocket: Poruke uspešno označene kao pročitane")
        } catch (e: Exception) {
            logWebSocketError("Greška pri označavanju poruka kao pročitanih", e)
        }
    }

    @GetMapping("/user/{otherUserId}")
    fun getUserMessages(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable otherUserId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ApiResponse<MessagePageResponse>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId traži poruke sa korisnikom $otherUserId (strana $page, veličina $size)")

        return try {
            val messagesPage = messageService.findMessagesBetweenUsersPaginated(userId, otherUserId, page, size)
            ResponseEntity.ok(ApiResponse(success = true, result = messagesPage))
        } catch (e: ResourceNotFoundException) {
            logger.warn("Konverzacija između korisnika $userId i $otherUserId nije pronađena", e)
            val apiError = ApiError(
                errorCode = "RESOURCE_NOT_FOUND",
                errorDescription = e.message ?: "Konverzacija nije pronađena"
            )
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri dobavljanju poruka")
        }
    }

    @MessageMapping("/typing")
    fun processTypingStatus(@Payload typingRequest: TypingStatusRequest, headerAccessor: SimpMessageHeaderAccessor) {
        logger.info("WEBSOCKET-DEBUG: Primljena informacija o kucanju: $typingRequest")

        try {
            val user = extractUserFromHeaderAccessor(headerAccessor) ?: return
            val userId = user.id!!
            
            logger.info("WebSocket: Korisnik $userId ${if (typingRequest.isTyping) "počinje" else "prestaje"} da kuca poruku korisniku ${typingRequest.receiverId}")

            webSocketService.sendTypingStatusToUser(
                senderUserId = userId,
                senderUserName = user.getFullName(),
                receiverUserId = typingRequest.receiverId,
                conversationId = typingRequest.conversationId,
                isTyping = typingRequest.isTyping
            )

            logger.info("WebSocket: Status kucanja uspešno poslat")
        } catch (e: Exception) {
            logWebSocketError("Greška pri slanju statusa kucanja", e)
        }
    }

    @PostMapping("/upload-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<String>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId otprema sliku za poruku")

        return try {
            val fileUrl = fileStorageService.storeFile(file)
            ResponseEntity.ok(ApiResponse(success = true, result = fileUrl))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri otpremanju slike")
        }
    }

    private fun <T> handleGenericException(
        e: Exception,
        defaultErrorDescription: String
    ): ResponseEntity<ApiResponse<T>> {
        logger.error(defaultErrorDescription, e)
        val apiError = ApiError(
            errorCode = "SERVER_ERROR",
            errorDescription = e.message ?: defaultErrorDescription
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
    }
    
    private fun extractUserFromHeaderAccessor(headerAccessor: SimpMessageHeaderAccessor): User? {
        val authentication = headerAccessor.user as? UsernamePasswordAuthenticationToken
        val user = authentication?.principal as? User
        
        if (user?.id == null) {
            logger.error("WebSocket: Korisnik nije autentifikovan ili je ID null")
            return null
        }
        
        return user
    }
    
    private fun sendToUser(userId: Long, destination: String, payload: Any, recipientDescription: String) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destination,
                payload
            )
            logger.info("WebSocket: Poruka uspešno poslata $recipientDescription $userId")
        } catch (e: Exception) {
            logger.error("WebSocket: Greška pri slanju poruke $recipientDescription $userId", e)
            throw e
        }
    }
    
    private fun logWebSocketError(message: String, e: Exception) {
        logger.error("WebSocket: $message", e)
        logger.error("WebSocket: Detalji greške: ${e.message}")
    }

    private fun sendPushNotification(
        senderId: Long,
        sender: User,
        receiverId: Long,
        messageDto: MessageDto,
        conversationId: Long
    ) {
        try {
            val receiver = userRepository.findById(receiverId).orElseThrow {
                ResourceNotFoundException("Korisnik sa ID-om $receiverId nije pronađen")
            }
            
            val receiverToken = receiver.getFirebaseToken()
            
            if (!receiverToken.isNullOrBlank()) {
                val title = sender.getFullName()
                val body = when (messageDto.messageType) {
                    MessageType.TEXT -> messageDto.content
                    MessageType.IMAGE -> "Poslao/la vam je sliku"
                    MessageType.LOCATION -> "Podelio/la je lokaciju sa vama"
                    else -> "Nova poruka"
                }
                
                val notificationData = mapOf(
                    "conversationId" to conversationId.toString(),
                    "senderId" to senderId.toString(),
                    "senderName" to sender.getFullName(),
                    "messageType" to messageDto.messageType.name
                )
                
                val firebaseMessage = FirebaseMessage(
                    title = title,
                    body = body.take(100),
                    type = NotificationType.NEW_MESSAGE,
                    data = notificationData
                )
                
                firebaseMessagingService.sendNotification(receiverToken, firebaseMessage)
                logger.info("Firebase notifikacija poslata korisniku: ${receiver.getUsername()}")
            } else {
                logger.info("Korisnik nema Firebase token, push notifikacija nije poslata: ${receiver.getUsername()}")
            }
        } catch (e: Exception) {
            logger.error("Greška pri slanju Firebase notifikacije", e)
        }
    }
} 