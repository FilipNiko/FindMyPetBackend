package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.Message
import com.spring.findmypet.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import org.springframework.data.domain.Pageable

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    
    /**
     * Pronalazi sve poruke u konverzaciji sortirane po vremenu
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.sentAt ASC")
    fun findAllByConversationOrderBySentAt(@Param("conversation") conversation: Conversation): List<Message>
    
    /**
     * Pronalazi broj nepročitanih poruka u konverzaciji za određenog korisnika
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.sender != :user AND m.isRead = false")
    fun countUnreadMessagesInConversation(
        @Param("conversation") conversation: Conversation,
        @Param("user") user: User
    ): Int
    
    /**
     * Pronalazi poslednju poruku u konverzaciji
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.sentAt DESC")
    fun findTopByConversationOrderBySentAtDesc(@Param("conversation") conversation: Conversation, pageable: Pageable): List<Message>
    
    /**
     * Ažurira status pročitanih poruka u konverzaciji
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE m.conversation = :conversation AND m.sender != :user AND m.isRead = false")
    fun markMessagesAsRead(
        @Param("conversation") conversation: Conversation,
        @Param("user") user: User,
        @Param("readAt") readAt: LocalDateTime
    ): Int
    
    /**
     * Pronalazi ID-eve svih poruka u konverzaciji
     */
    @Query("SELECT m.id FROM Message m WHERE m.conversation = :conversation")
    fun findMessageIdsByConversation(@Param("conversation") conversation: Conversation): List<Long>
} 