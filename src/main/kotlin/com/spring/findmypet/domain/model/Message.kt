package com.spring.findmypet.domain.model

import com.spring.findmypet.domain.dto.MessageType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    val conversation: Conversation,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,
    
    @Column(nullable = false)
    val content: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    val messageType: MessageType = MessageType.TEXT,
    
    @Column(name = "latitude")
    val latitude: Double? = null,
    
    @Column(name = "longitude")
    val longitude: Double? = null,
    
    @Column(name = "address", length = 512)
    val address: String? = null,
    
    @Column(name = "sent_at", nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,
    
    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,
    
    @Column(name = "timestamp", nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
) 