package com.spring.findmypet.repository

import com.spring.findmypet.domain.dto.MessageType
import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.Message
import com.spring.findmypet.domain.model.Role
import com.spring.findmypet.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime


@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Test
    fun `should save and find message`() {
        val sender = User(
            fullName = "Message Sender",
            email = "sender@example.com",
            phoneNumber = "+381601234567",
            password = "hashedPassword",
            role = Role.USER
        )
        val receiver = User(
            fullName = "Message Receiver", 
            email = "receiver@example.com",
            phoneNumber = "+381601234568",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(sender)
        testEntityManager.persistAndFlush(receiver)

        val conversation = Conversation(
            user1 = sender,
            user2 = receiver
        )
        testEntityManager.persistAndFlush(conversation)

        val message = Message(
            conversation = conversation,
            sender = sender,
            content = "Hello, this is a test message!"
        )

        val savedMessage = messageRepository.save(message)

        assertThat(savedMessage.id).isNotNull
        assertThat(savedMessage.content).isEqualTo("Hello, this is a test message!")
        assertThat(savedMessage.sender.getFullName()).isEqualTo("Message Sender")
        assertThat(savedMessage.isRead).isFalse()
        assertThat(savedMessage.messageType).isEqualTo(MessageType.TEXT)
    }

    @Test
    fun `should find message by id`() {
        val user1 = User(
            fullName = "User 1",
            email = "user1@example.com",
            phoneNumber = "+381601234569",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "User 2",
            email = "user2@example.com",
            phoneNumber = "+381601234570",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = Conversation(user1 = user1, user2 = user2)
        testEntityManager.persistAndFlush(conversation)

        val message = Message(
            conversation = conversation,
            sender = user1,
            content = "Find me by ID"
        )
        val savedMessage = messageRepository.save(message)

        val foundMessage = messageRepository.findById(savedMessage.id!!)

        assertThat(foundMessage).isPresent
        assertThat(foundMessage.get().content).isEqualTo("Find me by ID")
        assertThat(foundMessage.get().sender.getFullName()).isEqualTo("User 1")
    }

    @Test
    fun `should mark message as read`() {
        val sender = User(
            fullName = "Sender",
            email = "sender2@example.com",
            phoneNumber = "+381601234571",
            password = "hashedPassword",
            role = Role.USER
        )
        val receiver = User(
            fullName = "Receiver",
            email = "receiver2@example.com",
            phoneNumber = "+381601234572", 
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(sender)
        testEntityManager.persistAndFlush(receiver)

        val conversation = Conversation(user1 = sender, user2 = receiver)
        testEntityManager.persistAndFlush(conversation)

        val message = Message(
            conversation = conversation,
            sender = sender,
            content = "Unread message"
        )
        val savedMessage = messageRepository.save(message)

        assertThat(savedMessage.isRead).isFalse()
        assertThat(savedMessage.readAt).isNull()

        savedMessage.isRead = true
        savedMessage.readAt = LocalDateTime.now()
        val updatedMessage = messageRepository.save(savedMessage)

        assertThat(updatedMessage.isRead).isTrue()
        assertThat(updatedMessage.readAt).isNotNull()
    }

    @Test
    fun `should handle different message types`() {
        val user1 = User(
            fullName = "Type User 1",
            email = "type1@example.com",
            phoneNumber = "+381601234573",
            password = "hashedPassword", 
            role = Role.USER
        )
        val user2 = User(
            fullName = "Type User 2",
            email = "type2@example.com",
            phoneNumber = "+381601234574",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = Conversation(user1 = user1, user2 = user2)
        testEntityManager.persistAndFlush(conversation)

        val textMessage = Message(
            conversation = conversation,
            sender = user1,
            content = "Text message",
            messageType = MessageType.TEXT
        )

        val locationMessage = Message(
            conversation = conversation,
            sender = user2,
            content = "Location message",
            messageType = MessageType.LOCATION,
            latitude = 44.8176,
            longitude = 20.4587,
            address = "Belgrade Center"
        )

        val savedTextMessage = messageRepository.save(textMessage)
        val savedLocationMessage = messageRepository.save(locationMessage)

        assertThat(savedTextMessage.messageType).isEqualTo(MessageType.TEXT)
        assertThat(savedTextMessage.latitude).isNull()
        
        assertThat(savedLocationMessage.messageType).isEqualTo(MessageType.LOCATION)
        assertThat(savedLocationMessage.latitude).isEqualTo(44.8176)
        assertThat(savedLocationMessage.longitude).isEqualTo(20.4587)
        assertThat(savedLocationMessage.address).isEqualTo("Belgrade Center")
    }

    @Test
    fun `should start with empty database and save message successfully`() {
        val allMessages = messageRepository.findAll()
        assertThat(allMessages).isEmpty()

        val user1 = User(
            fullName = "Isolation User 1",
            email = "isolation1@example.com",
            phoneNumber = "+381601234575",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "Isolation User 2",
            email = "isolation2@example.com",
            phoneNumber = "+381601234576",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = Conversation(user1 = user1, user2 = user2)
        testEntityManager.persistAndFlush(conversation)

        val message = Message(
            conversation = conversation,
            sender = user1,
            content = "Isolation test message"
        )
        messageRepository.save(message)

        val allMessagesAfter = messageRepository.findAll()
        assertThat(allMessagesAfter).hasSize(1)
        assertThat(allMessagesAfter[0].content).isEqualTo("Isolation test message")
        assertThat(allMessagesAfter[0].sender.getFullName()).isEqualTo("Isolation User 1")
    }
}