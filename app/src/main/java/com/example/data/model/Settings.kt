package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val pricePerKw: Double = 0.5, // e.g., standard price per unit
    val pricePerAmp: Double = 0.0, // flat price per Ampere per month
    val fixedFee: Double = 0.0, // service maintenance charge
    val currency: String = "ILS",
    val pinCode: String = "", // legacy
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN", // "PIN" or "PASSWORD" or "BIOMETRIC"
    val appLockSecret: String = ""
)
