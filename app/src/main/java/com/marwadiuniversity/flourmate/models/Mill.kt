package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mills")
data class Mill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double = 4.8,
    val isAvailable: Boolean = true,
    val imageUri: String = "",
    val minPricePerKg: Double = 40.0,
    val latitude: Double = 22.3039,
    val longitude: Double = 70.8022
)
