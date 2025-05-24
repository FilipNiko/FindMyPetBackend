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
import org.springframework.data.domain.Page

@Repository
interface MessageRepository : JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.sentAt DESC")
    fun findAllByConversationOrderBySentAtDesc(
        @Param("conversation") conversation: Conversation,
        pageable: Pageable
    ): Page<Message>

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.sender != :user AND m.isRead = false")
    fun countUnreadMessagesInConversation(
        @Param("conversation") conversation: Conversation,
        @Param("user") user: User
    ): Int

    @Query("SELECT COUNT(m) FROM Message m JOIN m.conversation c WHERE (c.user1 = :user OR c.user2 = :user) AND m.sender != :user AND m.isRead = false")
    fun countTotalUnreadMessagesForUser(@Param("user") user: User): Int

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.sentAt DESC")
    fun findTopByConversationOrderBySentAtDesc(@Param("conversation") conversation: Conversation, pageable: Pageable): List<Message>

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE m.conversation = :conversation AND m.sender != :user AND m.isRead = false")
    fun markMessagesAsRead(
        @Param("conversation") conversation: Conversation,
        @Param("user") user: User,
        @Param("readAt") readAt: LocalDateTime
    ): Int

    @Query("SELECT m.id FROM Message m WHERE m.conversation = :conversation")
    fun findMessageIdsByConversation(@Param("conversation") conversation: Conversation): List<Long>
} 