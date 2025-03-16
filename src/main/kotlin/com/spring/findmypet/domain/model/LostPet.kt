package com.spring.findmypet.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lost_pets")
data class LostPet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val petType: PetType,

    @Column(nullable = false)
    val title: String,

    @Column
    val breed: String?,

    @Column(nullable = false)
    val color: String,

    @Column(nullable = false, length = 1000)
    val description: String,

    @Column(nullable = false)
    val gender: String,

    @Column(nullable = false)
    val hasChip: Boolean,

    @Column(nullable = false)
    val address: String,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @ElementCollection
    @CollectionTable(name = "lost_pet_photos", joinColumns = [JoinColumn(name = "lost_pet_id")])
    @Column(name = "photo_url")
    val photos: List<String>,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "LostPet(id=$id, petType=$petType, title='$title', breed=$breed, color='$color', " +
               "description='$description', gender='$gender', hasChip=$hasChip, address='$address', " +
               "latitude=$latitude, longitude=$longitude, photos=$photos, createdAt=$createdAt)"
    }
} 