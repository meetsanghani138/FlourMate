package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_locations")
data class DeliveryLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deliveryPartnerId: Long,
    val orderId: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
