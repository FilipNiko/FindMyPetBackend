package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.LostPetRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

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
    fun getLostPetsList(request: LostPetListRequest): LostPetListResponse {
        logger.info("Dohvatanje liste nestalih ljubimaca - lat: ${request.latitude}, lon: ${request.longitude}, radius: ${request.radius}km")
        logger.debug("Parametri zahteva: filter={}, sortiranje={}, page={}, size={}", 
                     request.petFilter, request.sortBy, request.page, request.size)
        logger.debug("Napredni filteri: breed={}, color={}, gender={}, hasChip={}", 
                     request.breed, request.color, request.gender, request.hasChip)

        val allPets = lostPetRepository.findAll()

        val filteredPets = allPets
            .asSequence()
            .filter { pet ->
                when (request.petFilter) {
                    PetFilter.ALL -> true
                    PetFilter.DOGS -> pet.petType == PetType.DOG
                    PetFilter.CATS -> pet.petType == PetType.CAT
                    PetFilter.OTHER -> pet.petType == PetType.OTHER
                }
            }
            .filter { pet -> request.breed == null || pet.breed?.contains(request.breed, ignoreCase = true) == true }
            .filter { pet -> request.color == null || pet.color.contains(request.color, ignoreCase = true) == true }
            .filter { pet -> request.gender == null || pet.gender == request.gender }
            .filter { pet -> request.hasChip == null || pet.hasChip == request.hasChip }
            .map { pet ->
                val distance = geoService.calculateDistance(
                    request.latitude, request.longitude,
                    pet.latitude, pet.longitude
                )
                pet to distance
            }
            .filter { (_, distance) -> distance <= request.radius * 1000 }
            .toList()

        val sortedPets = when (request.sortBy) {
            SortType.DISTANCE -> filteredPets.sortedBy { it.second }
            SortType.LATEST -> filteredPets.sortedByDescending { it.first.createdAt }
        }

        val totalElements = sortedPets.size.toLong()
        val totalPages = ceil(totalElements.toDouble() / request.size).toInt()
        val isLastPage = request.page >= totalPages - 1 || totalPages == 0
        
        val startIndex = request.page * request.size
        val endIndex = minOf((request.page + 1) * request.size, sortedPets.size)
        
        val pagedPets = if (startIndex < sortedPets.size) {
            sortedPets.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val petItems = pagedPets.map { (pet, distance) ->
            LostPetListItem(
                id = pet.id,
                mainPhotoUrl = getMainPhotoUrl(pet),
                timeAgo = timeFormatService.getTimeAgo(pet.createdAt),
                petName = pet.title,
                breed = pet.breed,
                color = pet.color,
                gender = pet.gender,
                hasChip = pet.hasChip,
                ownerName = pet.user.getFullName(),
                distance = geoService.formatDistance(distance),
                petType = pet.petType,
                allPhotos = pet.photos.map { photo -> "/uploads/$photo" }
            )
        }
        
        return LostPetListResponse(
            content = petItems,
            page = request.page,
            size = request.size,
            totalElements = totalElements,
            totalPages = totalPages,
            last = isLastPage
        )
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