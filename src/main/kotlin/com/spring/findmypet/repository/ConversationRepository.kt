package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

@Repository
interface ConversationRepository : JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    fun findConversationBetweenUsers(
        @Param("user1") user1: User,
        @Param("user2") user2: User
    ): Optional<Conversation>

    @Query("SELECT c FROM Conversation c WHERE c.user1 = :user OR c.user2 = :user ORDER BY c.updatedAt DESC")
    fun findAllConversationsForUser(
        @Param("user") user: User,
        pageable: Pageable
    ): Page<Conversation>
} 