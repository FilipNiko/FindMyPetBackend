package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
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
class LostPetRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var lostPetRepository: LostPetRepository

    @Test
    fun `should save and find lost pet`() {
        val testUser = User(
            fullName = "Pet Owner",
            email = "owner@example.com",
            phoneNumber = "+381601234567",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(testUser)

        val lostPet = LostPet(
            user = testUser,
            petType = PetType.DOG,
            title = "Lost Dog",
            breed = "Labrador",
            color = "Golden",
            description = "Friendly golden labrador",
            gender = "MALE",
            hasChip = true,
            address = "Belgrade Center",
            latitude = 44.8176,
            longitude = 20.4587,
            photos = listOf("dog-photo.jpg")
        )

        val savedPet = lostPetRepository.save(lostPet)

        assertThat(savedPet.id).isNotZero
        assertThat(savedPet.title).isEqualTo("Lost Dog")
        assertThat(savedPet.petType).isEqualTo(PetType.DOG)
        assertThat(savedPet.deleted).isFalse()
    }

    @Test
    fun `should find pets by type excluding deleted`() {
        val testUser = User(
            fullName = "Pet Owner 2",
            email = "owner2@example.com", 
            phoneNumber = "+381601234568",
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(testUser)

        val activePet = LostPet(
            user = testUser,
            petType = PetType.DOG,
            title = "Active Dog",
            breed = "German Shepherd",
            color = "Brown",
            description = "Active pet",
            gender = "FEMALE",
            hasChip = false,
            address = "Test Address",
            latitude = 44.8176,
            longitude = 20.4587,
            photos = listOf("active-dog.jpg")
        )

        val deletedPet = LostPet(
            user = testUser,
            petType = PetType.DOG,
            title = "Deleted Dog",
            breed = "Bulldog",
            color = "White",
            description = "Deleted pet",
            gender = "MALE",
            hasChip = false,
            address = "Test Address",
            latitude = 44.8176,
            longitude = 20.4587,
            photos = listOf("deleted-dog.jpg"),
            deleted = true
        )

        lostPetRepository.save(activePet)
        lostPetRepository.save(deletedPet)

        val activeDogs = lostPetRepository.findAllByPetTypeAndDeletedFalse(PetType.DOG)

        assertThat(activeDogs).hasSize(1)
        assertThat(activeDogs[0].title).isEqualTo("Active Dog")
        assertThat(activeDogs[0].deleted).isFalse()
    }

    @Test
    fun `should count non-deleted pets`() {
        assertThat(lostPetRepository.countByDeletedFalse()).isEqualTo(0)

        val testUser = User(
            fullName = "Count User",
            email = "count@example.com",
            phoneNumber = "+381601234569", 
            password = "hashedPassword",
            role = Role.USER
        )
        testEntityManager.persistAndFlush(testUser)

        val pet1 = LostPet(
            user = testUser,
            petType = PetType.CAT,
            title = "Cat 1",
            breed = "Persian",
            color = "White",
            description = "First cat",
            gender = "FEMALE",
            hasChip = true,
            address = "Address 1",
            latitude = 44.8176,
            longitude = 20.4587,
            photos = listOf("cat1.jpg")
        )

        val pet2 = LostPet(
            user = testUser,
            petType = PetType.DOG,
            title = "Dog 1",
            breed = "Poodle",
            color = "Black",
            description = "First dog",
            gender = "MALE",
            hasChip = false,
            address = "Address 2", 
            latitude = 44.8176,
            longitude = 20.4587,
            photos = listOf("dog1.jpg"),
            deleted = true  // This one is deleted
        )

        lostPetRepository.save(pet1)
        lostPetRepository.save(pet2)

        val count = lostPetRepository.countByDeletedFalse()

        assertThat(count).isEqualTo(1)
    }
}