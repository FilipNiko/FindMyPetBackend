package com.spring.findmypet.exception

enum class LostPetErrorCodes(val code: String, val message: String) {
    TITLE_EMPTY(
        code = "TITLE_EMPTY",
        message = "Naslov je obavezan"
    ),
    
    DESCRIPTION_SIZE(
        code = "DESCRIPTION_SIZE",
        message = "Opis mora imati između 50 i 1000 karaktera"
    ),
    
    COLOR_EMPTY(
        code = "COLOR_EMPTY",
        message = "Boja je obavezna"
    ),
    
    GENDER_INVALID(
        code = "GENDER_INVALID",
        message = "Pol mora biti 'Muški' ili 'Ženski'"
    ),
    
    ADDRESS_EMPTY(
        code = "ADDRESS_EMPTY",
        message = "Adresa je obavezna"
    ),
    
    COORDINATES_INVALID(
        code = "COORDINATES_INVALID",
        message = "Koordinate nisu validne"
    ),
    
    NO_PHOTOS(
        code = "NO_PHOTOS",
        message = "Mora biti priložena bar jedna fotografija"
    ),
    
    TOO_MANY_PHOTOS(
        code = "TOO_MANY_PHOTOS",
        message = "Maksimalan broj fotografija je 5"
    ),
    
    INVALID_REQUEST(
        code = "INVALID_REQUEST",
        message = "Neispravan zahtev"
    ),
    
    SYSTEM_ERROR(
        code = "SYSTEM_ERROR",
        message = "Došlo je do sistemske greške"
    )
} 