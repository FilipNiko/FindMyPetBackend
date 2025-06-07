package com.spring.findmypet.service

import com.spring.findmypet.domain.dto.*
import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.domain.model.Role
import com.spring.findmypet.domain.model.User
import com.spring.findmypet.exception.NotFoundException
import com.spring.findmypet.repository.LostPetRepository
import com.spring.findmypet.util.StringsUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class LostPetService(
    private val lostPetRepository: LostPetRepository,
    private val geoService: GeoService,
    private val timeFormatService: TimeFormatService,
    private val userService: UserService,
    private val firebaseMessagingService: FirebaseMessagingService
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

            sendPushNotificationsToNearbyUsers(savedPet)

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
                userId = savedPet.user.id ?: throw IllegalStateException("User ID is null"),
                found = savedPet.found,
                foundAt = savedPet.foundAt
            )
        } catch (e: Exception) {
            logger.error("Greška prilikom kreiranja prijave izgubljenog ljubimca", e)
            throw e
        }
    }

    private fun sendPushNotificationsToNearbyUsers(lostPet: LostPet) {
        try {
            logger.info("Tražim korisnike u blizini za slanje push notifikacija o izgubljenom ljubimcu ID: ${lostPet.id}")
            
            val nearbyUsers = userService.findUsersInRadius(
                latitude = lostPet.latitude,
                longitude = lostPet.longitude,
                lostPetId = lostPet.id
            )
            
            logger.info("Pronađeno ${nearbyUsers.size} korisnika za slanje push notifikacija")
            
            nearbyUsers.forEach { user ->
                val firebaseToken = user.getFirebaseToken()
                if (firebaseToken != null) {
                    val message = FirebaseMessage(
                        title = "Neko je izgubio kućnog ljubimca u vašoj blizini",
                        body = lostPet.title,
                        type = NotificationType.LOST_PET_NEARBY,
                        data = mapOf(
                            "lostPetId" to lostPet.id.toString(),
                            "latitude" to lostPet.latitude.toString(),
                            "longitude" to lostPet.longitude.toString()
                        )
                    )
                    
                    firebaseMessagingService.sendNotification(firebaseToken, message)
                    logger.debug("Poslata push notifikacija korisniku: ${user.getUsername()}")
                }
            }
            
            logger.info("Uspešno poslate push notifikacije korisnicima u blizini")
        } catch (e: Exception) {
            logger.error("Greška prilikom slanja push notifikacija", e)
        }
    }

    @Transactional(readOnly = true)
    fun getLostPetsList(request: LostPetListRequest): LostPetListResponse {
        logger.info("Dohvatanje liste nestalih ljubimaca - lat: ${request.latitude}, lon: ${request.longitude}, radius: ${request.radius}km")
        logger.debug("Parametri zahteva: filter={}, sortiranje={}, page={}, size={}, found={}", 
                     request.petFilter, request.sortBy, request.page, request.size, request.found)
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

        val foundFilter = request.found ?: false

        if (request.sortBy == SortType.LATEST) {
            val sortByLatest = true
            val pageable = PageRequest.of(request.page, request.size)

            val petsPage = lostPetRepository.findPetsWithFilters(
                petType = petType,
                breed = request.breed,
                color = request.color,
                gender = request.gender,
                hasChip = request.hasChip,
                found = foundFilter,
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
                        gender = StringsUtil.mapGender(pet.gender),
                        hasChip = pet.hasChip,
                        ownerName = pet.user.getFullName(),
                        distance = geoService.formatDistance(distance),
                        petType = pet.petType,
                        allPhotos = pet.photos.map { photo -> "/uploads/$photo" },
                        found = pet.found,
                        foundAt = pet.foundAt?.let { timeFormatService.getTimeAgo(it) }
                    )
                }

            val totalInRadius = if (request.sortBy == SortType.DISTANCE) {
                geoService.getExactCountInRadius(
                    petType, request.breed, request.color, request.gender, request.hasChip, foundFilter,
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
                found = foundFilter,
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
                    gender = StringsUtil.mapGender(pet.gender),
                    hasChip = pet.hasChip,
                    ownerName = pet.user.getFullName(),
                    distance = geoService.formatDistance(distance),
                    petType = pet.petType,
                    allPhotos = pet.photos.map { photo -> "/uploads/$photo" },
                    found = pet.found,
                    foundAt = pet.foundAt?.let { timeFormatService.getTimeAgo(it) }
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
    
    @Transactional(readOnly = true)
    fun getLostPetDetail(id: Long, latitude: Double, longitude: Double): LostPetDetailResponse {
        logger.info("Dohvatanje detalja za nestalog ljubimca sa ID: $id")
        logger.debug("Pozicija korisnika: [${latitude}, ${longitude}]")
        
        val lostPet = getLostPetById(id)

        val distanceInMeters = geoService.calculateDistance(
            latitude, longitude,
            lostPet.latitude, lostPet.longitude
        )

        val owner = lostPet.user
        val ownerInfo = OwnerInfo(
            id = owner.id ?: throw IllegalStateException("User ID is null"),
            fullName = owner.getFullName(),
            email = owner.getUsername(),
            phoneNumber = owner.getPhoneNumber(),
            avatarId = owner.getAvatarId()
        )
        
        return LostPetDetailResponse(
            id = lostPet.id,
            petType = lostPet.petType,
            title = lostPet.title,
            breed = lostPet.breed,
            color = lostPet.color,
            description = lostPet.description,
            gender = StringsUtil.mapGender(lostPet.gender),
            hasChip = lostPet.hasChip,
            address = lostPet.address,
            latitude = lostPet.latitude,
            longitude = lostPet.longitude,
            createdAt = lostPet.createdAt,
            timeAgo = timeFormatService.getTimeAgo(lostPet.createdAt),
            photos = lostPet.photos.map { photo -> "/uploads/$photo" },
            distance = geoService.formatDistance(distanceInMeters),
            distanceInMeters = distanceInMeters,
            owner = ownerInfo,
            found = lostPet.found,
            foundAt = lostPet.foundAt
        )
    }
    
    @Transactional(readOnly = true)
    fun getLostPetById(id: Long): LostPet {
        return lostPetRepository.findById(id)
            .orElseThrow { NotFoundException("Lost pet with ID $id not found") }
            .takeIf { !it.deleted } 
            ?: throw NotFoundException("Lost pet with ID $id not found or has been deleted")
    }
    
    @Transactional
    fun softDeleteLostPet(id: Long, currentUser: User) {
        val lostPet = getLostPetById(id)
        if (lostPet.user.id != currentUser.id && currentUser.getRole() != Role.ADMIN) {
            throw AccessDeniedException("You don't have permission to delete this pet report")
        }
        
        lostPet.deleted = true
        lostPet.deletedAt = LocalDateTime.now()
        lostPetRepository.save(lostPet)
        logger.info("Soft deleted lost pet with ID: $id by user: ${currentUser.username}")
    }

    @Transactional(readOnly = true)
    fun getLostPetForEdit(id: Long, currentUser: User): LostPetEditFormResponse {
        logger.info("Dohvatanje podataka za editovanje nestalog ljubimca sa ID: $id od strane korisnika: ${currentUser.username}")
        
        val lostPet = getLostPetById(id)

        if (lostPet.user.id != currentUser.id && currentUser.getRole() != Role.ADMIN) {
            throw AccessDeniedException("Nemate dozvolu da editujete ovaj oglas za nestalog ljubimca")
        }
        
        logger.info("Korisnik ${currentUser.username} ima dozvolu za editovanje nestalog ljubimca sa ID: $id")
        
        return LostPetEditFormResponse(
            petType = lostPet.petType,
            title = lostPet.title,
            breed = lostPet.breed,
            color = lostPet.color,
            description = lostPet.description,
            gender = lostPet.gender,
            hasChip = lostPet.hasChip,
            address = lostPet.address,
            latitude = lostPet.latitude,
            longitude = lostPet.longitude,
            photos = lostPet.photos.map { photo -> "/uploads/$photo" }
        )
    }

    @Transactional
    fun updateLostPet(request: UpdateLostPetRequest, currentUser: User): LostPetResponse {
        logger.info("Započinjem ažuriranje prijave izgubljenog ljubimca sa ID: ${request.id} od strane korisnika: ${currentUser.username}")
        logger.debug("Detalji zahteva za ažuriranje: $request")
        
        try {
            val existingPet = getLostPetById(request.id)

            if (existingPet.user.id != currentUser.id && currentUser.getRole() != Role.ADMIN) {
                throw AccessDeniedException("Nemate dozvolu da editujete ovaj oglas za nestalog ljubimca")
            }
            
            logger.info("Korisnik ${currentUser.username} ima dozvolu za editovanje nestalog ljubimca sa ID: ${request.id}")

            val photoNames = request.photos.map { url -> 
                logger.debug("Procesiranje URL-a fotografije: $url")
                if (url.startsWith("/uploads/")) {
                    url.substringAfterLast("/").also { 
                        logger.debug("Zadržavam postojeći naziv fajla: $it") 
                    }
                } else {
                    url.substringAfterLast("/").also { 
                        logger.debug("Ekstrahovan naziv fajla: $it") 
                    }
                }
            }

            val updatedPet = existingPet.copy(
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

            logger.debug("Kreiran ažurirani objekat izgubljenog ljubimca: $updatedPet")
            val savedPet = lostPetRepository.save(updatedPet)
            logger.info("Uspešno ažurirana prijava izgubljenog ljubimca sa ID: ${savedPet.id}")

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
                userId = savedPet.user.id ?: throw IllegalStateException("User ID is null"),
                found = savedPet.found,
                foundAt = savedPet.foundAt
            )
        } catch (e: Exception) {
            logger.error("Greška prilikom ažuriranja prijave izgubljenog ljubimca", e)
            throw e
        }
    }

    fun getMainPhotoUrl(pet: LostPet): String {
        return if (pet.photos.isNotEmpty()) {
            "/uploads/${pet.photos.first()}"
        } else {
            "/img/no-image.jpg"
        }
    }
    
    @Transactional(readOnly = true)
    fun getUserLostPets(user: User): List<LostPetListItem> {
        logger.info("Dohvatanje liste nestalih ljubimaca za korisnika: ${user.username}")
        
        val lostPets = lostPetRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user)
        logger.info("Pronađeno ${lostPets.size} nestalih ljubimaca za korisnika")
        
        return lostPets.map { pet ->
            LostPetListItem(
                id = pet.id,
                mainPhotoUrl = getMainPhotoUrl(pet),
                timeAgo = timeFormatService.getTimeAgo(pet.createdAt),
                petName = pet.title,
                breed = pet.breed,
                color = pet.color,
                gender = pet.gender,
                hasChip = pet.hasChip,
                ownerName = user.getFullName(),
                distance = "0 m",
                petType = pet.petType,
                allPhotos = pet.photos.map { photo -> "/uploads/$photo" },
                found = pet.found,
                foundAt = pet.foundAt?.let { timeFormatService.getTimeAgo(it) }
            )
        }
    }

    @Transactional
    fun markAsFound(id: Long, currentUser: User): MarkAsFoundResponse {
        logger.info("Označavanje nestalog ljubimca kao pronađenog - ID: $id, korisnik: ${currentUser.username}")
        
        val lostPet = getLostPetById(id)
        
        if (lostPet.user.id != currentUser.id && currentUser.getRole() != Role.ADMIN) {
            throw AccessDeniedException("Nemate dozvolu da označite ovaj oglas kao pronađen")
        }
        
        if (lostPet.found) {
            logger.warn("Ljubimac sa ID: $id je već označen kao pronađen")
            throw IllegalStateException("Ljubimac je već označen kao pronađen")
        }
        
        lostPet.found = true
        lostPet.foundAt = LocalDateTime.now()
        lostPetRepository.save(lostPet)
        
        logger.info("Uspešno označen ljubimac kao pronađen - ID: $id")
        
        return MarkAsFoundResponse(
            success = true,
            foundAt = lostPet.foundAt!!
        )
    }
} 