package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import com.example.data.model.Subscriber

@Database(entities = [Subscriber::class, InvoiceHistory::class, Settings::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriberDao(): SubscriberDao
    abstract fun invoiceHistoryDao(): InvoiceHistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aadad_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
