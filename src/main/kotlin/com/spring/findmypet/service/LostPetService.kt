package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.LostPetRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LostPetService(
    private val lostPetRepository: LostPetRepository,
    private val geoService: GeoService,
    private val timeFormatService: TimeFormatService
) {
    private val logger = LoggerFactory.getLogger(LostPetService::class.java)

    @Transactional
    fun reportLostPet(request: ReportLostPetRequest, user: User): LostPetResponse {
        logger.info("Započinjem kreiranje prijave izgubljenog ljubimca za korisnika: ${user.username}")
        logger.debug("Detalji zahteva: $request")
        
        try {
            val photoNames = request.photos.map { url -> 
                logger.debug("Procesiranje URL-a fotografije: $url")
                url.substringAfterLast("/").also { 
                    logger.debug("Ekstrahovan naziv fajla: $it") 
                }
            }
            
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
                photos = photoNames
            )

            logger.debug("Kreiran objekat izgubljenog ljubimca: $lostPet")
            val savedPet = lostPetRepository.save(lostPet)
            logger.info("Uspešno sačuvana prijava izgubljenog ljubimca sa ID: ${savedPet.id}")

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
        } catch (e: Exception) {
            logger.error("Greška prilikom kreiranja prijave izgubljenog ljubimca", e)
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun getLostPetsList(request: LostPetListRequest): List<LostPetListItem> {
        logger.info("Dohvatanje liste nestalih ljubimaca - lat: ${request.latitude}, lon: ${request.longitude}")
        logger.debug("Parametri zahteva: filter={}, sortiranje={}", request.petFilter, request.sortBy)
        
        // Dobavi sve ljubimce i primeni filtriranje
        val allPets = when (request.petFilter) {
            PetFilter.ALL -> lostPetRepository.findAll()
            PetFilter.DOGS -> lostPetRepository.findAllByPetType(PetType.DOG)
            PetFilter.CATS -> lostPetRepository.findAllByPetType(PetType.CAT)
            PetFilter.OTHER -> lostPetRepository.findAllByPetType(PetType.OTHER)
        }
        
        // Pripremi listu sa računanjem udaljenosti
        val petsWithDistance = allPets.map { pet ->
            val distance = geoService.calculateDistance(
                request.latitude, request.longitude,
                pet.latitude, pet.longitude
            )
            
            pet to distance
        }
        
        // Sortiraj po zahtevanom kriterijumu
        val sortedPets = when (request.sortBy) {
            SortType.DISTANCE -> petsWithDistance.sortedBy { it.second }
            SortType.LATEST -> petsWithDistance.sortedByDescending { it.first.createdAt }
        }
        
        // Konvertuj u odgovor
        return sortedPets.map { (pet, distance) ->
            LostPetListItem(
                id = pet.id,
                mainPhotoUrl = getMainPhotoUrl(pet),
                timeAgo = timeFormatService.getTimeAgo(pet.createdAt),
                petName = pet.title,
                breed = pet.breed,
                ownerName = pet.user.getFullName(),
                distance = geoService.formatDistance(distance),
                petType = pet.petType
            )
        }
    }
    
    private fun getMainPhotoUrl(pet: LostPet): String {
        return if (pet.photos.isNotEmpty()) {
            // Ovde možeš dodati kompletan URL fotografije
            "/uploads/${pet.photos.first()}"
        } else {
            // Podrazumevana slika ako nema fotografija
            "/img/no-image.jpg"
        }
    }
} 