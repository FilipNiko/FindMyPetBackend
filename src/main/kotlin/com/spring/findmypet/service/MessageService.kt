package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.Message
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.ConversationRepository
import com.spring.findmypet.repository.MessageRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(MessageService::class.java)

    @Transactional
    fun sendMessage(senderUserId: Long, messageRequest: MessageRequest): MessageDto {
        logger.info("Slanje poruke od korisnika $senderUserId korisniku ${messageRequest.receiverId}")

        val sender = userRepository.findById(senderUserId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $senderUserId nije pronađen") }
        
        val receiver = userRepository.findById(messageRequest.receiverId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om ${messageRequest.receiverId} nije pronađen") }

        val conversation = findOrCreateConversation(sender, receiver)

        val message = Message(
            conversation = conversation,
            sender = sender,
            content = messageRequest.content,
            messageType = messageRequest.messageType,
            latitude = messageRequest.latitude,
            longitude = messageRequest.longitude,
            address = messageRequest.address
        )

        val savedMessage = messageRepository.save(message)

        val currentTime = LocalDateTime.now()
        conversation.updatedAt = currentTime
        conversation.lastMessageTime = currentTime
        conversation.lastMessageType = messageRequest.messageType
        conversationRepository.save(conversation)

        return MessageDto(
            id = savedMessage.id,
            senderId = sender.id!!,
            senderName = getUserFullName(sender),
            content = savedMessage.content,
            messageType = savedMessage.messageType,
            latitude = savedMessage.latitude,
            longitude = savedMessage.longitude,
            address = savedMessage.address,
            sentAt = savedMessage.sentAt,
            isRead = savedMessage.isRead,
            readAt = savedMessage.readAt
        )
    }

    @Transactional
    fun findOrCreateConversation(user1: User, user2: User): Conversation {
        val existingConversation = conversationRepository.findConversationBetweenUsers(user1, user2)
        
        if (existingConversation.isPresent) {
            return existingConversation.get()
        }

        val now = LocalDateTime.now()
        val newConversation = Conversation(
            user1 = user1,
            user2 = user2,
            createdAt = now,
            updatedAt = now,
            lastMessageTime = now
        )
        
        return conversationRepository.save(newConversation)
    }

    @Transactional(readOnly = true)
    fun findConversationBetweenUsers(user1Id: Long, user2Id: Long): Conversation {
        val user1 = userRepository.findById(user1Id)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $user1Id nije pronađen") }
        
        val user2 = userRepository.findById(user2Id)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $user2Id nije pronađen") }
        
        return conversationRepository.findConversationBetweenUsers(user1, user2)
            .orElseThrow { ResourceNotFoundException("Konverzacija između korisnika $user1Id i $user2Id nije pronađena") }
    }

    @Transactional
    fun markMessagesAsRead(userId: Long, conversationId: Long): Conversation {
        val (user, conversation) = validateUserAndConversation(userId, conversationId)

        val now = LocalDateTime.now()
        val updatedCount = messageRepository.markMessagesAsRead(conversation, user, now)
        
        logger.info("Označeno $updatedCount poruka kao pročitano za korisnika $userId u konverzaciji $conversationId")
        
        return conversation
    }

    @Transactional(readOnly = true)
    fun getMessageIdsInConversation(conversationId: Long): List<Long> {
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena") }
        
        return messageRepository.findMessageIdsByConversation(conversation)
    }

    @Transactional(readOnly = true)
    fun findConversationsForUserPaginated(userId: Long, page: Int, size: Int): ConversationPageResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
        
        val pageable = PageRequest.of(page, size)
        val conversationsPage = conversationRepository.findAllConversationsForUser(user, pageable)
        
        val conversationsDto = conversationsPage.content.map { conversation ->
            val otherUser = if (conversation.user1.id == userId) conversation.user2 else conversation.user1

            val messagePageable = PageRequest.of(0, 1)
            val lastMessages = messageRepository.findTopByConversationOrderBySentAtDesc(conversation, messagePageable)
            val lastMessage = if (lastMessages.isNotEmpty()) lastMessages[0] else null
            
            val unreadCount = messageRepository.countUnreadMessagesInConversation(conversation, user)
            
            ConversationDto(
                id = conversation.id!!,
                otherUserId = otherUser.id!!,
                otherUserName = getUserFullName(otherUser),
                otherUserAvatarId = otherUser.getAvatarId(),
                lastMessage = lastMessage?.content,
                lastMessageType = lastMessage?.messageType,
                lastMessageSenderId = lastMessage?.sender?.id,
                lastMessageTime = lastMessage?.sentAt,
                unreadCount = unreadCount,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
            )
        }
        
        return ConversationPageResponse(
            content = conversationsDto,
            page = page,
            size = size,
            totalElements = conversationsPage.totalElements,
            totalPages = conversationsPage.totalPages,
            last = conversationsPage.isLast
        )
    }

    @Transactional(readOnly = true)
    fun findMessagesInConversationPaginated(userId: Long, conversationId: Long, page: Int, size: Int): MessagePageResponse {
        val (user, conversation) = validateUserAndConversation(userId, conversationId)
        
        val otherUser = if (conversation.user1.id == userId) conversation.user2 else conversation.user1
        
        val pageable = PageRequest.of(page, size)
        val messagesPage = messageRepository.findAllByConversationOrderBySentAtDesc(conversation, pageable)
        
        val messagesDto = messagesPage.content.map { message ->
            MessageDto(
                id = message.id,
                senderId = message.sender.id!!,
                senderName = getUserFullName(message.sender),
                content = message.content,
                messageType = message.messageType,
                latitude = message.latitude,
                longitude = message.longitude,
                address = message.address,
                sentAt = message.sentAt,
                isRead = message.isRead,
                readAt = message.readAt
            )
        }
        
        return MessagePageResponse(
            conversationId = conversationId,
            otherUserName = getUserFullName(otherUser),
            otherUserPhone = otherUser.getPhoneNumber(),
            otherUserAvatarId = otherUser.getAvatarId(),
            content = messagesDto,
            page = page,
            size = size,
            totalElements = messagesPage.totalElements,
            totalPages = messagesPage.totalPages,
            last = messagesPage.isLast
        )
    }

    @Transactional(readOnly = true)
    fun findMessagesBetweenUsersPaginated(userId: Long, otherUserId: Long, page: Int, size: Int): MessagePageResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
        
        val otherUser = userRepository.findById(otherUserId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $otherUserId nije pronađen") }

        val conversationOptional = conversationRepository.findConversationBetweenUsers(user, otherUser)
        
        if (conversationOptional.isPresent) {
            return findMessagesInConversationPaginated(userId, conversationOptional.get().id!!, page, size)
        } else {
            return MessagePageResponse(
                conversationId = null,
                otherUserName = getUserFullName(otherUser),
                otherUserPhone = otherUser.getPhoneNumber(),
                otherUserAvatarId = otherUser.getAvatarId(),
                content = emptyList(),
                page = page,
                size = size,
                totalElements = 0,
                totalPages = 0,
                last = true
            )
        }
    }

    private fun getUserFullName(user: User): String {
        return user.getFullName()
    }

    @Transactional(readOnly = true)
    fun validateUserAndConversation(userId: Long, conversationId: Long): Pair<User, Conversation> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }

        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena") }

        if (conversation.user1.id != userId && conversation.user2.id != userId) {
            throw ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena za korisnika $userId")
        }

        return Pair(user, conversation)
    }
} 