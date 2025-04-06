package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.model.PetType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max

data class LostPetListRequest(
    @field:DecimalMin(value = "-90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    @field:DecimalMax(value = "90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    val latitude: Double,
    
    @field:DecimalMin(value = "-180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    @field:DecimalMax(value = "180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    val longitude: Double,
    
    val sortBy: SortType = SortType.DISTANCE,
    
    val petFilter: PetFilter = PetFilter.ALL,
    
    // Paginacija
    @field:Min(value = 0, message = "Broj stranice mora biti 0 ili veći")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "Veličina stranice mora biti najmanje 1")
    @field:Max(value = 50, message = "Veličina stranice ne može biti veća od 50")
    val size: Int = 10,
    
    // Napredni filteri
    val breed: String? = null,
    val color: String? = null,
    val hasChip: Boolean? = null,
    val gender: String? = null,
    
    // Radijus pretraživanja u kilometrima
    @field:Min(value = 1, message = "Radijus mora biti najmanje 1 km")
    @field:Max(value = 100, message = "Radijus ne može biti veći od 100 km")
    val radius: Int = 5
)

data class LostPetListResponse(
    val content: List<LostPetListItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class LostPetListItem(
    val id: Long,
    val mainPhotoUrl: String,
    val timeAgo: String,
    val petName: String,
    val breed: String?,
    val color: String?,
    val gender: String?,
    val hasChip: Boolean,
    val ownerName: String,
    val distance: String,
    val petType: PetType,
    val allPhotos: List<String>
)

enum class SortType {
    DISTANCE,   // najbliže prvo
    LATEST      // najnovije prvo
}

enum class PetFilter {
    ALL,
    DOGS,
    CATS,
    OTHER
} 