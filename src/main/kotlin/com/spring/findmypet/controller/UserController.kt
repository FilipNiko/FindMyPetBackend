package com.spring.findmypet.controller

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.exception.InvalidCredentialsException
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    @GetMapping("/info")
    fun getUserInfo(@AuthenticationPrincipal userDetails: User): ResponseEntity<ApiResponse<UserInfoResponse>> {
        logger.info("Zahtev za informacije o korisniku: ${userDetails.getUsername()}")
        
        return try {
            val userInfo = userService.getUserInfo(userDetails.id!!)
            ResponseEntity.ok(ApiResponse(success = true, result = userInfo))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri dobavljanju informacija o korisniku")
        }
    }
    
    @PutMapping("/name")
    fun updateName(
        @AuthenticationPrincipal userDetails: User,
        @Valid @RequestBody request: UpdateNameRequest
    ): ResponseEntity<ApiResponse<UserInfoResponse>> {
        logger.info("Zahtev za promenu imena korisnika: ${userDetails.getUsername()}")
        
        return try {
            val updatedInfo = userService.updateUserName(userDetails.id!!, request.fullName)
            ResponseEntity.ok(ApiResponse(success = true, result = updatedInfo))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri promeni imena korisnika")
        }
    }
    
    @PutMapping("/avatar")
    fun updateAvatar(
        @AuthenticationPrincipal userDetails: User,
        @Valid @RequestBody request: UpdateAvatarRequest
    ): ResponseEntity<ApiResponse<UpdateAvatarResponse>> {
        logger.info("Zahtev za promenu avatara korisnika: ${userDetails.getUsername()}")
        
        return try {
            val updatedAvatarId = userService.updateUserAvatar(userDetails.id!!, request.avatarId)
            ResponseEntity.ok(ApiResponse(
                success = true, 
                result = UpdateAvatarResponse(success = true, avatarId = updatedAvatarId)
            ))
        } catch (e: IllegalArgumentException) {
            val apiError = ApiError(
                errorCode = "INVALID_AVATAR_ID",
                errorDescription = e.message ?: "Nevažeći ID avatara"
            )
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            handleGenericException(e, "Greška pri promeni avatara korisnika")
        }
    }
    
    @PutMapping("/password")
    fun updatePassword(
        @AuthenticationPrincipal userDetails: User,
        @Valid @RequestBody request: UpdatePasswordRequest
    ): ResponseEntity<ApiResponse<UpdatePasswordResponse>> {
        logger.info("Zahtev za promenu lozinke korisnika: ${userDetails.getUsername()}")
        
        return try {
            val success = userService.updateUserPassword(userDetails.id!!, request.currentPassword, request.newPassword)
            ResponseEntity.ok(ApiResponse(success = true, result = UpdatePasswordResponse(success = success)))
        } catch (e: InvalidCredentialsException) {
            val apiError = ApiError(
                errorCode = "CURRENTPASSWORD_ERROR",
                errorDescription = e.message ?: "Trenutna lozinka nije ispravna"
            )
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        } catch (e: Exception) {
            val apiError = ApiError(
                errorCode = "NEWPASSWORD_ERROR",
                errorDescription = e.message ?: "Neispravna nova lozinka"
            )
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, errors = listOf(apiError)))
        }
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
} 