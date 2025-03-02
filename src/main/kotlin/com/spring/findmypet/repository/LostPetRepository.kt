package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.LostPet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LostPetRepository : JpaRepository<LostPet, Long> 