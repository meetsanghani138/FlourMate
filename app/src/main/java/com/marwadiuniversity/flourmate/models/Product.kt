package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val millId: Long,
    val millName: String,
    val name: String,
    val category: String, // Wheat, Bajra, Jowar, Multigrain, Chana, etc.
    val price: Double,
    val unitVariant: String = "1 kg Pack", // 1 kg Pack, 5 kg Pack, 10 kg Pack, 25 kg Pack
    val stock: Int = 100,
    val description: String = "Freshly ground premium quality flour.",
    val imageUri: String = "",
    val isAvailable: Boolean = true
)
