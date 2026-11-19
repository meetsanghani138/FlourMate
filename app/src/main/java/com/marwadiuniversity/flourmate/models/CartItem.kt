package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val millId: Long,
    val millName: String,
    val productName: String,
    val price: Double,
    val quantity: Int = 1,
    val unitVariant: String = "1 kg",
    val imageUri: String = ""
)