package com.spring.findmypet.controller.auth

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.AuthResponse
import com.spring.findmypet.domain.dto.LoginRequest
import com.spring.findmypet.domain.dto.RefreshTokenRequest
import com.spring.findmypet.domain.dto.RegisterRequest
import com.spring.findmypet.service.AuthenticationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val authService: AuthenticationService
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.register(request)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }
    
    @PostMapping("/refresh-token")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }
} 