package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.UserRepository
import com.spring.findmypet.service.FileStorageService
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
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageService: MessageService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val webSocketService: WebSocketService,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {
    private val logger = LoggerFactory.getLogger(MessageController::class.java)

    @PostMapping
    fun sendMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody messageRequest: MessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val userId = (userDetails as User).id
        logger.info("Korisnik $userId šalje poruku korisniku ${messageRequest.receiverId}")

        return try {
            val messageDto = processSendMessage(userId!!, messageRequest)
            ResponseEntity.ok(ApiResponse(success = true, result = messageDto))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri slanju poruke")
        }
    }

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

    @GetMapping("/conversations/{conversationId}")
    fun getMessagesFromConversation(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable conversationId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ApiResponse<MessagePageResponse>> {
        val userId = (userDetails as User).id
        logger.info("Korisnik $userId traži poruke iz konverzacije $conversationId (strana $page, veličina $size)")

        return try {
            val messagesPage = messageService.findMessagesInConversationPaginated(userId!!, conversationId, page, size)
            ResponseEntity.ok(ApiResponse(success = true, result = messagesPage))
        } catch (e: ResourceNotFoundException) {
            handleResourceNotFoundException(e, "Konverzacija nije pronađena", userId, conversationId)
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri dobavljanju poruka")
        }
    }

    @PutMapping("/conversations/{conversationId}/read")
    fun markMessagesAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable conversationId: Long
    ): ResponseEntity<ApiResponse<Boolean>> {
        val userId = (userDetails as User).id
        logger.info("Korisnik $userId označava poruke kao pročitane u konverzaciji $conversationId")

        return try {
            val conversation = messageService.markMessagesAsRead(userId!!, conversationId)
            val otherUserId = if (userId == conversation.user1.id) conversation.user2.id!! else conversation.user1.id!!
            val messageIds = messageService.getMessageIdsInConversation(conversationId)

            webSocketService.sendReadStatusToUser(
                userId = otherUserId,
                conversationId = conversationId,
                messageIds = messageIds,
                readByUserId = userId,
                readByUserName = userDetails.getFullName()
            )

            ResponseEntity.ok(ApiResponse(success = true, result = true))
        } catch (e: ResourceNotFoundException) {
            handleResourceNotFoundException(e, "Konverzacija nije pronađena", userId, conversationId)
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri označavanju poruka kao pročitanih")
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
            sendWebSocketMessage(userId, messageRequest.receiverId, messageDto)
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

    @PostMapping("/image")
    fun sendImageMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody messageRequest: MessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId šalje sliku korisniku ${messageRequest.receiverId}")

        val imageRequest = messageRequest.copy(messageType = MessageType.IMAGE)

        return try {
            val messageDto = processSendMessage(userId, imageRequest)
            ResponseEntity.ok(ApiResponse(success = true, result = messageDto))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri slanju poruke sa slikom")
        }
    }

    @PostMapping("/location")
    fun sendLocationMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody messageRequest: MessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId šalje lokaciju korisniku ${messageRequest.receiverId}")

        validateLocationData(messageRequest)?.let { return it }

        val locationRequest = messageRequest.copy(messageType = MessageType.LOCATION)

        return try {
            val messageDto = processSendMessage(userId, locationRequest)
            ResponseEntity.ok(ApiResponse(success = true, result = messageDto))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri slanju poruke sa lokacijom")
        }
    }
    
    private fun validateLocationData(messageRequest: MessageRequest): ResponseEntity<ApiResponse<MessageDto>>? {
        if (messageRequest.latitude == null || messageRequest.longitude == null) {
            val apiError = ApiError(
                errorCode = "INVALID_REQUEST",
                errorDescription = "Koordinate lokacije su obavezne"
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }

        if (messageRequest.address == null) {
            val apiError = ApiError(
                errorCode = "INVALID_REQUEST",
                errorDescription = "Tekstualna adresa lokacije je obavezna"
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
        
        return null
    }

    private fun <T> handleResourceNotFoundException(
        e: ResourceNotFoundException,
        defaultErrorDescription: String,
        userId: Long?,
        resourceId: Long? = null
    ): ResponseEntity<ApiResponse<T>> {
        val logMessage = if (resourceId != null) {
            "Resurs $resourceId nije pronađen za korisnika $userId"
        } else {
            "Resurs nije pronađen za korisnika $userId"
        }
        logger.warn(logMessage, e)
        
        val apiError = ApiError(
            errorCode = "RESOURCE_NOT_FOUND",
            errorDescription = e.message ?: defaultErrorDescription
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(success = false, errors = listOf(apiError)))
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

    private fun processSendMessage(userId: Long, messageRequest: MessageRequest): MessageDto {
        val messageDto = messageService.sendMessage(userId, messageRequest)
        val conversation = messageService.findConversationBetweenUsers(userId, messageRequest.receiverId)

        webSocketService.sendMessageToBothUsers(
            senderUserId = userId,
            receiverUserId = messageRequest.receiverId,
            messageDto = messageDto,
            conversationId = conversation.id!!
        )

        logger.info("Poruka uspešno poslata i notifikacije poslate obema stranama")
        return messageDto
    }
} 