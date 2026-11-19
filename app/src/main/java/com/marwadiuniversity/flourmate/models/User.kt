package com.marwadiuniversity.flourmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String, // "CUSTOMER", "MILL_OWNER", "DELIVERY"
    val millName: String = "",
    val millAddress: String = "",
    val address: String = "123 Gandhi Road, Rajkot",
    val isVerified: Boolean = true,
    val profileImageUri: String = ""
)
