package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers")
data class Subscriber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String = "",
    val initialReading: Int = 0,
    val currentReading: Int = 0,
    val subscriptionType: String = "Meter",
    val amperage: Int = 0,
    val notes: String = "",
    val isRead: Boolean = false
)
