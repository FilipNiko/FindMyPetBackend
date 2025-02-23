package com.spring.findmypet.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "tokens")
data class Token(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val token: String,

    @Enumerated(EnumType.STRING)
    val tokenType: TokenType = TokenType.BEARER,

    var revoked: Boolean = false,

    var expired: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
)

enum class TokenType {
    BEARER
} 