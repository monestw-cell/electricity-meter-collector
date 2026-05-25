package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberId: Int,
    val date: String,
    val previousReading: Int,
    val currentReading: Int,
    val consumption: Int,
    val totalAmount: Double,
    val photoProofPath: String? = null,
    val isPaid: Boolean = false,
    val notes: String = ""
)
