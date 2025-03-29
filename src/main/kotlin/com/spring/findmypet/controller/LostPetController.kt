package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.validation.ValidationService
import com.spring.findmypet.service.LostPetService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/lost-pets")
class LostPetController(
    private val lostPetService: LostPetService,
    private val validationService: ValidationService
) {
    private val logger = LoggerFactory.getLogger(LostPetController::class.java)

    @PostMapping("/report")
    fun reportLostPet(
        @RequestBody request: ReportLostPetRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<LostPetResponse>> {
        logger.info("Primljen zahtev za prijavu izgubljenog ljubimca: $request")
        logger.info("Autentifikovani korisnik: ${user.username}")
        
        try {
            // Validacija
            val validationErrors = validationService.validate(request)
            if (validationErrors.isNotEmpty()) {
                logger.error("Validacione greške za ljubimce: ${validationErrors.map { it.errorCode }}")
                return ResponseEntity.badRequest()
                    .body(ApiResponse(success = false, errors = validationErrors))
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