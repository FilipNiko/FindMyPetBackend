package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.service.FileStorageService
import com.spring.findmypet.service.LostPetService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.spring.findmypet.domain.model.User
import com.fasterxml.jackson.databind.ObjectMapper

@RestController
@RequestMapping("/api/v1/lost-pets")
class LostPetController(
    private val lostPetService: LostPetService,
    private val fileStorageService: FileStorageService,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/report", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun reportLostPet(
        @RequestPart("data") requestJson: String,
        @RequestPart("photos") photos: List<MultipartFile>,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<LostPetResponse>> {
        if (photos.size > 5) {
            throw IllegalArgumentException("Maksimalan broj fotografija je 5")
        }

        val request = objectMapper.readValue(requestJson, ReportLostPetRequest::class.java)

        val photoUrls = photos.map { fileStorageService.storeFile(it) }

        val completeRequest = request.copy(photos = photoUrls)
        
        val response = lostPetService.reportLostPet(completeRequest, user)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }
} 