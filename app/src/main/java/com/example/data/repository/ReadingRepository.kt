package com.example.data.repository

import com.example.data.local.SubscriberDao
import com.example.data.local.InvoiceHistoryDao
import com.example.data.local.SettingsDao
import com.example.data.model.Subscriber
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import kotlinx.coroutines.flow.Flow

class ReadingRepository(
    private val subscriberDao: SubscriberDao,
    private val invoiceHistoryDao: InvoiceHistoryDao,
    private val settingsDao: SettingsDao
) {
    val subscribers: Flow<List<Subscriber>> = subscriberDao.getAllSubscribers()
    val settings: Flow<Settings> = settingsDao.getSettings()
    val allInvoices: Flow<List<InvoiceHistory>> = invoiceHistoryDao.getAllInvoices()

    suspend fun getSubscriberByIdSync(id: Int): Subscriber? {
        return subscriberDao.getSubscriberByIdSync(id)
    }

    suspend fun insertSubscriber(subscriber: Subscriber) {
        subscriberDao.insertSubscriber(subscriber)
    }

    suspend fun updateSubscriber(subscriber: Subscriber) {
        subscriberDao.updateSubscriber(subscriber)
    }

    suspend fun deleteSubscriber(subscriber: Subscriber) {
        subscriberDao.deleteSubscriber(subscriber)
    }

    fun getInvoicesForSubscriber(subscriberId: Int): Flow<List<InvoiceHistory>> {
        return invoiceHistoryDao.getInvoicesForSubscriber(subscriberId)
    }

    suspend fun getInvoicesForSubscriberSync(subscriberId: Int): List<InvoiceHistory> {
        return invoiceHistoryDao.getInvoicesForSubscriberSync(subscriberId)
    }

    suspend fun insertInvoice(invoice: InvoiceHistory) {
        invoiceHistoryDao.insertInvoice(invoice)
    }

    suspend fun deleteInvoice(invoice: InvoiceHistory) {
        invoiceHistoryDao.deleteInvoice(invoice)
    }

    suspend fun getSettingsSync(): Settings {
        return settingsDao.getSettingsSync() ?: Settings().also {
            settingsDao.insertSettings(it)
        }
    }

    suspend fun updateSettings(settings: Settings) {
        settingsDao.insertSettings(settings)
    }
}
