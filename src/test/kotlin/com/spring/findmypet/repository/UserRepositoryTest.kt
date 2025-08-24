package com.spring.findmypet.repository

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
class UserRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should find user by email`() {
        val testUser = User(
            fullName = "Marko Petrović",
            email = "marko.petrovic@test.com",
            phoneNumber = "+381601234567", 
            password = "hashedPassword123",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(testUser)

        val foundUser = userRepository.findByEmail("marko.petrovic@test.com")

        assertThat(foundUser).isPresent
        assertThat(foundUser.get().getFullName()).isEqualTo("Marko Petrović")
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER)
    }
    
    @Test
    fun `should return empty when user not found by email`() {
        val foundUser = userRepository.findByEmail("nepostojeci@test.com")

        assertThat(foundUser).isEmpty
    }
    
    @Test
    fun `should check if user exists by email`() {
        val testUser = User(
            fullName = "Ana Jovanović",
            email = "ana.jovanovic@test.com", 
            phoneNumber = "+381602345678",
            password = "hashedPassword456",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(testUser)

        assertThat(userRepository.existsByEmail("ana.jovanovic@test.com")).isTrue
        assertThat(userRepository.existsByEmail("nepostojeci@test.com")).isFalse
    }
    
    @Test
    fun `should start with empty database and save user successfully`() {
        val allUsers = userRepository.findAll()
        assertThat(allUsers).isEmpty()

        val testUser = User(
            fullName = "Test Isolation",
            email = "isolation@test.com",
            phoneNumber = "+381600000000",
            password = "testPassword",
            role = Role.USER
        )
        
        testEntityManager.persistAndFlush(testUser)

        val allUsersAfter = userRepository.findAll()
        assertThat(allUsersAfter).hasSize(1)
        assertThat(allUsersAfter[0].getFullName()).isEqualTo("Test Isolation")
    }
}