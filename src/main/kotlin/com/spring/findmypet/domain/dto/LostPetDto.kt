package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.validation.LostPetValidationTags
import jakarta.validation.constraints.*

data class ReportLostPetRequest(
    @field:NotNull
    val petType: PetType,

    @field:NotBlank(message = LostPetValidationTags.TITLE_EMPTY)
    val title: String,

    val breed: String?,

    @field:NotBlank(message = LostPetValidationTags.COLOR_EMPTY)
    val color: String,

    @field:NotBlank
    @field:Size(
        min = 50,
        max = 1000,
        message = LostPetValidationTags.DESCRIPTION_SIZE
    )
    val description: String,

    @field:Pattern(
        regexp = "^(MALE|FEMALE)$",
        message = LostPetValidationTags.GENDER_INVALID
    )
    val gender: String,

    val hasChip: Boolean,

    @field:NotBlank(message = LostPetValidationTags.ADDRESS_EMPTY)
    val address: String,

    @field:NotNull(message = LostPetValidationTags.COORDINATES_INVALID)
    @field:DecimalMin(value = "-90.0")
    @field:DecimalMax(value = "90.0")
    val latitude: Double,

    @field:NotNull(message = LostPetValidationTags.COORDINATES_INVALID)
    @field:DecimalMin(value = "-180.0")
    @field:DecimalMax(value = "180.0")
    val longitude: Double,

    @field:NotEmpty(message = LostPetValidationTags.NO_PHOTOS)
    @field:Size(max = 5, message = LostPetValidationTags.TOO_MANY_PHOTOS)
    val photos: List<String>
)

data class LostPetResponse(
    val id: Long,
    val petType: PetType,
    val title: String,
    val breed: String?,
    val color: String,
    val description: String,
    val gender: String,
    val hasChip: Boolean,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photos: List<String>,
    val userId: Long
) 