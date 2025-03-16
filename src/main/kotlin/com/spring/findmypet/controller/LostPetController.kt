package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.service.LostPetService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import com.spring.findmypet.domain.model.User

@RestController
@RequestMapping("/api/v1/lost-pets")
class LostPetController(
    private val lostPetService: LostPetService
) {
    private val logger = LoggerFactory.getLogger(LostPetController::class.java)

    @PostMapping("/report")
    fun reportLostPet(
        @Valid @RequestBody request: ReportLostPetRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<LostPetResponse>> {
        logger.info("Primljen zahtev za prijavu izgubljenog ljubimca: $request")
        logger.info("Autentifikovani korisnik: ${user.username}")
        
        try {
            if (request.photos.size > 5) {
                logger.error("Previše fotografija: ${request.photos.size}")
                throw IllegalArgumentException("Maksimalan broj fotografija je 5")
            }
            
            val response = lostPetService.reportLostPet(request, user)
            logger.info("Uspešno kreirana prijava izgubljenog ljubimca sa ID: ${response.id}")
            return ResponseEntity.ok(ApiResponse(success = true, result = response))
        } catch (e: Exception) {
            logger.error("Greška prilikom prijave izgubljenog ljubimca", e)
            throw e
        }
    }
} 