package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.MessageDto
import com.spring.findmypet.domain.dto.MessageRequest
import com.spring.findmypet.domain.dto.PetMessageRequest
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.service.LostPetService
import com.spring.findmypet.service.MessageService
import com.spring.findmypet.service.WebSocketService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * Kontroler za slanje poruka vlasniku kućnog ljubimca
 */
@RestController
@RequestMapping("/api/lost-pets")
class LostPetMessageController(
    private val lostPetService: LostPetService,
    private val messageService: MessageService,
    private val webSocketService: WebSocketService
) {
    private val logger = LoggerFactory.getLogger(LostPetMessageController::class.java)
    
    /**
     * Endpoint za slanje poruke vlasniku kućnog ljubimca
     */
    @PostMapping("/{petId}/messages")
    fun sendMessageToPetOwner(
        @PathVariable petId: Long,
        @RequestBody request: PetMessageRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val userId = (userDetails as User).id!!
        logger.info("Korisnik $userId šalje poruku vlasniku ljubimca $petId")
        
        try {
            // Pronađi ljubimca
            val lostPet = lostPetService.findById(petId)
                .orElseThrow { ResourceNotFoundException("Ljubimac sa ID-om $petId nije pronađen") }
            
            // Kreiraj zahtev za poruku
            val messageRequest = MessageRequest(
                receiverId = lostPet.user.id!!,
                content = request.content
            )
            
            // Pošalji poruku
            val messageDto = messageService.sendMessage(userId, messageRequest)
            
            // Dobavi konverzaciju da bismo znali njen ID
            val conversation = messageService.findConversationBetweenUsers(userId, lostPet.user.id!!)
            
            // Šalji WebSocket notifikacije obema stranama
            webSocketService.sendMessageToBothUsers(
                senderUserId = userId,
                receiverUserId = lostPet.user.id!!,
                messageDto = messageDto,
                conversationId = conversation.id!!
            )
            
            logger.info("Poruka uspešno poslata vlasniku ljubimca $petId")
            return ResponseEntity.ok(ApiResponse(success = true, result = messageDto))
            
        } catch (e: ResourceNotFoundException) {
            logger.warn("Resurs nije pronađen", e)
            val apiError = com.spring.findmypet.domain.dto.ApiError(
                errorCode = "RESOURCE_NOT_FOUND",
                errorDescription = e.message ?: "Resurs nije pronađen"
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
                
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
} 