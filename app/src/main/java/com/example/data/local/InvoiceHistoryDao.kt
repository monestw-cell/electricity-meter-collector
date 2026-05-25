package com.example.data.local

import androidx.room.*
import com.example.data.model.InvoiceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceHistoryDao {
    @Query("SELECT * FROM invoice_history WHERE subscriberId = :subscriberId ORDER BY date DESC")
    fun getInvoicesForSubscriber(subscriberId: Int): Flow<List<InvoiceHistory>>

    @Query("SELECT * FROM invoice_history WHERE subscriberId = :subscriberId")
    suspend fun getInvoicesForSubscriberSync(subscriberId: Int): List<InvoiceHistory>

    @Query("SELECT * FROM invoice_history ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceHistory)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceHistory)
}
