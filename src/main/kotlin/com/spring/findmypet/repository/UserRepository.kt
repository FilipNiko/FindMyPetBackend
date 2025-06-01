package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findByEmailContainingIgnoreCase(email: String, pageable: Pageable): Page<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.receiveNotifications = true
        AND u.firebaseToken IS NOT NULL
        AND u.notificationLatitude IS NOT NULL 
        AND u.notificationLongitude IS NOT NULL
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(u.notificationLatitude)) * 
                cos(radians(u.notificationLongitude) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(u.notificationLatitude))
            ) <= u.notificationRadius
        )
    """)
    fun findUsersForPushNotification(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double
    ): List<User>
} 