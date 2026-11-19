package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_assignments")
data class DeliveryAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val deliveryPartnerId: Long,
    val status: String = "Assigned", // Assigned, Accepted, Rejected, PickedUp, Delivered
    val timestamp: Long = System.currentTimeMillis()
)
