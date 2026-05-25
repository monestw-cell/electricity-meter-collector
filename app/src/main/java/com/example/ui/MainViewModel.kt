package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import com.example.data.model.Subscriber
import com.example.data.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ReadingRepository(
        database.subscriberDao(),
        database.invoiceHistoryDao(),
        database.settingsDao()
    )

    val allSubscribers: StateFlow<List<Subscriber>> = repository.allSubscribers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceHistory>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<Settings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    fun getInvoicesForSubscriber(subId: Int): Flow<List<InvoiceHistory>> =
        repository.getInvoicesForSubscriber(subId)

    fun addSubscriber(
        name: String,
        phone: String,
        initialReading: Int,
        subscriptionType: String,
        amperage: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val sub = Subscriber(
                name = name,
                phoneNumber = phone,
                initialReading = initialReading,
                currentReading = initialReading,
                subscriptionType = subscriptionType,
                amperage = amperage,
                notes = notes
            )
            repository.insertSubscriber(sub)
        }
    }

    fun updateSubscriber(subscriber: Subscriber) {
        viewModelScope.launch {
            repository.updateSubscriber(subscriber)
        }
    }

    fun deleteSubscriber(subscriber: Subscriber) {
        viewModelScope.launch {
            repository.deleteSubscriber(subscriber)
        }
    }

    fun saveReading(
        subscriberId: Int,
        reading: Int,
        note: String,
        photoProofPath: String?
    ) {
        viewModelScope.launch {
            val subscriber = repository.getSubscriberByIdSync(subscriberId) ?: return@launch
            val activeSettings = repository.getSettingsSync()

            val previousReading = subscriber.currentReading
            val consumption = (reading - previousReading).coerceAtLeast(0)
            val amount = consumption * activeSettings.pricePerKw

            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
            val currentDateStr = dateFormat.format(Date())

            val invoice = InvoiceHistory(
                subscriberId = subscriberId,
                date = currentDateStr,
                previousReading = previousReading,
                currentReading = reading,
                consumption = consumption,
                totalAmount = amount,
                photoProofPath = photoProofPath,
                isPaid = false,
                notes = note
            )

            // Insert Invoice
            repository.insertInvoice(invoice)

            // Update Subscriber's current reading and mark as read
            val updatedSub = subscriber.copy(
                currentReading = reading,
                isRead = true
            )
            repository.updateSubscriber(updatedSub)
        }
    }

    fun resetAllReadingsStatus() {
        viewModelScope.launch {
            val list = repository.allSubscribers.first()
            list.forEach { subscriber ->
                repository.updateSubscriber(subscriber.copy(isRead = false))
            }
        }
    }

    fun toggleReadStatus(subscriber: Subscriber) {
        viewModelScope.launch {
            repository.updateSubscriber(subscriber.copy(isRead = !subscriber.isRead))
        }
    }

    fun updateSettings(
        pricePerKw: Double,
        currency: String,
        appLockEnabled: Boolean,
        appLockType: String,
        appLockSecret: String
    ) {
        viewModelScope.launch {
            val current = repository.getSettingsSync()
            val updated = current.copy(
                pricePerKw = pricePerKw,
                currency = currency,
                appLockEnabled = appLockEnabled,
                appLockType = appLockType,
                appLockSecret = appLockSecret
            )
            repository.updateSettings(updated)
        }
    }

    fun payInvoice(invoice: InvoiceHistory) {
        viewModelScope.launch {
            repository.updateInvoice(invoice.copy(isPaid = true))
        }
    }

    fun deleteInvoice(invoice: InvoiceHistory) {
        viewModelScope.launch {
            // Also adjust subscriber reading back if deleting the latest reading
            repository.deleteInvoice(invoice)
            val sub = repository.getSubscriberByIdSync(invoice.subscriberId)
            if (sub != null && sub.currentReading == invoice.currentReading) {
                repository.updateSubscriber(sub.copy(currentReading = invoice.previousReading))
            }
        }
    }
}
