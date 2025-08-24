package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.Role
import com.spring.findmypet.domain.model.Token
import com.spring.findmypet.domain.model.TokenType
import com.spring.findmypet.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class TokenRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Test
    fun `should save and find token`() {
        val testUser = User(
            fullName = "Token User",
            email = "token@example.com",
            phoneNumber = "+381601234567",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(testUser)

        val token = Token(
            token = "jwt-token-string-123",
            tokenType = TokenType.BEARER,
            user = testUser
        )

        val savedToken = tokenRepository.save(token)

        assertThat(savedToken.id).isNotNull
        assertThat(savedToken.token).isEqualTo("jwt-token-string-123")
        assertThat(savedToken.tokenType).isEqualTo(TokenType.BEARER)
        assertThat(savedToken.revoked).isFalse()
        assertThat(savedToken.expired).isFalse()
        assertThat(savedToken.user.getFullName()).isEqualTo("Token User")
    }

    @Test
    fun `should find token by token string`() {
        val user = User(
            fullName = "Find Token User",
            email = "findtoken@example.com",
            phoneNumber = "+381601234568",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(user)

        val tokenString = "unique-token-find-123"
        val token = Token(
            token = tokenString,
            user = user
        )
        tokenRepository.save(token)

        val foundToken = tokenRepository.findByToken(tokenString)

        assertThat(foundToken).isPresent
        assertThat(foundToken.get().token).isEqualTo(tokenString)
        assertThat(foundToken.get().user.getFullName()).isEqualTo("Find Token User")
    }

    @Test
    fun `should return empty when token not found`() {
        val foundToken = tokenRepository.findByToken("non-existent-token")

        assertThat(foundToken).isEmpty
    }

    @Test
    fun `should handle token revocation and expiration`() {
        val user = User(
            fullName = "Status User",
            email = "status@example.com",
            phoneNumber = "+381601234569",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(user)

        val activeToken = Token(
            token = "active-token",
            user = user
        )

        val revokedToken = Token(
            token = "revoked-token",
            user = user,
            revoked = true
        )

        val expiredToken = Token(
            token = "expired-token",
            user = user,
            expired = true
        )

        tokenRepository.save(activeToken)
        tokenRepository.save(revokedToken) 
        tokenRepository.save(expiredToken)

        val activeFound = tokenRepository.findByToken("active-token")
        val revokedFound = tokenRepository.findByToken("revoked-token")
        val expiredFound = tokenRepository.findByToken("expired-token")

        assertThat(activeFound.get().revoked).isFalse()
        assertThat(activeFound.get().expired).isFalse()

        assertThat(revokedFound.get().revoked).isTrue()
        assertThat(revokedFound.get().expired).isFalse()

        assertThat(expiredFound.get().revoked).isFalse()
        assertThat(expiredFound.get().expired).isTrue()
    }

    @Test
    fun `should update token status`() {
        val user = User(
            fullName = "Update User",
            email = "update@example.com",
            phoneNumber = "+381601234570",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(user)

        val token = Token(
            token = "update-token",
            user = user
        )
        val savedToken = tokenRepository.save(token)

        assertThat(savedToken.revoked).isFalse()
        assertThat(savedToken.expired).isFalse()

        savedToken.revoked = true
        savedToken.expired = true
        val updatedToken = tokenRepository.save(savedToken)

        assertThat(updatedToken.revoked).isTrue()
        assertThat(updatedToken.expired).isTrue()

        val reloadedToken = tokenRepository.findByToken("update-token")
        assertThat(reloadedToken.get().revoked).isTrue()
        assertThat(reloadedToken.get().expired).isTrue()
    }

    @Test
    fun `should start with empty database and save token successfully`() {
        val allTokens = tokenRepository.findAll()
        assertThat(allTokens).isEmpty()

        // When - add test data
        val user = User(
            fullName = "Isolation User",
            email = "isolation@example.com", 
            phoneNumber = "+381601234571",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(user)

        val token = Token(
            token = "isolation-token",
            user = user
        )
        tokenRepository.save(token)

        val allTokensAfter = tokenRepository.findAll()
        assertThat(allTokensAfter).hasSize(1)
        assertThat(allTokensAfter[0].token).isEqualTo("isolation-token")
        assertThat(allTokensAfter[0].user.getFullName()).isEqualTo("Isolation User")
    }
}