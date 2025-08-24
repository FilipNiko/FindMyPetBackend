package com.spring.findmypet.service

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class TimeFormatService {

    fun getTimeAgo(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(dateTime, now)
        
        return when {
            duration.toMinutes() < 1 -> "pre nekoliko sekundi"
            duration.toMinutes() < 60 -> {
                val minutes = duration.toMinutes()
                if (minutes == 1L) "pre 1 minut" else "pre $minutes minuta"
            }
            duration.toHours() < 24 -> {
                val hours = duration.toHours()
                formatHours(hours)
            }
            else -> {
                val days = duration.toDays()
                if (days == 1L) "pre 1 dan" else "pre $days dana"
            }
        }
    }
    
    private fun formatHours(hours: Long): String {
        return when {
            hours == 1L -> "pre 1 sat"
            hours < 5L -> "pre $hours sata"
            else -> "pre $hours sati"
        }
    }
} 