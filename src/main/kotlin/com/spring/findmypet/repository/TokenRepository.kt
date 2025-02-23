package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface TokenRepository : JpaRepository<Token, Long> {
    @Query("""
        select t from Token t inner join User u 
        on t.user.id = u.id 
        where u.id = :userId and (t.expired = false or t.revoked = false)
    """)
    fun findAllValidTokensByUser(userId: Long): List<Token>

    fun findByToken(token: String): Optional<Token>
} 