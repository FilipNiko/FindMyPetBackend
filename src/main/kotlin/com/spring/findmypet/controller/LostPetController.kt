package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.service.LostPetService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import com.spring.findmypet.domain.model.User

@RestController
@RequestMapping("/api/v1/lost-pets")
class LostPetController(
    private val lostPetService: LostPetService
) {
    @PostMapping("/report")
    fun reportLostPet(
        @Valid @RequestBody request: ReportLostPetRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ApiResponse<LostPetResponse>> {
        if (request.photos.size > 5) {
            throw IllegalArgumentException("Maksimalan broj fotografija je 5")
        }
        
        val response = lostPetService.reportLostPet(request, user)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }
} 