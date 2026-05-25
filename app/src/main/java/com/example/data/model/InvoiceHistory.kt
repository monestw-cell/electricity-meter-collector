package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_history")
data class InvoiceHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberId: Int,
    val previousReading: Int,
    val currentReading: Int,
    val consumption: Int,
    val amount: Double,
    val date: String,
    val notes: String = "",
    val photoProofPath: String? = null
)
