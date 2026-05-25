package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val pricePerKw: Double = 2.5, // Default price per Kw in ILS
    val pricePerAmp: Double = 0.0,
    val fixedFee: Double = 0.0,
    val pinCode: String = "",
    val currency: String = "ILS",
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN", // "PIN", "PASSWORD", "BIOMETRIC"
    val appLockSecret: String = ""
)
