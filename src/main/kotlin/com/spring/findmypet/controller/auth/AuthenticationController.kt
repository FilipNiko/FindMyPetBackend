package com.spring.findmypet.controller.auth

import com.spring.findmypet.domain.dto.ApiResponse
import com.spring.findmypet.domain.dto.AuthResponse
import com.spring.findmypet.domain.dto.LoginRequest
import com.spring.findmypet.domain.dto.RegisterRequest
import com.spring.findmypet.domain.validation.ValidationService
import com.spring.findmypet.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val authService: AuthenticationService,
    private val validationService: ValidationService
) {
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val validationErrors = validationService.validate(request)
        if (validationErrors.isNotEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse(success = false, errors = validationErrors))
        }
        
        val response = authService.register(request)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val validationErrors = validationService.validate(request)
        if (validationErrors.isNotEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse(success = false, errors = validationErrors))
        }
        
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse(success = true, result = response))
    }
} 