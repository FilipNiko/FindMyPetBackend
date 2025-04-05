package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.model.PetType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class LostPetListRequest(
    @field:DecimalMin(value = "-90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    @field:DecimalMax(value = "90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    val latitude: Double,
    
    @field:DecimalMin(value = "-180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    @field:DecimalMax(value = "180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    val longitude: Double,
    
    val sortBy: SortType = SortType.DISTANCE,
    
    val petFilter: PetFilter = PetFilter.ALL
)

data class LostPetListItem(
    val id: Long,
    val mainPhotoUrl: String,
    val timeAgo: String,
    val petName: String,
    val breed: String?,
    val ownerName: String,
    val distance: String,
    val petType: PetType
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