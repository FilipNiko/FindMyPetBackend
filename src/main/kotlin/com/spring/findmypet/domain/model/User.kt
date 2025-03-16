package com.spring.findmypet.domain.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    private var fullName: String,

    @Column(nullable = false, unique = true)
    private var email: String,

    @Column(nullable = false)
    private var phoneNumber: String,

    @Column(nullable = false)
    private var password: String,

    @Enumerated(EnumType.STRING)
    private var role: Role = Role.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    private var tokens: MutableList<Token> = mutableListOf()

) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getFullName(): String = fullName
    fun getPhoneNumber(): String = phoneNumber
    fun getRole(): Role = role

    override fun toString(): String {
        return "User(id=$id, fullName='$fullName', email='$email', phoneNumber='$phoneNumber', role=$role)"
    }
} 