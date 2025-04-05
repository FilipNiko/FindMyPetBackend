package com.spring.findmypet.service

import org.springframework.stereotype.Service
import kotlin.math.*

@Service
class GeoService {
    
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
    
    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }
} 