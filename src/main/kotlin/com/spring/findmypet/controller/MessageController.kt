package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.ConversationDto
import com.spring.findmypet.domain.dto.MessageDto
import com.spring.findmypet.domain.dto.MessageRequest
import com.spring.findmypet.domain.dto.MessageResponseDto
import com.spring.findmypet.domain.dto.TypingStatusRequest
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.UserRepository
import com.spring.findmypet.service.MessageService
import com.spring.findmypet.service.WebSocketService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets

/**
 * Kontroler za REST endpointe za rukovanje porukama
 */
@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageService: MessageService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val webSocketService: WebSocketService,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(MessageController::class.java)

    /**
     * Endpoint za slanje poruke
     * Poruka se šalje putem REST API-ja, ali se notifikacije šalju i preko WebSocketa
     */
    @PostMapping
    fun sendMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody messageRequest: MessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId šalje poruku korisniku ${messageRequest.receiverId}")

        try {
            // Šaljemo poruku preko servisa
            val messageDto = messageService.sendMessage(userId, messageRequest)
            
            // Dobavljamo konverzaciju da bismo znali njen ID
            val conversation = messageService.findConversationBetweenUsers(userId, messageRequest.receiverId)
            
            // Šaljemo WebSocket notifikacije obema stranama
            webSocketService.sendMessageToBothUsers(
                senderUserId = userId,
                receiverUserId = messageRequest.receiverId,
                messageDto = messageDto,
                conversationId = conversation.id!!
            )

            logger.info("Poruka uspešno poslata i notifikacije poslate obema stranama")
            return ResponseEntity.ok(ApiResponse(success = true, result = messageDto))
            
        } catch (e: Exception) {
            logger.error("Greška pri slanju poruke", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "SERVER_ERROR",
                errorDescription = e.message ?: "Greška pri slanju poruke"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
    }
    
    /**
     * Endpoint za dobijanje svih konverzacija korisnika
     */
    @GetMapping("/conversations")
    fun getConversations(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<List<ConversationDto>>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId traži svoje konverzacije")
        
        try {
            val conversations = messageService.findConversationsForUser(userId)
            return ResponseEntity.ok(ApiResponse(success = true, result = conversations))
        } catch (e: Exception) {
            logger.error("Greška pri dobavljanju konverzacija", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "SERVER_ERROR",
                errorDescription = e.message ?: "Greška pri dobavljanju konverzacija"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
    }
    
    /**
     * Endpoint za dobijanje poruka iz određene konverzacije
     */
    @GetMapping("/conversations/{conversationId}")
    fun getMessagesFromConversation(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable conversationId: Long
    ): ResponseEntity<ApiResponse<List<MessageDto>>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId traži poruke iz konverzacije $conversationId")
        
        try {
            val messages = messageService.findMessagesInConversation(userId, conversationId)
            return ResponseEntity.ok(ApiResponse(success = true, result = messages))
        } catch (e: ResourceNotFoundException) {
            logger.warn("Konverzacija $conversationId nije pronađena za korisnika $userId", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "RESOURCE_NOT_FOUND",
                errorDescription = e.message ?: "Konverzacija nije pronađena"
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            logger.error("Greška pri dobavljanju poruka", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "SERVER_ERROR",
                errorDescription = e.message ?: "Greška pri dobavljanju poruka"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
    }
    
    /**
     * Endpoint za označavanje svih poruka u konverzaciji kao pročitanih
     */
    @PutMapping("/conversations/{conversationId}/read")
    fun markMessagesAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable conversationId: Long
    ): ResponseEntity<ApiResponse<Boolean>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId označava poruke kao pročitane u konverzaciji $conversationId")
        
        try {
            // Označi poruke kao pročitane i dobij konverzaciju
            val conversation = messageService.markMessagesAsRead(userId, conversationId)
            
            // Identifikuj ID drugog korisnika u konverzaciji
            val otherUserId = if (userId == conversation.user1.id) conversation.user2.id!! else conversation.user1.id!!
            
            // Dobij ID-eve poruka koje su označene kao pročitane
            val messageIds = messageService.getMessageIdsInConversation(conversationId)
            
            // Pošalji WebSocket obaveštenje drugom korisniku
            webSocketService.sendReadStatusToUser(
                userId = otherUserId,
                conversationId = conversationId,
                messageIds = messageIds,
                readByUserId = userId,
                readByUserName = userDetails.getFullName()
            )
            
            return ResponseEntity.ok(ApiResponse(success = true, result = true))
        } catch (e: ResourceNotFoundException) {
            logger.warn("Konverzacija $conversationId nije pronađena za korisnika $userId", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "RESOURCE_NOT_FOUND",
                errorDescription = e.message ?: "Konverzacija nije pronađena"
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            logger.error("Greška pri označavanju poruka kao pročitanih", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "SERVER_ERROR",
                errorDescription = e.message ?: "Greška pri označavanju poruka kao pročitanih"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
    }

    @MessageMapping("/chat")
    fun processMessage(@Payload messageRequest: MessageRequest, headerAccessor: SimpMessageHeaderAccessor) {
        logger.info("WEBSOCKET-DEBUG: Primljena nova poruka na /app/chat endpoint")
        logger.info("WEBSOCKET-DEBUG: Sadržaj poruke: $messageRequest")
        logger.info("WEBSOCKET-DEBUG: SessionId: ${headerAccessor.sessionId}, SessionAttributes: ${headerAccessor.sessionAttributes}")
        
        try {
            // Dobavljanje korisničkog ID-a iz autentifikacije
            val authentication = headerAccessor.user as? UsernamePasswordAuthenticationToken
            logger.info("WEBSOCKET-DEBUG: Authentication objekat: $authentication")
            
            val user = authentication?.principal as? User
            logger.info("WEBSOCKET-DEBUG: User objekat: ${user}, isNull: ${user == null}")
            
            if (user == null || user.id == null) {
                logger.error("WebSocket: Korisnik nije autentifikovan ili je ID null")
                return
            }
            
            val userId = user.id!!
            logger.info("WebSocket: Korisnik $userId šalje poruku korisniku ${messageRequest.receiverId}")
        
            val messageDto = messageService.sendMessage(userId, messageRequest)
            logger.info("WEBSOCKET-DEBUG: Poruka uspešno sačuvana u bazi, ID: ${messageDto.id}")
            
            // Kreiranje poruke koja će biti poslata
            val messagePayload = mapOf(
                "messageData" to messageDto, 
                "conversationId" to messageService.findConversationBetweenUsers(userId, messageRequest.receiverId).id
            )
            logger.info("WEBSOCKET-DEBUG: Pripremljen payload za slanje: $messagePayload")
            
            // Detaljno logovanje pre slanja notifikacije
            logger.info("WebSocket: Šaljem notifikaciju primaocu ${messageRequest.receiverId} na destinaciju /user/${messageRequest.receiverId}/queue/messages")
            
            // Pošalji notifikaciju primaocu poruke
            try {
                messagingTemplate.convertAndSendToUser(
                    messageRequest.receiverId.toString(),
                    "/queue/messages",
                    messagePayload
                )
                logger.info("WEBSOCKET-DEBUG: Poruka uspešno poslata primaocu ${messageRequest.receiverId}")
            } catch (e: Exception) {
                logger.error("WEBSOCKET-DEBUG: Greška pri slanju poruke primaocu", e)
            }
            
            // Detaljno logovanje pre slanja notifikacije pošiljaocu
            logger.info("WebSocket: Šaljem notifikaciju pošiljaocu $userId na destinaciju /user/$userId/queue/messages")
            
            // Pošalji notifikaciju i pošiljaocu (da vidi svoju poruku)
            try {
                messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/messages",
                    messagePayload
                )
                logger.info("WEBSOCKET-DEBUG: Poruka uspešno poslata pošiljaocu $userId")
            } catch (e: Exception) {
                logger.error("WEBSOCKET-DEBUG: Greška pri slanju poruke pošiljaocu", e)
            }
            
            logger.info("WebSocket: Poruka uspešno poslata obema stranama")
        } catch (e: Exception) {
            logger.error("WebSocket: Greška pri slanju poruke", e)
            logger.error("WEBSOCKET-DEBUG: Detalji greške: ${e.message}")
            logger.error("WEBSOCKET-DEBUG: Stack trace: ", e)
        }
    }
    
    @MessageMapping("/read")
    fun markMessagesAsReadSocket(@Payload payload: Any, headerAccessor: SimpMessageHeaderAccessor) {
        try {
            // Dobavljanje korisničkog ID-a iz autentifikacije
            val authentication = headerAccessor.user as? UsernamePasswordAuthenticationToken
            val user = authentication?.principal as? User
            
            if (user == null || user.id == null) {
                logger.error("WebSocket: Korisnik nije autentifikovan ili je ID null")
                return
            }
            
            val userId = user.id!!
            
            // Ispisivanje stvarnog tipa i sadržaja payload-a za dijagnostiku
            logger.info("WebSocket: Tip payload-a je ${payload.javaClass.name}, vrednost: $payload")
            
            // Konvertujemo payload u Long, sa posebnim rukovanjem za byte[]
            val conversationId = when (payload) {
                is Long -> payload
                is String -> payload.toLongOrNull()
                is Number -> payload.toLong()
                is ByteArray -> {
                    // Konvertovanje ByteArray u String, pa u Long
                    val payloadStr = String(payload, StandardCharsets.UTF_8)
                    logger.info("WebSocket: Konvertovan ByteArray u String: '$payloadStr'")
                    payloadStr.toLongOrNull()
                }
                else -> {
                    // Pokušajmo konvertovati pozivom toString
                    try {
                        payload.toString().toLongOrNull()
                    } catch (e: Exception) {
                        logger.error("WebSocket: Neispravan format za conversationId: $payload")
                        null
                    }
                }
            }
            
            if (conversationId == null) {
                logger.error("WebSocket: Nije moguće konvertovati '$payload' u conversationId")
                return
            }
            
            logger.info("WebSocket: Korisnik $userId označava poruke kao pročitane u konverzaciji $conversationId")
        
            // Označi poruke kao pročitane
            val conversation = messageService.markMessagesAsRead(userId, conversationId)
            
            // Pošalji obaveštenje da su poruke pročitane drugom korisniku
            val otherUserId = if (userId == conversation.user1.id) conversation.user2.id else conversation.user1.id
            
            // Detaljno logovanje pre slanja notifikacije o pročitanom statusu
            logger.info("WebSocket: Šaljem notifikaciju o pročitanim porukama korisniku $otherUserId na destinaciju /user/$otherUserId/queue/read-status")
            
            // Logovanje celokupne poruke koja se šalje
            val readStatusMessage = mapOf(
                "conversationId" to conversationId,
                "readByUserId" to userId
            )
            logger.info("WebSocket: Poruka o pročitanom statusu: $readStatusMessage")
            
            messagingTemplate.convertAndSendToUser(
                otherUserId.toString(),
                "/queue/read-status",
                readStatusMessage
            )
            
            logger.info("WebSocket: Poruke uspešno označene kao pročitane")
        } catch (e: Exception) {
            logger.error("WebSocket: Greška pri označavanju poruka kao pročitanih", e)
        }
    }

    /**
     * Endpoint za dobijanje poruka sa drugim korisnikom
     * Koristi ID drugog korisnika umesto ID-a konverzacije
     * Vraća objekat koji sadrži informacije o konverzaciji i listu poruka
     */
    @GetMapping("/user/{otherUserId}")
    fun getMessagesWithUser(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable otherUserId: Long
    ): ResponseEntity<ApiResponse<MessageResponseDto>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId traži poruke sa korisnikom $otherUserId")
        
        try {
            // Pronađi korisnika sa kojim komuniciramo
            val otherUser = userRepository.findById(otherUserId)
                .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $otherUserId nije pronađen") }
            
            // Pronađi konverzaciju između trenutnog korisnika i drugog korisnika
            val conversation = messageService.findConversationBetweenUsers(userId, otherUserId)
            
            // Dobavi poruke iz te konverzacije
            val messages = messageService.findMessagesInConversation(userId, conversation.id!!)
            
            // Kreiraj objekat sa svim potrebnim informacijama
            val responseDto = MessageResponseDto(
                conversationId = conversation.id!!,
                otherUserName = otherUser.getFullName(),
                otherUserPhone = otherUser.getPhoneNumber(),
                messages = messages
            )
            
            logger.info("Vraćam MessageResponseDto objekat sa ${messages.size} poruka")
            
            return ResponseEntity.ok(ApiResponse(success = true, result = responseDto))
        } catch (e: ResourceNotFoundException) {
            logger.warn("Konverzacija između korisnika $userId i $otherUserId nije pronađena", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "RESOURCE_NOT_FOUND",
                errorDescription = e.message ?: "Konverzacija nije pronađena"
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            logger.error("Greška pri dobavljanju poruka", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "SERVER_ERROR",
                errorDescription = e.message ?: "Greška pri dobavljanju poruka"
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
    }

    /**
     * WebSocket endpoint za praćenje kada korisnik kuca poruku
     */
    @MessageMapping("/typing")
    fun processTypingStatus(@Payload typingRequest: TypingStatusRequest, headerAccessor: SimpMessageHeaderAccessor) {
        logger.info("WEBSOCKET-DEBUG: Primljena informacija o kucanju na /app/typing endpoint")
        logger.info("WEBSOCKET-DEBUG: Sadržaj zahteva: $typingRequest")
        
        try {
            // Dobavljanje korisničkog ID-a iz autentifikacije
            val authentication = headerAccessor.user as? UsernamePasswordAuthenticationToken
            val user = authentication?.principal as? User
            
            if (user == null || user.id == null) {
                logger.error("WebSocket: Korisnik nije autentifikovan ili je ID null")
                return
            }
            
            val userId = user.id!!
            logger.info("WebSocket: Korisnik $userId ${if(typingRequest.isTyping) "počinje" else "prestaje"} da kuca poruku korisniku ${typingRequest.receiverId}")
            
            // Pošalji WebSocket notifikaciju o statusu kucanja
            webSocketService.sendTypingStatusToUser(
                senderUserId = userId,
                senderUserName = user.getFullName(),
                receiverUserId = typingRequest.receiverId,
                conversationId = typingRequest.conversationId,
                isTyping = typingRequest.isTyping
            )
            
            logger.info("WebSocket: Status kucanja uspešno poslat")
        } catch (e: Exception) {
            logger.error("WebSocket: Greška pri slanju statusa kucanja", e)
            logger.error("WEBSOCKET-DEBUG: Detalji greške: ${e.message}")
            logger.error("WEBSOCKET-DEBUG: Stack trace: ", e)
        }
    }
} 