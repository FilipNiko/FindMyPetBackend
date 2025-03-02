package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.LostPetResponse
import com.spring.findmypet.domain.dto.ReportLostPetRequest
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.LostPetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LostPetService(
    private val lostPetRepository: LostPetRepository
) {

    @Transactional
    fun reportLostPet(request: ReportLostPetRequest, user: User): LostPetResponse {
        val lostPet = LostPet(
            user = user,
            petType = request.petType,
            title = request.title,
            breed = request.breed,
            color = request.color,
            description = request.description,
            gender = request.gender,
            hasChip = request.hasChip,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            photos = request.photos
        )

        val savedPet = lostPetRepository.save(lostPet)

        return LostPetResponse(
            id = savedPet.id,
            petType = savedPet.petType,
            title = savedPet.title,
            breed = savedPet.breed,
            color = savedPet.color,
            description = savedPet.description,
            gender = savedPet.gender,
            hasChip = savedPet.hasChip,
            address = savedPet.address,
            latitude = savedPet.latitude,
            longitude = savedPet.longitude,
            photos = savedPet.photos,
            userId = savedPet.user.id ?: throw IllegalStateException("User ID is null")
        )
    }
} 