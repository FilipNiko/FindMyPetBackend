package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.UserInfoResponse
import com.spring.findmypet.domain.exception.InvalidCredentialsException
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.MessageRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    @Transactional(readOnly = true)
    fun getUserInfo(userId: Long): UserInfoResponse {
        val user = getUserById(userId)
        val unreadCount = messageRepository.countTotalUnreadMessagesForUser(user)
        
        logger.info("Dobavljene informacije o korisniku: ${user.getUsername()}, broj nepročitanih poruka: $unreadCount")
        
        return UserInfoResponse(
            fullName = user.getFullName(),
            email = user.getUsername(),
            unreadMessagesCount = unreadCount
        )
    }
    
    @Transactional
    fun updateUserName(userId: Long, newName: String): UserInfoResponse {
        val user = getUserById(userId)
        
        user.setFullName(newName)
        val updatedUser = userRepository.save(user)
        
        logger.info("Ažurirano ime korisnika ${user.getUsername()} u $newName")
        
        val unreadCount = messageRepository.countTotalUnreadMessagesForUser(updatedUser)
        
        return UserInfoResponse(
            fullName = updatedUser.getFullName(),
            email = updatedUser.getUsername(),
            unreadMessagesCount = unreadCount
        )
    }
    
    @Transactional
    fun updateUserPassword(userId: Long, currentPassword: String, newPassword: String): Boolean {
        val user = getUserById(userId)

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Pokušaj promene lozinke sa pogrešnom trenutnom lozinkom za korisnika: ${user.getUsername()}")
            throw InvalidCredentialsException("Trenutna lozinka nije ispravna")
        }

        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        
        logger.info("Uspešno promenjena lozinka za korisnika: ${user.getUsername()}")
        return true
    }
    
    fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
    }
} 