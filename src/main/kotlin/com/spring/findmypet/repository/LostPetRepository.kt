package com.spring.findmypet.repository

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LostPetRepository : JpaRepository<LostPet, Long> {
    
    fun findAllByPetType(petType: PetType): List<LostPet>
    
    fun findAllByOrderByCreatedAtDesc(): List<LostPet>
    
    fun findByPetTypeOrderByCreatedAtDesc(petType: PetType): List<LostPet>

    @Query("""
        SELECT lp FROM LostPet lp
        WHERE (:petType IS NULL OR lp.petType = :petType)
        AND (:breed IS NULL OR LOWER(lp.breed) LIKE LOWER(CONCAT('%', :breed, '%')))
        AND (:color IS NULL OR LOWER(lp.color) LIKE LOWER(CONCAT('%', :color, '%')))
        AND (:gender IS NULL OR lp.gender = :gender)
        AND (:hasChip IS NULL OR lp.hasChip = :hasChip)
        AND (
            :minLatitude IS NULL OR :maxLatitude IS NULL OR
            :minLongitude IS NULL OR :maxLongitude IS NULL OR
            (lp.latitude BETWEEN :minLatitude AND :maxLatitude AND
             lp.longitude BETWEEN :minLongitude AND :maxLongitude)
        )
        ORDER BY
        CASE WHEN :sortByLatest = TRUE THEN lp.createdAt END DESC,
        CASE WHEN :sortByLatest = FALSE THEN lp.id END ASC
    """)
    fun findPetsWithFilters(
        @Param("petType") petType: PetType?,
        @Param("breed") breed: String?,
        @Param("color") color: String?,
        @Param("gender") gender: String?,
        @Param("hasChip") hasChip: Boolean?,
        @Param("minLatitude") minLatitude: Double?,
        @Param("maxLatitude") maxLatitude: Double?,
        @Param("minLongitude") minLongitude: Double?,
        @Param("maxLongitude") maxLongitude: Double?,
        @Param("sortByLatest") sortByLatest: Boolean,
        pageable: Pageable
    ): Page<LostPet>

    @Query("""
        SELECT COUNT(lp) FROM LostPet lp
        WHERE (:petType IS NULL OR lp.petType = :petType)
        AND (:breed IS NULL OR LOWER(lp.breed) LIKE LOWER(CONCAT('%', :breed, '%')))
        AND (:color IS NULL OR LOWER(lp.color) LIKE LOWER(CONCAT('%', :color, '%')))
        AND (:gender IS NULL OR lp.gender = :gender)
        AND (:hasChip IS NULL OR lp.hasChip = :hasChip)
        AND (
            :minLatitude IS NULL OR :maxLatitude IS NULL OR
            :minLongitude IS NULL OR :maxLongitude IS NULL OR
            (lp.latitude BETWEEN :minLatitude AND :maxLatitude AND
             lp.longitude BETWEEN :minLongitude AND :maxLongitude)
        )
    """)
    fun countPetsWithFilters(
        @Param("petType") petType: PetType?,
        @Param("breed") breed: String?,
        @Param("color") color: String?,
        @Param("gender") gender: String?,
        @Param("hasChip") hasChip: Boolean?,
        @Param("minLatitude") minLatitude: Double?,
        @Param("maxLatitude") maxLatitude: Double?,
        @Param("minLongitude") minLongitude: Double?,
        @Param("maxLongitude") maxLongitude: Double?
    ): Long
} 