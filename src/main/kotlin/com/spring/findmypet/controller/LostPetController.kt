package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.ApiError
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.domain.dto.UpdateLostPetRequest
import com.spring.findmypet.domain.dto.LostPetEditFormResponse
import com.spring.findmypet.domain.dto.LostPetListRequest
import com.spring.findmypet.domain.dto.LostPetListResponse
import com.spring.findmypet.domain.dto.LostPetListItem
import com.spring.findmypet.domain.dto.LostPetDetailResponse
import com.spring.findmypet.domain.dto.OwnerInfo
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.validation.ValidationService
import com.spring.findmypet.exception.NotFoundException
import com.spring.findmypet.service.LostPetService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.access.AccessDeniedException
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

    @GetMapping("/{id}/owner")
    fun getPetOwner(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: User?
    ): ResponseEntity<ApiResponse<OwnerInfo>> {
        logger.info("Primljen zahtev za dobijanje podataka o vlasniku ljubimca sa ID: $id")
        
        try {
            val lostPet = lostPetService.getLostPetById(id)
            
            val ownerInfo = OwnerInfo(
                id = lostPet.user.id ?: throw IllegalStateException("User ID is null"),
                fullName = lostPet.user.getFullName(),
                email = lostPet.user.getUsername(),
                phoneNumber = lostPet.user.getPhoneNumber(),
                avatarId = lostPet.user.getAvatarId()
            )
            
            logger.info("Uspešno vraćeni podaci o vlasniku ljubimca: ${ownerInfo.fullName}")
            return ResponseEntity.ok(ApiResponse(success = true, result = ownerInfo))
            
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
            logger.error("Greška prilikom dobavljanja podataka o vlasniku ljubimca", e)
            throw e
        }
    }
    
    @GetMapping("/my-pets")
    fun getMyLostPets(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<List<LostPetListItem>>> {
        logger.info("Primljen zahtev za listu izgubljenih ljubimaca korisnika: ${user.username}")
        
        try {
            val result = lostPetService.getUserLostPets(user)
            logger.info("Uspešno vraćena lista sa ${result.size} izgubljenih ljubimaca korisnika")
            return ResponseEntity.ok(ApiResponse(success = true, result = result))
        } catch (e: Exception) {
            logger.error("Greška prilikom dobavljanja liste izgubljenih ljubimaca korisnika", e)
            throw e
        }
    }
    
    @GetMapping("/{id}/edit")
    fun getLostPetForEdit(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ApiResponse<LostPetEditFormResponse>> {
        logger.info("Primljen zahtev za dohvatanje podataka za editovanje nestalog ljubimca sa ID: $id od strane korisnika: ${currentUser.username}")
        
        try {
            val result = lostPetService.getLostPetForEdit(id, currentUser)
            logger.info("Uspešno vraćeni podaci za editovanje nestalog ljubimca sa ID: $id")
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
        } catch (e: AccessDeniedException) {
            logger.error("Korisnik nema dozvolu za editovanje ovog ljubimca", e)
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(
                    success = false, 
                    errors = listOf(ApiError(
                        errorCode = "access_denied",
                        errorDescription = e.message ?: "Nemate dozvolu da editujete ovaj oglas"
                    ))
                ))
        } catch (e: Exception) {
            logger.error("Greška prilikom dohvatanja podataka za editovanje nestalog ljubimca", e)
            throw e
        }
    }

    @PutMapping("/{id}")
    fun updateLostPet(
        @PathVariable id: Long,
        @RequestBody request: UpdateLostPetRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ApiResponse<LostPetResponse>> {
        logger.info("Primljen zahtev za ažuriranje nestalog ljubimca sa ID: $id od strane korisnika: ${currentUser.username}")
        logger.debug("Detalji zahteva za ažuriranje: $request")

        if (id != request.id) {
            logger.error("ID u URL-u ($id) se ne slaže sa ID-om u zahtjevu (${request.id})")
            return ResponseEntity.badRequest()
                .body(ApiResponse(
                    success = false, 
                    errors = listOf(ApiError(
                        errorCode = "id_mismatch",
                        errorDescription = "ID u URL-u se ne slaže sa ID-om u zahtjevu"
                    ))
                ))
        }
        
        try {
            val validationErrors = validationService.validate(request)
            if (validationErrors.isNotEmpty()) {
                logger.error("Validacione greške za ažuriranje ljubimca: ${validationErrors.map { it.errorCode }}")
                return ResponseEntity.badRequest()
                    .body(ApiResponse(success = false, errors = validationErrors))
            }
            
            val response = lostPetService.updateLostPet(request, currentUser)
            logger.info("Uspešno ažuriran nestali ljubimac sa ID: ${response.id}")
            return ResponseEntity.ok(ApiResponse(success = true, result = response))
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
        } catch (e: AccessDeniedException) {
            logger.error("Korisnik nema dozvolu za editovanje ovog ljubimca", e)
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(
                    success = false, 
                    errors = listOf(ApiError(
                        errorCode = "access_denied",
                        errorDescription = e.message ?: "Nemate dozvolu da editujete ovaj oglas"
                    ))
                ))
        } catch (e: Exception) {
            logger.error("Greška prilikom ažuriranja nestalog ljubimca", e)
            throw e
        }
    }

    @DeleteMapping("/{id}")
    fun deleteLostPet(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        logger.info("Primljen zahtev za brisanje prijave nestalog ljubimca sa ID: $id od strane korisnika: ${currentUser.username}")
        
        try {
            lostPetService.softDeleteLostPet(id, currentUser)
            logger.info("Uspešno obrisana prijava nestalog ljubimca sa ID: $id")
            return ResponseEntity.ok(ApiResponse(
                success = true, 
                result = mapOf("message" to "Uspešno obrisana prijava za nestalog ljubimca")
            ))
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
        } catch (e: AccessDeniedException) {
            logger.error("Nedozvoljena operacija: ${e.message}")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(
                    success = false, 
                    errors = listOf(ApiError(
                        errorCode = "access_denied",
                        errorDescription = e.message ?: "Nedozvoljena operacija"
                    ))
                ))
        } catch (e: Exception) {
            logger.error("Greška prilikom brisanja prijave nestalog ljubimca", e)
            throw e
        }
    }
} 