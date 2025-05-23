package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.AuthResponse
import com.spring.findmypet.domain.dto.LoginRequest
import com.spring.findmypet.domain.dto.RegisterRequest
import com.spring.findmypet.domain.exception.EmailAlreadyExistsException
import com.spring.findmypet.domain.exception.InvalidCredentialsException
import com.spring.findmypet.domain.exception.InvalidTokenException
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.Token
import com.spring.findmypet.domain.model.TokenType
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.validation.ValidationMessages
import com.spring.findmypet.repository.TokenRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(ValidationMessages.EMAIL_ALREADY_REGISTERED)
        }

        val user = User(
            fullName = request.fullName,
            email = request.email,
            phoneNumber = request.phoneNumber,
            password = passwordEncoder.encode(request.password)
        )
        
        if (!request.firebaseToken.isNullOrBlank()) {
            user.setFirebaseToken(request.firebaseToken)
            logger.info("Postavljen Firebase token za novog korisnika: ${request.email}")
        }

        val savedUser = userRepository.save(user)
        val accessToken = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        saveUserToken(savedUser, accessToken)

        return AuthResponse(
            userId = savedUser.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken,
            fullName = user.getFullName(),
            email = user.username,
            role = user.getRole().name
        )
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    request.email,
                    request.password
                )
            )
        } catch (e: BadCredentialsException) {
            throw InvalidCredentialsException(ValidationMessages.INVALID_CREDENTIALS)
        }

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { ResourceNotFoundException(ValidationMessages.USER_NOT_FOUND) }
        
        if (!request.firebaseToken.isNullOrBlank()) {
            user.setFirebaseToken(request.firebaseToken)
            userRepository.save(user)
            logger.info("Ažuriran Firebase token za korisnika: ${request.email}")
        }
        
        val accessToken = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        
        revokeAllUserTokens(user)
        saveUserToken(user, accessToken)

        return AuthResponse(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken,
            fullName = user.getFullName(),
            email = user.username,
            role = user.getRole().name
        )
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthResponse {
        val username = jwtService.extractUsername(refreshToken)
            ?: throw InvalidTokenException(ValidationMessages.INVALID_TOKEN)
            
        val user = userRepository.findByEmail(username)
            .orElseThrow { InvalidTokenException(ValidationMessages.INVALID_TOKEN) }
            
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw InvalidTokenException(ValidationMessages.INVALID_TOKEN)
        }
            
        val accessToken = jwtService.generateToken(user)
        revokeAllUserTokens(user)
        saveUserToken(user, accessToken)
        
        return AuthResponse(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken,
            fullName = user.getFullName(),
            email = user.username,
            role = user.getRole().name
        )
    }

    private fun saveUserToken(user: User, jwtToken: String) {
        val token = Token(
            token = jwtToken,
            tokenType = TokenType.BEARER,
            user = user
        )
        tokenRepository.save(token)
    }

    private fun revokeAllUserTokens(user: User) {
        val validUserTokens = tokenRepository.findAllValidTokensByUser(user.id!!)
        if (validUserTokens.isEmpty()) return

        validUserTokens.forEach { token ->
            token.expired = true
            token.revoked = true
        }
        tokenRepository.saveAll(validUserTokens)
    }
} 