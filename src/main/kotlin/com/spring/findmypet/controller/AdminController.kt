package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.UserListItemDto
import com.spring.findmypet.domain.dto.BanUserRequest
import com.spring.findmypet.domain.dto.LostPetListItem
import com.spring.findmypet.domain.dto.AdminStatisticsDto
import com.spring.findmypet.exception.NotFoundException
import com.spring.findmypet.service.AdminService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService
) {
    private val logger = LoggerFactory.getLogger(AdminController::class.java)
    
    @GetMapping("/statistics")
    fun getStatistics(): ResponseEntity<ApiResponse<AdminStatisticsDto>> {
        logger.info("Primljen zahtev za admin statistike")
        val statistics = adminService.getStatistics()
        return ResponseEntity.ok(ApiResponse(success = true, result = statistics))
    }
    
    @GetMapping("/users")
    fun getAllUsers(pageable: Pageable): ResponseEntity<ApiResponse<Page<UserListItemDto>>> {
        logger.info("Primljen zahtev za listu svih korisnika, strana: ${pageable.pageNumber}, veličina: ${pageable.pageSize}")
        val users = adminService.getAllUsers(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, result = users))
    }
    
    @GetMapping("/users/search")
    fun searchUsersByEmail(
        @RequestParam email: String,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<UserListItemDto>>> {
        logger.info("Primljen zahtev za pretragu korisnika po emailu: $email")
        val users = adminService.searchUsersByEmail(email, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, result = users))
    }
    
    @GetMapping("/users/{userId}/lost-pets")
    fun getUserLostPets(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LostPetListItem>>> {
        logger.info("Primljen zahtev za listu prijavljenih ljubimaca korisnika sa ID: $userId")
        try {
            val lostPets = adminService.getUserLostPets(userId, pageable)
            return ResponseEntity.ok(ApiResponse(success = true, result = lostPets))
        } catch (e: NotFoundException) {
            logger.error("Korisnik nije pronađen", e)
            return ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/users/{userId}/ban")
    fun banUser(
        @PathVariable userId: Long,
        @RequestBody request: BanUserRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        logger.info("Primljen zahtev za banovanje korisnika sa ID: $userId, razlog: ${request.reason}")
        try {
            val result = adminService.banUser(userId, request.banned, request.reason)
            return ResponseEntity.ok(ApiResponse(
                success = true,
                result = mapOf(
                    "message" to if (request.banned) "Korisnik je uspešno banovan" else "Ban je uspešno uklonjen sa korisnika",
                    "userId" to userId,
                    "banned" to request.banned,
                    "banReason" to (request.reason ?: ""),
                    "deletedPetsCount" to result.deletedPetsCount
                )
            ))
        } catch (e: NotFoundException) {
            logger.error("Korisnik nije pronađen", e)
            return ResponseEntity.notFound().build()
        }
    }
}
