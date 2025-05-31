package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.UserInfoResponse
import com.spring.findmypet.domain.dto.NotificationSettingsResponse
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
            unreadMessagesCount = unreadCount,
            avatarId = user.getAvatarId(),
            receiveNotifications = user.getReceiveNotifications(),
            notificationRadius = user.getNotificationRadius(),
            notificationLatitude = user.getNotificationLatitude(),
            notificationLongitude = user.getNotificationLongitude()
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
            unreadMessagesCount = unreadCount,
            avatarId = updatedUser.getAvatarId(),
            receiveNotifications = user.getReceiveNotifications(),
            notificationRadius = user.getNotificationRadius(),
            notificationLatitude = user.getNotificationLatitude(),
            notificationLongitude = user.getNotificationLongitude()
        )
    }
    
    @Transactional
    fun updateUserAvatar(userId: Long, avatarId: String): String {
        val user = getUserById(userId)
        
        validateAvatarId(avatarId)
        
        user.setAvatarId(avatarId)
        val updatedUser = userRepository.save(user)
        
        logger.info("Ažuriran avatar korisnika ${user.getUsername()} u $avatarId")
        
        return updatedUser.getAvatarId()
    }
    
    private fun validateAvatarId(avatarId: String) {
        val validAvatarIds = listOf("DOG", "CAT", "RABBIT", "BIRD", "HAMSTER", "TURTLE", "GUINEA_PIG", "LIZARD", "INITIALS")
        
        if (avatarId !in validAvatarIds) {
            throw IllegalArgumentException("Nevažeći ID avatara: $avatarId. Dozvoljene vrednosti su: ${validAvatarIds.joinToString(", ")}")
        }
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
    
    @Transactional
    fun updateNotificationSettings(
        userId: Long, 
        receiveNotifications: Boolean, 
        radius: Int,
        latitude: Double?,
        longitude: Double?
    ): NotificationSettingsResponse {
        val user = getUserById(userId)
        
        user.setReceiveNotifications(receiveNotifications)
        user.setNotificationRadius(radius)
        user.setNotificationLocation(latitude, longitude)
        
        val updatedUser = userRepository.save(user)
        
        logger.info("Ažurirane postavke notifikacija za korisnika ${user.getUsername()}: primanje=${receiveNotifications}, radius=${radius}km")
        
        return NotificationSettingsResponse(
            success = true,
            receiveNotifications = updatedUser.getReceiveNotifications(),
            notificationRadius = updatedUser.getNotificationRadius(),
            latitude = updatedUser.getNotificationLatitude(),
            longitude = updatedUser.getNotificationLongitude()
        )
    }
    
    @Transactional(readOnly = true)
    fun findUsersInRadius(latitude: Double, longitude: Double, lostPetId: Long): List<User> {
        logger.info("Tražim korisnike oko lokacije [$latitude, $longitude] za slanje notifikacije o izgubljenom ljubimcu ID: $lostPetId")
        
        return userRepository.findUsersForPushNotification(
            latitude = latitude,
            longitude = longitude
        )
    }
    
    fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
    }
} 