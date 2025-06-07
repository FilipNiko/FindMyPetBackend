package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.LostPetListItem
import com.spring.findmypet.domain.dto.UserListItemDto
import com.spring.findmypet.domain.dto.AdminStatisticsDto
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.domain.exception.ResourceNotFoundException
import com.spring.findmypet.repository.LostPetRepository
import com.spring.findmypet.repository.UserRepository
import com.spring.findmypet.repository.TokenRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val lostPetRepository: LostPetRepository,
    private val lostPetService: LostPetService,
    private val timeFormatService: TimeFormatService,
    private val tokenRepository: TokenRepository
) {
    private val logger = LoggerFactory.getLogger(AdminService::class.java)

    @Transactional(readOnly = true)
    fun getAllUsers(pageable: Pageable): Page<UserListItemDto> {
        logger.info("Dobavljanje liste svih korisnika, strana: ${pageable.pageNumber}, veličina: ${pageable.pageSize}")
        
        return userRepository.findAll(pageable).map { user ->
            mapUserToListItem(user)
        }
    }

    @Transactional(readOnly = true)
    fun searchUsersByEmail(email: String, pageable: Pageable): Page<UserListItemDto> {
        logger.info("Pretraga korisnika po email adresi koja sadrži: $email")

        return userRepository.findByEmailContainingIgnoreCase(email, pageable).map { user ->
            mapUserToListItem(user)
        }
    }

    @Transactional(readOnly = true)
    fun getUserLostPets(userId: Long, pageable: Pageable): Page<LostPetListItem> {
        logger.info("Dobavljanje liste prijavljenih ljubimaca za korisnika sa ID: $userId")
        
        val user = getUserById(userId)
        
        return lostPetRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable).map { lostPet ->
            LostPetListItem(
                id = lostPet.id,
                mainPhotoUrl = lostPetService.getMainPhotoUrl(lostPet),
                timeAgo = timeFormatService.getTimeAgo(lostPet.createdAt),
                petName = lostPet.title,
                breed = lostPet.breed,
                color = lostPet.color,
                gender = lostPet.gender,
                hasChip = lostPet.hasChip,
                ownerName = user.getFullName(),
                distance = "N/A",
                petType = lostPet.petType,
                allPhotos = lostPet.photos.map { photo -> "/uploads/$photo" },
                found = lostPet.found,
                foundAt = lostPet.foundAt?.let { timeFormatService.getTimeAgo(it) }
            )
        }
    }

    @Transactional(readOnly = true)
    fun getStatistics(): AdminStatisticsDto {
        logger.info("Dobavljanje admin statistika")
        
        val totalUsers = userRepository.count()
        val totalLostPetReports = lostPetRepository.countByDeletedFalse()
        
        logger.info("Statistike: ukupno korisnika=$totalUsers, ukupno prijava=$totalLostPetReports")
        
        return AdminStatisticsDto(
            totalUsers = totalUsers,
            totalLostPetReports = totalLostPetReports
        )
    }

    @Transactional
    fun banUser(userId: Long, banned: Boolean, reason: String?): BanResult {
        logger.info("${if (banned) "Banovanje" else "Odbanovanje"} korisnika sa ID: $userId")
        
        val user = getUserById(userId)
        user.setBanStatus(banned, reason)
        userRepository.save(user)
        
        var deletedPetsCount = 0

        if (banned) {
            deletedPetsCount = markAllUserPetsAsDeleted(user)
            
            invalidateAllUserTokens(user)
            logger.info("Invalidovani svi tokeni korisnika ${user.getUsername()} zbog banovanja")
        }
        
        logger.info("Korisnik ${user.getUsername()} je uspešno ${if (banned) "banovan" else "odbanovan"}")
        if (banned) {
            logger.info("Obrisano $deletedPetsCount prijava ljubimaca korisnika ${user.getUsername()}")
        }
        
        return BanResult(user.id!!, banned, banReason = reason, deletedPetsCount)
    }

    private fun invalidateAllUserTokens(user: User) {
        val userTokens = tokenRepository.findAllValidTokensByUser(user.id!!)
        if (userTokens.isNotEmpty()) {
            userTokens.forEach { token ->
                token.expired = true
                token.revoked = true
            }
            tokenRepository.saveAll(userTokens)
            logger.info("Invalidovano ${userTokens.size} tokena za korisnika ${user.getUsername()}")
        }
    }

    private fun markAllUserPetsAsDeleted(user: User): Int {
        val userPets = lostPetRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user)
        var count = 0
        
        userPets.forEach { pet ->
            lostPetService.softDeleteLostPet(pet.id!!, user)
            count++
        }
        
        return count
    }

    private fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Korisnik sa ID-om $userId nije pronađen") }
    }

    private fun mapUserToListItem(user: User): UserListItemDto {
        return UserListItemDto(
            id = user.id!!,
            email = user.getUsername(),
            fullName = user.getFullName(),
            banned = user.isBanned(),
            banReason = user.getBanReason()
        )
    }

    data class BanResult(
        val userId: Long,
        val banned: Boolean,
        val banReason: String?,
        val deletedPetsCount: Int
    )
}
