package com.example.data.repository

import com.example.data.local.SubscriberDao
import com.example.data.local.InvoiceHistoryDao
import com.example.data.local.SettingsDao
import com.example.data.model.Subscriber
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingRepository(
    private val subscriberDao: SubscriberDao,
    private val invoiceHistoryDao: InvoiceHistoryDao,
    private val settingsDao: SettingsDao
) {
    val allSubscribers: Flow<List<Subscriber>> = subscriberDao.getAllSubscribers()
    val allInvoices: Flow<List<InvoiceHistory>> = invoiceHistoryDao.getAllInvoices()
    
    val settings: Flow<Settings> = settingsDao.getSettings().map { it ?: Settings() }

    fun getInvoicesForSubscriber(subId: Int): Flow<List<InvoiceHistory>> =
        invoiceHistoryDao.getInvoicesForSubscriber(subId)

    suspend fun insertSubscriber(subscriber: Subscriber): Long =
        subscriberDao.insertSubscriber(subscriber)

    suspend fun updateSubscriber(subscriber: Subscriber) =
        subscriberDao.updateSubscriber(subscriber)

    suspend fun deleteSubscriber(subscriber: Subscriber) =
        subscriberDao.deleteSubscriber(subscriber)

    suspend fun insertInvoice(invoice: InvoiceHistory): Long =
        invoiceHistoryDao.insertInvoice(invoice)

    suspend fun updateInvoice(invoice: InvoiceHistory) =
        invoiceHistoryDao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: InvoiceHistory) =
        invoiceHistoryDao.deleteInvoice(invoice)

    suspend fun updateSettings(settings: Settings) =
        settingsDao.insertOrUpdate(settings)

    suspend fun getSettingsSync(): Settings =
        settingsDao.getSettingsSync() ?: Settings()

    suspend fun getSubscriberByIdSync(id: Int): Subscriber? =
        subscriberDao.getSubscriberByIdSync(id)
}
