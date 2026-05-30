package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_history")
data class InvoiceHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberId: Int,
    val previousReading: Double,
    val currentReading: Double,
    val consumption: Double,
    val amount: Double,
    val date: String,
    val notes: String = "",
    val photoProofPath: String? = null,
    val isPaid: Boolean = false
)
