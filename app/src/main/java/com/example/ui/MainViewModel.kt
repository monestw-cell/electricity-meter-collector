package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import com.example.data.model.Subscriber
import com.example.data.repository.ReadingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

    val subscribers: StateFlow<List<Subscriber>> = repository.subscribers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<Settings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    val allInvoices: StateFlow<List<InvoiceHistory>> = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize settings once
        viewModelScope.launch {
            repository.getSettingsSync()
        }
    }

    fun addSubscriber(name: String, phoneNumber: String, initialReading: Double, notes: String) {
        viewModelScope.launch {
            val subscriber = Subscriber(
                name = name,
                phoneNumber = phoneNumber,
                initialReading = initialReading,
                currentReading = initialReading,
                notes = notes,
                subscriptionType = "Meter",
                amperage = 5,
                isRead = false
            )
            repository.insertSubscriber(subscriber)
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

    fun recordReading(
        subscriber: Subscriber,
        newReading: Double,
        notes: String,
        photoProofPath: String?,
        onComplete: (InvoiceHistory) -> Unit
    ) {
        viewModelScope.launch {
            val previousReading = subscriber.currentReading
            val consumption = (newReading - previousReading).coerceAtLeast(0.0)
            val currentSettings = repository.getSettingsSync()
            val rate = currentSettings.pricePerKw
            val amount = consumption * rate

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentDateStr = formatter.format(Date())

            val invoice = InvoiceHistory(
                subscriberId = subscriber.id,
                previousReading = previousReading,
                currentReading = newReading,
                consumption = consumption,
                amount = amount,
                date = currentDateStr,
                notes = notes,
                photoProofPath = photoProofPath
            )

            // Save invoice
            repository.insertInvoice(invoice)

            // Update subscriber
            val updatedSubscriber = subscriber.copy(
                currentReading = newReading,
                isRead = true
            )
            repository.updateSubscriber(updatedSubscriber)

            onComplete(invoice)
        }
    }

    fun deleteInvoice(invoice: InvoiceHistory) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            // Retrieve subscriber to reset their reading or state isRead if last invoice deleted
            val sub = repository.getSubscriberByIdSync(invoice.subscriberId)
            if (sub != null) {
                // Find previous invoices for this subscriber
                val remainingInvoices = repository.getInvoicesForSubscriberSync(invoice.subscriberId)
                if (remainingInvoices.isEmpty()) {
                    repository.updateSubscriber(
                        sub.copy(
                            currentReading = sub.initialReading,
                            isRead = false
                        )
                    )
                } else {
                    val lastRemaining = remainingInvoices.first()
                    repository.updateSubscriber(
                        sub.copy(
                            currentReading = lastRemaining.currentReading,
                            isRead = true
                        )
                    )
                }
            }
        }
    }

    fun updateSettings(
        pricePerKw: Double,
        currency: String = "ILS",
        appLockEnabled: Boolean,
        appLockType: String,
        appLockSecret: String
    ) {
        viewModelScope.launch {
            val s = Settings(
                id = 1,
                pricePerKw = pricePerKw,
                currency = currency,
                appLockEnabled = appLockEnabled,
                appLockType = appLockType,
                appLockSecret = appLockSecret
            )
            repository.updateSettings(s)
        }
    }

    fun resetDailyLectures() {
        viewModelScope.launch {
            subscribers.value.forEach { sub ->
                repository.updateSubscriber(sub.copy(isRead = false))
            }
        }
    }

    fun toggleInvoicePaidStatus(invoice: InvoiceHistory) {
        viewModelScope.launch {
            val updated = invoice.copy(isPaid = !invoice.isPaid)
            repository.insertInvoice(updated)
        }
    }

    suspend fun getAverageConsumptionForSubscriber(subscriberId: Int): Double {
        val invoices = repository.getInvoicesForSubscriberSync(subscriberId)
        if (invoices.isEmpty()) return -1.0
        val sum = invoices.sumOf { it.consumption }
        return sum.toDouble() / invoices.size
    }
}
