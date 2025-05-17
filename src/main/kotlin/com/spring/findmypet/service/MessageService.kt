package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.ConversationDto
import com.spring.findmypet.domain.dto.MessageDto
import com.spring.findmypet.domain.dto.MessageRequest
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.Message
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.ConversationRepository
import com.spring.findmypet.repository.MessageRepository
import com.spring.findmypet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.springframework.data.domain.PageRequest

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(MessageService::class.java)
    
    /**
     * Šalje novu poruku između korisnika
     * Ako ne postoji konverzacija između njih, kreira se nova
     */
    @Transactional
    fun sendMessage(senderUserId: Long, messageRequest: MessageRequest): MessageDto {
        logger.info("Slanje poruke od korisnika $senderUserId korisniku ${messageRequest.receiverId}")
        
        // Dohvatamo pošiljaoca i primaoca
        val sender = userRepository.findById(senderUserId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $senderUserId nije pronađen") }
        
        val receiver = userRepository.findById(messageRequest.receiverId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om ${messageRequest.receiverId} nije pronađen") }
        
        // Pronalazimo ili kreiramo konverzaciju
        val conversation = findOrCreateConversation(sender, receiver)
        
        // Kreiramo poruku
        val message = Message(
            conversation = conversation,
            sender = sender,
            content = messageRequest.content
        )
        
        // Čuvamo poruku
        val savedMessage = messageRepository.save(message)
        
        // Ažuriramo vreme poslednje aktivnosti i poslednje poruke u konverzaciji
        val currentTime = LocalDateTime.now()
        conversation.updatedAt = currentTime
        conversation.lastMessageTime = currentTime
        conversationRepository.save(conversation)
        
        // Kreiramo i vraćamo DTO
        return MessageDto(
            id = savedMessage.id,
            senderId = sender.id!!,
            senderName = getUserFullName(sender),
            content = savedMessage.content,
            sentAt = savedMessage.sentAt,
            isRead = savedMessage.isRead,
            readAt = savedMessage.readAt
        )
    }
    
    /**
     * Pronalazi konverzaciju između dva korisnika ili kreira novu ako ne postoji
     */
    @Transactional
    fun findOrCreateConversation(user1: User, user2: User): Conversation {
        // Proveravamo da li konverzacija već postoji
        val existingConversation = conversationRepository.findConversationBetweenUsers(user1, user2)
        
        if (existingConversation.isPresent) {
            return existingConversation.get()
        }
        
        // Ako ne postoji, kreiramo novu
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
    
    /**
     * Pronalazi konverzaciju između dva korisnika
     */
    @Transactional(readOnly = true)
    fun findConversationBetweenUsers(user1Id: Long, user2Id: Long): Conversation {
        val user1 = userRepository.findById(user1Id)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $user1Id nije pronađen") }
        
        val user2 = userRepository.findById(user2Id)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $user2Id nije pronađen") }
        
        return conversationRepository.findConversationBetweenUsers(user1, user2)
            .orElseThrow { ResourceNotFoundException("Konverzacija između korisnika $user1Id i $user2Id nije pronađena") }
    }
    
    /**
     * Vraća listu svih konverzacija korisnika s informacijama o poslednjoj poruci i broju nepročitanih
     */
    @Transactional(readOnly = true)
    fun findConversationsForUser(userId: Long): List<ConversationDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
        
        val conversations = conversationRepository.findAllConversationsForUser(user)
        
        return conversations.map { conversation ->
            val otherUser = if (conversation.user1.id == userId) conversation.user2 else conversation.user1
            
            // Dobijamo poslednju poruku koristeći PageRequest da ograničimo na 1 rezultat
            val pageable = PageRequest.of(0, 1)
            val lastMessages = messageRepository.findTopByConversationOrderBySentAtDesc(conversation, pageable)
            val lastMessage = if (lastMessages.isNotEmpty()) lastMessages[0] else null
            
            val unreadCount = messageRepository.countUnreadMessagesInConversation(conversation, user)
            
            ConversationDto(
                id = conversation.id!!,
                otherUserId = otherUser.id!!,
                otherUserName = getUserFullName(otherUser),
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.sentAt,
                unreadCount = unreadCount,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
            )
        }
    }
    
    /**
     * Vraća sve poruke iz konverzacije
     */
    @Transactional(readOnly = true)
    fun findMessagesInConversation(userId: Long, conversationId: Long): List<MessageDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
        
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena") }
        
        // Provera da li korisnik ima pristup ovoj konverzaciji
        if (conversation.user1.id != userId && conversation.user2.id != userId) {
            throw ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena za korisnika $userId")
        }
        
        val messages = messageRepository.findAllByConversationOrderBySentAt(conversation)
        
        return messages.map { message ->
            MessageDto(
                id = message.id,
                senderId = message.sender.id!!,
                senderName = getUserFullName(message.sender),
                content = message.content,
                sentAt = message.sentAt,
                isRead = message.isRead,
                readAt = message.readAt
            )
        }
    }
    
    /**
     * Označava sve poruke u konverzaciji kao pročitane za korisnika
     */
    @Transactional
    fun markMessagesAsRead(userId: Long, conversationId: Long): Conversation {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
        
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena") }
        
        // Provera da li korisnik ima pristup ovoj konverzaciji
        if (conversation.user1.id != userId && conversation.user2.id != userId) {
            throw ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena za korisnika $userId")
        }
        
        // Označavamo sve nepročitane poruke kao pročitane
        val now = LocalDateTime.now()
        val updatedCount = messageRepository.markMessagesAsRead(conversation, user, now)
        
        logger.info("Označeno $updatedCount poruka kao pročitano za korisnika $userId u konverzaciji $conversationId")
        
        return conversation
    }
    
    /**
     * Vraća ID-eve svih poruka u konverzaciji
     */
    @Transactional(readOnly = true)
    fun getMessageIdsInConversation(conversationId: Long): List<Long> {
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { ResourceNotFoundException("Konverzacija sa ID-om $conversationId nije pronađena") }
        
        return messageRepository.findMessageIdsByConversation(conversation)
    }
    
    /**
     * Pomoćna metoda za dobijanje punog imena korisnika
     */
    private fun getUserFullName(user: User): String {
        return user.getFullName()
    }
} 