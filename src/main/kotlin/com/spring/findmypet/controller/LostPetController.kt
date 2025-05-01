package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.domain.dto.LostPetListRequest
import com.spring.findmypet.domain.dto.LostPetListResponse
import com.spring.findmypet.domain.dto.LostPetDetailResponse
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.validation.ValidationService
import com.spring.findmypet.exception.NotFoundException
import com.spring.findmypet.service.LostPetService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

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

    @PostMapping("/list")
    fun getLostPetsList(
        @Valid @RequestBody request: LostPetListRequest
    ): ResponseEntity<ApiResponse<LostPetListResponse>> {
        logger.info("Primljen zahtev za listu nestalih ljubimaca. Lokacija: [${request.latitude}, ${request.longitude}]")
        logger.debug("Parametri zahteva: filter=${request.petFilter}, sort=${request.sortBy}, page=${request.page}, size=${request.size}, radius=${request.radius}km")
        logger.debug("Napredni filteri: breed=${request.breed}, color=${request.color}, gender=${request.gender}, hasChip=${request.hasChip}")
        
        try {
            val result = lostPetService.getLostPetsList(request)
            logger.info("Uspešno vraćena lista sa ${result.content.size} nestalih ljubimaca (strana ${result.page + 1} od ${result.totalPages})")
            return ResponseEntity.ok(ApiResponse(success = true, result = result))
        } catch (e: Exception) {
            logger.error("Greška prilikom dobavljanja liste nestalih ljubimaca", e)
            throw e
        }
    }
    
    @GetMapping("/{id}")
    fun getLostPetDetail(
        @PathVariable id: Long,
        @RequestParam @DecimalMin(value = "-90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
        @DecimalMax(value = "90.0", message = "Vrednost geografske širine mora biti između -90 i 90") 
        latitude: Double,
        @RequestParam @DecimalMin(value = "-180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
        @DecimalMax(value = "180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
        longitude: Double
    ): ResponseEntity<ApiResponse<LostPetDetailResponse>> {
        logger.info("Primljen zahtev za detalje nestalog ljubimca sa ID: $id")
        logger.debug("Pozicija korisnika: [${latitude}, ${longitude}]")
        
        try {
            val result = lostPetService.getLostPetDetail(id, latitude, longitude)
            logger.info("Uspešno vraćeni detalji za nestalog ljubimca: ${result.title}")
            return ResponseEntity.ok(ApiResponse(success = true, result = result))
        } catch (e: NotFoundException) {
            logger.error("Ljubimac nije pronađen", e)
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false, 
                    errors = listOf(ApiError(
                        errorCode = "pet_not_found",
                        errorDescription = e.message ?: "Ljubimac nije pronađen"
                    ))
                ))
        } catch (e: Exception) {
            logger.error("Greška prilikom dobavljanja detalja nestalog ljubimca", e)
            throw e
        }
    }
} 