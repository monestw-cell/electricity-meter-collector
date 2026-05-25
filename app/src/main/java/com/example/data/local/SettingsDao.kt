package com.example.data.local

import androidx.room.*
import com.example.data.model.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsSync(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)
}
