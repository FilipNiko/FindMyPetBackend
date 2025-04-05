package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LostPetRepository : JpaRepository<LostPet, Long> {
    
    fun findAllByPetType(petType: PetType): List<LostPet>
    
    fun findAllByOrderByCreatedAtDesc(): List<LostPet>
    
    fun findByPetTypeOrderByCreatedAtDesc(petType: PetType): List<LostPet>
} 