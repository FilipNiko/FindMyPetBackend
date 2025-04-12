package com.spring.findmypet.service

import com.spring.findmypet.domain.model.LostPet
import com.spring.findmypet.domain.model.PetType
import com.spring.findmypet.repository.LostPetRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.math.*

@Service
class GeoService(private val lostPetRepository: LostPetRepository) {
    
    companion object {
        // Približna vrednost za 1 stepen geografske širine u kilometrima
        const val KM_PER_DEGREE_LAT = 111.0
        // Faktor sigurnosti za geografski okvir (povećava okvir)
        const val GEO_SAFETY_FACTOR = 1.2
    }
    
    /**
     * Računa udaljenost između dve geografske tačke koristeći formulu haversine
     * @param lat1 geografska širina prve tačke u stepenima
     * @param lon1 geografska dužina prve tačke u stepenima
     * @param lat2 geografska širina druge tačke u stepenima
     * @param lon2 geografska dužina druge tačke u stepenima
     * @return udaljenost u metrima
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // Zemljin poluprečnik u metrima
        
        val latRad1 = Math.toRadians(lat1)
        val lonRad1 = Math.toRadians(lon1)
        val latRad2 = Math.toRadians(lat2)
        val lonRad2 = Math.toRadians(lon2)
        
        val dLat = latRad2 - latRad1
        val dLon = lonRad2 - lonRad1
        
        val a = sin(dLat / 2).pow(2) + cos(latRad1) * cos(latRad2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Formatira udaljenost u čitljiv format
     * @param distanceInMeters udaljenost u metrima
     * @return formatirana udaljenost (npr. "5 m", "1.2 km")
     */
    fun formatDistance(distanceInMeters: Double): String {
        return when {
            distanceInMeters < 1000 -> "${distanceInMeters.roundToInt()} m"
            else -> "${(distanceInMeters / 1000).roundTo(1)} km"
        }
    }
    
    /**
     * Izračunava geografski okvir (bounding box) za datu tačku i radijus u kilometrima.
     * Okvir je pravougaonik koji obuhvata krug radijusa oko tačke.
     */
    fun calculateGeoBoundingBox(latitude: Double, longitude: Double, radiusKm: Double): GeoBoundingBox {
        // Radijus sa faktorom sigurnosti
        val safeRadius = radiusKm * GEO_SAFETY_FACTOR
        
        // Izračunaj promenu geografske širine
        val deltaLat = safeRadius / KM_PER_DEGREE_LAT
        
        // Izračunaj promenu geografske dužine (zavisi od geografske širine)
        // Na ekvatoru 1 stepen dužine ≈ 111 km, a smanjuje se sa povećanjem geografske širine
        val kmPerDegreeLng = KM_PER_DEGREE_LAT * cos(Math.toRadians(latitude))
        val deltaLng = if (kmPerDegreeLng > 0) safeRadius / kmPerDegreeLng else safeRadius
        
        return GeoBoundingBox(
            minLat = latitude - deltaLat,
            maxLat = latitude + deltaLat,
            minLng = longitude - deltaLng,
            maxLng = longitude + deltaLng
        )
    }
    
    /**
     * Dobija tačan broj ljubimaca u radijusu
     */
    fun getExactCountInRadius(
        petType: PetType?,
        breed: String?,
        color: String?,
        gender: String?,
        hasChip: Boolean?,
        geoBox: GeoBoundingBox,
        latitude: Double,
        longitude: Double,
        radiusKm: Int
    ): Long {
        // Dobavi sve ljubimce unutar bbox-a
        val allPets = lostPetRepository.findPetsWithFilters(
            petType = petType,
            breed = breed,
            color = color,
            gender = gender,
            hasChip = hasChip,
            minLatitude = geoBox.minLat,
            maxLatitude = geoBox.maxLat,
            minLongitude = geoBox.minLng,
            maxLongitude = geoBox.maxLng,
            sortByLatest = false, // Nije bitno sortiranje
            pageable = Pageable.unpaged()
        ).content
        
        // Filtriraj samo one koji su zaista unutar radijusa
        return allPets.count { pet ->
            val distance = calculateDistance(
                latitude, longitude,
                pet.latitude, pet.longitude
            )
            distance <= radiusKm * 1000
        }.toLong()
    }
    
    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }
    
    /**
     * Pomocna klasa koja predstavlja geografski okvir
     */
    data class GeoBoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double
    )
} 