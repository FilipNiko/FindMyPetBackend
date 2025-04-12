package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.repository.LostPetRepository
import com.spring.findmypet.service.GeoService.GeoBoundingBox
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.abs

@Service
class LostPetService(
    private val lostPetRepository: LostPetRepository,
    private val geoService: GeoService,
    private val timeFormatService: TimeFormatService
) {
    private val logger = LoggerFactory.getLogger(LostPetService::class.java)

    companion object {
        // Približna vrednost za 1 stepen geografske širine u kilometrima
        private const val KM_PER_DEGREE_LAT = 111.0
        // Faktor sigurnosti za geografski okvir (povećava okvir)
        private const val GEO_SAFETY_FACTOR = 1.2
    }

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

        val geoBox = geoService.calculateGeoBoundingBox(request.latitude, request.longitude, request.radius.toDouble())

        val petType = if (request.petFilter == PetFilter.ALL) null else 
                     when (request.petFilter) {
                         PetFilter.DOGS -> PetType.DOG
                         PetFilter.CATS -> PetType.CAT
                         PetFilter.OTHER -> PetType.OTHER
                         else -> null
                     }

        if (request.sortBy == SortType.LATEST) {
            val sortByLatest = true
            val pageable = PageRequest.of(request.page, request.size)

            val petsPage = lostPetRepository.findPetsWithFilters(
                petType = petType,
                breed = request.breed,
                color = request.color,
                gender = request.gender,
                hasChip = request.hasChip,
                minLatitude = geoBox.minLat,
                maxLatitude = geoBox.maxLat,
                minLongitude = geoBox.minLng,
                maxLongitude = geoBox.maxLng,
                sortByLatest = sortByLatest,
                pageable = pageable
            )

            val filteredContent = petsPage.content
                .map { pet ->
                    val distance = geoService.calculateDistance(
                        request.latitude, request.longitude,
                        pet.latitude, pet.longitude
                    )
                    pet to distance
                }
                .filter { (_, distance) -> distance <= request.radius * 1000 }
                .map { (pet, distance) ->
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

            val totalInRadius = if (filteredContent.size < petsPage.content.size) {
                geoService.getExactCountInRadius(
                    petType, request.breed, request.color, request.gender, request.hasChip,
                    geoBox, request.latitude, request.longitude, request.radius
                )
            } else {
                petsPage.totalElements
            }

            val totalPages = ceil(totalInRadius.toDouble() / request.size).toInt()
            val isLastPage = request.page >= totalPages - 1 || totalPages == 0
            
            return LostPetListResponse(
                content = filteredContent,
                page = request.page,
                size = request.size,
                totalElements = totalInRadius,
                totalPages = totalPages,
                last = isLastPage
            )
        } else {
            val allInBox = lostPetRepository.findPetsWithFilters(
                petType = petType,
                breed = request.breed,
                color = request.color,
                gender = request.gender,
                hasChip = request.hasChip,
                minLatitude = geoBox.minLat,
                maxLatitude = geoBox.maxLat,
                minLongitude = geoBox.minLng,
                maxLongitude = geoBox.maxLng,
                sortByLatest = false,
                pageable = Pageable.unpaged()
            ).content

            val allWithDistance = allInBox
                .map { pet ->
                    val distance = geoService.calculateDistance(
                        request.latitude, request.longitude,
                        pet.latitude, pet.longitude
                    )
                    pet to distance
                }
                .filter { (_, distance) -> distance <= request.radius * 1000 }
                .sortedBy { (_, distance) -> distance }

            val totalElements = allWithDistance.size.toLong()
            val totalPages = ceil(totalElements.toDouble() / request.size).toInt()
            val isLastPage = request.page >= totalPages - 1 || totalPages == 0
            
            val startIndex = request.page * request.size
            val endIndex = minOf((request.page + 1) * request.size, allWithDistance.size)
            
            val pagedItems = if (startIndex < allWithDistance.size) {
                allWithDistance.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            val content = pagedItems.map { (pet, distance) ->
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
                content = content,
                page = request.page,
                size = request.size,
                totalElements = totalElements,
                totalPages = totalPages,
                last = isLastPage
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