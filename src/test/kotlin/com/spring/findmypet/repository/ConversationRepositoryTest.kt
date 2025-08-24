package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.Conversation
import com.spring.findmypet.domain.model.Role
import com.spring.findmypet.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class ConversationRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var conversationRepository: ConversationRepository

    @Test
    fun `should save and find conversation`() {
        val user1 = User(
            fullName = "User One",
            email = "user1@example.com",
            phoneNumber = "+381601234567",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "User Two",
            email = "user2@example.com", 
            phoneNumber = "+381601234568",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = Conversation(
            user1 = user1,
            user2 = user2
        )

        val savedConversation = conversationRepository.save(conversation)

        assertThat(savedConversation.id).isNotNull
        assertThat(savedConversation.user1.getFullName()).isEqualTo("User One")
        assertThat(savedConversation.user2.getFullName()).isEqualTo("User Two")
    }

    @Test
    fun `should find conversation by id`() {
        val user1 = User(
            fullName = "Find User 1",
            email = "find1@example.com",
            phoneNumber = "+381601234569",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "Find User 2",
            email = "find2@example.com",
            phoneNumber = "+381601234570", 
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = conversationRepository.save(Conversation(user1 = user1, user2 = user2))

        val foundConversation = conversationRepository.findById(conversation.id!!)

        assertThat(foundConversation).isPresent
        assertThat(foundConversation.get().user1.getFullName()).isEqualTo("Find User 1")
        assertThat(foundConversation.get().user2.getFullName()).isEqualTo("Find User 2")
    }

    @Test
    fun `should count conversations`() {
        assertThat(conversationRepository.count()).isEqualTo(0)

        val user1 = User(
            fullName = "Count User 1",
            email = "count1@example.com",
            phoneNumber = "+381601234571",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "Count User 2", 
            email = "count2@example.com",
            phoneNumber = "+381601234572",
            password = "hashedPassword",
            role = Role.USER
        )
        val user3 = User(
            fullName = "Count User 3",
            email = "count3@example.com",
            phoneNumber = "+381601234573",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)
        testEntityManager.persistAndFlush(user3)

        conversationRepository.save(Conversation(user1 = user1, user2 = user2))
        conversationRepository.save(Conversation(user1 = user2, user2 = user3))

        val count = conversationRepository.count()

        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `should start with empty database and save conversation successfully`() {
        val allConversations = conversationRepository.findAll()
        assertThat(allConversations).isEmpty()

        val user1 = User(
            fullName = "Isolation User 1",
            email = "isolation1@example.com",
            phoneNumber = "+381601234574",
            password = "hashedPassword",
            role = Role.USER
        )
        val user2 = User(
            fullName = "Isolation User 2",
            email = "isolation2@example.com",
            phoneNumber = "+381601234575",
            password = "hashedPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(user1)
        testEntityManager.persistAndFlush(user2)

        val conversation = Conversation(user1 = user1, user2 = user2)
        conversationRepository.save(conversation)

        val allConversationsAfter = conversationRepository.findAll()
        assertThat(allConversationsAfter).hasSize(1)
        assertThat(allConversationsAfter[0].user1.getFullName()).isEqualTo("Isolation User 1")
        assertThat(allConversationsAfter[0].user2.getFullName()).isEqualTo("Isolation User 2")
    }
}