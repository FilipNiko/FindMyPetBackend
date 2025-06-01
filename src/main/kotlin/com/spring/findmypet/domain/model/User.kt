package com.spring.findmypet.domain.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

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

    @Column(name = "firebase_token")
    private var firebaseToken: String? = null,
    
    @Column(name = "avatar_id")
    private var avatarId: String = "INITIALS",
    
    @Column(name = "receive_notifications")
    private var receiveNotifications: Boolean = false,
    
    @Column(name = "notification_radius")
    private var notificationRadius: Int = 0,
    
    @Column(name = "notification_latitude")
    private var notificationLatitude: Double? = null,
    
    @Column(name = "notification_longitude")
    private var notificationLongitude: Double? = null,
    
    @Column(name = "banned")
    private var banned: Boolean = false,
    
    @Column(name = "ban_reason")
    private var banReason: String? = null,
    
    @Column(name = "banned_at")
    private var bannedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    private var tokens: MutableList<Token> = mutableListOf()

) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = !banned

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getFullName(): String = fullName
    fun getPhoneNumber(): String = phoneNumber
    fun getRole(): Role = role
    fun getFirebaseToken(): String? = firebaseToken
    fun getAvatarId(): String = avatarId
    fun getReceiveNotifications(): Boolean = receiveNotifications
    fun getNotificationRadius(): Int = notificationRadius
    fun getNotificationLatitude(): Double? = notificationLatitude
    fun getNotificationLongitude(): Double? = notificationLongitude
    fun isBanned(): Boolean = banned
    fun getBanReason(): String? = banReason
    fun getBannedAt(): LocalDateTime? = bannedAt
    
    fun setFullName(name: String) {
        this.fullName = name
    }
    
    fun setPassword(newPassword: String) {
        this.password = newPassword
    }
    
    fun setFirebaseToken(token: String?) {
        this.firebaseToken = token
    }
    
    fun setAvatarId(avatar: String) {
        this.avatarId = avatar
    }
    
    fun setReceiveNotifications(receive: Boolean) {
        this.receiveNotifications = receive
    }
    
    fun setNotificationRadius(radius: Int) {
        this.notificationRadius = radius
    }
    
    fun setNotificationLocation(latitude: Double?, longitude: Double?) {
        this.notificationLatitude = latitude
        this.notificationLongitude = longitude
    }
    
    fun setBanStatus(banned: Boolean, reason: String? = null) {
        this.banned = banned
        this.banReason = reason
        this.bannedAt = if (banned) LocalDateTime.now() else null
    }

    override fun toString(): String {
        return "User(id=$id, fullName='$fullName', email='$email', phoneNumber='$phoneNumber', role=$role)"
    }
} 