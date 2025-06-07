package com.spring.findmypet.domain.dto

import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.validation.ValidBreed
import com.spring.findmypet.domain.validation.PetWithBreed
import jakarta.validation.constraints.*

@ValidBreed
data class ReportLostPetRequest(
    @field:NotNull(message = "Tip ljubimca je obavezan")
    override val petType: PetType,

    @field:NotBlank(message = "Naslov je obavezan")
    val title: String,

    override val breed: String?,

    @field:NotBlank(message = "Boja je obavezna")
    val color: String,

    @field:NotBlank(message = "Opis je obavezan")
    @field:Size(
        min = 50,
        max = 1000,
        message = "Opis mora imati između 50 i 1000 karaktera"
    )
    val description: String,

    @field:Pattern(
        regexp = "^(MALE|FEMALE)$",
        message = "Pol mora biti 'Muški' ili 'Ženski'"
    )
    val gender: String,

    val hasChip: Boolean,

    @field:NotBlank(message = "Adresa je obavezna")
    val address: String,

    @field:NotNull(message = "Koordinate nisu validne")
    @field:DecimalMin(value = "-90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    @field:DecimalMax(value = "90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    val latitude: Double,

    @field:NotNull(message = "Koordinate nisu validne")
    @field:DecimalMin(value = "-180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    @field:DecimalMax(value = "180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    val longitude: Double,

    @field:NotEmpty(message = "Mora biti priložena bar jedna fotografija")
    @field:Size(max = 5, message = "Maksimalan broj fotografija je 5")
    val photos: List<String>
) : PetWithBreed

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

@ValidBreed
data class UpdateLostPetRequest(
    @field:NotNull(message = "ID ljubimca je obavezan")
    val id: Long,
    
    @field:NotNull(message = "Tip ljubimca je obavezan")
    override val petType: PetType,

    @field:NotBlank(message = "Naslov je obavezan")
    val title: String,

    override val breed: String?,

    @field:NotBlank(message = "Boja je obavezna")
    val color: String,

    @field:NotBlank(message = "Opis je obavezan")
    @field:Size(
        min = 50,
        max = 1000,
        message = "Opis mora imati između 50 i 1000 karaktera"
    )
    val description: String,

    @field:Pattern(
        regexp = "^(MALE|FEMALE)$",
        message = "Pol mora biti 'Muški' ili 'Ženski'"
    )
    val gender: String,

    val hasChip: Boolean,

    @field:NotBlank(message = "Adresa je obavezna")
    val address: String,

    @field:NotNull(message = "Koordinate nisu validne")
    @field:DecimalMin(value = "-90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    @field:DecimalMax(value = "90.0", message = "Vrednost geografske širine mora biti između -90 i 90")
    val latitude: Double,

    @field:NotNull(message = "Koordinate nisu validne")
    @field:DecimalMin(value = "-180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    @field:DecimalMax(value = "180.0", message = "Vrednost geografske dužine mora biti između -180 i 180")
    val longitude: Double,

    @field:NotEmpty(message = "Mora biti priložena bar jedna fotografija")
    @field:Size(max = 5, message = "Maksimalan broj fotografija je 5")
    val photos: List<String>
) : PetWithBreed

data class LostPetEditFormResponse(
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
    val photos: List<String>
) 