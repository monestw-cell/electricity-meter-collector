package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InvoiceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceHistoryDao {
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceHistory>>

    @Query("SELECT * FROM invoices WHERE subscriberId = :subscriberId ORDER BY id DESC")
    fun getInvoicesForSubscriber(subscriberId: Int): Flow<List<InvoiceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceHistory): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceHistory)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceHistory)
}
