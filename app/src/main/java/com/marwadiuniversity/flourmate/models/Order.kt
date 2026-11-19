package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val millId: Long,
    val millName: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val paymentMethod: String = "COD", // UPI, COD, CARD
    val status: String = "Pending", // Pending, Accepted, Preparing, Ready for Pickup, Out for Delivery, Delivered, Rejected, Cancelled
    val timestamp: Long = System.currentTimeMillis(),
    val otp: String = "4321",
    val deliveryPartnerId: Long = 0,
    val itemCount: Int = 1,
    val orderType: String = "DELIVERY", // DELIVERY, PICKUP
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 30.0,
    val completedTimestamp: Long = 0L
)
